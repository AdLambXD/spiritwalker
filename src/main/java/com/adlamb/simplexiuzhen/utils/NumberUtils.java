package com.adlamb.simplexiuzhen.utils;

/**
 * 数值工具类 - 简化版本
 */
public class NumberUtils {
    
    /**
     * 格式化浮点数，避免精度误差（2位小数）
     */
    public static String formatDouble(double value) {
        return String.format("%.2f", Math.round(value * 100.0) / 100.0);
    }
}