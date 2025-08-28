package com.tkjy.questionsystem

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.webkit.JavascriptInterface
import android.widget.Toast
import java.io.IOException

/**
 * WebApp接口类 - 用于替换localStorage功能
 * 提供数据持久化存储和文件操作功能
 */
class WebAppInterface(private val context: Context) {
    
    // SharedPreferences实例，替代localStorage
    private val prefs: SharedPreferences = context.getSharedPreferences("TKJY_DATA", Context.MODE_PRIVATE)
    
    // 文件操作相关的临时数据
    private var pendingFileData: String? = null
    private var pendingFileName: String? = null
    private var pendingMimeType: String? = null
    
    /**
     * 保存数据 - 替换localStorage.setItem()
     * JavaScript调用: AndroidBridge.saveData(key, value)
     */
    @JavascriptInterface
    fun saveData(key: String, value: String) {
        try {
            prefs.edit().putString(key, value).apply()
            println("WebAppInterface: 数据已保存 - Key: $key, Length: ${value.length}")
        } catch (e: Exception) {
            e.printStackTrace()
            println("WebAppInterface: 保存数据失败 - ${e.message}")
        }
    }
    
    /**
     * 加载数据 - 替换localStorage.getItem()  
     * JavaScript调用: AndroidBridge.loadData(key)
     */
    @JavascriptInterface
    fun loadData(key: String): String? {
        return try {
            val data = prefs.getString(key, null)
            println("WebAppInterface: 数据已读取 - Key: $key, Found: ${data != null}")
            data
        } catch (e: Exception) {
            e.printStackTrace()
            println("WebAppInterface: 读取数据失败 - ${e.message}")
            null
        }
    }
    
    /**
     * 删除数据
     * JavaScript调用: AndroidBridge.removeData(key)
     */
    @JavascriptInterface
    fun removeData(key: String) {
        try {
            prefs.edit().remove(key).apply()
            println("WebAppInterface: 数据已删除 - Key: $key")
        } catch (e: Exception) {
            e.printStackTrace()
            println("WebAppInterface: 删除数据失败 - ${e.message}")
        }
    }
    
    /**
     * 清空所有数据
     * JavaScript调用: AndroidBridge.clearAllData()
     */
    @JavascriptInterface
    fun clearAllData() {
        try {
            prefs.edit().clear().apply()
            println("WebAppInterface: 所有数据已清空")
            showToast("所有数据已清空")
        } catch (e: Exception) {
            e.printStackTrace()
            println("WebAppInterface: 清空数据失败 - ${e.message}")
        }
    }
    
    /**
     * 获取所有存储的键名
     * JavaScript调用: AndroidBridge.getAllKeys()
     */
    @JavascriptInterface
    fun getAllKeys(): String {
        return try {
            val keys = prefs.all.keys.toList()
            val gson = com.google.gson.Gson()
            gson.toJson(keys)
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }
    
    /**
     * 打开文件选择器 - 替换HTML文件输入框
     * JavaScript调用: AndroidBridge.openFilePicker()
     */
    @JavascriptInterface
    fun openFilePicker() {
        try {
            println("WebAppInterface: 打开文件选择器")
            val activity = context as MainActivity
            activity.runOnUiThread {
                activity.openFilePicker()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("打开文件选择器失败: ${e.message}")
        }
    }
    
    /**
     * 保存文件 - 替换HTML的下载功能
     * JavaScript调用: AndroidBridge.saveFile(fileName, data, mimeType)
     */
    @JavascriptInterface
    fun saveFile(fileName: String, data: String, mimeType: String) {
        try {
            println("WebAppInterface: 准备保存文件 - $fileName")
            
            // 保存文件数据到临时变量
            pendingFileData = data
            pendingFileName = fileName
            pendingMimeType = mimeType
            
            val activity = context as MainActivity
            activity.runOnUiThread {
                activity.saveFile(fileName, mimeType)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("保存文件失败: ${e.message}")
        }
    }
    
    /**
     * 处理文件保存 - 内部方法，由MainActivity调用
     */
    fun handleFileSave(uri: Uri) {
        try {
            if (pendingFileData == null) {
                showToast("没有待保存的数据")
                return
            }
            
            val outputStream = context.contentResolver.openOutputStream(uri)
            outputStream?.use { stream ->
                stream.write(pendingFileData!!.toByteArray(Charsets.UTF_8))
            }
            
            showToast("文件保存成功")
            println("WebAppInterface: 文件已保存 - ${pendingFileName}")
            
            // 清理临时数据
            pendingFileData = null
            pendingFileName = null
            pendingMimeType = null
            
        } catch (e: IOException) {
            e.printStackTrace()
            showToast("文件保存失败: ${e.message}")
        }
    }
    
    /**
     * 显示Toast消息
     * JavaScript调用: AndroidBridge.showToast(message)
     */
    @JavascriptInterface
    fun showToast(message: String) {
        try {
            val activity = context as MainActivity
            activity.runOnUiThread {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 获取应用信息
     * JavaScript调用: AndroidBridge.getAppInfo()
     */
    @JavascriptInterface
    fun getAppInfo(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val appInfo = mapOf(
                "versionName" to packageInfo.versionName,
                "versionCode" to packageInfo.versionCode,
                "packageName" to context.packageName,
                "platform" to "Android",
                "buildTime" to System.currentTimeMillis()
            )
            
            val gson = com.google.gson.Gson()
            gson.toJson(appInfo)
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }
    
    /**
     * 获取设备信息
     * JavaScript调用: AndroidBridge.getDeviceInfo()
     */
    @JavascriptInterface
    fun getDeviceInfo(): String {
        return try {
            val displayMetrics = context.resources.displayMetrics
            val deviceInfo = mapOf(
                "screenWidth" to displayMetrics.widthPixels,
                "screenHeight" to displayMetrics.heightPixels,
                "density" to displayMetrics.density,
                "densityDpi" to displayMetrics.densityDpi,
                "androidVersion" to android.os.Build.VERSION.RELEASE,
                "apiLevel" to android.os.Build.VERSION.SDK_INT,
                "manufacturer" to android.os.Build.MANUFACTURER,
                "model" to android.os.Build.MODEL,
                "brand" to android.os.Build.BRAND
            )
            
            val gson = com.google.gson.Gson()
            gson.toJson(deviceInfo)
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }
    
    /**
     * 日志记录
     * JavaScript调用: AndroidBridge.log(level, message)
     */
    @JavascriptInterface
    fun log(level: String, message: String) {
        when (level.lowercase()) {
            "error" -> println("WebApp ERROR: $message")
            "warn" -> println("WebApp WARN: $message")
            "info" -> println("WebApp INFO: $message")
            "debug" -> println("WebApp DEBUG: $message")
            else -> println("WebApp LOG: $message")
        }
    }
    
    /**
     * 检查网络状态
     * JavaScript调用: AndroidBridge.isNetworkAvailable()
     */
    @JavascriptInterface
    fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                as android.net.ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 振动反馈
     * JavaScript调用: AndroidBridge.vibrate(duration)
     */
    @JavascriptInterface
    fun vibrate(duration: Int) {
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) 
                    as android.os.VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    android.os.VibrationEffect.createOneShot(
                        duration.toLong(),
                        android.os.VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration.toLong())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}