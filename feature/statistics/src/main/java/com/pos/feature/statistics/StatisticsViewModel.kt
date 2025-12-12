package com.pos.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pos.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * 统计数据状态
 *
 * @property totalSales 总销售额
 * @property totalOrders 订单总数
 * @property averageOrderValue 平均订单额
 * @property customerUnitPrice 客单价
 * @property isLoading 是否加载中
 */
data class StatisticsState(
    val totalSales: Double = 0.0,
    val totalOrders: Int = 0,
    val averageOrderValue: Double = 0.0,
    val customerUnitPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String = ""
)

/**
 * 统计 ViewModel
 * 处理销售数据统计和分析
 *
 * @property orderRepository 订单仓库
 * @author POS Team
 * @since 1.0.0
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    init {
        loadStatistics("today")
    }

    /**
     * 加载指定时间段的统计数据
     *
     * @param period 时间段（today|week|month|year）
     */
    fun loadStatistics(period: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = "") }

            try {
                val (startTime, endTime) = when (period) {
                    "today" -> getTodayRange()
                    "week" -> getWeekRange()
                    "month" -> getMonthRange()
                    "year" -> getYearRange()
                    else -> getTodayRange()
                }

                // 获取订单数据
                orderRepository.getOrdersByDateRange(startTime, endTime).collect { orders ->
                    if (orders.isNotEmpty()) {
                        val totalSales = orders.sumOf { it.totalAmount }
                        val totalOrders = orders.size
                        val averageOrderValue = totalSales / totalOrders
                        val customerUnitPrice = calculateCustomerUnitPrice(orders)

                        _state.update {
                            it.copy(
                                isLoading = false,
                                totalSales = totalSales,
                                totalOrders = totalOrders,
                                averageOrderValue = averageOrderValue,
                                customerUnitPrice = customerUnitPrice
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                totalSales = 0.0,
                                totalOrders = 0,
                                averageOrderValue = 0.0,
                                customerUnitPrice = 0.0
                            )
                        }
                    }
                }

                Timber.i("统计数据加载成功，周期: $period")
            } catch (e: Exception) {
                Timber.e(e, "加载统计数据失败")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "加载失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 计算客单价（客户平均购买金额）
     */
    private fun calculateCustomerUnitPrice(orders: List<com.pos.data.db.entity.OrderEntity>): Double {
        if (orders.isEmpty()) return 0.0

        // 简化计算：假设每个订单代表一个客户
        // 实际应该统计不同客户的购买金额
        val totalItems = orders.sumOf { it.itemCount }
        val totalSales = orders.sumOf { it.totalAmount }

        return if (totalItems > 0) totalSales / totalItems else 0.0
    }

    /**
     * 获取今天的时间范围
     */
    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis

        return Pair(startTime, endTime)
    }

    /**
     * 获取本周的时间范围
     */
    private fun getWeekRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_WEEK, 7)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis

        return Pair(startTime, endTime)
    }

    /**
     * 获取本月的时间范围
     */
    private fun getMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.set(Calendar.DAY_OF_MONTH, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis

        return Pair(startTime, endTime)
    }

    /**
     * 获取本年的时间范围
     */
    private fun getYearRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        calendar.add(Calendar.YEAR, 1)
        calendar.set(Calendar.DAY_OF_YEAR, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis

        return Pair(startTime, endTime)
    }
}
