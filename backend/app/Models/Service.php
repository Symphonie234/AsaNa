<?php

namespace App\Models;

use App\Enums\ServiceStatus;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Service extends Model
{
    use HasFactory;

    protected $fillable = [
        'city_id',
        'category_id',
        'office_id',
        'name',
        'slug',
        'description',
        'instructions',
        'is_premium',
        'status',
        'verified_at',
        'verified_by',
        'verification_notes',
        'source',
    ];

    protected function casts(): array
    {
        return [
            'is_premium' => 'boolean',
            'status' => ServiceStatus::class,
            'verified_at' => 'datetime',
        ];
    }

    public function city(): BelongsTo
    {
        return $this->belongsTo(City::class);
    }

    public function category(): BelongsTo
    {
        return $this->belongsTo(Category::class);
    }

    public function office(): BelongsTo
    {
        return $this->belongsTo(Office::class);
    }

    public function verifier(): BelongsTo
    {
        return $this->belongsTo(User::class, 'verified_by');
    }

    public function requirements(): HasMany
    {
        return $this->hasMany(Requirement::class)->orderBy('sort_order');
    }

    public function fees(): HasMany
    {
        return $this->hasMany(ServiceFee::class);
    }

    public function getRouteKeyName(): string
    {
        return 'slug';
    }

    public function scopePublished(Builder $query): void
    {
        $query->where('status', ServiceStatus::Published);
    }

    public function scopeSearch(Builder $query, string $term): void
    {
        // whereLike is case-insensitive by default and picks the right SQL
        // per driver (ILIKE on Postgres) — plain LIKE is case-sensitive on
        // Postgres, which silently missed matches like "cedula" vs "Cedula".
        $query->where(fn (Builder $query) => $query
            ->whereLike('name', "%{$term}%")
            ->orWhereLike('description', "%{$term}%"));
    }
}
