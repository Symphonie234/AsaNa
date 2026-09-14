<?php

namespace App\Services\Billing;

final readonly class GooglePlayPurchase
{
    public function __construct(
        public bool $isPurchased,
        public ?\DateTimeImmutable $purchasedAt,
    ) {}
}
