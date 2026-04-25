package com.railprep.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable data object Paywall : AppRoute
}
