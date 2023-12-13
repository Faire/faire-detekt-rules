package com.faire.detekt.utils

import io.gitlab.arturbosch.detekt.api.Rule
import org.jetbrains.kotlin.resolve.BindingContext

internal fun Rule.isTypeResolutionAvailable(): Boolean = bindingContext != BindingContext.EMPTY
