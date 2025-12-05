package com.pos.feature.scanner.viewmodel

import com.pos.core.mvi.MviViewModel
import com.pos.feature.scanner.mvi.ScannerIntent
import com.pos.feature.scanner.mvi.ScannerState
import com.pos.feature.scanner.mvi.ScannerEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor() : MviViewModel<ScannerIntent, ScannerState, ScannerEffect>(
    initialState = ScannerState()
) {

    override suspend fun processIntent(intent: ScannerIntent) {
        when (intent) {
            is ScannerIntent.StartCamera -> startCamera()
            is ScannerIntent.StopCamera -> stopCamera()
            is ScannerIntent.BarcodeDetected -> processBarcodeDetected(intent.barcode)
            is ScannerIntent.ToggleFlash -> toggleFlash()
        }
    }

    private suspend fun startCamera() {
        try {
            if (!currentState.cameraPermissionGranted) {
                sendEffect(ScannerEffect.RequestCameraPermission)
                return
            }
            updateState { it.copy(isCameraActive = true, error = null) }
            Timber.d("Camera started")
        } catch (e: Exception) {
            Timber.e(e, "Error starting camera")
            updateState { it.copy(error = e.message) }
            sendEffect(ScannerEffect.ShowError("启动相机失败: ${e.message}"))
        }
    }

    private suspend fun stopCamera() {
        try {
            updateState { it.copy(isCameraActive = false) }
            Timber.d("Camera stopped")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping camera")
        }
    }

    private suspend fun processBarcodeDetected(barcode: String) {
        try {
            updateState { it.copy(isProcessing = true, lastScannedBarcode = barcode) }
            Timber.d("Barcode detected: $barcode")
            sendEffect(ScannerEffect.BarcodeScanned(barcode))
            updateState { it.copy(isProcessing = false) }
        } catch (e: Exception) {
            Timber.e(e, "Error processing barcode: $barcode")
            updateState { it.copy(isProcessing = false, error = e.message) }
            sendEffect(ScannerEffect.ShowError("处理扫描失败: ${e.message}"))
        }
    }

    private suspend fun toggleFlash() {
        try {
            updateState { state ->
                state.copy(isFlashEnabled = !state.isFlashEnabled)
            }
            Timber.d("Flash toggled: ${currentState.isFlashEnabled}")
        } catch (e: Exception) {
            Timber.e(e, "Error toggling flash")
            sendEffect(ScannerEffect.ShowError("切换闪光灯失败: ${e.message}"))
        }
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        updateState { it.copy(cameraPermissionGranted = granted) }
    }
}
