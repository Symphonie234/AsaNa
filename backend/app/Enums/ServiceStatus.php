<?php

namespace App\Enums;

enum ServiceStatus: string
{
    case Draft = 'draft';
    case Review = 'review';
    case Published = 'published';
    case Outdated = 'outdated';
    case Archived = 'archived';
}
