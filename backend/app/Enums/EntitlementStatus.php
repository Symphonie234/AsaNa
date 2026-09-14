<?php

namespace App\Enums;

enum EntitlementStatus: string
{
    case Active = 'active';
    case Refunded = 'refunded';
    case Revoked = 'revoked';
    case Expired = 'expired';
}
