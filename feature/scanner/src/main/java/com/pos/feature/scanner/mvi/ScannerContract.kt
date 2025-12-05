package com.pos.feature.scanner.mvi

import com.pos.core.mvi.MviIntent
import com.pos.core.mvi.MviState
import com.pos.core.mvi.MviEffect

/**
 * 扫码屏幕的Intent
 */
sealed class ScannerIntent : MviIntent {
    object StartCamera : ScannerIntent()
    object StopCamera : ScannerIntent()
    data class BarcodeDetected(val barcode: String) : ScannerIntent()
    object ToggleFlash : ScannerIntent()
}

/**
 * 扫码屏幕的State
 */
data class ScannerState(
    val isCameraActive: Boolean = false,
    val isFlashEnabled: Boolean = false,
    val lastScannedBarcode: String? = null,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val cameraPermissionGranted: Boolean = false
) : MviState

/**
 * 扫码屏幕的Effect
 */
sealed class ScannerEffect : MviEffect {
    data class BarcodeScanned(val barcode: String) : ScannerEffect()
    data class ShowError(val message: String) : ScannerEffect()
    object RequestCameraPermission : ScannerEffect()
    object CloseScannerScreen : ScannerEffect()
}
