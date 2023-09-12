package com.mcal.apkparser.xml

import com.mcal.apkparser.util.FileHelper
import java.io.*

class ManifestParser {
    private var decoder: AXmlDecoder? = null
    private var parser: AXmlResourceParser? = null
    private var byteArray: ByteArray? = null

    constructor(path: String) : this(File(path).readBytes())
    constructor(file: File) : this(file.readBytes())
    constructor(inputStream: InputStream) : this(inputStream.readBytes())
    constructor(byteArray: ByteArray) {
        reload(byteArray)
    }


    private fun reload(byteArray: ByteArray) {
        this.byteArray = byteArray.also { bytes ->
            bytes.inputStream().use { inputStream ->
                decoder = AXmlDecoder.decode(inputStream).also {
                    parser = AXmlResourceParser().apply {
                        open(ByteArrayInputStream(it.data), it.mTableStrings)
                    }
                }
            }
        }
    }

    fun get(): ByteArray? {
        return byteArray
    }

    fun getPackageName() = findAttributeStringValue("manifest", "package")
    fun setPackageName(attributeValue: String) = patching(attributeValue, "manifest", "package")

    fun getLabel() = findAttributeStringValue("application", LABEL)
//    fun setLabel(attributeValue: String) = patching(attributeValue, "application", LABEL)

    fun getIcon() = findAttributeStringValue("application", ICON)
//    fun setIcon(attributeValue: String) = patching(attributeValue, "application", ICON)

    fun getTheme() = findAttributeStringValue("application", THEME)
//    fun setTheme(attributeValue: String) = patching(attributeValue, "application", THEME)


    fun getApplicationName() = findAttributeStringValue("application", NAME)
    fun setApplicationName(attributeValue: String) = patching(attributeValue, "application", NAME)

    fun getAppComponentFactoryName() =
        findAttributeStringValue("application", APP_COMPONENT_FACTORY)

    fun setAppComponentFactoryName(attributeValue: String) =
        patching(attributeValue, "application", APP_COMPONENT_FACTORY)

    fun getVersionCode() = findAttributeStringValue("manifest", VERSION_CODE)
    fun setVersionCode(attributeValue: String) = patching(attributeValue, "manifest", VERSION_CODE)

    fun getVersionName() = findAttributeStringValue("manifest", VERSION_NAME)
    fun setVersionName(attributeValue: String) = patching(attributeValue, "manifest", VERSION_NAME)

    fun getCompileSdkVersion() = findAttributeStringValue("manifest", COMPILE_SDK_VERSION)
    fun setCompileSdkVersion(attributeValue: String) =
        patching(attributeValue, "manifest", COMPILE_SDK_VERSION)

    fun getCompileSdkVersionCodename() =
        findAttributeStringValue("manifest", COMPILE_SDK_VERSION_CODENAME)

    fun setCompileSdkVersionCodename(attributeValue: String) =
        patching(attributeValue, "manifest", COMPILE_SDK_VERSION_CODENAME)

    fun getMinSdkVersion() = findAttributeStringValue("manifest", MIN_SDK_VERSION)
    fun setMinSdkVersion(attributeValue: String) =
        patching(attributeValue, "manifest", MIN_SDK_VERSION)

    fun getTargetSdkVersion() = findAttributeStringValue("manifest", TARGET_SDK_VERSION)
    fun setTargetSdkVersion(attributeValue: String) =
        patching(attributeValue, "manifest", TARGET_SDK_VERSION)

    fun getExtractNativeLibs() = findAttributeBooleanValue("application", EXTRACT_NATIVE_LIBS)
    fun setExtractNativeLibs(enabled: Boolean) =
        patching(enabled.toString(), "application", EXTRACT_NATIVE_LIBS)

    fun getAllowBackup() = findAttributeBooleanValue("application", ALLOW_BACKUP)
    fun setAllowBackup(enabled: Boolean) = patching(enabled.toString(), "application", ALLOW_BACKUP)

    fun getLargeHeap() = findAttributeBooleanValue("application", LARGE_HEAP)
    fun setLargeHeap(enabled: Boolean) = patching(enabled.toString(), "application", LARGE_HEAP)

    fun getSupportsRtl() = findAttributeBooleanValue("application", SUPPORTS_RTL)
    fun setSupportsRtl(enabled: Boolean) = patching(enabled.toString(), "application", SUPPORTS_RTL)

    fun getUsesCleartextTraffic() = findAttributeBooleanValue("application", USES_CLEARTEXT_TRAFFIC)
    fun setUsesCleartextTraffic(enabled: Boolean) =
        patching(enabled.toString(), "application", USES_CLEARTEXT_TRAFFIC)

    fun getRequestLegacyExternalStorage() =
        findAttributeBooleanValue("application", REQUEST_LEGACY_EXTERNAL_STORAGE)

    fun setRequestLegacyExternalStorage(enabled: Boolean) =
        patching(enabled.toString(), "application", REQUEST_LEGACY_EXTERNAL_STORAGE)

    fun getPreserveLegacyExternalStorage() =
        findAttributeBooleanValue("application", PRESERVE_LEGACY_EXTERNAL_STORAGE)

    fun setPreserveLegacyExternalStorage(enabled: Boolean) =
        patching(enabled.toString(), "application", PRESERVE_LEGACY_EXTERNAL_STORAGE)

    fun getAllServiceName() = findAttributeListValue("service", NAME)
    fun getAllReceiverName() = findAttributeListValue("receiver", NAME)
    fun getAllActivityName() = findAttributeListValue("activity", NAME)

    private fun patching(attributeValue: String, name: String, attributeName: String) {
        try {
            val decoder = decoder
            if (decoder != null) {
                val parser = parser
                if (parser != null) {
                    var success = false
                    var type: Int
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
                                            System.arraycopy(
                                                newData,
                                                off + 20,
                                                newData,
                                                off,
                                                20 * i
                                            )
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
                    ByteArrayOutputStream().use {
                        decoder.write(list, it)
                        reload(it.toByteArray())
                    }
                }
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
            val decoder = decoder
            if (decoder != null) {
                val parser = parser
                if (parser != null) {
                    var success = false
                    var type: Int
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
                                            System.arraycopy(
                                                newData,
                                                off + 20,
                                                newData,
                                                off,
                                                20 * i
                                            )
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
                    ByteArrayOutputStream().use {
                        decoder.write(list, it)
                        reload(it.toByteArray())
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun findAttributeStringValue(name: String, attributeName: String): String? {
        try {
            val parser = parser
            if (parser != null) {
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun findAttributeListValue(name: String, attributeNameResource: Int): List<String> {
        val list = arrayListOf<String>()
        try {
            val parser = parser
            if (parser != null) {
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun findAttributeBooleanValue(name: String, attributeNameResource: Int): Boolean {
        try {
            val parser = parser
            if (parser != null) {
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun findAttributeStringValue(name: String, attributeNameResource: Int): String? {
        try {
            val parser = parser
            if (parser != null) {
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

        private const val APP_COMPONENT_FACTORY = 0x0101057a
        private const val VERSION_CODE = 0x0101021b
        private const val VERSION_NAME = 0x0101021c

        private const val THEME = 0x01010000
        private const val LABEL = 0x01010001
        private const val ICON = 0x01010002
        private const val NAME = 0x01010003

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
