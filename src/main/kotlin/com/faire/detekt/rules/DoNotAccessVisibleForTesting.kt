package com.faire.detekt.rules

import com.faire.detekt.utils.getAnnotationName
import com.faire.detekt.utils.isTypeResolutionAvailable
import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Entity
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.descriptors.containingPackage
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.resolve.bindingContextUtil.getEnclosingDescriptor
import org.jetbrains.kotlin.resolve.bindingContextUtil.getParentOfTypeCodeFragmentAware
import org.jetbrains.kotlin.resolve.bindingContextUtil.getReferenceTargets
import org.jetbrains.kotlin.resolve.lazy.descriptors.LazyAnnotationDescriptor
import org.jetbrains.kotlin.utils.KotlinExceptionWithAttachments

/**
 * [com.google.common.annotations.VisibleForTesting] annotates symbols that have to be made public for tests to use.
 * Hence, production code should not treat these symbols as public. If a symbol annotated with @VisibleForTesting is
 * accessed from a different package, either the caller is violating API contract (as if they were accessing a private
 * symbol), or the symbol should be made public or internal (depending on where it is used).
 *
 * This rule itself isn't aware of the difference between production and test code. Path filters should be used to skip
 * all test files.
 */
internal class DoNotAccessVisibleForTesting(config: Config = Config.empty) : Rule(config, "Do not access symbols annotated with @VisibleForTesting from other packages. These symbols are made public for testing only."), RequiresAnalysisApi {
  override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
    super.visitSimpleNameExpression(expression)
    if (!isTypeResolutionAvailable()) {
      return
    }

    analyze(expression) {
      val here = getParentOfTypeCodeFragmentAware(expression, KtNamedDeclaration::class.java)
//      if (expression.getReferenceTargets(bindingContext).any {
//            it.hasAnnotation("VisibleForTesting") && here.containingPackage() != it.containingPackage()
//          }
//      ) {
//        report(
//            Finding(
//                entity = Entity.from(expression),
//                message = description,
//            ),
//        )
//      }
    }
  }

  private fun KaSession.getParentOfTypeCodeFragmentAware(
      element: KtElement,
      parentType: Class<KtNamedDeclaration>,
  ): DeclarationDescriptor {
    val declaration = element.getParentOfTypeCodeFragmentAware(parentType)
            ?: throw KotlinExceptionWithAttachments("No parent KtNamedDeclaration for of type ${element.javaClass}")
                .withPsiAttachment("element.kt", element)
    return if (declaration is KtFunctionLiteral) {
      this.getParentOfTypeCodeFragmentAware(declaration, parentType)
    } else {
      throw KotlinExceptionWithAttachments("No descriptor for named declaration of type ${declaration.javaClass}")
              .withPsiAttachment("declaration.kt", declaration)
    }
  }
}

private fun Annotated.hasAnnotation(annotationName: String): Boolean {
  return annotations.any {
    (it as? LazyAnnotationDescriptor)?.annotationEntry?.getAnnotationName() == annotationName
  }
}
