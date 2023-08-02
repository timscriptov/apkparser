[![](https://jitpack.io/v/TimScriptov/apkparser.svg)](https://jitpack.io/#TimScriptov/apkparser)

## Add it in your root build.gradle at the end of repositories:
```groovy
    allprojects {
        repositories {
            //...
            maven { url 'https://jitpack.io' }
        }
    }
```

## Add the dependency
```groovy
    dependencies {
        implementation 'com.github.TimScriptov:apkparser:Tag'
    }
```

## Read AndroidManifest.xml
```kotlin
    val manifestData = ManifestParser(File("path"))
    val name = manifestData.applicationName
```

```java
    final ManifestParser manifestData = new ManifestParser(new File("path"));
    final String name = manifestData.getApplicationName();
```

## Update AndroidManifest.xml
```kotlin
    val editor = ManifestParser(File("path"))
    editor.applicationName = "com.mypackage.MyApp"
```

```java
    final ManifestParser editor = new ManifestParser(new File("path"));
    editor.setApplicationName("com.mypackage.MyApp");
```
