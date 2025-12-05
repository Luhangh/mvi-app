package com.pos.core.mvi

/**
 * MVI架构中的Intent（意图）接口
 * 代表用户的所有可能操作
 */
interface MviIntent

/**
 * MVI架构中的State（状态）接口
 * 代表UI的完整状态
 */
interface MviState

/**
 * MVI架构中的Effect（副作用）接口
 * 代表一次性事件（导航、提示等）
 */
interface MviEffect
