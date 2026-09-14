<?php

use App\Models\City;

it('lists cities ordered by name', function () {
    City::factory()->create(['name' => 'Zamboanga City']);
    City::factory()->create(['name' => 'Danao City']);

    $response = $this->getJson('/api/v1/cities');

    $response->assertOk();
    expect($response->json('data.*.name'))->toBe(['Danao City', 'Zamboanga City']);
});

it('shows a single city by slug', function () {
    $city = City::factory()->create(['name' => 'Danao City', 'slug' => 'danao-city']);

    $response = $this->getJson('/api/v1/cities/danao-city');

    $response->assertOk()->assertJsonPath('data.id', $city->id);
});

it('returns 404 for an unknown city slug', function () {
    $this->getJson('/api/v1/cities/does-not-exist')->assertNotFound();
});
