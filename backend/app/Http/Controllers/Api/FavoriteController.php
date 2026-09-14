<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\ServiceResource;
use App\Models\Service;
use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\AnonymousResourceCollection;
use Illuminate\Http\Response;

class FavoriteController extends Controller
{
    public function index(Request $request): AnonymousResourceCollection
    {
        $favorites = $request->user()
            ->favoriteServices()
            ->with(['city', 'category', 'office'])
            ->orderByDesc('favorites.created_at')
            ->get();

        return ServiceResource::collection($favorites);
    }

    public function store(Request $request): AnonymousResourceCollection
    {
        $request->validate(['slug' => ['required', 'string', 'exists:services,slug']]);

        $service = Service::where('slug', $request->string('slug'))->firstOrFail();
        $request->user()->favoriteServices()->syncWithoutDetaching([$service->id]);

        return $this->index($request);
    }

    public function destroy(Request $request, Service $service): Response
    {
        $request->user()->favoriteServices()->detach($service->id);

        return response()->noContent();
    }
}
