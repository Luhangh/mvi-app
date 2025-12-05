package com.pos.feature.scanner.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pos.feature.scanner.viewmodel.ScannerViewModel
import com.pos.feature.scanner.mvi.ScannerIntent
import com.pos.feature.scanner.mvi.ScannerEffect
import com.pos.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = hiltViewModel(),
    onBarcodeScanned: (String) -> Unit = { },
    onNavigateBack: () -> Unit = { }
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.handleIntent(ScannerIntent.StartCamera)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ScannerEffect.BarcodeScanned -> {
                    snackbarHostState.showSnackbar(
                        "扫码成功: ${effect.barcode}",
                        duration = SnackbarDuration.Short
                    )
                    onBarcodeScanned(effect.barcode)
                    onNavigateBack()
                }
                is ScannerEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is ScannerEffect.RequestCameraPermission -> {
                    snackbarHostState.showSnackbar(
                        "需要相机权限",
                        duration = SnackbarDuration.Short
                    )
                }
                is ScannerEffect.CloseScannerScreen -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("条形码扫描") },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.handleIntent(ScannerIntent.ToggleFlash)
                        }
                    ) {
                        Icon(
                            painterResource(android.R.drawable.ic_dialog_info),
                            contentDescription = if (state.isFlashEnabled) "关闭闪光灯" else "打开闪光灯",
                            tint = if (state.isFlashEnabled) Color.Yellow else Color.White
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if (!state.isCameraActive) {
            LoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                message = "初始化相机..."
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 扫码区域
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    // 这里应该放置相机预览
                    // 实际项目中使用 CameraX 的 PreviewView
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .background(
                                    color = Color.Transparent,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // 扫描框
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = Color.Transparent,
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                        }

                        Text(
                            "将条形码对准扫描框",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        if (state.isProcessing) {
                            Text(
                                "处理中...",
                                color = Color.Yellow,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // 扫码结果显示
                if (state.lastScannedBarcode != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "最后扫码结果",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            state.lastScannedBarcode!!,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 错误显示
                if (state.error != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEBEE))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "错误",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Text(
                            state.error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }

                // 操作按钮
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (state.lastScannedBarcode != null) {
                                viewModel.handleIntent(
                                    ScannerIntent.BarcodeDetected(state.lastScannedBarcode!!)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.lastScannedBarcode != null
                    ) {
                        Text("确认扫码")
                    }

                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("返回")
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = state.isCameraActive) {
        if (!state.isCameraActive && state.cameraPermissionGranted) {
            viewModel.handleIntent(ScannerIntent.StartCamera)
        }
    }
}
