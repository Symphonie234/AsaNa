<?php

namespace App\Providers;

use App\Services\Billing\GooglePlayApiVerifier;
use App\Services\Billing\GooglePlayVerifier;
use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        $this->app->bind(GooglePlayVerifier::class, fn () => new GooglePlayApiVerifier(
            packageName: config('services.google_play.package_name'),
            serviceAccountPath: config('services.google_play.service_account_path'),
        ));
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        //
    }
}
