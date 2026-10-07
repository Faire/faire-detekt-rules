package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedElementSelector

/**
 * Prevents statically importing functions declared on `object` types (including companion objects) and Java static
 * methods. Call sites should reference them through their type instead, e.g. `MySpecificObject.theStaticFunction()`
 * rather than `import com.faire.MySpecificObject.theStaticFunction` followed by `theStaticFunction()`.
 *
 * Statically importing these functions hurts readability in diffs: a reviewer usually only sees the changed lines,
 * and without the type name next to the call, the context of where the function comes from is missing from them.
 *
 * Some types are conventionally always statically imported (e.g. AssertJ's `Assertions`). These are configured via
 * `allowedTypes`, which accepts either simple names or fully qualified names. An entry also covers objects nested in
 * the type, such as its companion object.
 *
 * Imports are first filtered by name, relying on packages starting with a lowercase letter and types with an
 * uppercase one, so that only imports that can be a function on a type reach type resolution.
 */
internal class DoNotStaticallyImportObjectFunctions(config: Config = Config.empty) :
    Rule(
        config,
        "Do not statically import functions of object types, reference them through the type name instead.",
    ),
    RequiresAnalysisApi {

  private val allowedTypes: List<String> by config(defaultValue = listOf<String>())

  override fun visitImportDirective(importDirective: KtImportDirective) {
    super.visitImportDirective(importDirective)

    val importedReference = importDirective.importedReference ?: return
    val (ownerReference, importedName) = if (importDirective.isAllUnder) {
      importedReference to null
    } else {
      val qualifiedReference = importedReference as? KtQualifiedExpression ?: return
      qualifiedReference.receiverExpression to (importDirective.importedFqName?.shortName() ?: return)
    }
    val ownerNameReference = ownerReference.getQualifiedElementSelector() as? KtNameReferenceExpression ?: return
    val ownerFqName = FqName(ownerReference.text)
    if (!ownerFqName.couldBeType() || importedName?.couldBeFunction() == false || ownerFqName.isAllowed()) return

    val importedOwner = analyze(importDirective) {
      val ownerSymbol = ownerNameReference.mainReference.resolveToSymbol() as? KaClassSymbol ?: return@analyze null
      val scope = if (ownerSymbol.classKind.isObject) ownerSymbol.memberScope else ownerSymbol.staticMemberScope
      val callables = if (importedName == null) scope.callables { true } else scope.callables(importedName)
      val importsFunction = callables.any { it is KaNamedFunctionSymbol && !it.isExtension }
      if (!importsFunction) return@analyze null

      val classId = ownerSymbol.classId ?: return@analyze null
      if (ownerSymbol.classKind == KaClassKind.COMPANION_OBJECT) classId.outerClassId else classId
    } ?: return

    val ownerName = importedOwner.shortClassName.asString()
    report(
        Finding(
            entity = Entity.from(importDirective),
            message = "Do not statically import functions of $ownerName, " +
                "call them as $ownerName.${importedName ?: "function"}() instead.",
        ),
    )
  }

  private fun FqName.couldBeType(): Boolean = shortName().asString().first().isUpperCase()

  private fun Name.couldBeFunction(): Boolean = asString().first().isLowerCase()

  private fun FqName.isAllowed(): Boolean {
    val segments = pathSegments().map { it.asString() }
    return segments.indices
        .filter { segments[it].first().isUpperCase() }
        .any { index ->
          val simpleName = segments[index]
          val qualifiedName = segments.take(index + 1).joinToString(".")
          allowedTypes.any { it == simpleName || it == qualifiedName }
        }
  }
}
