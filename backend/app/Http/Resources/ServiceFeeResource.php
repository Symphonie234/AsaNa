<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class ServiceFeeResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'description' => $this->description,
            'amount_min' => $this->amount_min,
            'amount_max' => $this->amount_max,
            'notes' => $this->notes,
        ];
    }
}
