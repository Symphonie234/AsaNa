<?php

namespace Database\Seeders;

use App\Enums\ServiceStatus;
use App\Models\Category;
use App\Models\City;
use App\Models\Office;
use App\Models\Service;
use Illuminate\Database\Seeder;
use Illuminate\Support\Str;

/**
 * Seeds the initial Danao City structure: the city itself, the MVP
 * categories, and one fully-worked example service (Barangay Clearance)
 * so the API and admin have real data to work against.
 *
 * The example service is intentionally left in DRAFT status with no
 * verified_at — its office contact details are placeholders and must
 * be confirmed by a human against the real Danao City Hall / barangay
 * office before it is published (see CLAUDE.md content accuracy rules).
 */
class DanaoCitySeeder extends Seeder
{
    public function run(): void
    {
        $danao = City::firstOrCreate(
            ['slug' => 'danao-city'],
            ['name' => 'Danao City', 'province' => 'Cebu'],
        );

        $categories = collect([
            'Government',
            'Documents',
            'Business',
            'Health',
            'Emergency',
            'Other',
        ])->mapWithKeys(fn (string $name) => [
            $name => Category::firstOrCreate(
                ['slug' => Str::slug($name)],
                ['name' => $name],
            ),
        ]);

        $cityHall = Office::firstOrCreate(
            ['city_id' => $danao->id, 'name' => 'Danao City Hall'],
            [
                'address' => 'Danao City Hall, Danao City, Cebu',
                'latitude' => null,
                'longitude' => null,
                'phone' => null,
                'email' => null,
                'website' => null,
                'verified_at' => null,
            ],
        );

        Service::firstOrCreate(
            ['slug' => 'barangay-clearance'],
            [
                'city_id' => $danao->id,
                'category_id' => $categories['Government']->id,
                'office_id' => $cityHall->id,
                'name' => 'Barangay Clearance',
                'description' => 'A certificate issued by your barangay confirming you are a resident of good standing. Commonly required for job applications, business permits, and other government transactions.',
                'instructions' => "1. Go to your Barangay Hall.\n2. Submit the required documents to the barangay office.\n3. Pay the applicable fee.\n4. Claim the certificate.",
                'is_premium' => false,
                'status' => ServiceStatus::Draft,
                'verified_at' => null,
                'verified_by' => null,
                'verification_notes' => 'Placeholder office contact details. Needs confirmation of exact requirements, fee, and office hours against the actual barangay hall before publishing.',
                'source' => null,
            ],
        )->requirements()->createMany([
            ['name' => 'Valid government-issued ID', 'is_required' => true, 'sort_order' => 1],
            ['name' => 'Personal appearance', 'is_required' => true, 'sort_order' => 2],
            ['name' => 'Cedula (Community Tax Certificate)', 'is_required' => true, 'sort_order' => 3],
        ]);
    }
}
