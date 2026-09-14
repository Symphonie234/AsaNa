<?php

use App\Http\Controllers\Api\CategoryController;
use App\Http\Controllers\Api\CityController;
use App\Http\Controllers\Api\ServiceController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

Route::get('/user', function (Request $request) {
    return $request->user();
})->middleware('auth:sanctum');

Route::prefix('v1')->group(function () {
    Route::get('cities', [CityController::class, 'index']);
    Route::get('cities/{city:slug}', [CityController::class, 'show']);

    Route::get('categories', [CategoryController::class, 'index']);

    Route::get('services', [ServiceController::class, 'index']);
    Route::get('services/{service:slug}', [ServiceController::class, 'show']);

    Route::get('search', [ServiceController::class, 'search']);
});
