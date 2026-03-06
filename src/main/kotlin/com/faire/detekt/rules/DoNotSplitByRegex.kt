package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

private val STRING_CLASS_ID = ClassId.topLevel(FqName("kotlin.String"))
private val REGEX_CLASS_ID = ClassId.topLevel(FqName("kotlin.text.Regex"))

/**
 * Prefer using a string literal instead of a regex in [String.split].
 *
 * In Java, [String.split] only takes a regex, and there is a heuristic to use an optimized version if the regex is a
 * string literal. Kotlin, however, does not have that heuristic and encourages the explicit use of a string literal.
 * There is a HUGE performance penalty in terms of both CPU and memory.
 * https://youtrack.jetbrains.com/issue/KT-16661/Performance-overhead-in-string-splitting-in-Kotlin-versus-Java
 */
internal class DoNotSplitByRegex(config: Config = Config.empty) :
    Rule(
        config,
        "use string literals in split() whenever possible for better performance and less memory. " +
            "If you have to use regexes, suppress the rule by @Suppress(\"DoNotSplitByRegex\"), " +
            "and make sure the regex is initialized only once (i.e. statically).",
    ),
    RequiresAnalysisApi {
  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)
    if (expression.referenceExpression()?.text != "split") {
      return
    }

    analyze(expression) {
      val call = expression.resolveToCall()?.successfulFunctionCallOrNull() ?: return@analyze
      val receiverType = call.partiallyAppliedSymbol.extensionReceiver?.type ?: return@analyze
      if (receiverType !is KaClassType || receiverType.classId != STRING_CLASS_ID) {
        return@analyze
      }

      val hasRegexArg = call.argumentMapping.values.any { signature ->
        val paramType = signature.returnType
        paramType is KaClassType && paramType.classId == REGEX_CLASS_ID
      }

      if (hasRegexArg) {
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
