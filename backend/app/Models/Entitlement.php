<?php

namespace App\Models;

use App\Enums\EntitlementStatus;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Entitlement extends Model
{
    use HasFactory;

    protected $fillable = [
        'user_id',
        'product_id',
        'purchase_token',
        'status',
        'purchased_at',
        'expires_at',
    ];

    protected function casts(): array
    {
        return [
            'status' => EntitlementStatus::class,
            'purchased_at' => 'datetime',
            'expires_at' => 'datetime',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
