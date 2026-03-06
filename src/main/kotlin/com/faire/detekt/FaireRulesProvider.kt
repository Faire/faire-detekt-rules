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
import com.faire.detekt.rules.DoNotUseIsZeroAssertions
import com.faire.detekt.rules.DoNotUsePropertyAccessInAssert
import com.faire.detekt.rules.DoNotUseSingleOnFilter
import com.faire.detekt.rules.DoNotUseSizePropertyInAssert
import com.faire.detekt.rules.FilterNotNullOverMapNotNullForFiltering
import com.faire.detekt.rules.GetOrDefaultShouldBeReplacedWithGetOrElse
import com.faire.detekt.rules.NoDuplicateKeysInMapOf
import com.faire.detekt.rules.NoEmptyLinesInConstructorParameters
import com.faire.detekt.rules.NoExtensionFunctionOnNullableReceiver
import com.faire.detekt.rules.NoFunctionReferenceToJavaClass
import com.faire.detekt.rules.NoNonPrivateGlobalVariables
import com.faire.detekt.rules.NoNullableLambdaWithDefaultNull
import com.faire.detekt.rules.NoPairWithAmbiguousTypes
import com.faire.detekt.rules.PreferIgnoreCase
import com.faire.detekt.rules.PreventBannedImports
import com.faire.detekt.rules.ReturnValueOfLetMustBeUsed
import com.faire.detekt.rules.UseEntriesInsteadOfValuesOnEnum
import com.faire.detekt.rules.UseFirstNotNullOf
import com.faire.detekt.rules.UseFirstOrNullInsteadOfFind
import com.faire.detekt.rules.UseMapNotNullInsteadOfFilterNotNull
import com.faire.detekt.rules.UseNoneMatchInsteadOfFirstOrNullIsNull
import com.faire.detekt.rules.UseOfCollectionInsteadOfEmptyCollection
import com.faire.detekt.rules.UseSetInsteadOfListToSet
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

internal class FaireRulesProvider : RuleSetProvider {
  override val ruleSetId = RuleSetId("FaireRuleSet")

  override fun instance(): RuleSet = RuleSet(
      ruleSetId,
      listOf(
          { AlwaysUseIsTrueOrIsFalse(it) },
          { DoNotAccessVisibleForTesting(it) },
          { DoNotAssertIsEqualOnTheResultOfSingle(it) },
          { DoNotNameCompanionObject(it) },
          { DoNotSplitByRegex(it) },
          { DoNotUseDirectReceiverReferenceInsideWith(it) },
          { DoNotUsePropertyAccessInAssert(it) },
          { DoNotUseHasSizeForEmptyListInAssert(it) },
          { DoNotUseIsEqualToWhenArgumentIsOne(it) },
          { DoNotUseIsEqualToWhenArgumentIsZero(it) },
          { DoNotUseIsOneAssertions(it) },
          { DoNotUseIsZeroAssertions(it) },
          { DoNotUseSingleOnFilter(it) },
          { DoNotUseSizePropertyInAssert(it) },
          { FilterNotNullOverMapNotNullForFiltering(it) },
          { GetOrDefaultShouldBeReplacedWithGetOrElse(it) },
          { NoDuplicateKeysInMapOf(it) },
          { NoEmptyLinesInConstructorParameters(it) },
          { NoExtensionFunctionOnNullableReceiver(it) },
          { NoFunctionReferenceToJavaClass(it) },
          { NoNonPrivateGlobalVariables(it) },
          { NoNullableLambdaWithDefaultNull(it) },
          { NoPairWithAmbiguousTypes(it) },
          { PreferIgnoreCase(it) },
          { PreventBannedImports(it) },
          { ReturnValueOfLetMustBeUsed(it) },
          { UseEntriesInsteadOfValuesOnEnum(it) },
          { UseFirstNotNullOf(it) },
          { UseFirstOrNullInsteadOfFind(it) },
          { UseMapNotNullInsteadOfFilterNotNull(it) },
          { UseNoneMatchInsteadOfFirstOrNullIsNull(it) },
          { UseOfCollectionInsteadOfEmptyCollection(it) },
          { UseSetInsteadOfListToSet(it) },
      ),
  )
}
