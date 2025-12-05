package com.pos.feature.printer.mvi

import com.pos.core.mvi.MviIntent
import com.pos.core.mvi.MviState
import com.pos.core.mvi.MviEffect

/**
 * 打印屏幕的Intent
 */
sealed class PrinterIntent : MviIntent {
    data class PrintReceipt(
        val orderId: String,
        val orderNumber: String,
        val items: List<PrintItem>,
        val totalAmount: Double,
        val paymentMethod: String
    ) : PrinterIntent()

    data class PrintKitchenOrder(
        val orderId: String,
        val items: List<PrintItem>
    ) : PrinterIntent()

    object CheckPrinterStatus : PrinterIntent()
    object RetryPrint : PrinterIntent()
    object SkipPrinting : PrinterIntent()
    data class SelectPrinter(val printerName: String) : PrinterIntent()
    data class SetCopies(val copies: Int) : PrinterIntent()
}

/**
 * 打印项目
 */
data class PrintItem(
    val name: String,
    val quantity: Int,
    val price: Double,
    val subtotal: Double
)

/**
 * 打印机状态
 */
enum class PrinterStatus {
    READY, BUSY, ERROR, OFFLINE, NOT_FOUND
}

/**
 * 打印任务状态
 */
enum class PrintJobStatus {
    PENDING, PRINTING, SUCCESS, FAILED, CANCELLED
}

/**
 * 打印屏幕的State
 */
data class PrinterState(
    val orderId: String? = null,
    val printerStatus: PrinterStatus = PrinterStatus.NOT_FOUND,
    val availablePrinters: List<String> = emptyList(),
    val selectedPrinter: String? = null,
    val printJobStatus: PrintJobStatus = PrintJobStatus.PENDING,
    val jobId: String? = null,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val copies: Int = 1,
    val progress: Int = 0
) : MviState

/**
 * 打印屏幕的Effect
 */
sealed class PrinterEffect : MviEffect {
    data class PrintSuccess(val jobId: String) : PrinterEffect()
    data class PrintFailed(val message: String) : PrinterEffect()
    data class ShowError(val message: String) : PrinterEffect()
    data class PrintersDiscovered(val printers: List<String>) : PrinterEffect()
    object NavigateToOrderHistory : PrinterEffect()
    object PrinterNotFound : PrinterEffect()
}
