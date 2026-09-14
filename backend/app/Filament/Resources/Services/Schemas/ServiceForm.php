<?php

namespace App\Filament\Resources\Services\Schemas;

use App\Enums\ServiceStatus;
use Filament\Forms\Components\DateTimePicker;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\TextInput;
use Filament\Forms\Components\Textarea;
use Filament\Forms\Components\Toggle;
use Filament\Schemas\Schema;
use Illuminate\Support\Str;

class ServiceForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->components([
                Select::make('city_id')
                    ->relationship('city', 'name')
                    ->searchable()
                    ->preload()
                    ->required(),
                Select::make('category_id')
                    ->relationship('category', 'name')
                    ->searchable()
                    ->preload()
                    ->required(),
                Select::make('office_id')
                    ->relationship('office', 'name')
                    ->searchable()
                    ->preload(),
                TextInput::make('name')
                    ->required()
                    ->live(onBlur: true)
                    ->afterStateUpdated(fn (string $state, callable $set) => $set('slug', Str::slug($state))),
                TextInput::make('slug')
                    ->required()
                    ->unique(ignoreRecord: true),
                Textarea::make('description')
                    ->columnSpanFull(),
                Textarea::make('instructions')
                    ->helperText('One step per line. Shown to users as a numbered checklist.')
                    ->columnSpanFull(),
                Toggle::make('is_premium')
                    ->helperText('Only visible to users with a lifetime unlock.'),
                Select::make('status')
                    ->options(ServiceStatus::class)
                    ->default(ServiceStatus::Draft)
                    ->required(),
                DateTimePicker::make('verified_at')
                    ->helperText('When this content was last confirmed accurate.'),
                Select::make('verified_by')
                    ->relationship('verifier', 'name')
                    ->searchable()
                    ->preload()
                    ->label('Verified by'),
                Textarea::make('verification_notes')
                    ->columnSpanFull(),
                TextInput::make('source'),
            ]);
    }
}
