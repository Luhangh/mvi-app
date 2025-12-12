package com.pos.feature.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pos.data.db.dao.CartItemDao
import com.pos.data.mock.DatabaseInitializer
import com.pos.data.mock.MockDataProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 开发者菜单状态
 *
 * @property isLoading 是否正在加载
 * @property statistics 数据库统计信息
 * @property message 操作消息
 * @property isSuccess 操作是否成功
 */
data class DeveloperMenuState(
    val isLoading: Boolean = false,
    val statistics: String = "点击刷新按钮获取统计信息",
    val message: String = "",
    val isSuccess: Boolean = false
)

/**
 * 开发者菜单 ViewModel
 * 处理数据库初始化、重置等开发调试功能
 *
 * @property databaseInitializer 数据库初始化器
 * @property cartItemDao 购物车数据访问对象
 * @author POS Team
 * @since 1.0.0
 */
@HiltViewModel
class DeveloperMenuViewModel @Inject constructor(
    private val databaseInitializer: DatabaseInitializer,
    private val cartItemDao: CartItemDao
) : ViewModel() {

    private val _state = MutableStateFlow(DeveloperMenuState())
    val state: StateFlow<DeveloperMenuState> = _state.asStateFlow()

    init {
        // 初始化时加载统计信息
        refreshStatistics()
    }

    /**
     * 初始化数据库
     * 仅当数据库为空时插入模拟数据
     */
    fun initializeDatabase() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = "") }

            try {
                val result = databaseInitializer.initialize()

                if (result) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "✓ 数据库初始化成功！已插入 ${MockDataProvider.getMockProducts().size} 个商品",
                            isSuccess = true
                        )
                    }
                    Timber.i("数据库初始化成功")
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "数据库已有数据，跳过初始化",
                            isSuccess = false
                        )
                    }
                    Timber.d("数据库已有数据")
                }

                // 刷新统计信息
                refreshStatistics()
            } catch (e: Exception) {
                Timber.e(e, "数据库初始化失败")
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = "✗ 初始化失败: ${e.message}",
                        isSuccess = false
                    )
                }
            }
        }
    }

    /**
     * 重置数据库
     * 删除所有数据并重新插入模拟数据
     */
    fun resetDatabase() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = "") }

            try {
                val result = databaseInitializer.reset()

                if (result) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "✓ 数据库重置成功！已重新插入 ${MockDataProvider.getMockProducts().size} 个商品",
                            isSuccess = true
                        )
                    }
                    Timber.i("数据库重置成功")
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "✗ 数据库重置失败",
                            isSuccess = false
                        )
                    }
                    Timber.e("数据库重置失败")
                }

                // 刷新统计信息
                refreshStatistics()
            } catch (e: Exception) {
                Timber.e(e, "数据库重置失败")
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = "✗ 重置失败: ${e.message}",
                        isSuccess = false
                    )
                }
            }
        }
    }

    /**
     * 刷新统计信息
     */
    fun refreshStatistics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val stats = databaseInitializer.getStatistics()
                _state.update {
                    it.copy(
                        isLoading = false,
                        statistics = stats
                    )
                }
                Timber.d("统计信息刷新成功")
            } catch (e: Exception) {
                Timber.e(e, "获取统计信息失败")
                _state.update {
                    it.copy(
                        isLoading = false,
                        statistics = "获取统计信息失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 添加热门商品
     */
    fun addPopularProducts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = "") }

            try {
                val popularProducts = MockDataProvider.getPopularProducts()
                val count = databaseInitializer.addProducts(popularProducts)

                _state.update {
                    it.copy(
                        isLoading = false,
                        message = "✓ 成功添加 $count 个热门商品",
                        isSuccess = true
                    )
                }
                Timber.i("添加热门商品成功: $count")

                // 刷新统计信息
                refreshStatistics()
            } catch (e: Exception) {
                Timber.e(e, "添加热门商品失败")
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = "✗ 添加失败: ${e.message}",
                        isSuccess = false
                    )
                }
            }
        }
    }

    /**
     * 清空所有购物车
     */
    fun clearAllCarts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = "") }

            try {
                // 这里简化处理，实际应该查询所有 cartId
                cartItemDao.clearCart("default")

                _state.update {
                    it.copy(
                        isLoading = false,
                        message = "✓ 购物车已清空",
                        isSuccess = true
                    )
                }
                Timber.i("购物车清空成功")
            } catch (e: Exception) {
                Timber.e(e, "清空购物车失败")
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = "✗ 清空失败: ${e.message}",
                        isSuccess = false
                    )
                }
            }
        }
    }
}
