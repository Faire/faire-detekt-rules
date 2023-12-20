package com.faire.detekt.utils

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.supertypes

private fun KotlinType.getClassFqName(): FqName? {
  return (constructor.declarationDescriptor as? ClassDescriptor)?.fqNameSafe
}

internal fun KotlinType.isTypeInClassFqNames(fqNames: Collection<FqName>): Boolean {
  if (getClassFqName() in fqNames) return true

  return supertypes().any { it.getClassFqName() in fqNames }
}
