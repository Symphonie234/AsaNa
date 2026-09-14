<?php

namespace Database\Factories;

use App\Models\City;
use App\Models\Office;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<Office>
 */
class OfficeFactory extends Factory
{
    protected $model = Office::class;

    public function definition(): array
    {
        return [
            'city_id' => City::factory(),
            'name' => fake()->company().' Office',
            'address' => fake()->streetAddress(),
            'latitude' => fake()->latitude(9.9, 10.6),
            'longitude' => fake()->longitude(123.8, 124.1),
            'phone' => fake()->phoneNumber(),
            'email' => fake()->safeEmail(),
            'website' => null,
            'verified_at' => now(),
        ];
    }
}
