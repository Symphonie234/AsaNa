<?php

namespace Database\Factories;

use App\Enums\EntitlementStatus;
use App\Models\Entitlement;
use App\Models\User;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<Entitlement>
 */
class EntitlementFactory extends Factory
{
    protected $model = Entitlement::class;

    public function definition(): array
    {
        return [
            'user_id' => User::factory(),
            'product_id' => 'asanaph.lifetime',
            'purchase_token' => fake()->unique()->uuid(),
            'status' => EntitlementStatus::Active,
            'purchased_at' => now(),
            'expires_at' => null,
        ];
    }
}
