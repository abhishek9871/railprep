package com.railprep.core.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import com.railprep.core.design.tokens.Radius

val RailPrepShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.Xs),
    small      = RoundedCornerShape(Radius.Sm),
    medium     = RoundedCornerShape(Radius.Md),
    large      = RoundedCornerShape(Radius.Lg),
    extraLarge = RoundedCornerShape(Radius.Xl),
)
