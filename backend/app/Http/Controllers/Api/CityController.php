<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\CityResource;
use App\Models\City;
use Illuminate\Http\Resources\Json\AnonymousResourceCollection;

class CityController extends Controller
{
    public function index(): AnonymousResourceCollection
    {
        return CityResource::collection(City::orderBy('name')->get());
    }

    public function show(City $city): CityResource
    {
        return new CityResource($city);
    }
}
