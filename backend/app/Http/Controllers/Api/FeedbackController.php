<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StoreFeedbackRequest;
use App\Models\Feedback;
use Illuminate\Http\JsonResponse;

class FeedbackController extends Controller
{
    public function store(StoreFeedbackRequest $request): JsonResponse
    {
        Feedback::create([
            ...$request->validated(),
            'user_id' => $request->user('sanctum')?->id,
        ]);

        return response()->json(null, 201);
    }
}
