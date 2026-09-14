<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ServiceFee extends Model
{
    use HasFactory;

    protected $fillable = [
        'service_id',
        'description',
        'amount_min',
        'amount_max',
        'notes',
    ];

    protected function casts(): array
    {
        return [
            'amount_min' => 'decimal:2',
            'amount_max' => 'decimal:2',
        ];
    }

    public function service(): BelongsTo
    {
        return $this->belongsTo(Service::class);
    }
}
