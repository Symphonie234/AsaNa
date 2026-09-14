<?php

use App\Models\Service;
use App\Models\User;

it('rejects favorites endpoints without authentication', function () {
    $this->getJson('/api/v1/me/favorites')->assertUnauthorized();
});

it('lists the authenticated user\'s favorites', function () {
    $user = User::factory()->create();
    $service = Service::factory()->create(['name' => 'Barangay Clearance']);
    $user->favoriteServices()->attach($service->id);

    $response = $this->actingAs($user, 'sanctum')->getJson('/api/v1/me/favorites');

    $response->assertOk();
    expect($response->json('data.*.name'))->toBe(['Barangay Clearance']);
});

it('adds a service to favorites', function () {
    $user = User::factory()->create();
    $service = Service::factory()->create(['slug' => 'barangay-clearance']);

    $response = $this->actingAs($user, 'sanctum')->postJson('/api/v1/me/favorites', ['slug' => 'barangay-clearance']);

    $response->assertOk();
    expect($user->favoriteServices()->where('services.id', $service->id)->exists())->toBeTrue();
});

it('does not duplicate a favorite added twice', function () {
    $user = User::factory()->create();
    $service = Service::factory()->create(['slug' => 'barangay-clearance']);

    $this->actingAs($user, 'sanctum')->postJson('/api/v1/me/favorites', ['slug' => 'barangay-clearance']);
    $this->actingAs($user, 'sanctum')->postJson('/api/v1/me/favorites', ['slug' => 'barangay-clearance']);

    expect($user->favoriteServices()->where('services.id', $service->id)->count())->toBe(1);
});

it('removes a service from favorites', function () {
    $user = User::factory()->create();
    $service = Service::factory()->create(['slug' => 'barangay-clearance']);
    $user->favoriteServices()->attach($service->id);

    $response = $this->actingAs($user, 'sanctum')->deleteJson('/api/v1/me/favorites/barangay-clearance');

    $response->assertNoContent();
    expect($user->favoriteServices()->where('services.id', $service->id)->exists())->toBeFalse();
});
