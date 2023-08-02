package com.mcal.apkparser.xml

import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class ReadManifest {
    private lateinit var aXml: AXmlDecoder
    private lateinit var parser: AXmlResourceParser

    constructor(path: String) {
        parse(File(path).readBytes())
    }

    constructor(manifest: File) {
        parse(manifest.readBytes())
    }

    constructor(manifest: InputStream) {
        parse(manifest.readBytes())
    }

    constructor(manifest: ByteArray) {
        parse(manifest)
    }

    private fun parse(byteArray: ByteArray) {
        aXml = AXmlDecoder.decode(byteArray.inputStream())
        parser = AXmlResourceParser().apply {
            open(ByteArrayInputStream(aXml.data), aXml.mTableStrings)
        }
    }

    val packageName by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeStringValue("manifest", "package")
    }
    val applicationName by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeStringValue("application", NAME)
    }
    val appComponentFactoryName by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeStringValue("application", APP_COMPONENT_FACTORY)
    }
    val extractNativeLibs by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeBooleanValue("application", EXTRACT_NATIVE_LIBS)
    }
    val allowBackup by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeBooleanValue("application", ALLOW_BACKUP)
    }
    val largeHeap by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeBooleanValue("application", LARGE_HEAP)
    }
    val supportsRtl by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeBooleanValue("application", SUPPORTS_RTL)
    }
    val usesCleartextTraffic by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeBooleanValue("application", USES_CLEARTEXT_TRAFFIC)
    }
    val requestLegacyExternalStorage by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeBooleanValue("application", REQUEST_LEGACY_EXTERNAL_STORAGE)
    }
    val preserveLegacyExternalStorage by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeBooleanValue("application", PRESERVE_LEGACY_EXTERNAL_STORAGE)
    }
    val allServiceName by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeListValue("service", NAME)
    }
    val allReceiverName by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeListValue("receiver", NAME)
    }
    val allActivityName by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeListValue("activity", NAME)
    }
    val versionCode by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeStringValue("manifest", VERSION_CODE)
    }
    val versionName by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeStringValue("manifest", VERSION_NAME)
    }
    val compileSdkVersion by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeStringValue("manifest", COMPILE_SDK_VERSION)
    }
    val compileSdkVersionCodename by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeStringValue("manifest", COMPILE_SDK_VERSION_CODENAME)
    }
    val minSdkVersion by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeStringValue("manifest", MIN_SDK_VERSION)
    }
    val targetSdkVersion by lazy(LazyThreadSafetyMode.NONE) {
        findAttributeStringValue("manifest", TARGET_SDK_VERSION)
    }

    fun findAttributeListValue(name: String, attributeNameResource: Int): List<String> {
        val list = arrayListOf<String>()
        try {
            var type: Int
            while (parser.next().also { type = it } != XmlPullParser.END_DOCUMENT) {
                if (type != XmlPullParser.START_TAG) {
                    continue
                }
                if (parser.name == name) {
                    for (i in 0 until parser.attributeCount) {
                        if (parser.getAttributeNameResource(i) == attributeNameResource) {
                            list.add(parser.getAttributeValue(i))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun findAttributeBooleanValue(name: String, attributeNameResource: Int): Boolean {
        try {
            var type: Int
            while (parser.next().also { type = it } != XmlPullParser.END_DOCUMENT) {
                if (type != XmlPullParser.START_TAG) {
                    continue
                }
                if (parser.name == name) {
                    for (i in 0 until parser.attributeCount) {
                        if (parser.getAttributeNameResource(i) == attributeNameResource) {
                            return parser.getAttributeBooleanValue(i, false)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun findAttributeStringValue(name: String, attributeName: String): String? {
        try {
            var type: Int
            while (parser.next().also { type = it } != XmlPullParser.END_DOCUMENT) {
                if (type != XmlPullParser.START_TAG) {
                    continue
                }
                if (parser.name == name) {
                    for (i in 0 until parser.attributeCount) {
                        if (parser.getAttributeName(i) == attributeName) {
                            return parser.getAttributeValue(i)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun findAttributeStringValue(name: String, attributeNameResource: Int): String? {
        try {
            var type: Int
            while (parser.next().also { type = it } != XmlPullParser.END_DOCUMENT) {
                if (type != XmlPullParser.START_TAG) {
                    continue
                }
                if (parser.name == name) {
                    for (i in 0 until parser.attributeCount) {
                        if (parser.getAttributeNameResource(i) == attributeNameResource) {
                            return parser.getAttributeValue(i)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        /**
         * https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/res/res/values/public-final.xml
         */
        private const val EXTRACT_NATIVE_LIBS = 0x010104ea
        private const val NAME = 0x01010003
        private const val APP_COMPONENT_FACTORY = 0x0101057a
        private const val VERSION_CODE = 0x0101021b
        private const val VERSION_NAME = 0x0101021c

        private const val COMPILE_SDK_VERSION = 0x01010572
        private const val COMPILE_SDK_VERSION_CODENAME = 0x01010573
        private const val ALLOW_BACKUP = 0x01010280
        private const val LARGE_HEAP = 0x0101035a
        private const val SUPPORTS_RTL = 0x010103af
        private const val USES_CLEARTEXT_TRAFFIC = 0x010104ec
        private const val REQUEST_LEGACY_EXTERNAL_STORAGE = 0x01010603
        private const val PRESERVE_LEGACY_EXTERNAL_STORAGE = 0x01010614
        private const val MIN_SDK_VERSION = 0x0101020c
        private const val TARGET_SDK_VERSION = 0x01010270
    }
}
