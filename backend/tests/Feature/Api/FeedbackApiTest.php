<?php

use App\Models\Feedback;
use App\Models\User;

it('accepts feedback from a guest', function () {
    $response = $this->postJson('/api/v1/feedback', [
        'message' => 'Could not find the Search button on the Home screen.',
        'context' => 'Home',
        'app_version' => '1.0.0',
        'device_info' => 'Pixel 7, Android 14',
    ]);

    $response->assertCreated();
    expect(Feedback::sole())
        ->message->toBe('Could not find the Search button on the Home screen.')
        ->user_id->toBeNull();
});

it('attaches the authenticated user to their feedback', function () {
    $user = User::factory()->create();

    $this->actingAs($user, 'sanctum')->postJson('/api/v1/feedback', [
        'message' => 'The Save button did not respond to a tap.',
    ]);

    expect(Feedback::sole())->user_id->toBe($user->id);
});

it('requires a message', function () {
    $response = $this->postJson('/api/v1/feedback', ['context' => 'Home']);

    $response->assertUnprocessable()->assertJsonValidationErrors('message');
});
