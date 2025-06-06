package com.faire.detekt

import com.faire.detekt.rules.AlwaysUseIsTrueOrIsFalse
import com.faire.detekt.rules.DoNotAccessVisibleForTesting
import com.faire.detekt.rules.DoNotAssertIsEqualOnTheResultOfSingle
import com.faire.detekt.rules.DoNotNameCompanionObject
import com.faire.detekt.rules.DoNotSplitByRegex
import com.faire.detekt.rules.DoNotUseDirectReceiverReferenceInsideWith
import com.faire.detekt.rules.DoNotUseHasSizeForEmptyListInAssert
import com.faire.detekt.rules.DoNotUseIsEqualToWhenArgumentIsOne
import com.faire.detekt.rules.DoNotUseIsEqualToWhenArgumentIsZero
import com.faire.detekt.rules.DoNotUseIsOneAssertions
import com.faire.detekt.rules.DoNotUsePropertyAccessInAssert
import com.faire.detekt.rules.DoNotUseSingleOnFilter
import com.faire.detekt.rules.DoNotUseSizePropertyInAssert
import com.faire.detekt.rules.FilterNotNullOverMapNotNullForFiltering
import com.faire.detekt.rules.GetOrDefaultShouldBeReplacedWithGetOrElse
import com.faire.detekt.rules.NoDuplicateKeysInMapOf
import com.faire.detekt.rules.NoExtensionFunctionOnNullableReceiver
import com.faire.detekt.rules.NoFunctionReferenceToJavaClass
import com.faire.detekt.rules.NoNonPrivateGlobalVariables
import com.faire.detekt.rules.NoNullableLambdaWithDefaultNull
import com.faire.detekt.rules.NoPairWithAmbiguousTypes
import com.faire.detekt.rules.PreferIgnoreCase
import com.faire.detekt.rules.PreventBannedImports
import com.faire.detekt.rules.ReturnValueOfLetMustBeUsed
import com.faire.detekt.rules.UseEntriesInsteadOfValuesOnEnum
import com.faire.detekt.rules.UseFirstOrNullInsteadOfFind
import com.faire.detekt.rules.UseMapNotNullInsteadOfFilterNotNull
import com.faire.detekt.rules.UseOfCollectionInsteadOfEmptyCollection
import com.faire.detekt.rules.UseSetInsteadOfListToSet
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

internal class FaireRulesProvider : RuleSetProvider {
  override val ruleSetId = "FaireRuleSet"

  override fun instance(config: Config): RuleSet = RuleSet(
      ruleSetId,
      listOf(
          AlwaysUseIsTrueOrIsFalse(config),
          DoNotAccessVisibleForTesting(config),
          DoNotAssertIsEqualOnTheResultOfSingle(config),
          DoNotNameCompanionObject(config),
          DoNotSplitByRegex(config),
          DoNotUseDirectReceiverReferenceInsideWith(config),
          DoNotUsePropertyAccessInAssert(config),
          DoNotUseHasSizeForEmptyListInAssert(config),
          DoNotUseIsEqualToWhenArgumentIsOne(config),
          DoNotUseIsEqualToWhenArgumentIsZero(config),
          DoNotUseIsOneAssertions(config),
          DoNotUseSingleOnFilter(config),
          DoNotUseSizePropertyInAssert(config),
          FilterNotNullOverMapNotNullForFiltering(config),
          GetOrDefaultShouldBeReplacedWithGetOrElse(config),
          NoDuplicateKeysInMapOf(config),
          NoExtensionFunctionOnNullableReceiver(config),
          NoFunctionReferenceToJavaClass(config),
          NoNonPrivateGlobalVariables(config),
          NoNullableLambdaWithDefaultNull(config),
          NoPairWithAmbiguousTypes(config),
          PreferIgnoreCase(config),
          PreventBannedImports(config),
          ReturnValueOfLetMustBeUsed(config),
          UseEntriesInsteadOfValuesOnEnum(config),
          UseFirstOrNullInsteadOfFind(config),
          UseMapNotNullInsteadOfFilterNotNull(config),
          UseOfCollectionInsteadOfEmptyCollection(config),
          UseSetInsteadOfListToSet(config),
      ),
  )
}
