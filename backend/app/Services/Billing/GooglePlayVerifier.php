<?php

namespace App\Services\Billing;

interface GooglePlayVerifier
{
    /**
     * Asks Google whether a purchase token is real and actually paid for.
     * This is the one thing that must never be trusted from the Android
     * client directly — anyone can send a plausible-looking fake token.
     *
     * @throws BillingVerificationException if Google can't be reached or
     *                                      isn't configured (a real error, distinct from "not purchased")
     */
    public function verifyProductPurchase(string $productId, string $purchaseToken): GooglePlayPurchase;
}
