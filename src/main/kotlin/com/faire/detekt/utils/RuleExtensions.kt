package com.faire.detekt.utils

import dev.detekt.api.Rule
import org.jetbrains.kotlin.resolve.BindingContext

internal fun Rule.isTypeResolutionAvailable(): Boolean = false
