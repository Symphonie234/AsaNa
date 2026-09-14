<?php

use App\Models\Entitlement;
use App\Models\User;
use App\Services\Billing\BillingVerificationException;
use App\Services\Billing\GooglePlayPurchase;
use App\Services\Billing\GooglePlayVerifier;

function fakeVerifier(GooglePlayPurchase|Throwable $result): void
{
    app()->bind(GooglePlayVerifier::class, fn () => new class($result) implements GooglePlayVerifier
    {
        public function __construct(private readonly GooglePlayPurchase|Throwable $result) {}

        public function verifyProductPurchase(string $productId, string $purchaseToken): GooglePlayPurchase
        {
            if ($this->result instanceof Throwable) {
                throw $this->result;
            }

            return $this->result;
        }
    });
}

it('rejects verify without authentication', function () {
    $this->postJson('/api/v1/billing/google-play/verify', [
        'product_id' => 'asanaph.lifetime',
        'purchase_token' => 'token',
    ])->assertUnauthorized();
});

it('rejects an unknown product id', function () {
    $user = User::factory()->create();

    $this->actingAs($user, 'sanctum')->postJson('/api/v1/billing/google-play/verify', [
        'product_id' => 'not-a-real-product',
        'purchase_token' => 'token',
    ])->assertStatus(422)->assertJsonValidationErrors('product_id');
});

it('creates an active entitlement for a verified purchase', function () {
    $user = User::factory()->create();
    fakeVerifier(new GooglePlayPurchase(isPurchased: true, purchasedAt: new DateTimeImmutable('2026-01-01')));

    $response = $this->actingAs($user, 'sanctum')->postJson('/api/v1/billing/google-play/verify', [
        'product_id' => 'asanaph.lifetime',
        'purchase_token' => 'valid-token',
    ]);

    $response->assertOk()->assertJsonPath('data.status', 'active');
    expect(Entitlement::where('user_id', $user->id)->where('status', 'active')->exists())->toBeTrue();
});

it('rejects a purchase token Google says was not purchased', function () {
    $user = User::factory()->create();
    fakeVerifier(new GooglePlayPurchase(isPurchased: false, purchasedAt: null));

    $response = $this->actingAs($user, 'sanctum')->postJson('/api/v1/billing/google-play/verify', [
        'product_id' => 'asanaph.lifetime',
        'purchase_token' => 'fake-token',
    ]);

    $response->assertStatus(422);
    expect(Entitlement::where('user_id', $user->id)->exists())->toBeFalse();
});

it('returns 503 when Google Play verification is unavailable', function () {
    $user = User::factory()->create();
    fakeVerifier(new BillingVerificationException('not configured'));

    $this->actingAs($user, 'sanctum')->postJson('/api/v1/billing/google-play/verify', [
        'product_id' => 'asanaph.lifetime',
        'purchase_token' => 'token',
    ])->assertStatus(503);
});

it('does not duplicate an entitlement when the same purchase is verified twice', function () {
    $user = User::factory()->create();
    fakeVerifier(new GooglePlayPurchase(isPurchased: true, purchasedAt: new DateTimeImmutable));

    $body = ['product_id' => 'asanaph.lifetime', 'purchase_token' => 'same-token'];
    $this->actingAs($user, 'sanctum')->postJson('/api/v1/billing/google-play/verify', $body);
    $this->actingAs($user, 'sanctum')->postJson('/api/v1/billing/google-play/verify', $body);

    expect(Entitlement::where('purchase_token', 'same-token')->count())->toBe(1);
});

it('lists only the authenticated user\'s entitlements', function () {
    $user = User::factory()->create();
    $otherUser = User::factory()->create();
    Entitlement::factory()->for($user)->create(['product_id' => 'asanaph.lifetime']);
    Entitlement::factory()->for($otherUser)->create(['product_id' => 'asanaph.lifetime']);

    $response = $this->actingAs($user, 'sanctum')->getJson('/api/v1/billing/entitlements');

    $response->assertOk();
    expect($response->json('data'))->toHaveCount(1);
});
