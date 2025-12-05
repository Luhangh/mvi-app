package com.pos.feature.printer.viewmodel

import com.pos.core.mvi.MviViewModel
import com.pos.data.repository.PrintRepository
import com.pos.feature.printer.mvi.PrinterIntent
import com.pos.feature.printer.mvi.PrinterState
import com.pos.feature.printer.mvi.PrinterEffect
import com.pos.feature.printer.mvi.PrinterStatus
import com.pos.feature.printer.mvi.PrintJobStatus
import com.pos.feature.printer.mvi.PrintItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PrinterViewModel @Inject constructor(
    private val printRepository: PrintRepository
) : MviViewModel<PrinterIntent, PrinterState, PrinterEffect>(
    initialState = PrinterState()
) {

    init {
        handleIntent(PrinterIntent.CheckPrinterStatus)
        discoverAvailablePrinters()
    }

    override suspend fun processIntent(intent: PrinterIntent) {
        when (intent) {
            is PrinterIntent.PrintReceipt -> printReceipt(intent)
            is PrinterIntent.PrintKitchenOrder -> printKitchenOrder(intent)
            is PrinterIntent.CheckPrinterStatus -> checkPrinterStatus()
            is PrinterIntent.RetryPrint -> retryPrint()
            is PrinterIntent.SkipPrinting -> skipPrinting()
            is PrinterIntent.SelectPrinter -> selectPrinter(intent.printerName)
            is PrinterIntent.SetCopies -> setCopies(intent.copies)
        }
    }

    private suspend fun printReceipt(intent: PrinterIntent.PrintReceipt) {
        try {
            if (currentState.selectedPrinter == null) {
                sendEffect(PrinterEffect.PrinterNotFound)
                return
            }

            updateState { state ->
                state.copy(
                    isProcessing = true,
                    printJobStatus = PrintJobStatus.PRINTING,
                    orderId = intent.orderId
                )
            }

            val receiptContent = generateReceiptContent(
                intent.orderNumber,
                intent.items,
                intent.totalAmount,
                intent.paymentMethod
            )

            printRepository.submitPrintJob(intent.orderId, receiptContent, currentState.copies)
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { jobId ->
                            updateState { state ->
                                state.copy(
                                    jobId = jobId,
                                    printJobStatus = PrintJobStatus.SUCCESS,
                                    isProcessing = false
                                )
                            }
                            sendEffect(PrinterEffect.PrintSuccess(jobId))
                            Timber.d("Print job submitted: $jobId")
                        },
                        onError = { error ->
                            updateState { state ->
                                state.copy(
                                    printJobStatus = PrintJobStatus.FAILED,
                                    isProcessing = false,
                                    error = error.message
                                )
                            }
                            sendEffect(PrinterEffect.PrintFailed(error.message ?: "打印失败"))
                            Timber.e(error, "Print job failed")
                        },
                        onLoading = {
                            updateState { state ->
                                state.copy(isProcessing = true)
                            }
                        }
                    )
                }
        } catch (e: Exception) {
            Timber.e(e, "Error printing receipt")
            updateState { state ->
                state.copy(
                    printJobStatus = PrintJobStatus.FAILED,
                    isProcessing = false,
                    error = e.message
                )
            }
            sendEffect(PrinterEffect.ShowError("打印收据失败: ${e.message}"))
        }
    }

    private suspend fun printKitchenOrder(intent: PrinterIntent.PrintKitchenOrder) {
        try {
            if (currentState.selectedPrinter == null) {
                sendEffect(PrinterEffect.PrinterNotFound)
                return
            }

            updateState { state ->
                state.copy(
                    isProcessing = true,
                    printJobStatus = PrintJobStatus.PRINTING,
                    orderId = intent.orderId
                )
            }

            val kitchenOrderContent = generateKitchenOrderContent(intent.orderId, intent.items)

            printRepository.submitPrintJob(intent.orderId, kitchenOrderContent, 1)
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { jobId ->
                            updateState { state ->
                                state.copy(
                                    jobId = jobId,
                                    printJobStatus = PrintJobStatus.SUCCESS,
                                    isProcessing = false
                                )
                            }
                            sendEffect(PrinterEffect.PrintSuccess(jobId))
                            Timber.d("Kitchen order printed: $jobId")
                        },
                        onError = { error ->
                            updateState { state ->
                                state.copy(
                                    printJobStatus = PrintJobStatus.FAILED,
                                    isProcessing = false,
                                    error = error.message
                                )
                            }
                            sendEffect(PrinterEffect.PrintFailed(error.message ?: "厨房订单打印失败"))
                            Timber.e(error, "Kitchen order print failed")
                        },
                        onLoading = {
                            updateState { state ->
                                state.copy(isProcessing = true)
                            }
                        }
                    )
                }
        } catch (e: Exception) {
            Timber.e(e, "Error printing kitchen order")
            updateState { state ->
                state.copy(
                    printJobStatus = PrintJobStatus.FAILED,
                    isProcessing = false,
                    error = e.message
                )
            }
            sendEffect(PrinterEffect.ShowError("打印厨房订单失败: ${e.message}"))
        }
    }

    private suspend fun checkPrinterStatus() {
        try {
            // 模拟检查打印机状态
            val printers = listOf("Printer-1", "Printer-2", "Printer-3")
            updateState { state ->
                state.copy(
                    printerStatus = PrinterStatus.READY,
                    availablePrinters = printers,
                    selectedPrinter = printers.firstOrNull()
                )
            }
            sendEffect(PrinterEffect.PrintersDiscovered(printers))
            Timber.d("Printer status checked: READY")
        } catch (e: Exception) {
            Timber.e(e, "Error checking printer status")
            updateState { state ->
                state.copy(printerStatus = PrinterStatus.ERROR)
            }
            sendEffect(PrinterEffect.ShowError("检查打印机状态失败: ${e.message}"))
        }
    }

    private suspend fun retryPrint() {
        try {
            if (currentState.orderId == null) {
                sendEffect(PrinterEffect.ShowError("订单ID为空"))
                return
            }
            updateState { state ->
                state.copy(
                    printJobStatus = PrintJobStatus.PENDING,
                    isProcessing = false
                )
            }
            Timber.d("Retrying print for order: ${currentState.orderId}")
        } catch (e: Exception) {
            Timber.e(e, "Error retrying print")
            sendEffect(PrinterEffect.ShowError("重试打印失败: ${e.message}"))
        }
    }

    private suspend fun skipPrinting() {
        try {
            updateState { state ->
                state.copy(printJobStatus = PrintJobStatus.CANCELLED)
            }
            sendEffect(PrinterEffect.NavigateToOrderHistory)
            Timber.d("Printing skipped for order: ${currentState.orderId}")
        } catch (e: Exception) {
            Timber.e(e, "Error skipping printing")
            sendEffect(PrinterEffect.ShowError("跳过打印失败: ${e.message}"))
        }
    }

    private suspend fun selectPrinter(printerName: String) {
        try {
            updateState { state ->
                state.copy(selectedPrinter = printerName)
            }
            Timber.d("Printer selected: $printerName")
        } catch (e: Exception) {
            Timber.e(e, "Error selecting printer")
            sendEffect(PrinterEffect.ShowError("选择打印机失败: ${e.message}"))
        }
    }

    private suspend fun setCopies(copies: Int) {
        try {
            if (copies < 1 || copies > 10) {
                sendEffect(PrinterEffect.ShowError("打印份数必须在1-10之间"))
                return
            }
            updateState { state ->
                state.copy(copies = copies)
            }
            Timber.d("Copies set to: $copies")
        } catch (e: Exception) {
            Timber.e(e, "Error setting copies")
            sendEffect(PrinterEffect.ShowError("设置打印份数失败: ${e.message}"))
        }
    }

    private fun discoverAvailablePrinters() {
        handleIntent(PrinterIntent.CheckPrinterStatus)
    }

    private fun generateReceiptContent(
        orderNumber: String,
        items: List<PrintItem>,
        totalAmount: Double,
        paymentMethod: String
    ): String {
        return buildString {
            appendLine("========== 收据 ==========")
            appendLine("订单号: $orderNumber")
            appendLine("时间: ${System.currentTimeMillis()}")
            appendLine("=====================================")
            appendLine()

            items.forEach { item ->
                appendLine("${item.name} x${item.quantity}")
                appendLine("  单价: ¥${String.format("%.2f", item.price)}")
                appendLine("  小计: ¥${String.format("%.2f", item.subtotal)}")
            }

            appendLine()
            appendLine("=====================================")
            appendLine("总计: ¥${String.format("%.2f", totalAmount)}")
            appendLine("支付方式: $paymentMethod")
            appendLine()
            appendLine("感谢您的光顾！")
            appendLine("========== 收据 ==========")
        }
    }

    private fun generateKitchenOrderContent(
        orderId: String,
        items: List<PrintItem>
    ): String {
        return buildString {
            appendLine("========= 厨房订单 =========")
            appendLine("订单ID: $orderId")
            appendLine("时间: ${System.currentTimeMillis()}")
            appendLine("================================")
            appendLine()

            items.forEach { item ->
                appendLine("${item.name}")
                appendLine("数量: ${item.quantity}")
                appendLine("---")
            }

            appendLine()
            appendLine("================================")
            appendLine("========= 厨房订单 =========")
        }
    }
}
