package com.faire.detekt.utils

import com.intellij.core.CoreEncodingRegistry
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lang.impl.PsiBuilderFactoryImpl
import com.intellij.mock.MockApplication
import com.intellij.mock.MockFileDocumentManagerImpl
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.impl.CoreProgressManager
import com.intellij.openapi.vfs.encoding.EncodingManager

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
    registerService(EncodingManager::class.java, CoreEncodingRegistry())
  }

  override fun isUnitTestMode(): Boolean = false
}
