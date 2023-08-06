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
    val parser = ManifestParser(File("path"))
    val name = parser.getApplicationName()
```

```java
    final ManifestParser parser = new ManifestParser(new File("path"));
    final String name = parser.getApplicationName();
```

## Update AndroidManifest.xml
```kotlin
    val parser = ManifestParser(File("path"))
    parser.setApplicationName("com.mypackage.MyApp")
```

```java
    final ManifestParser parser = new ManifestParser(new File("path"));
    parser.setApplicationName("com.mypackage.MyApp");
```
