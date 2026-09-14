<?php

namespace App\Filament\Resources\Entitlements\Schemas;

use App\Enums\EntitlementStatus;
use Filament\Forms\Components\DateTimePicker;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\TextInput;
use Filament\Schemas\Schema;

class EntitlementForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->components([
                Select::make('user_id')
                    ->relationship('user', 'name')
                    ->searchable()
                    ->preload()
                    ->required(),
                TextInput::make('product_id')
                    ->required(),
                TextInput::make('purchase_token')
                    ->label('Google Play purchase token')
                    ->required()
                    ->unique(ignoreRecord: true)
                    ->columnSpanFull(),
                Select::make('status')
                    ->options(EntitlementStatus::class)
                    ->default(EntitlementStatus::Active)
                    ->required(),
                DateTimePicker::make('purchased_at')
                    ->required(),
                DateTimePicker::make('expires_at')
                    ->helperText('Leave blank for a lifetime entitlement.'),
            ]);
    }
}
