<?php

use App\Models\User;

it('registers a new user and returns a token', function () {
    $response = $this->postJson('/api/v1/auth/register', [
        'name' => 'Juan Dela Cruz',
        'email' => 'juan@example.com',
        'password' => 'password123',
        'password_confirmation' => 'password123',
    ]);

    $response->assertCreated()
        ->assertJsonPath('data.user.email', 'juan@example.com')
        ->assertJsonStructure(['data' => ['token', 'user' => ['id', 'name', 'email']]]);

    expect(User::where('email', 'juan@example.com')->exists())->toBeTrue();
});

it('rejects registration with a duplicate email', function () {
    User::factory()->create(['email' => 'juan@example.com']);

    $response = $this->postJson('/api/v1/auth/register', [
        'name' => 'Juan Dela Cruz',
        'email' => 'juan@example.com',
        'password' => 'password123',
        'password_confirmation' => 'password123',
    ]);

    $response->assertStatus(422)->assertJsonValidationErrors('email');
});

it('logs in with correct credentials', function () {
    $user = User::factory()->create(['email' => 'juan@example.com', 'password' => 'password123']);

    $response = $this->postJson('/api/v1/auth/login', [
        'email' => 'juan@example.com',
        'password' => 'password123',
    ]);

    $response->assertOk()->assertJsonPath('data.user.id', $user->id);
});

it('rejects login with the wrong password', function () {
    User::factory()->create(['email' => 'juan@example.com', 'password' => 'password123']);

    $response = $this->postJson('/api/v1/auth/login', [
        'email' => 'juan@example.com',
        'password' => 'wrong-password',
    ]);

    $response->assertStatus(422)->assertJsonValidationErrors('email');
});

it('returns the authenticated user from /me', function () {
    $user = User::factory()->create();

    $response = $this->actingAs($user, 'sanctum')->getJson('/api/v1/me');

    $response->assertOk()->assertJsonPath('data.id', $user->id);
});

it('rejects /me without authentication', function () {
    $this->getJson('/api/v1/me')->assertUnauthorized();
});

it('logs out and revokes the current token', function () {
    $user = User::factory()->create();
    $token = $user->createToken('mobile')->plainTextToken;

    $response = $this->withHeader('Authorization', "Bearer {$token}")->postJson('/api/v1/auth/logout');

    $response->assertNoContent();
    expect($user->tokens()->count())->toBe(0);
});
