<?php

namespace Database\Factories;

use App\Enums\ServiceStatus;
use App\Models\Category;
use App\Models\City;
use App\Models\Office;
use App\Models\Service;
use Illuminate\Database\Eloquent\Factories\Factory;
use Illuminate\Support\Str;

/**
 * @extends Factory<Service>
 */
class ServiceFactory extends Factory
{
    protected $model = Service::class;

    public function definition(): array
    {
        $name = fake()->unique()->words(3, true);

        return [
            'city_id' => City::factory(),
            'category_id' => Category::factory(),
            'office_id' => Office::factory(),
            'name' => ucfirst($name),
            'slug' => Str::slug($name),
            'description' => fake()->paragraph(),
            'instructions' => fake()->paragraph(),
            'is_premium' => false,
            'status' => ServiceStatus::Published,
            'verified_at' => now(),
            'verified_by' => null,
            'verification_notes' => null,
            'source' => 'City Government Office',
        ];
    }
}
