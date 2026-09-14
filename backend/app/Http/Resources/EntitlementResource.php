<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class EntitlementResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'product_id' => $this->product_id,
            'status' => $this->status,
            'purchased_at' => $this->purchased_at,
            'expires_at' => $this->expires_at,
        ];
    }
}
