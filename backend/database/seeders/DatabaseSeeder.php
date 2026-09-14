<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    use WithoutModelEvents;

    /**
     * Seed the application's database.
     */
    public function run(): void
    {
        User::factory()->create([
            'name' => 'Gerand Parawan',
            'email' => 'gerandparawan23@gmail.com',
            'is_admin' => true,
        ]);

        $this->call(DanaoCitySeeder::class);
    }
}
