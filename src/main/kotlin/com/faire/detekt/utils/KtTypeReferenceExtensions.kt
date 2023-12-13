package com.faire.detekt.utils

import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType

internal fun KtTypeReference.getTypeName(): String? {
  val element = typeElement ?: return null
  return when (element) {
    is KtUserType -> element.referencedName
    is KtNullableType -> (element.innerType as? KtUserType)?.referencedName
    else -> null
  }
}
