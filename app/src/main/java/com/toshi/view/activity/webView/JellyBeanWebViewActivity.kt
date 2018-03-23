/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toshi.view.activity.webView

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.webkit.ValueCallback
import android.webkit.WebSettings
import android.webkit.WebView
import com.toshi.BuildConfig
import com.toshi.R
import com.toshi.extensions.setActivityResultAndFinish
import com.toshi.presenter.webview.SofaHostWrapper
import com.toshi.presenter.webview.ToshiChromeWebViewClient
import com.toshi.util.FileUtil
import com.toshi.util.PermissionUtil
import com.toshi.view.fragment.DialogFragment.ChooserDialog
import com.toshi.viewModel.ViewModelFactory.WebViewViewModelFactory
import com.toshi.viewModel.WebViewViewModel
import kotlinx.android.synthetic.main.activity_lollipop_view_view.*
import kotlinx.android.synthetic.main.view_address_bar_input.*
import java.io.File

class JellyBeanWebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA__ADDRESS = "address"
        const val EXIT_ACTION = "exitAction"
        const val RESULT_CODE = 101
        private const val PICK_IMAGE = 1
        private const val CAPTURE_IMAGE = 2
        private const val IMAGE_TYPE = "image/*"
        private const val ALPHA_DISABLED = 0.4f
        private const val ALPHA_ENABLED = 1f
    }

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var capturedImagePath: String? = null
    private var currentUrl: String? = null
    private lateinit var viewModel: WebViewViewModel
    private lateinit var sofaHostWrapper: SofaHostWrapper
    private lateinit var webViewClient: JellyBeanWebClient

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lollipop_view_view)
        initWebClient()
        load()
    }

    private fun initWebClient() {
        initViewModel()
        initClickListeners()
        initWebSettings()
        injectEverything()
        initObservers()
    }

    private fun initViewModel() {
        val url = intent.getStringExtra(EXTRA__ADDRESS).orEmpty()
        input.text = url
        viewModel = ViewModelProviders.of(
                this,
                WebViewViewModelFactory(url)
        ).get(WebViewViewModel::class.java)
    }

    private fun initClickListeners() {
        input.onBackClickedListener = { handleBackButtonClicked() }
        input.onForwardClickedListener = { handleForwardButtonClicked() }
        input.onGoClickedListener = { viewModel.url.postValue(it) }
        input.onExitClickedListener = { handleExitClicked() }
    }

    private fun handleBackButtonClicked() {
        if (webview.canGoBack()) webview.goBack()
    }

    private fun handleForwardButtonClicked() {
        if (webview.canGoForward()) webview.goForward()
    }

    override fun onBackPressed() {
        if (webview.canGoBack()) webview.goBack()
        else super.onBackPressed()
    }

    private fun handleExitClicked() {
        val isListeningForExitAction = intent.getBooleanExtra(EXIT_ACTION, false)
        if (isListeningForExitAction) setActivityResultAndFinish(RESULT_CODE)
        else finish()
    }

    private fun initWebSettings() {
        val webSettings = webview.settings
        webSettings.javaScriptEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.domStorageEnabled = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.WEB_DEBUG_ENABLED)
        }
    }

    private fun injectEverything() {
        val address = viewModel.tryGetAddress()
        sofaHostWrapper = SofaHostWrapper(this, webview, address)
        webview.addJavascriptInterface(sofaHostWrapper.sofaHost, "SOFAHost")
        webViewClient = JellyBeanWebClient(
                this,
                { viewModel.updateToolbar() },
                { url -> currentUrl = url },
                this::handleWebResourceResponse,
                { onPageCommitVisible(it) }

        )
        webview.webViewClient = webViewClient
        val chromeWebClient = ToshiChromeWebViewClient(this::handleFileChooserCallback)
        chromeWebClient.progressListener = { progressBar.setProgress(it) }
        webview.webChromeClient = chromeWebClient
    }

    private fun onPageCommitVisible(url: String?) {
        if (url != null) input.text = url
        updateToolbarNavigation()
    }

    private fun handleWebResourceResponse(response: SofaInjectResponse) {
        webview.loadDataWithBaseURL(
                response.address,
                response.data,
                response.mimeType,
                response.encoding,
                null
        )
    }

    private fun initObservers() {
        viewModel.toolbarUpdate.observe(this, Observer { updateToolbar() })
        viewModel.url.observe(this, Observer { load() })
    }

    private fun updateToolbar() {
        input.text = currentUrl ?: webview.url
        title = webview.title
        updateToolbarNavigation()
    }

    private fun updateToolbarNavigation() {
        backButton.alpha = if (webview.canGoBack()) ALPHA_ENABLED else ALPHA_DISABLED
        forwardButton.alpha = if (webview.canGoForward()) ALPHA_ENABLED else ALPHA_DISABLED
    }

    private fun load() = webViewClient.newPageLoad(viewModel.tryGetAddress())

    private fun handleFileChooserCallback(valueCallback: ValueCallback<Array<Uri>>?): Boolean {
        this.filePathCallback = valueCallback
        showImageChooserDialog()
        return true
    }

    private fun showImageChooserDialog() {
        val chooserDialog = ChooserDialog.newInstance()
        chooserDialog.setOnChooserClickListener(object : ChooserDialog.OnChooserClickListener {
            override fun captureImageClicked() {
                checkCameraPermission()
            }

            override fun importImageFromGalleryClicked() {
                checkExternalStoragePermission()
            }
        })
        chooserDialog.show(supportFragmentManager, ChooserDialog.TAG)
    }

    private fun checkExternalStoragePermission() {
        PermissionUtil.hasPermission(
                this,
                READ_EXTERNAL_STORAGE,
                PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSION,
                { startGalleryActivity() }
        )
    }

    private fun checkCameraPermission() {
        PermissionUtil.hasPermission(
                this,
                CAMERA,
                PermissionUtil.CAMERA_PERMISSION,
                { startCameraActivity() }
        )
    }

    private fun startCameraActivity() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) == null) return
        val photoFile = FileUtil.createImageFileWithRandomName()
        capturedImagePath = photoFile.absolutePath
        val photoURI = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + FileUtil.FILE_PROVIDER_NAME,
                photoFile
        )
        PermissionUtil.grantUriPermission(this, cameraIntent, photoURI)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(cameraIntent, CAPTURE_IMAGE)
    }

    private fun startGalleryActivity() {
        val pickPictureIntent = Intent()
                .setType(IMAGE_TYPE)
                .setAction(Intent.ACTION_GET_CONTENT)
        if (pickPictureIntent.resolveActivity(packageManager) == null) return
        val chooserIntent = Intent.createChooser(
                pickPictureIntent,
                getString(R.string.select_picture)
        )
        startActivityForResult(chooserIntent, PICK_IMAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!PermissionUtil.isPermissionGranted(grantResults)) return
        when (requestCode) {
            PermissionUtil.CAMERA_PERMISSION -> startCameraActivity()
            PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSION -> startGalleryActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultIntent)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            PICK_IMAGE -> postFileCallback(resultIntent?.data)
            CAPTURE_IMAGE -> handleCapturesImage(capturedImagePath)
        }
    }

    private fun handleCapturesImage(path: String?) {
        if (capturedImagePath == null) return
        val uri = Uri.fromFile(File(path))
        postFileCallback(uri)
        capturedImagePath = null
    }

    private fun postFileCallback(uri: Uri?) {
        if (uri == null) return
        val uriArray = arrayOf(uri)
        filePathCallback?.onReceiveValue(uriArray)
        filePathCallback = null
    }

    override fun onResume() {
        super.onResume()
        webview.onResume()
        webview.resumeTimers()
    }

    override fun onPause() {
        webview.onPause()
        webview.pauseTimers()
        super.onPause()
    }

    override fun onDestroy() {
        sofaHostWrapper.clear()
        webViewClient.clear()
        webview.destroy()
        super.onDestroy()
    }
}