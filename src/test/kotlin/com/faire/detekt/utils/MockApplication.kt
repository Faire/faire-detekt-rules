package com.faire.detekt.utils

import org.jetbrains.kotlin.com.intellij.lang.PsiBuilderFactory
import org.jetbrains.kotlin.com.intellij.lang.impl.PsiBuilderFactoryImpl
import org.jetbrains.kotlin.com.intellij.mock.MockApplication
import org.jetbrains.kotlin.com.intellij.mock.MockFileDocumentManagerImpl
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.editor.impl.DocumentImpl
import org.jetbrains.kotlin.com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.kotlin.com.intellij.openapi.progress.ProgressManager
import org.jetbrains.kotlin.com.intellij.openapi.progress.impl.CoreProgressManager

internal class MockApplication(parentDisposable: Disposable) : MockApplication(parentDisposable) {
  init {
    registerService(
        FileDocumentManager::class.java,
        MockFileDocumentManagerImpl(
            null,
        ) { charSequence ->
          DocumentImpl(charSequence)
        },
    )
    registerService(PsiBuilderFactory::class.java, PsiBuilderFactoryImpl())
    registerService(ProgressManager::class.java, CoreProgressManager())
  }

  override fun isUnitTestMode(): Boolean = false
}
