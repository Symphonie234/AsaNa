<?php

namespace App\Http\Controllers\Api;

use App\Enums\ServiceStatus;
use App\Http\Controllers\Controller;
use App\Http\Resources\ServiceResource;
use App\Models\City;
use App\Models\Service;
use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\AnonymousResourceCollection;
use Illuminate\Pagination\LengthAwarePaginator;
use Illuminate\Validation\Rule;

class ServiceController extends Controller
{
    public function index(Request $request): AnonymousResourceCollection
    {
        return ServiceResource::collection($this->filteredServices($request));
    }

    public function show(Service $service): ServiceResource
    {
        abort_unless($service->status === ServiceStatus::Published, 404);

        $service->load(['city', 'category', 'office.officeHours', 'requirements', 'fees']);

        return new ServiceResource($service);
    }

    public function search(Request $request): AnonymousResourceCollection
    {
        $request->validate(['q' => ['required', 'string', 'min:1']]);

        return ServiceResource::collection(
            $this->filteredServices($request, search: $request->string('q')->toString())
        );
    }

    private function filteredServices(Request $request, ?string $search = null): LengthAwarePaginator
    {
        $request->validate([
            'city' => ['sometimes', 'string', Rule::exists(City::class, 'slug')],
            'category' => ['sometimes', 'string'],
            'search' => ['sometimes', 'string'],
        ]);

        return Service::query()
            ->published()
            ->with(['city', 'category', 'office'])
            ->when($request->string('city')->toString(), fn ($query, $slug) => $query->whereHas('city', fn ($q) => $q->where('slug', $slug)))
            ->when($request->string('category')->toString(), fn ($query, $slug) => $query->whereHas('category', fn ($q) => $q->where('slug', $slug)))
            ->when($search ?? $request->string('search')->toString(), fn ($query, $term) => $query->search($term))
            ->orderBy('name')
            ->paginate();
    }
}
