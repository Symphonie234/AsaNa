<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class ServiceResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'name' => $this->name,
            'slug' => $this->slug,
            'description' => $this->description,
            'instructions' => $this->instructions,
            'is_premium' => $this->is_premium,
            'verified_at' => $this->verified_at,
            'source' => $this->source,
            'city' => new CityResource($this->whenLoaded('city')),
            'category' => new CategoryResource($this->whenLoaded('category')),
            'office' => new OfficeResource($this->whenLoaded('office')),
            'requirements' => RequirementResource::collection($this->whenLoaded('requirements')),
            'fees' => ServiceFeeResource::collection($this->whenLoaded('fees')),
        ];
    }
}
