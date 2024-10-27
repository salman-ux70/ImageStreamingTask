package com.assignment.imagestreaming.ui.fragments

import android.Manifest
import android.content.Context
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.assignment.imagestreaming.R
import com.assignment.imagestreaming.data.NetworkResult
import com.assignment.imagestreaming.databinding.FragmentMainBinding
import com.assignment.imagestreaming.extensions.hide
import com.assignment.imagestreaming.extensions.show
import com.assignment.imagestreaming.extensions.showSettings
import com.assignment.imagestreaming.model.ImageDbModel
import com.assignment.imagestreaming.services.ImageUploadWorker
import com.assignment.imagestreaming.ui.FileUploadViewModel
import com.assignment.imagestreaming.utils.AppUtils.compressImage
import com.assignment.imagestreaming.utils.AppUtils.fileToMultipartBody
import com.assignment.imagestreaming.utils.AppUtils.isAppInBackground
import com.assignment.imagestreaming.utils.AppUtils.isInternetAvailable
import com.assignment.imagestreaming.utils.PermissionUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment(), DefaultLifecycleObserver {
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var cameraSelector: CameraSelector
    private lateinit var preview: Preview
    private lateinit var imageCapture: ImageCapture


    private var captureJob: Job? = null

    @Inject
    lateinit var workManager: WorkManager


    private var binding: FragmentMainBinding? = null

    private var mActivity: FragmentActivity? = null

    private val viewmodel: FileUploadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Fragment>.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity ->
            binding?.let { binding ->
                if (PermissionUtils.hasCameraPermissions(activity)) {

                    setupCamera(activity)
                    apiObserver(activity)
                } else {

                    requestCameraPermission()

                }

            }
        }

    }

    override fun onStart(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStart(owner)
        lifecycleScope.launch {
            captureImagesContinuously(requireActivity())
        }
    }

    // Called when the fragment goes to the background
    override fun onStop(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStop(owner)
        stopImageCapture()
    }

    override fun onPause(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onPause(owner)

        stopImageCapture()
    }

    private fun stopImageCapture() {
        captureJob?.cancel()
        captureJob = null
    }

    private fun apiObserver(activity: FragmentActivity) {
        lifecycleScope.launch {
            viewmodel.uploadResult.collect { result ->
                when (result) {
                    NetworkResult.Loading -> {
                        binding?.imageProgress?.show()
                    }

                    is NetworkResult.Success -> {
                        Toast.makeText(activity, getString(R.string.success), Toast.LENGTH_SHORT).show()
                        binding?.imageProgress?.hide()
                    }

                    is NetworkResult.Error -> {
                        Toast.makeText(activity, "$result", Toast.LENGTH_SHORT).show()
                        binding?.imageProgress?.hide()
                    }


                }


            }
        }
    }

    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun setupCamera(activity: FragmentActivity) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

        cameraProviderFuture.addListener({
            // Get camera provider
            cameraProvider = cameraProviderFuture.get()

            // Set up the Preview use case
            preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding?.previewView?.surfaceProvider)
            }

            // Set up the ImageCapture use case
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // Select back camera as the default
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to the camera lifecycle
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CameraX", "Failed to bind use cases", exc)
            }
            lifecycleScope.launch {

                captureImagesContinuously(activity)
            }
        }, ContextCompat.getMainExecutor(activity))

    }

    private fun captureImagesContinuously(activity: FragmentActivity) {
        captureJob = viewLifecycleOwner.lifecycleScope.launch withContext@{

            withContext(Dispatchers.IO) {
                val dataFromDb = viewmodel.getAllDataFromDb()
                if (!dataFromDb.isNullOrEmpty() && !isInternetAvailable(activity)) return@withContext
                dataFromDb.forEach { data ->
                    processFileForUpload(data.imageFile, activity)

                }
            }
            while (isActive) {
                delay(1000)
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    captureAndUploadImage(activity)

                }
            }


        }


    }

    private suspend fun captureAndUploadImage(activity: FragmentActivity) {
        withContext(Dispatchers.IO) {
            val outputOptions = ImageCapture.OutputFileOptions.Builder(createTempFile()).build()
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(activity),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        lifecycleScope.launch {
                            outputFileResults.savedUri?.path?.let { path ->
                                val originalFile = File(path)
                                processFileForUpload(originalFile, activity)
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraCapture", "Error capturing image", exception)
                    }
                }
            )
        }
    }

    private suspend fun processFileForUpload(
        fileToUpload: File,
        activity: FragmentActivity
    ) {
        val compressedFile =
            compressImage(fileToUpload, 80)
        if (isAppInBackground(activity)) {
            scheduleImageUpload(fileToUpload)
        } else {
            // Convert file to MultipartBody.Part
            if (isInternetAvailable(activity)) {
                val multipartBody =
                    fileToMultipartBody(compressedFile)

                try {
                    viewmodel.uploadImage(activity, multipartBody)

                } catch (e: Exception) {
                    Log.e("CameraCapture", "Error uploading image", e)
                }
            }else{
                val imageDbModel = ImageDbModel(0,fileToUpload)
                viewmodel.saveDataToDatabase(imageDbModel)
            }
        }
    }


    private fun scheduleImageUpload(file: File) {
        val imageData = workDataOf(ImageUploadWorker.KEY_IMAGE_URI to file.toUri().toString())

        val uploadWorkRequest = OneTimeWorkRequestBuilder<ImageUploadWorker>()
            .setInputData(imageData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueue(uploadWorkRequest)

        // Observe both state and progress
        workManager.getWorkInfoByIdLiveData(uploadWorkRequest.id)
            .observe(viewLifecycleOwner) { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> Log.d("WorkerStatus", "Worker is enqueued")
                    WorkInfo.State.RUNNING -> Log.d("WorkerStatus", "Worker is running")
                    WorkInfo.State.SUCCEEDED -> Log.d(
                        "WorkerStatus",
                        "Work completed successfully"
                    )

                    WorkInfo.State.FAILED -> Log.d("WorkerStatus", "Work failed")
                    WorkInfo.State.CANCELLED -> Log.d("WorkerStatus", "Work was cancelled")
                    else -> Log.d("WorkerStatus", "Worker state: ${workInfo.state}")
                }
            }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as FragmentActivity
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission: Boolean ->
            if (permission) {
                mActivity?.let { activity ->
                    setupCamera(activity)
                }
            } else {
                activity?.runOnUiThread {
                    context?.let { context ->
                        MaterialAlertDialogBuilder(context).setTitle("Permission Required")
                            .setMessage("Camera permissions are required for this purpose")
                            .setCancelable(false).setNegativeButton("Deny") { dialog, _ ->

                                dialog.dismiss()
                            }.setPositiveButton("Grant") { _, _ ->
                                context.showSettings()
                            }.show()
                    }
                }
            }
        }

}