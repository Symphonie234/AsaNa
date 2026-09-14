<?php

namespace App\Filament\Resources\Offices\RelationManagers;

use Filament\Actions\BulkActionGroup;
use Filament\Actions\CreateAction;
use Filament\Actions\DeleteAction;
use Filament\Actions\DeleteBulkAction;
use Filament\Actions\EditAction;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\TimePicker;
use Filament\Resources\RelationManagers\RelationManager;
use Filament\Schemas\Schema;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Table;

class OfficeHoursRelationManager extends RelationManager
{
    protected static string $relationship = 'officeHours';

    private const DAYS = [
        0 => 'Sunday',
        1 => 'Monday',
        2 => 'Tuesday',
        3 => 'Wednesday',
        4 => 'Thursday',
        5 => 'Friday',
        6 => 'Saturday',
    ];

    public function form(Schema $schema): Schema
    {
        return $schema
            ->components([
                Select::make('day_of_week')
                    ->options(self::DAYS)
                    ->required(),
                TimePicker::make('opens_at')
                    ->required(),
                TimePicker::make('closes_at')
                    ->required(),
            ]);
    }

    public function table(Table $table): Table
    {
        return $table
            ->recordTitleAttribute('day_of_week')
            ->emptyStateHeading('No office hours')
            ->emptyStateDescription('Add an opening and closing time for each day this office is open.')
            ->columns([
                TextColumn::make('day_of_week')
                    ->formatStateUsing(fn (int $state) => self::DAYS[$state])
                    ->sortable(),
                TextColumn::make('opens_at')
                    ->time(),
                TextColumn::make('closes_at')
                    ->time(),
            ])
            ->defaultSort('day_of_week')
            ->headerActions([
                CreateAction::make(),
            ])
            ->recordActions([
                EditAction::make(),
                DeleteAction::make(),
            ])
            ->toolbarActions([
                BulkActionGroup::make([
                    DeleteBulkAction::make(),
                ]),
            ]);
    }
}
