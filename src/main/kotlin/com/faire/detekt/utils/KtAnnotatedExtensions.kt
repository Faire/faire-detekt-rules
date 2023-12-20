/**
 * Stolen from the Detekt source code. These extensions to [KtAnnotated] may also be useful for our rules.
 */
package com.faire.detekt.utils

import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtUserType

internal fun KtAnnotationEntry.getAnnotationName(): String? {
  val tr = typeReference ?: return null
  val type = tr.typeElement
  if (type is KtUserType) {
    return type.referencedName
  }
  return null
}
