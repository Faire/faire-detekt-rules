package com.faire.detekt.rules

import com.faire.detekt.utils.getAnnotationName
import com.faire.detekt.utils.isTypeResolutionAvailable
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.descriptors.containingPackage
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.resolve.bindingContextUtil.getEnclosingDescriptor
import org.jetbrains.kotlin.resolve.bindingContextUtil.getReferenceTargets
import org.jetbrains.kotlin.resolve.lazy.descriptors.LazyAnnotationDescriptor

/**
 * [com.google.common.annotations.VisibleForTesting] annotates symbols that have to be made public for tests to use.
 * Hence, production code should not treat these symbols as public. If a symbol annotated with @VisibleForTesting is
 * accessed from a different package, either the caller is violating API contract (as if they were accessing a private
 * symbol), or the symbol should be made public or internal (depending on where it is used).
 *
 * This rule itself isn't aware of the difference between production and test code. Path filters should be used to skip
 * all test files.
 */
@RequiresTypeResolution
internal class DoNotAccessVisibleForTesting(config: Config = Config.empty) : Rule(config) {
  override val issue: Issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Defect,
      description = "Do not access symbols annotated with @VisibleForTesting from other packages. " +
          "These symbols are made public for testing only.",
      debt = Debt.TEN_MINS,
  )

  override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
    super.visitSimpleNameExpression(expression)
    if (!isTypeResolutionAvailable()) {
      return
    }
    val here = try {
      getEnclosingDescriptor(bindingContext, expression)
    } catch (e: Exception) {
      // Skip elements without a descriptor (e.g. package names, import directives).
      return
    }
    if (expression.getReferenceTargets(bindingContext).any {
          it.hasAnnotation("VisibleForTesting") && here.containingPackage() != it.containingPackage()
        }
    ) {
      report(
          CodeSmell(
              issue = issue,
              entity = Entity.from(expression),
              message = issue.description,
          ),
      )
    }
  }
}

private fun Annotated.hasAnnotation(annotationName: String): Boolean {
  return annotations.any {
    (it as? LazyAnnotationDescriptor)?.annotationEntry?.getAnnotationName() == annotationName
  }
}
