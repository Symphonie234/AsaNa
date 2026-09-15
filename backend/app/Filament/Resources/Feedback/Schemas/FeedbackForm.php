<?php

namespace App\Filament\Resources\Feedback\Schemas;

use Filament\Forms\Components\Textarea;
use Filament\Forms\Components\TextInput;
use Filament\Forms\Components\Toggle;
use Filament\Schemas\Schema;

class FeedbackForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->components([
                Textarea::make('message')
                    ->disabled()
                    ->columnSpanFull(),
                TextInput::make('user.name')
                    ->label('From')
                    ->placeholder('Anonymous')
                    ->disabled(),
                TextInput::make('context')
                    ->disabled(),
                TextInput::make('app_version')
                    ->disabled(),
                TextInput::make('device_info')
                    ->disabled(),
                Toggle::make('is_resolved'),
            ]);
    }
}
