<?php

namespace App\Services\Billing;

use Firebase\JWT\JWT;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Http;

/**
 * Verifies a purchase token against the real Android Publisher API.
 *
 * Deliberately hand-rolled (JWT-sign a service-account assertion, exchange
 * it for an access token, call one REST endpoint) instead of pulling in
 * google/apiclient — that package generates bindings for Google's entire
 * API surface to use a single endpoint here, which is a lot of weight for
 * what's a ~30-line OAuth2 flow.
 */
class GooglePlayApiVerifier implements GooglePlayVerifier
{
    private const TOKEN_URL = 'https://oauth2.googleapis.com/token';

    private const SCOPE = 'https://www.googleapis.com/auth/androidpublisher';

    public function __construct(
        private readonly string $packageName,
        private readonly ?string $serviceAccountPath,
    ) {}

    public function verifyProductPurchase(string $productId, string $purchaseToken): GooglePlayPurchase
    {
        $url = sprintf(
            'https://androidpublisher.googleapis.com/androidpublisher/v3/applications/%s/purchases/products/%s/tokens/%s',
            $this->packageName,
            $productId,
            $purchaseToken,
        );

        $response = Http::withToken($this->getAccessToken())->get($url);

        if ($response->status() === 404) {
            return new GooglePlayPurchase(isPurchased: false, purchasedAt: null);
        }

        if ($response->failed()) {
            throw new BillingVerificationException("Google Play API request failed: HTTP {$response->status()}");
        }

        $data = $response->json();
        $purchaseTimeMillis = $data['purchaseTimeMillis'] ?? null;

        return new GooglePlayPurchase(
            // purchaseState: 0 = purchased, 1 = canceled, 2 = pending.
            isPurchased: ($data['purchaseState'] ?? null) === 0,
            purchasedAt: $purchaseTimeMillis
                ? \DateTimeImmutable::createFromFormat('U', (string) intdiv((int) $purchaseTimeMillis, 1000))
                : null,
        );
    }

    private function getAccessToken(): string
    {
        if (! $this->serviceAccountPath || ! is_readable($this->serviceAccountPath)) {
            throw new BillingVerificationException(
                'Google Play billing is not configured: no service account credentials found. '.
                'Set GOOGLE_PLAY_SERVICE_ACCOUNT_PATH once the app is registered in Play Console.',
            );
        }

        return Cache::remember('google_play_access_token', 3000, function () {
            $credentials = json_decode(file_get_contents($this->serviceAccountPath), true, flags: JSON_THROW_ON_ERROR);

            $now = time();
            $jwt = JWT::encode([
                'iss' => $credentials['client_email'],
                'scope' => self::SCOPE,
                'aud' => self::TOKEN_URL,
                'iat' => $now,
                'exp' => $now + 3600,
            ], $credentials['private_key'], 'RS256');

            $response = Http::asForm()->post(self::TOKEN_URL, [
                'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
                'assertion' => $jwt,
            ]);

            if ($response->failed()) {
                throw new BillingVerificationException('Could not authenticate with Google Play: '.$response->body());
            }

            return $response->json('access_token');
        });
    }
}
