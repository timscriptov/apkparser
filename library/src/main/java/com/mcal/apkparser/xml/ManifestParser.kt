package com.mcal.apkparser.xml

import com.mcal.apkparser.util.FileHelper
import java.io.*

class ManifestParser: BaseManifestParser {
    constructor(path: String) : this(File(path).readBytes())
    constructor(file: File) : this(file.readBytes())
    constructor(inputStream: InputStream) : this(inputStream.readBytes())
    constructor(byteArray: ByteArray) {
        reload(byteArray)
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
}
