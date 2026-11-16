package com.faire.detekt.rules

import dev.detekt.api.Config

/**
 * Instead of using `.mapNotNull { ... }.first()`, use `firstNotNullOf { ... }` to transform and select
 * an element in a collection simultaneously.
 *
 * Reasons:
 *  Performance: `firstNotNullOf {}` processes each element at most once and early returns if it finds a not-null
 *               result after the transformation. On the other hand, if not used on a sequence, `.mapNotNull {}.first()`
 *               processes the entire collection, which performs transformations on elements we immediately throw away.
 *
 *  Readability: `firstNotNullOf {}` clearly expresses the intent to select the first element after the transformation.
 *               Using `.mapNotNull { ... }.first()` is more verbose.
 */
internal class UseFirstNotNullOf(config: Config = Config.empty) :
    MapNotNullFollowedByFirstRule(
    config,
    firstFunctionName = "first",
    replacementFunctionName = "firstNotNullOf",
)
