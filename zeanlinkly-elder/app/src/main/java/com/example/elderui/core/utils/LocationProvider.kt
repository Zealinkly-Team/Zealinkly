package com.example.elderui.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import android.location.Location
import kotlinx.coroutines.tasks.await

/**
 * 位置提供者，用于获取设备的当前地理位置
 */
class LocationProvider(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * 异步获取最后一次的已知位置。
     * 如果没有权限，将抛出 SecurityException。
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Location? {
        // 权限检查已在调用此方法前由 Accompanist 处理，
        // 但为防止 lint 错误和意外调用，此处保留检查。
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("Location permission not granted.")
        }

        // 使用 await() 扩展函数，代码更简洁且类型安全
        return fusedLocationClient.lastLocation.await()
    }
}
