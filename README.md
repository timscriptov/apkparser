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
    val manifestData = ReadManifest(File("path"))
    val name = manifestData.applicationName
```

```java
    final ReadManifest manifestData = new ReadManifest(new File("path"));
    final String name = manifestData.applicationName;
```

## Update AndroidManifest.xml
```kotlin
    val editor = EditManifest(File("path"))
    editor.setApplicationName("com.mypackage.MyApp")
```

```java
    final EditManifest editor = new EditManifest(new File("path"));
    editor.setApplicationName("com.mypackage.MyApp");
```
