package com.faire.detekt.utils

import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.getSuperNames

private fun KtClassOrObject.implementsSuperWithPrefix(prefix: String): Boolean {
  return getSuperNames().any { it.startsWith(prefix) }
}

// This is an assumption that anything prefixed with "Db" inherits from DbEntity
internal fun KtClassOrObject.implementsDbEntity(): Boolean {
  return implementsSuperWithPrefix("Db")
}

internal fun KtClassOrObject.containsAnnotation(annotation: String): Boolean {
  return annotationEntries.any { it.shortName?.identifier == annotation }
}

private fun KtClassOrObject.getAnnotation(annotation: String): KtAnnotationEntry? {
  return annotationEntries.firstOrNull { it.shortName?.identifier == annotation }
}

