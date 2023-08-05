package com.mcal.template

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mcal.apkparser.xml.ManifestParser
import com.mcal.template.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val valueBinding = binding.value
        var manifestBytes = assets.open("AndroidManifest.xml").readBytes()
        binding.setPkg.setOnClickListener {
            val parser = ManifestParser(manifestBytes)
            CoroutineScope(Dispatchers.IO).launch {
                parser.setPackageName(valueBinding.text.toString())
                parser.get()?.let {
                    manifestBytes = it
                }
                withContext(Dispatchers.Main) {
                    val name = parser.getPackageName()
                    if (name != null) {
                        dialog(name)
                    } else {
                        dialog("null")
                    }
                }
            }
        }

        binding.getPkg.setOnClickListener {
            val parser = ManifestParser(manifestBytes)
            val name = parser.getPackageName()
            if (name != null) {
                dialog(name)
            } else {
                dialog("null")
            }
        }

        binding.setAn.setOnClickListener {
            val parser = ManifestParser(manifestBytes)
            CoroutineScope(Dispatchers.IO).launch {
                parser.setApplicationName(valueBinding.text.toString())
                parser.get()?.let {
                    manifestBytes = it
                }
                withContext(Dispatchers.Main) {
                    val name = parser.getApplicationName()
                    if (name != null) {
                        dialog(name)
                    } else {
                        dialog("null")
                    }
                }
            }
        }

        binding.getAn.setOnClickListener {
            val parser = ManifestParser(manifestBytes)
            val name = parser.getApplicationName()
            if (name != null) {
                dialog(name)
            } else {
                dialog("null")
            }
        }

        binding.setAcf.setOnClickListener {
            val parser = ManifestParser(manifestBytes)
            CoroutineScope(Dispatchers.IO).launch {
                parser.setAppComponentFactoryName(valueBinding.text.toString())
                parser.get()?.let {
                    manifestBytes = it
                }
                withContext(Dispatchers.Main) {
                    val name = parser.getAppComponentFactoryName()
                    if (name != null) {
                        dialog(name)
                    } else {
                        dialog("null")
                    }
                }
            }
        }

        binding.getAcf.setOnClickListener {
            val parser = ManifestParser(assets.open("AndroidManifest.xml"))
            val name = parser.getAppComponentFactoryName()
            if (name != null) {
                dialog(name)
            } else {
                dialog("null")
            }
        }

        binding.getLabel.setOnClickListener {
            val parser = ManifestParser(assets.open("AndroidManifest.xml"))
            val name = parser.getLabel()
            if (name != null) {
                dialog(name)
            } else {
                dialog("null")
            }
        }

        binding.getAllActivities.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                var result = ""
                assets.open("AndroidManifest.xml").use {
                    val parser = ManifestParser(it)
                    val list = parser.getAllActivityName()
                    for (i in list.indices) {
                        result += "\n${list[i]}"
                    }
                }
                withContext(Dispatchers.Main) {
                    dialog(result)
                }
            }
        }
    }

    private fun dialog(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton(android.R.string.ok, null)
        }.show()
    }
}