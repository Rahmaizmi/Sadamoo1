package com.example.sadamoo.users

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sadamoo.databinding.ActivityCameraScanBinding
import com.example.sadamoo.users.helper.Classifier
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sadamoo.R
import android.net.Uri
import com.example.sadamoo.utils.applyStatusBarPadding



class CameraScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraScanBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var getFile: File? = null

    private var camera: Camera? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var isFlashOn = false

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap != null) runModel(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraScanBinding.inflate(layoutInflater)
        binding.root.applyStatusBarPadding()
        setContentView(binding.root)

        // Check camera permissions
        if (allPermissionsGranted()) {
            startCamera() // langsung start kalau sudah ada izin
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        // Setup UI
        setupUI()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setupUI() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Capture button
        binding.fabCapture.setOnClickListener {
            capturePhoto()
        }

        // Gallery button
        binding.btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Flash toggle
        binding.btnFlash.setOnClickListener {
            toggleFlash()
        }

        // Switch camera button
        binding.btnSwitchCamera.setOnClickListener {
            switchCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

                // Image capture
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // Pilih kamera (default back)
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind dan simpan instance kamera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Toast.makeText(this, "Gagal membuka kamera: ${exc.message}", Toast.LENGTH_SHORT)
                    .show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return
        showScanningAnimation()

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            createTempFile("sapi_scan", ".jpg")
        ).build()

        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val bitmap = imageProxyToBitmap(image) // <- Bitmap OK
                        runModel(bitmap)
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@CameraScanActivity,
                                "Gagal proses gambar: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            resetScanningUI()
                        }
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@CameraScanActivity,
                            "Gagal ambil foto: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        resetScanningUI()
                    }
                }
            }
        )
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        // ImageCapture default -> JPEG; cukup decode plane[0]
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(cacheDir, "captured_image_${System.currentTimeMillis()}.jpg")
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return file
    }


    private fun runModel(bitmap: Bitmap) {
        val classifier = Classifier(this)
        val results = classifier.classify(bitmap)
        val top = results.maxByOrNull { it.confidence }

        val uiLabel = top?.title ?: "Tidak Dapat Mendeteksi"
        val confidence = top?.confidence ?: 0f

        val imageFile = saveBitmapToFile(bitmap)

        runOnUiThread {
            val intent = Intent(this, ScanResultActivity::class.java).apply {
                putExtra("image", imageFile.absolutePath)
                putExtra("cattle_type", uiLabel)
                putExtra("confidence_score", confidence)
                putExtra("is_healthy", confidence > 0.8f)
            }
            startActivity(intent)
            finish()
        }
    }


    private fun showScanningAnimation() {
        binding.apply {
            // Hide capture button and show loading
            fabCapture.visibility = View.GONE
            btnBack.visibility = View.GONE
            btnGallery.visibility = View.GONE
            btnFlash.visibility = View.GONE
            btnSwitchCamera.visibility = View.GONE

            // Show scanning overlay
            layoutScanning.visibility = View.VISIBLE

            // Start scanning animation
            startScanningEffect()
        }
    }

    private fun resetScanningUI() {
        binding.apply {
            layoutScanning.visibility = View.GONE
            fabCapture.visibility = View.VISIBLE
            btnBack.visibility = View.VISIBLE
            btnGallery.visibility = View.VISIBLE
            btnFlash.visibility = View.VISIBLE
            btnSwitchCamera.visibility = View.VISIBLE
        }
    }

    private fun startScanningEffect() {
        val scanningTexts = arrayOf(
            "Menganalisis gambar...",
            "Mendeteksi jenis sapi...",
            "Memeriksa kondisi kesehatan...",
            "Mengidentifikasi penyakit...",
            "Menghitung tingkat kepercayaan...",
            "Hampir selesai..."
        )

        var currentIndex = 0
        val handler = Handler(Looper.getMainLooper())

        val updateText = object : Runnable {
            override fun run() {
                if (currentIndex < scanningTexts.size) {
                    binding.tvScanningText.text = scanningTexts[currentIndex]
                    currentIndex++
                    handler.postDelayed(this, 500) // Update every 0.5 seconds
                }
            }
        }
        handler.post(updateText)

        // Animate scanning line
        animateScanningLine()
    }

    private fun animateScanningLine() {
        // Simple scanning line animation
        binding.viewScanLine.animate()
            .translationY(400f)
            .setDuration(2000)
            .withEndAction {
                binding.viewScanLine.animate()
                    .translationY(-400f)
                    .setDuration(1000)
                    .start()
            }
            .start()
    }

    private fun toggleFlash() {
        camera?.cameraControl?.enableTorch(!isFlashOn)
        isFlashOn = !isFlashOn

        if (isFlashOn) {
            binding.btnFlash.setImageResource(R.drawable.ic_flash_on)
        } else {
            binding.btnFlash.setImageResource(R.drawable.ic_flash_off)
        }
    }

    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Izin kamera diperlukan untuk menggunakan fitur scan.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
