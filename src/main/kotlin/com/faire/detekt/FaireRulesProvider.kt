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
import dev.detekt.api.RuleName
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider
import kotlin.to

internal class FaireRulesProvider : RuleSetProvider {
  override val ruleSetId = RuleSetId("FaireRuleSet")

  override fun instance(): RuleSet = RuleSet(
      ruleSetId,
      mapOf(
          RuleName(AlwaysUseIsTrueOrIsFalse::class.simpleName!!) to { config -> AlwaysUseIsTrueOrIsFalse(config) },
          RuleName(DoNotAccessVisibleForTesting::class.simpleName!!) to { config -> DoNotAccessVisibleForTesting(config) },
          RuleName(DoNotAssertIsEqualOnTheResultOfSingle::class.simpleName!!) to { config -> DoNotAssertIsEqualOnTheResultOfSingle(config) },
          RuleName(DoNotNameCompanionObject::class.simpleName!!) to { config -> DoNotNameCompanionObject(config) },
          RuleName(DoNotSplitByRegex::class.simpleName!!) to { config -> DoNotSplitByRegex(config) },
          RuleName(DoNotUseDirectReceiverReferenceInsideWith::class.simpleName!!) to { config -> DoNotUseDirectReceiverReferenceInsideWith(config) },
          RuleName(DoNotUsePropertyAccessInAssert::class.simpleName!!) to { config -> DoNotUsePropertyAccessInAssert(config) },
          RuleName(DoNotUseHasSizeForEmptyListInAssert::class.simpleName!!) to { config -> DoNotUseHasSizeForEmptyListInAssert(config) },
          RuleName(DoNotUseIsEqualToWhenArgumentIsOne::class.simpleName!!) to { config -> DoNotUseIsEqualToWhenArgumentIsOne(config) },
          RuleName(DoNotUseIsEqualToWhenArgumentIsZero::class.simpleName!!) to { config -> DoNotUseIsEqualToWhenArgumentIsZero(config) },
          RuleName(DoNotUseIsOneAssertions::class.simpleName!!) to { config -> DoNotUseIsOneAssertions(config) },
          RuleName(DoNotUseIsZeroAssertions::class.simpleName!!) to { config -> DoNotUseIsZeroAssertions(config) },
          RuleName(DoNotUseSingleOnFilter::class.simpleName!!) to { config -> DoNotUseSingleOnFilter(config) },
          RuleName(DoNotUseSizePropertyInAssert::class.simpleName!!) to { config -> DoNotUseSizePropertyInAssert(config) },
          RuleName(FilterNotNullOverMapNotNullForFiltering::class.simpleName!!) to { config -> FilterNotNullOverMapNotNullForFiltering(config) },
          RuleName(GetOrDefaultShouldBeReplacedWithGetOrElse::class.simpleName!!) to { config -> GetOrDefaultShouldBeReplacedWithGetOrElse(config) },
          RuleName(NoDuplicateKeysInMapOf::class.simpleName!!) to { config -> NoDuplicateKeysInMapOf(config) },
          RuleName(NoEmptyLinesInConstructorParameters::class.simpleName!!) to { config -> NoEmptyLinesInConstructorParameters(config) },
          RuleName(NoExtensionFunctionOnNullableReceiver::class.simpleName!!) to { config -> NoExtensionFunctionOnNullableReceiver(config) },
          RuleName(NoFunctionReferenceToJavaClass::class.simpleName!!) to { config -> NoFunctionReferenceToJavaClass(config) },
          RuleName(NoNonPrivateGlobalVariables::class.simpleName!!) to { config -> NoNonPrivateGlobalVariables(config) },
          RuleName(NoNullableLambdaWithDefaultNull::class.simpleName!!) to { config -> NoNullableLambdaWithDefaultNull(config) },
          RuleName(NoPairWithAmbiguousTypes::class.simpleName!!) to { config -> NoPairWithAmbiguousTypes(config) },
          RuleName(PreferIgnoreCase::class.simpleName!!) to { config -> PreferIgnoreCase(config) },
          RuleName(PreventBannedImports::class.simpleName!!) to { config -> PreventBannedImports(config) },
          RuleName(ReturnValueOfLetMustBeUsed::class.simpleName!!) to { config -> ReturnValueOfLetMustBeUsed(config) },
          RuleName(UseEntriesInsteadOfValuesOnEnum::class.simpleName!!) to { config -> UseEntriesInsteadOfValuesOnEnum(config) },
          RuleName(UseFirstNotNullOf::class.simpleName!!) to { config -> UseFirstNotNullOf(config) },
          RuleName(UseFirstOrNullInsteadOfFind::class.simpleName!!) to { config -> UseFirstOrNullInsteadOfFind(config) },
          RuleName(UseMapNotNullInsteadOfFilterNotNull::class.simpleName!!) to { config -> UseMapNotNullInsteadOfFilterNotNull(config) },
          RuleName(UseNoneMatchInsteadOfFirstOrNullIsNull::class.simpleName!!) to { config -> UseNoneMatchInsteadOfFirstOrNullIsNull(config) },
          RuleName(UseOfCollectionInsteadOfEmptyCollection::class.simpleName!!) to { config -> UseOfCollectionInsteadOfEmptyCollection(config) },
          RuleName(UseSetInsteadOfListToSet::class.simpleName!!) to { config -> UseSetInsteadOfListToSet(config) },
      ),
  )
}
