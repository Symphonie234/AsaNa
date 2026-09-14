<?php

use App\Enums\ServiceStatus;
use App\Models\Category;
use App\Models\City;
use App\Models\Service;

it('only lists published services', function () {
    Service::factory()->create(['name' => 'Published Service', 'status' => ServiceStatus::Published]);
    Service::factory()->create(['name' => 'Draft Service', 'status' => ServiceStatus::Draft]);

    $response = $this->getJson('/api/v1/services');

    $response->assertOk();
    expect($response->json('data.*.name'))->toBe(['Published Service']);
});

it('filters services by city slug', function () {
    $danao = City::factory()->create(['slug' => 'danao-city']);
    $cebu = City::factory()->create(['slug' => 'cebu-city']);

    Service::factory()->create(['city_id' => $danao->id, 'name' => 'Danao Service']);
    Service::factory()->create(['city_id' => $cebu->id, 'name' => 'Cebu Service']);

    $response = $this->getJson('/api/v1/services?city=danao-city');

    $response->assertOk();
    expect($response->json('data.*.name'))->toBe(['Danao Service']);
});

it('filters services by category slug', function () {
    $government = Category::factory()->create(['slug' => 'government']);
    $health = Category::factory()->create(['slug' => 'health']);

    Service::factory()->create(['category_id' => $government->id, 'name' => 'Government Service']);
    Service::factory()->create(['category_id' => $health->id, 'name' => 'Health Service']);

    $response = $this->getJson('/api/v1/services?category=government');

    $response->assertOk();
    expect($response->json('data.*.name'))->toBe(['Government Service']);
});

it('shows a published service with its relations', function () {
    $service = Service::factory()->create(['slug' => 'barangay-clearance']);
    $service->requirements()->create(['name' => 'Valid ID', 'sort_order' => 1]);
    $service->fees()->create(['description' => 'Processing fee', 'amount_min' => 50]);

    $response = $this->getJson('/api/v1/services/barangay-clearance');

    $response->assertOk()
        ->assertJsonPath('data.slug', 'barangay-clearance')
        ->assertJsonPath('data.requirements.0.name', 'Valid ID')
        ->assertJsonPath('data.fees.0.description', 'Processing fee');
});

it('returns 404 for a draft service', function () {
    $service = Service::factory()->create(['slug' => 'not-ready-yet', 'status' => ServiceStatus::Draft]);

    $this->getJson("/api/v1/services/{$service->slug}")->assertNotFound();
});

it('requires a query for search', function () {
    $this->getJson('/api/v1/search')->assertStatus(422);
});

it('searches published services by name', function () {
    Service::factory()->create(['name' => 'Barangay Clearance']);
    Service::factory()->create(['name' => 'Business Permit']);

    $response = $this->getJson('/api/v1/search?q=barangay');

    $response->assertOk();
    expect($response->json('data.*.name'))->toBe(['Barangay Clearance']);
});
