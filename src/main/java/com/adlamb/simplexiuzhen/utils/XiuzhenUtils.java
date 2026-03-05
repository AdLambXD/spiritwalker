package com.adlamb.simplexiuzhen.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;

/**
 * 工具类 - 提供常用的静态方法
 * 减少代码重复，提高可维护性
 */
public class XiuzhenUtils {
    
    /**
     * 安全获取配置中的整数值
     */
    public static int getConfigInt(ConfigurationSection config, String path, int defaultValue) {
        if (config == null) return defaultValue;
        
        Object value = config.get(path);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * 安全获取配置中的双精度浮点数值
     */
    public static double getConfigDouble(ConfigurationSection config, String path, double defaultValue) {
        if (config == null) return defaultValue;
        
        Object value = config.get(path);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * 安全获取配置中的布尔值
     */
    public static boolean getConfigBoolean(ConfigurationSection config, String path, boolean defaultValue) {
        if (config == null) return defaultValue;
        
        Object value = config.get(path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
    
    /**
     * 安全获取配置中的长整数值
     */
    public static long getConfigLong(ConfigurationSection config, String path, long defaultValue) {
        if (config == null) return defaultValue;
        
        Object value = config.get(path);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof Double) {
            return ((Double) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * 检查集合是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * 检查映射是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
    
    /**
     * 检查字符串是否为空或空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 格式化消息颜色
     */
    public static String colorize(String message) {
        // 将 & 颜色代码转换为 § 格式
        return message.replace('&', '§');
    }
    
    /**
     * 获取分页数据
     */
    public static <T> List<T> getPage(List<T> list, int page, int pageSize) {
        if (isEmpty(list) || page < 1 || pageSize < 1) {
            return List.of();
        }
        
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, list.size());
        
        if (startIndex >= list.size()) {
            return List.of();
        }
        
        return list.subList(startIndex, endIndex);
    }
    
    /**
     * 计算总页数
     */
    public static int getTotalPages(int totalCount, int pageSize) {
        if (totalCount <= 0 || pageSize <= 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalCount / pageSize);
    }
    
    /**
     * 安全获取配置中的字符串值
     */
    public static String getConfigString(ConfigurationSection config, String path, String defaultValue) {
        if (config == null) return defaultValue;
        
        Object value = config.get(path);
        return value != null ? value.toString() : defaultValue;
    }
}