package com.tkjy.questionsystem

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewFeature.isFeatureSupported
import com.tkjy.questionsystem.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var webAppInterface: WebAppInterface
    
    // 文件选择器
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var fileSaverLauncher: ActivityResultLauncher<Intent>
    
    // 权限请求
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置全屏显示
        setupFullscreen()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupWebView()
        setupActivityResultLaunchers()
        requestPermissions()
    }
    
    private fun setupFullscreen() {
        // 隐藏状态栏和导航栏实现全屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }
    
    private fun setupWebView() {
        webAppInterface = WebAppInterface(this)
        
        binding.webview.apply {
            // 启用JavaScript和必要的WebView功能
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
                
                // 平板优化设置
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = false
                displayZoomControls = false
                
                // 字体设置
                textZoom = 100
                minimumFontSize = 16
                
                // 性能优化
                setRenderPriority(WebSettings.RenderPriority.HIGH)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                }
            }
            
            // 添加JavaScript接口
            addJavascriptInterface(webAppInterface, "AndroidBridge")
            
            // 设置WebView客户端
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // 页面加载完成后的处理
                    view?.evaluateJavascript("if(typeof init === 'function') init();", null)
                }
                
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    Toast.makeText(this@MainActivity, "页面加载出错", Toast.LENGTH_SHORT).show()
                }
            }
            
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                    // 打印JavaScript控制台消息，便于调试
                    println("WebView Console: ${message.message()}")
                    return true
                }
                
                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    // 处理JavaScript alert
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    result?.confirm()
                    return true
                }
            }
            
            // 启用远程调试（仅在DEBUG版本中）
            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
        }
        
        // 加载本地HTML文件
        binding.webview.loadUrl("file:///android_asset/index.html")
    }
    
    private fun setupActivityResultLaunchers() {
        // 文件选择器
        filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    handleFileSelected(uri)
                }
            }
        }
        
        // 文件保存器
        fileSaverLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    webAppInterface.handleFileSave(uri)
                }
            }
        }
        
        // 权限请求
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (!allGranted) {
                Toast.makeText(this, "需要文件访问权限才能正常使用导入/导出功能", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun requestPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_DOCUMENTS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_DOCUMENTS)
            }
        } else {
            // Android 12 及以下
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
        
        if (permissionsNeeded.isNotEmpty()) {
            permissionLauncher.launch(permissionsNeeded.toTypedArray())
        }
    }
    
    private fun handleFileSelected(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val content = inputStream?.bufferedReader()?.use { it.readText() }
            
            if (content != null) {
                // 调用JavaScript函数处理文件内容
                binding.webview.evaluateJavascript(
                    "if(typeof onFileSelected === 'function') onFileSelected(`$content`);", 
                    null
                )
                Toast.makeText(this, "文件导入成功", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "文件读取失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/json",
                "text/plain",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ))
        }
        filePickerLauncher.launch(intent)
    }
    
    fun saveFile(fileName: String, mimeType: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = mimeType
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        fileSaverLauncher.launch(intent)
    }
    
    override fun onBackPressed() {
        if (binding.webview.canGoBack()) {
            binding.webview.goBack()
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        binding.webview.destroy()
    }
}