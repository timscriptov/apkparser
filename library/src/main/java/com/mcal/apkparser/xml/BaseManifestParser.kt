package com.mcal.apkparser.xml

import com.mcal.apkparser.util.FileHelper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException


open class BaseManifestParser {
    var byteArray: ByteArray? = null
    var decoder: AXmlDecoder? = null
    var parser: AXmlResourceParser? = null

    fun reload(byteArray: ByteArray) {
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

    fun findAttributeStringValue(name: String, attributeNameResource: Int): String? {
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

    fun findAttributeStringValue(name: String, attributeName: String): String? {
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

    fun findAttributeBooleanValue(name: String, attributeNameResource: Int): Boolean {
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

    fun patching(attributeValue: String, name: String, attributeNameResource: Int) {
        try {
            val decoder = decoder
            if (decoder != null) {
                val parser = parser
                if (parser != null) {
                    var success = false
                    var isFoundAttribute = false
                    while (true) {
                        val type = parser.next()
                        if (type == XmlPullParser.END_DOCUMENT) {
                            break
                        } else if (type == XmlPullParser.START_TAG && parser.getName() == name) {
                            val size = parser.getAttributeCount()
                            var i = 0
                            while (true) {
                                if (i >= size) {
                                    break
                                } else if (parser.getAttributeNameResource(i) != attributeNameResource) {
                                    i++
                                } else {
                                    isFoundAttribute = true
                                    val index = decoder.mTableStrings.size
                                    val data = decoder.getData()
                                    val off = parser.currentAttributeStart + 20 * i + 8
                                    FileHelper.writeInt(data, off, index)
                                    FileHelper.writeInt(data, off + 8, index)
                                    break
                                }
                            }
                            if (!isFoundAttribute) {
                                var off2 = parser.currentAttributeStart
                                val data2 = decoder.getData()
                                val newData = ByteArray(data2.size + 20)
                                System.arraycopy(data2, 0, newData, 0, off2)
                                System.arraycopy(data2, off2, newData, off2 + 20, data2.size - off2)
                                val chunkSize = FileHelper.readInt(newData, off2 - 32)
                                FileHelper.writeInt(newData, off2 - 32, chunkSize + 20)
                                FileHelper.writeInt(newData, off2 - 8, size + 1)
                                val idIndex = parser.findResourceID(attributeNameResource)
                                if (idIndex == -1) {
                                    throw IOException("idIndex == -1")
                                }
                                var isMax = true
                                var i2 = 0
                                while (true) {
                                    if (i2 >= size) {
                                        break
                                    }
                                    val id = parser.getAttributeNameResource(i2)
                                    if (id <= attributeNameResource) {
                                        i2++
                                    } else {
                                        isMax = false
                                        if (i2 != 0) {
                                            System.arraycopy(newData, off2 + 20, newData, off2, 20 * i2)
                                            off2 += 20 * i2
                                        }
                                    }
                                }
                                if (isMax) {
                                    System.arraycopy(newData, off2 + 20, newData, off2, 20 * size)
                                    off2 += 20 * size
                                }
                                FileHelper.writeInt(
                                    newData,
                                    off2,
                                    decoder.mTableStrings.find(SCHEMAS)
                                )
                                FileHelper.writeInt(newData, off2 + 4, idIndex)
                                FileHelper.writeInt(newData, off2 + 8, decoder.mTableStrings.size)
                                FileHelper.writeInt(newData, off2 + 12, TYPE_STRING)
                                FileHelper.writeInt(newData, off2 + 16, decoder.mTableStrings.size)
                                decoder.setData(newData)
                            }
                            success = true
                        }
                    }
                    if (!success) {
                        throw IOException("failed to modify AndroidManifest.xml")
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

    fun removeAttribute(name: String, attributeNameResource: Int) {
        try {
            val decoder = decoder
            if (decoder != null) {
                val parser = parser
                if (parser != null) {
                    var success = false
                    while (true) {
                        val type = parser.next()
                        if (type == XmlPullParser.END_DOCUMENT) {
                            break
                        } else if (type == XmlPullParser.START_TAG && parser.getName() == name) {
                            val size = parser.getAttributeCount()
                            var i = 0
                            while (true) {
                                if (i >= size) {
                                    break
                                } else if (parser.getAttributeNameResource(i) != attributeNameResource) {
                                    i++
                                } else {
                                    val attrStart = parser.currentAttributeStart
                                    val off = attrStart + 20 * i
                                    val data = decoder.getData()
                                    val newData = ByteArray(data.size - 20)
                                    System.arraycopy(data, 0, newData, 0, off)
                                    System.arraycopy(data, off + 20, newData, off, data.size - off - 20)
                                    val chunkSize = FileHelper.readInt(newData, attrStart - 32)
                                    FileHelper.writeInt(newData, attrStart - 32, chunkSize - 20)
                                    FileHelper.writeInt(newData, attrStart - 8, size - 1)
                                    decoder.setData(newData)
                                    break
                                }
                            }
                            success = true
                        }
                    }
                    if (!success) {
                        throw IOException("Failed to modify AndroidManifest.xml")
                    }
                    val list = ArrayList<String>(decoder.mTableStrings.size)
                    decoder.mTableStrings.getStrings(list)
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


    companion object {
        /**
         * https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/res/res/values/public-final.xml
         */
        const val EXTRACT_NATIVE_LIBS = 0x010104ea

        const val APP_COMPONENT_FACTORY = 0x0101057a
        const val VERSION_CODE = 0x0101021b
        const val VERSION_NAME = 0x0101021c

        const val THEME = 0x01010000
        const val LABEL = 0x01010001
        const val ICON = 0x01010002
        const val NAME = 0x01010003

        const val COMPILE_SDK_VERSION = 0x01010572
        const val COMPILE_SDK_VERSION_CODENAME = 0x01010573
        const val ALLOW_BACKUP = 0x01010280
        const val LARGE_HEAP = 0x0101035a
        const val SUPPORTS_RTL = 0x010103af
        const val USES_CLEARTEXT_TRAFFIC = 0x010104ec
        const val REQUEST_LEGACY_EXTERNAL_STORAGE = 0x01010603
        const val PRESERVE_LEGACY_EXTERNAL_STORAGE = 0x01010614
        const val MIN_SDK_VERSION = 0x0101020c
        const val TARGET_SDK_VERSION = 0x01010270

        const val TYPE_STRING = 0x03000008

        const val SCHEMAS = "http://schemas.android.com/apk/res/android"
    }
}