package com.mcal.apkparser.xml

import com.mcal.apkparser.util.FileHelper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream

class EditManifest {
    private lateinit var aXml: AXmlDecoder
    private lateinit var parser: AXmlResourceParser
    private lateinit var byteArray: ByteArray

    constructor(path: String) {
        parse(File(path).readBytes())
    }

    constructor(file: File) {
        parse(file.readBytes())
    }

    constructor(byteArray: ByteArray) {
        parse(byteArray)
    }

    constructor(manifestStream: InputStream) {
        parse(manifestStream.readBytes())
    }

    private fun parse(byteArray: ByteArray) {
        this.byteArray = byteArray
        aXml = AXmlDecoder.decode(byteArray.inputStream())
        parser = AXmlResourceParser().apply {
            open(ByteArrayInputStream(aXml.data), aXml.mTableStrings)
        }
    }

    fun get(): ByteArray {
        return byteArray
    }

    fun setPackageName(attributeValue: String) {
        patching(attributeValue, "application", "package")
    }

    fun setApplicationName(attributeValue: String) {
        patching(attributeValue, "application", NAME)
    }

    fun setAppComponentFactoryName(attributeValue: String) {
        patching(attributeValue, "application", APP_COMPONENT_FACTORY)
    }

    fun setExtractNativeLibs(attributeValue: Boolean) {
        patching(attributeValue.toString(), "application", EXTRACT_NATIVE_LIBS)
    }

    fun setAllowBackup(attributeValue: Boolean) {
        patching(attributeValue.toString(), "application", ALLOW_BACKUP)
    }

    fun setLargeHeap(attributeValue: Boolean) {
        patching(attributeValue.toString(), "application", LARGE_HEAP)
    }

    fun setSupportsRtl(attributeValue: Boolean) {
        patching(attributeValue.toString(), "application", SUPPORTS_RTL)
    }

    fun setUsesCleartextTraffic(attributeValue: Boolean) {
        patching(attributeValue.toString(), "application", USES_CLEARTEXT_TRAFFIC)
    }

    fun setRequestLegacyExternalStorage(attributeValue: Boolean) {
        patching(
            attributeValue.toString(),
            "application",
            REQUEST_LEGACY_EXTERNAL_STORAGE
        )
    }

    fun setPreserveLegacyExternalStorage(attributeValue: Boolean) {
        patching(
            attributeValue.toString(),
            "application",
            PRESERVE_LEGACY_EXTERNAL_STORAGE
        )
    }

    fun setVersionCode(attributeValue: Int) {
        patching(attributeValue.toString(), "application", VERSION_CODE)
    }

    fun setVersionName(attributeValue: Int) {
        patching(attributeValue.toString(), "application", VERSION_NAME)
    }

    fun setCompileSdkVersion(attributeValue: Int) {
        patching(attributeValue.toString(), "application", COMPILE_SDK_VERSION)
    }

    fun setCompileSdkVersionCodename(attributeValue: String) {
        patching(attributeValue, "application", COMPILE_SDK_VERSION_CODENAME)
    }

    fun setMinSdkVersion(attributeValue: Int) {
        patching(attributeValue.toString(), "application", MIN_SDK_VERSION)
    }

    fun setTargetSdkVersionCodename(attributeValue: String) {
        patching(attributeValue, "application", TARGET_SDK_VERSION)
    }

    private fun patching(attributeValue: String, name: String, attributeName: String) {
        var success = false
        var type: Int
        try {
            while (parser.next().also { type = it } != XmlPullParser.END_DOCUMENT) {
                if (type != XmlPullParser.START_TAG) continue
                if (parser.name == name) {
                    var isFoundAttribute = false
                    val size = parser.attributeCount
                    for (i in 0 until size) {
                        if (parser.getAttributeName(i) == attributeName) {
                            isFoundAttribute = true
                            val index = aXml.mTableStrings.size
                            val data = aXml.data
                            var off = parser.currentAttributeStart + 20 * i
                            off += 8
                            FileHelper.writeInt(data, off, index)
                            off += 8
                            FileHelper.writeInt(data, off, index)
                        }
                    }
                    if (!isFoundAttribute) {
                        var off = parser.currentAttributeStart
                        val data = aXml.data
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
                            aXml.mTableStrings.find(SCHEMAS)
                        )
                        FileHelper.writeInt(newData, off + 4, idIndex)
                        FileHelper.writeInt(newData, off + 8, aXml.mTableStrings.size)
                        FileHelper.writeInt(newData, off + 12, TYPE_STRING)
                        FileHelper.writeInt(newData, off + 16, aXml.mTableStrings.size)
                        aXml.data = newData
                    }
                    success = true
                    break
                }
            }
            if (!success) {
                throw IOException()
            }
            val list: ArrayList<String> = ArrayList(aXml.mTableStrings.size)
            aXml.mTableStrings.getStrings(list)
            list.add(attributeValue)
            val byteArrayOutputStream = ByteArrayOutputStream()
            aXml.write(list, byteArrayOutputStream)

            parse(byteArrayOutputStream.toByteArray())
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
                            val index = aXml.mTableStrings.size
                            val data = aXml.data
                            var off = parser.currentAttributeStart + 20 * i
                            off += 8
                            FileHelper.writeInt(data, off, index)
                            off += 8
                            FileHelper.writeInt(data, off, index)
                        }
                    }
                    if (!isFoundAttribute) {
                        var off = parser.currentAttributeStart
                        val data = aXml.data
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
                            aXml.mTableStrings.find(SCHEMAS)
                        )
                        FileHelper.writeInt(newData, off + 4, idIndex)
                        FileHelper.writeInt(newData, off + 8, aXml.mTableStrings.size)
                        FileHelper.writeInt(newData, off + 12, TYPE_STRING)
                        FileHelper.writeInt(newData, off + 16, aXml.mTableStrings.size)
                        aXml.data = newData
                    }
                    success = true
                    break
                }
            }
            if (!success) {
                throw IOException()
            }
            val list = ArrayList<String>(aXml.mTableStrings.size)
            aXml.mTableStrings.getStrings(list)
            list.add(attributeValue)
            val byteArrayOutputStream = ByteArrayOutputStream()
            aXml.write(list, byteArrayOutputStream)
            parse(byteArrayOutputStream.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
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
