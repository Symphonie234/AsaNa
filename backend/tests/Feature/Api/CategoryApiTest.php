<?php

use App\Models\Category;

it('lists categories ordered by name', function () {
    Category::factory()->create(['name' => 'Health']);
    Category::factory()->create(['name' => 'Business']);

    $response = $this->getJson('/api/v1/categories');

    $response->assertOk();
    expect($response->json('data.*.name'))->toBe(['Business', 'Health']);
});
