<?php

namespace App\Http\Controllers\Api;

use App\Enums\EntitlementStatus;
use App\Http\Controllers\Controller;
use App\Http\Requests\VerifyPurchaseRequest;
use App\Http\Resources\EntitlementResource;
use App\Models\Entitlement;
use App\Services\Billing\BillingVerificationException;
use App\Services\Billing\GooglePlayVerifier;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\AnonymousResourceCollection;

class BillingController extends Controller
{
    public function verify(VerifyPurchaseRequest $request, GooglePlayVerifier $verifier): JsonResponse
    {
        $productId = $request->string('product_id')->toString();
        $purchaseToken = $request->string('purchase_token')->toString();

        try {
            $purchase = $verifier->verifyProductPurchase($productId, $purchaseToken);
        } catch (BillingVerificationException $e) {
            report($e);

            return response()->json([
                'message' => 'Billing verification is temporarily unavailable. Please try again shortly.',
            ], 503);
        }

        if (! $purchase->isPurchased) {
            return response()->json([
                'message' => 'This purchase could not be verified.',
            ], 422);
        }

        $entitlement = Entitlement::updateOrCreate(
            ['purchase_token' => $purchaseToken],
            [
                'user_id' => $request->user()->id,
                'product_id' => $productId,
                'status' => EntitlementStatus::Active,
                'purchased_at' => $purchase->purchasedAt ?? now(),
                'expires_at' => null,
            ],
        );

        return response()->json(['data' => new EntitlementResource($entitlement)]);
    }

    public function entitlements(Request $request): AnonymousResourceCollection
    {
        return EntitlementResource::collection($request->user()->entitlements);
    }
}
