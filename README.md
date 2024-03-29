# faire-detekt-rules
An opinionated ruleset for Detekt, which aims to reduce bugs, improve readability, and standardize code conventions.

## Usage
In the `dependencies` block of your `build.gradle` file, add the following:
```groovy
detektPlugins("com.github.Faire:faire-detekt-rules:v0.1.1")
```
or if you're using a `libs.version.toml` file, add this there:
```
faire-detekt-rules = { module = "com.github.Faire:faire-detekt-rules", version = "v0.1.1" }
```
and this in your `build.gradle` file:
```groovy
detektPlugins(rootProject.libs.faire.detekt.rules)
```

You may need to add jitpack as a maven repository:
```groovy
maven {
  url = uri("https://jitpack.io")
}
```

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
  UseFirstOrNullInsteadOfFind:
    active: true
    autoCorrect: true
  UseMapNotNullInsteadOfFilterNotNull:
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

**DoNotAccessVisibleForTesting:**
It is strong recommended to configure this rule with `excludes: ["**/*Test.kt"]` (following the test file naming
convention of the project) to allow test code access to the annotated members.

## Type Resolution
A number of the rules require [type resolution](https://detekt.dev/docs/gettingstarted/type-resolution/) to be enabled to function properly. If type resolution is not enabled, the rules will silently continue.
