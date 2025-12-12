package com.pos.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 设置页面
 *
 * 功能特性：
 * - 主题切换（亮色/暗色）
 * - 应用信息
 * - 版本更新
 * - 关于应用
 * - 开发者选项
 *
 * @param onNavigateBack 返回回调
 * @param viewModel 设置 ViewModel
 * @author POS Team
 * @since 1.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // 外观设置
            SettingSection(
                title = "外观",
                icon = Icons.Default.Palette
            ) {
                ThemeSelector(
                    currentTheme = state.theme,
                    onThemeChange = { viewModel.setTheme(it) }
                )
            }

            Divider()

            // 打印设置
            SettingSection(
                title = "打印",
                icon = Icons.Default.Print
            ) {
                SettingItem(
                    title = "打印机设置",
                    subtitle = "配置热敏打印机",
                    onClick = { /* 打开打印设置 */ }
                )

                SettingItem(
                    title = "打印份数",
                    subtitle = "默认 1 份",
                    onClick = { /* 打开份数设置 */ }
                )

                SettingItem(
                    title = "纸张宽度",
                    subtitle = "58mm",
                    onClick = { /* 打开纸张设置 */ }
                )
            }

            Divider()

            // 销售设置
            SettingSection(
                title = "销售",
                icon = Icons.Default.ShoppingCart
            ) {
                SettingItem(
                    title = "启用折扣",
                    subtitle = "允许在结账时应用折扣",
                    isToggle = true,
                    toggleValue = state.discountEnabled,
                    onToggle = { viewModel.setDiscountEnabled(it) }
                )

                SettingItem(
                    title = "启用会员系统",
                    subtitle = "支持会员积分功能",
                    isToggle = true,
                    toggleValue = state.membershipEnabled,
                    onToggle = { viewModel.setMembershipEnabled(it) }
                )

                SettingItem(
                    title = "最小订单额",
                    subtitle = "无最小限制",
                    onClick = { /* 打开设置 */ }
                )
            }

            Divider()

            // 数据管理
            SettingSection(
                title = "数据",
                icon = Icons.Default.Storage
            ) {
                SettingItem(
                    title = "备份数据",
                    subtitle = "导出订单和商品数据",
                    onClick = { viewModel.backupData() }
                )

                SettingItem(
                    title = "清空缓存",
                    subtitle = "释放存储空间",
                    onClick = { viewModel.clearCache() }
                )

                SettingItem(
                    title = "导入商品",
                    subtitle = "从文件导入商品列表",
                    onClick = { /* 打开导入 */ }
                )
            }

            Divider()

            // 关于应用
            SettingSection(
                title = "关于",
                icon = Icons.Default.Info
            ) {
                SettingItem(
                    title = "应用版本",
                    subtitle = "v${state.appVersion}",
                    onClick = { }
                )

                SettingItem(
                    title = "检查更新",
                    subtitle = "当前为最新版本",
                    onClick = { viewModel.checkUpdate() }
                )

                SettingItem(
                    title = "关于应用",
                    subtitle = "了解更多信息",
                    onClick = { showAboutDialog = true }
                )

                SettingItem(
                    title = "隐私政策",
                    subtitle = "查看隐私政策",
                    onClick = { /* 打开隐私政策 */ }
                )

                SettingItem(
                    title = "用户协议",
                    subtitle = "查看用户协议",
                    onClick = { /* 打开协议 */ }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 页脚信息
            Text(
                "© 2025 POS 系统. 版权所有",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }

    // 关于应用对话框
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("关于 POS 系统") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("版本: v${state.appVersion}")
                    Text("更新日期: 2025-12-10")

                    Divider()

                    Text(
                        "现代化 POS 收银系统",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "采用最新 Android 技术栈构建的专业收银平台，" +
                                "为零售商户提供完整的销售、支付和数据分析解决方案。",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
}

/**
 * 设置分组组件
 */
@Composable
private fun SettingSection(
    title: String,
    icon: androidx.compose.material.icons.filled.Add,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        content()
    }
}

/**
 * 单个设置项目
 */
@Composable
private fun SettingItem(
    title: String,
    subtitle: String = "",
    isToggle: Boolean = false,
    toggleValue: Boolean = false,
    onToggle: (Boolean) -> Unit = { },
    onClick: () -> Unit = { },
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isToggle) { onClick() }
            .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )

                if (subtitle.isNotEmpty()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (isToggle) {
                Switch(
                    checked = toggleValue,
                    onCheckedChange = onToggle
                )
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * 主题选择器
 */
@Composable
private fun ThemeSelector(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            "light" to "亮色主题",
            "dark" to "暗色主题",
            "system" to "跟随系统"
        ).forEach { (theme, label) ->
            ThemeOption(
                label = label,
                isSelected = currentTheme == theme,
                onClick = { onThemeChange(theme) }
            )
        }
    }
}

/**
 * 主题选项
 */
@Composable
private fun ThemeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
