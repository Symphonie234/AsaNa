<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\BillingController;
use App\Http\Controllers\Api\CategoryController;
use App\Http\Controllers\Api\CityController;
use App\Http\Controllers\Api\FavoriteController;
use App\Http\Controllers\Api\ServiceController;
use Illuminate\Support\Facades\Route;

Route::prefix('v1')->group(function () {
    Route::get('cities', [CityController::class, 'index']);
    Route::get('cities/{city:slug}', [CityController::class, 'show']);

    Route::get('categories', [CategoryController::class, 'index']);

    Route::get('services', [ServiceController::class, 'index']);
    Route::get('services/{service:slug}', [ServiceController::class, 'show']);

    Route::get('search', [ServiceController::class, 'search']);

    Route::post('auth/register', [AuthController::class, 'register'])->middleware('throttle:5,1');
    Route::post('auth/login', [AuthController::class, 'login'])->middleware('throttle:5,1');

    Route::middleware('auth:sanctum')->group(function () {
        Route::post('auth/logout', [AuthController::class, 'logout']);
        Route::get('me', [AuthController::class, 'me']);

        Route::get('me/favorites', [FavoriteController::class, 'index']);
        Route::post('me/favorites', [FavoriteController::class, 'store']);
        Route::delete('me/favorites/{service:slug}', [FavoriteController::class, 'destroy']);

        Route::post('billing/google-play/verify', [BillingController::class, 'verify'])
            ->middleware('throttle:10,1');
        Route::get('billing/entitlements', [BillingController::class, 'entitlements']);
    });
});
