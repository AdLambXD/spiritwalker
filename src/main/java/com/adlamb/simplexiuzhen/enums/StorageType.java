package com.adlamb.simplexiuzhen.enums;

/**
 * 存储类型枚举
 */
public enum StorageType {
    YAML("YAML"),
    MYSQL("MYSQL");
    
    private final String typeName;
    
    StorageType(String typeName) {
        this.typeName = typeName;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    /**
     * 根据字符串获取存储类型
     */
    public static StorageType fromString(String type) {
        if (type == null) return YAML;
        
        for (StorageType storageType : values()) {
            if (storageType.typeName.equalsIgnoreCase(type)) {
                return storageType;
            }
        }
        return YAML;
    }
    
    /**
     * 检查是否为数据库类型
     */
    public boolean isDatabase() {
        return this == MYSQL;
    }
    
    /**
     * 检查是否为文件类型
     */
    public boolean isFileBased() {
        return this == YAML;
    }
}