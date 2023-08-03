package com.mcal.apkparser.xml

import com.mcal.apkparser.util.FileHelper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream

class ManifestParser {
    private var aXml: AXmlDecoder? = null
    private var parser = AXmlResourceParser()
    private var byteArray = byteArrayOf()

    constructor(path: String) : this(File(path).readBytes())
    constructor(file: File) : this(file.readBytes())
    constructor(inputStream: InputStream) : this(inputStream.readBytes())
    constructor(byteArray: ByteArray) {
        reload(byteArray)
    }


    private fun reload(byteArray: ByteArray) {
        this.byteArray = byteArray
        aXml = AXmlDecoder.decode(byteArray.inputStream()).also {
            parser.open(ByteArrayInputStream(it.data), it.mTableStrings)
        }
    }

    fun get(): ByteArray {
        return byteArray
    }

    var packageName: String? = null
        get() = findAttributeStringValue("manifest", "package")
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue, "manifest", "package")
            }
            field = attributeValue
        }

    var applicationName: String? = null
        get() = findAttributeStringValue("application", NAME)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue, "application", NAME)
            }
            field = attributeValue
        }

    var appComponentFactoryName: String? = null
        get() = findAttributeStringValue("application", APP_COMPONENT_FACTORY)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue, "application", APP_COMPONENT_FACTORY)
            }
            field = attributeValue
        }

    var versionCode: String? = null
        get() = findAttributeStringValue("manifest", VERSION_CODE)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue, "manifest", VERSION_CODE)
            }
            field = attributeValue
        }

    var versionName: String? = null
        get() = findAttributeStringValue("manifest", VERSION_NAME)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue, "manifest", VERSION_NAME)
            }
            field = attributeValue
        }

    var compileSdkVersion: String? = null
        get() = findAttributeStringValue("manifest", COMPILE_SDK_VERSION)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue, "manifest", COMPILE_SDK_VERSION)
            }
            field = attributeValue
        }

    var compileSdkVersionCodename: String? = null
        get() = findAttributeStringValue("manifest", COMPILE_SDK_VERSION_CODENAME)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue, "manifest", COMPILE_SDK_VERSION_CODENAME)
            }
            field = attributeValue
        }

    var minSdkVersion: String? = null
        get() = findAttributeStringValue("manifest", MIN_SDK_VERSION)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue, "manifest", MIN_SDK_VERSION)
            }
            field = attributeValue
        }

    var targetSdkVersion: String? = null
        get() = findAttributeStringValue("manifest", TARGET_SDK_VERSION)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue, "manifest", TARGET_SDK_VERSION)
            }
            field = attributeValue
        }

    var extractNativeLibs: Boolean? = false
        get() = findAttributeBooleanValue("application", EXTRACT_NATIVE_LIBS)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue.toString(), "application", EXTRACT_NATIVE_LIBS)
            }
            field = attributeValue
        }

    var allowBackup: Boolean? = false
        get() = findAttributeBooleanValue("application", ALLOW_BACKUP)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue.toString(), "application", ALLOW_BACKUP)
            }
            field = attributeValue
        }

    var largeHeap: Boolean? = false
        get() = findAttributeBooleanValue("application", LARGE_HEAP)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue.toString(), "application", LARGE_HEAP)
            }
            field = attributeValue
        }

    var supportsRtl: Boolean? = false
        get() = findAttributeBooleanValue("application", SUPPORTS_RTL)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue.toString(), "application", SUPPORTS_RTL)
            }
            field = attributeValue
        }

    var usesCleartextTraffic: Boolean? = false
        get() = findAttributeBooleanValue("application", USES_CLEARTEXT_TRAFFIC)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue.toString(), "application", USES_CLEARTEXT_TRAFFIC)
            }
            field = attributeValue
        }

    var requestLegacyExternalStorage: Boolean? = false
        get() = findAttributeBooleanValue("application", REQUEST_LEGACY_EXTERNAL_STORAGE)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue.toString(), "application", REQUEST_LEGACY_EXTERNAL_STORAGE)
            }
            field = attributeValue
        }

    var preserveLegacyExternalStorage: Boolean? = false
        get() = findAttributeBooleanValue("application", PRESERVE_LEGACY_EXTERNAL_STORAGE)
        set(attributeValue) {
            if (attributeValue != null) {
                patching(attributeValue.toString(), "application", PRESERVE_LEGACY_EXTERNAL_STORAGE)
            }
            field = attributeValue
        }

    val allServiceName = findAttributeListValue("service", NAME)
    val allReceiverName = findAttributeListValue("receiver", NAME)
    val allActivityName = findAttributeListValue("activity", NAME)

    private fun patching(attributeValue: String, name: String, attributeName: String) {
        try {
            var success = false
            var type: Int
            val decoder = aXml
            if (decoder != null) {
                while (parser.next().also { type = it } != XmlPullParser.END_DOCUMENT) {
                    if (type != XmlPullParser.START_TAG) continue
                    if (parser.name == name) {
                        var isFoundAttribute = false
                        val size = parser.attributeCount
                        for (i in 0 until size) {
                            if (parser.getAttributeName(i) == attributeName) {
                                isFoundAttribute = true
                                val index = decoder.mTableStrings.size
                                val data = decoder.data
                                var off = parser.currentAttributeStart + 20 * i
                                off += 8
                                FileHelper.writeInt(data, off, index)
                                off += 8
                                FileHelper.writeInt(data, off, index)
                            }
                        }
                        if (!isFoundAttribute) {
                            var off = parser.currentAttributeStart
                            val data = decoder.data
                            val newData = ByteArray(data.size + 20)
                            System.arraycopy(data, 0, newData, 0, off)
                            System.arraycopy(data, off, newData, off + 20, data.size - off)

                            // chunkSize
                            val chunkSize = FileHelper.readInt(newData, off - 32)
                            FileHelper.writeInt(newData, off - 32, chunkSize + 20)

                            // attributeCount
                            FileHelper.writeInt(newData, off - 8, size + 1)
                            val idIndex = parser.findResourceID(NAME)
                            if (idIndex == -1) {
                                throw IOException("idIndex == -1")
                            }
                            var isMax = true
                            for (i in 0 until size) {
                                val id = parser.getAttributeNameResource(i)
                                if (id > NAME) {
                                    isMax = false
                                    if (i != 0) {
                                        System.arraycopy(newData, off + 20, newData, off, 20 * i)
                                        off += 20 * i
                                    }
                                    break
                                }
                            }
                            if (isMax) {
                                System.arraycopy(newData, off + 20, newData, off, 20 * size)
                                off += 20 * size
                            }
                            FileHelper.writeInt(
                                newData,
                                off,
                                decoder.mTableStrings.find(SCHEMAS)
                            )
                            FileHelper.writeInt(newData, off + 4, idIndex)
                            FileHelper.writeInt(newData, off + 8, decoder.mTableStrings.size)
                            FileHelper.writeInt(newData, off + 12, TYPE_STRING)
                            FileHelper.writeInt(newData, off + 16, decoder.mTableStrings.size)
                            decoder.data = newData
                        }
                        success = true
                        break
                    }
                }
                if (!success) {
                    throw IOException()
                }
                val list = ArrayList<String>(decoder.mTableStrings.size)
                decoder.mTableStrings.getStrings(list)
                list.add(attributeValue)
                val byteArrayOutputStream = ByteArrayOutputStream()
                decoder.write(list, byteArrayOutputStream)

                reload(byteArrayOutputStream.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun patching(
        attributeValue: String,
        name: String,
        attributeNameResource: Int
    ) {
        try {
            var success = false
            var type: Int
            val decoder = aXml
            if (decoder != null) {
                while (parser.next().also { type = it } != XmlPullParser.END_DOCUMENT) {
                    if (type != XmlPullParser.START_TAG) {
                        continue
                    }
                    if (parser.name == name) {
                        var isFoundAttribute = false
                        val size = parser.attributeCount
                        for (i in 0 until size) {
                            if (parser.getAttributeNameResource(i) == attributeNameResource) {
                                isFoundAttribute = true
                                val index = decoder.mTableStrings.size
                                val data = decoder.data
                                var off = parser.currentAttributeStart + 20 * i
                                off += 8
                                FileHelper.writeInt(data, off, index)
                                off += 8
                                FileHelper.writeInt(data, off, index)
                            }
                        }
                        if (!isFoundAttribute) {
                            var off = parser.currentAttributeStart
                            val data = decoder.data
                            val newData = ByteArray(data.size + 20)
                            System.arraycopy(data, 0, newData, 0, off)
                            System.arraycopy(data, off, newData, off + 20, data.size - off)

                            // chunkSize
                            val chunkSize = FileHelper.readInt(newData, off - 32)
                            FileHelper.writeInt(newData, off - 32, chunkSize + 20)

                            // attributeCount
                            FileHelper.writeInt(newData, off - 8, size + 1)
                            val idIndex = parser.findResourceID(NAME)
                            if (idIndex == -1) {
                                throw IOException("idIndex == -1")
                            }
                            var isMax = true
                            for (i in 0 until size) {
                                val id = parser.getAttributeNameResource(i)
                                if (id > NAME) {
                                    isMax = false
                                    if (i != 0) {
                                        System.arraycopy(newData, off + 20, newData, off, 20 * i)
                                        off += 20 * i
                                    }
                                    break
                                }
                            }
                            if (isMax) {
                                System.arraycopy(newData, off + 20, newData, off, 20 * size)
                                off += 20 * size
                            }
                            FileHelper.writeInt(
                                newData,
                                off,
                                decoder.mTableStrings.find(SCHEMAS)
                            )
                            FileHelper.writeInt(newData, off + 4, idIndex)
                            FileHelper.writeInt(newData, off + 8, decoder.mTableStrings.size)
                            FileHelper.writeInt(newData, off + 12, TYPE_STRING)
                            FileHelper.writeInt(newData, off + 16, decoder.mTableStrings.size)
                            decoder.data = newData
                        }
                        success = true
                        break
                    }
                }
                if (!success) {
                    throw IOException()
                }
                val list = ArrayList<String>(decoder.mTableStrings.size)
                decoder.mTableStrings.getStrings(list)
                list.add(attributeValue)
                val byteArrayOutputStream = ByteArrayOutputStream()
                decoder.write(list, byteArrayOutputStream)
                reload(byteArrayOutputStream.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun findAttributeStringValue(name: String, attributeName: String): String? {
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

    private fun findAttributeListValue(name: String, attributeNameResource: Int): List<String> {
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

    private fun findAttributeBooleanValue(name: String, attributeNameResource: Int): Boolean {
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

    private fun findAttributeStringValue(name: String, attributeNameResource: Int): String? {
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

        private const val TYPE_STRING = 0x03000008

        private const val SCHEMAS = "http://schemas.android.com/apk/res/android"
    }
}
