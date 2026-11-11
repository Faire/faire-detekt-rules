package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.annotations.KaAnnotated
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaDeclarationSymbol
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType

/**
 * [com.google.common.annotations.VisibleForTesting] annotates symbols that have to be made public for tests to use.
 * Hence, production code should not treat these symbols as public. If a symbol annotated with @VisibleForTesting is
 * accessed from a different package, either the caller is violating API contract (as if they were accessing a private
 * symbol), or the symbol should be made public or internal (depending on where it is used).
 *
 * For an annotated class, references to the class and calls to its constructors are reported.
 *
 * This rule itself isn't aware of the difference between production and test code. Path filters should be used to skip
 * all test files.
 */
internal class DoNotAccessVisibleForTesting(config: Config = Config.empty) :
    Rule(
        config,
        "Do not access symbols annotated with @VisibleForTesting from other packages. " +
            "These symbols are made public for testing only.",
    ),
    RequiresAnalysisApi {
  override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
    super.visitSimpleNameExpression(expression)
    if (expression.getNonStrictParentOfType<KtImportDirective>() != null) return
    if (expression.getNonStrictParentOfType<KtPackageDirective>() != null) return

    analyze(expression) {
      val resolvedSymbol = expression.mainReference.resolveToSymbol() as? KaDeclarationSymbol ?: return@analyze
      // Members inherited from a generic class resolve to a copy in the subclass's package
      val symbol = (resolvedSymbol as? KaCallableSymbol)?.fakeOverrideOriginal ?: resolvedSymbol
      val annotatedSymbols = listOfNotNull(symbol, (symbol as? KaConstructorSymbol)?.containingDeclaration)
      if (annotatedSymbols.none { it.hasVisibleForTestingAnnotation() }) return@analyze

      val targetPackage = when (symbol) {
        is KaConstructorSymbol -> symbol.containingClassId?.packageFqName
        is KaCallableSymbol -> symbol.callableId?.packageName
        is KaClassLikeSymbol -> symbol.classId?.packageFqName
        else -> null
      } ?: return@analyze
      val callerPackage = expression.containingKtFile.packageFqName

      if (callerPackage != targetPackage) {
        report(
            Finding(
                entity = Entity.from(expression),
                message = description,
            ),
        )
      }
    }
  }
}

private fun KaAnnotated.hasVisibleForTestingAnnotation(): Boolean =
    annotations.classIds.any { classId -> classId.shortClassName.asString() == "VisibleForTesting" }
