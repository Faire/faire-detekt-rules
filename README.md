# faire-detekt-rules
An opinionated ruleset for Detekt, which aims to reduce bugs, improve readability, and standardize code conventions.

## Usage
In the `dependencies` block of your `build.gradle` file, add the following:
```groovy
detektPlugins("com.faire:faire-detekt-rules:0.5.3")
```
or if you're using a `libs.version.toml` file, add this there:
```
faire-detekt-rules = { module = "com.faire:faire-detekt-rules", version = "0.5.3" }
```
and this in your `build.gradle` file:
```groovy
detektPlugins(rootProject.libs.faire.detekt.rules)
```

You can find the latest version [here](https://central.sonatype.com/artifact/com.faire/faire-detekt-rules).

## Configuration
The below block is a starting point for the configuration to add to `detekt.yml`, and can be modified as needed:
```yaml
FaireRuleSet:
  active: true
  AlwaysUseIsTrueOrIsFalse:
    active: true
    autoCorrect: true
  DoNotAccessVisibleForTesting:
    active: true
    excludes: ["**/*Test.kt"]
  DoNotSplitByRegex:
    active: true
  DoNotStaticallyImportObjectFunctions:
    active: true
    allowedTypes: ["org.assertj.core.api.Assertions"]
  DoNotUseDirectReceiverReferenceInsideWith:
    active: true
  DoNotUseHasSizeForEmptyListInAssert:
    active: true
    autoCorrect: true
  DoNotUseIsEqualToWhenArgumentIsOne:
    active: true
    autoCorrect: true
  DoNotUseIsEqualToWhenArgumentIsZero:
    active: true
    autoCorrect: true
  DoNotUsePropertyAccessInAssert:
    active: true
    autoCorrect: true
  DoNotUseSingleOnFilter:
    active: true
    autoCorrect: true
  DoNotUseSizePropertyInAssert:
    active: true
  GetOrDefaultShouldBeReplacedWithGetOrElse:
    active: true
  NestedWithIsBanned:
    active: false
  NoDuplicateKeysInMapOf:
    active: true
  NoExtensionFunctionOnNullableReceiver:
    active: true
  NoNonPrivateGlobalVariables:
    active: true
  NoNullableLambdaWithDefaultNull:
    active: true
  NoPairWithAmbiguousTypes:
    active: true
  PreferIgnoreCase:
    active: true
    autoCorrect: true
  PreventBannedImports:
    active: true
    autoCorrect: true
  ReturnValueOfLetMustBeUsed:
    active: true
  UseEntriesInsteadOfValuesOnEnum:
    active: true
    autoCorrect: false
  UseFirstNotNullOf:
    active: true
    autoCorrect: true
  UseFirstNotNullOfOrNull:
    active: true
    autoCorrect: true
  UseFirstOrNullInsteadOfFind:
    active: true
    autoCorrect: true
  UseMapNotNullInsteadOfFilterNotNull:
    active: true
    autoCorrect: true
  UseMatchAssertionsOnCollections:
    active: true
    autoCorrect: true
  UseNoneMatchInsteadOfFirstOrNullIsNull:
    active: true
    autoCorrect: true
  UseOfCollectionInsteadOfEmptyCollection:
    active: true
    autoCorrect: true
  UseSetInsteadOfListToSet:
    active: true
    autoCorrect: true
```

### Per-rule Configuration
**PreventBannedImports:**
By default, this rule does not prevent any imports and must be configured explicitly. For imports that have an
alternative, configure the list using `withAlternatives` in the format `banned=alternative`, and the import will be
corrected automatically, if auto correct is enabled. For example `java.lang.Integer.max=kotlin.math.max`. For imports
that do not have an alternative, configure the list using `withoutAlternatives`.

**DoNotStaticallyImportObjectFunctions:**
Flags static imports of functions declared on `object` types, companion objects, and Java static methods, so call
sites reference them through the type name (e.g. `MySpecificObject.theStaticFunction()`). By default no types are
allowed. Types that should always be statically imported, such as AssertJ's `Assertions`, can be listed in
`allowedTypes` by fully qualified name (e.g. `org.assertj.core.api.Assertions`). An entry also covers objects nested
in the type, such as its companion object. This rule requires type resolution.

**DoNotAccessVisibleForTesting:**
It is strong recommended to configure this rule with `excludes: ["**/*Test.kt"]` (following the test file naming
convention of the project) to allow test code access to the annotated members.

**NestedWithIsBanned:**
Flags a `with` call inside the lambda of another `with` call. Calls with a receiver, such as `scoper.with(...)`, are
not flagged. Imports of `kotlin.with` under an alias are matched. This rule is inactive by default; set
`active: true` to enable it.

**UseMatchAssertionsOnCollections:**
Flags assertions such as `assertThat(items.any { predicate }).isTrue()` or
`assertThat(items.filter { predicate }).isEmpty()`, and auto-corrects them to `assertThat(items).anyMatch { predicate }`
or `assertThat(items).noneMatch { predicate }`. Only `Iterable` receivers are flagged. This rule requires type
resolution.

## Type Resolution
A number of the rules require [type resolution](https://detekt.dev/docs/gettingstarted/type-resolution/) to be enabled to function properly. If type resolution is not enabled, the rules will silently continue.

# Releasing

To release a new version: 

1. Create a PR that bumps the `version` in [`build.gradle.kts`](./build.gradle.kts).
2. Once this PR merges with the new version and completes CI, create a new version through Github releases
