package com.faire.detekt.rules

import dev.detekt.api.Config

/**
 * Instead of using `.mapNotNull { ... }.firstOrNull()`, use `firstNotNullOfOrNull { ... }` to transform and select
 * an element in a collection simultaneously.
 *
 * Reasons:
 *  Performance: `firstNotNullOfOrNull {}` processes each element at most once and early returns if it finds a
 *               not-null result after the transformation. On the other hand, if not used on a sequence,
 *               `.mapNotNull {}.firstOrNull()` processes the entire collection, which performs transformations on
 *               elements we immediately throw away.
 *
 *  Readability: `firstNotNullOfOrNull {}` clearly expresses the intent to select the first not-null element after the
 *               transformation. Using `.mapNotNull { ... }.firstOrNull()` is more verbose.
 */
internal class UseFirstNotNullOfOrNull(config: Config = Config.empty) :
    MapNotNullFollowedByFirstRule(
    config,
    firstFunctionName = "firstOrNull",
    replacementFunctionName = "firstNotNullOfOrNull",
)
