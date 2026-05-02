package com.vinay.qr

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Size
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.vinay.qr.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var analysisExecutor: ExecutorService

    // Gallery Picker
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { scanImageFromGallery(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        analysisExecutor = Executors.newSingleThreadExecutor()

        // Hide old button
        binding.button.visibility = View.GONE

        // Add Gallery Button (as ImageButton at bottom right)
        addGalleryImageButton()

        initCamera()
    }

    private fun addGalleryImageButton() {
        val galleryButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_gallery) // Default gallery icon
            background = null
            scaleType = android.widget.ImageView.ScaleType.CENTER
            setPadding(32, 32, 32, 32)
            setOnClickListener {
                pickImageLauncher.launch("image/*")
            }
        }

        val params = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
            setMargins(0, 0, 64, 64) // bottom right with margin
        }

        (binding.root as android.view.ViewGroup).addView(galleryButton, params)
    }

    private fun initCamera() {
        requestCameraPermissionIfMissing { granted ->
            if (granted) startCamera()
            else Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.included.previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setResolutionSelector(
                    ResolutionSelector.Builder().setResolutionStrategy(
                        ResolutionStrategy(Size(1280, 720), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
                    ).build()
                )
                .build()

            imageAnalysis.setAnalyzer(analysisExecutor, QRCodeAnalyzer(
                barcodeFormats = intArrayOf(Barcode.FORMAT_QR_CODE),
                onSuccess = { barcode ->
                    imageAnalysis.clearAnalyzer()
                    onSuccess(barcode)
                },
                onFailure = { onFailure(it) }
            ))

            cameraProvider.unbindAll()
            try {
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                binding.included.overlayView.setViewFinder()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun onSuccess(result: String) {
        Toast.makeText(this, "Scanned URL:\n$result", Toast.LENGTH_LONG).show()
        binding.root.postDelayed({ initCamera() }, 2000)
    }

    private fun onFailure(exception: Exception) {
        Toast.makeText(this, "Scan failed", Toast.LENGTH_SHORT).show()
    }

    // ====================== GALLERY SCAN ======================
    private fun scanImageFromGallery(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(this, uri)
            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
            )

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val url = barcodes.first().rawValue ?: "No URL found"
                        onSuccess(url)
                    } else {
                        Toast.makeText(this, "No QR code found in image", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to scan image", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestCameraPermissionIfMissing(onResult: (Boolean) -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            onResult(true)
        } else {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { onResult(it) }
                .launch(Manifest.permission.CAMERA)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::analysisExecutor.isInitialized) analysisExecutor.shutdown()
    }
}