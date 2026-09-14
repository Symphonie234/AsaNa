<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class OfficeHour extends Model
{
    use HasFactory;

    protected $fillable = [
        'office_id',
        'day_of_week',
        'opens_at',
        'closes_at',
    ];

    public function office(): BelongsTo
    {
        return $this->belongsTo(Office::class);
    }
}
