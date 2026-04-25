package cn.zdjc.wms.project.common.utils;

import com.baomidou.mybatisplus.annotation.TableField;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlUtil {
    
    // ... 其他原有方法 ...
    
    /**
     * 获取带驼峰别名的列定义（用于原生SQL查询）
     * 例如: workstation_code as workstationCode, create_datetime as createDatetime
     */
    public static String getColumnsWithAlias(Class<?> entityClass) {
        StringBuilder columns = new StringBuilder();
        boolean first = true;
        
        // 通过反射获取实体类字段（含父类）
        List<Field> fields = getAllFields(entityClass);
        
        for (Field field : fields) {
            // 跳过静态/瞬态字段
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            
            // 获取数据库列名（支持 @TableField 注解）
            String columnName = getColumnName(field);
            String fieldName = field.getName(); // 驼峰命名
            
            if (!first) {
                columns.append(", ");
            }
            columns.append(columnName).append(" AS ").append(fieldName);
            first = false;
        }
        
        return columns.toString();
    }
    
    /**
     * 获取字段对应的数据库列名（支持 @TableField(value="xxx")）
     */
    private static String getColumnName(Field field) {
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField != null && StringUtils.isNotBlank(tableField.value())) {
            return tableField.value();
        }
        // 默认：驼峰转下划线
        return camelToUnderline(field.getName());
    }
    
    /**
     * 驼峰命名转下划线命名
     */
    public static String camelToUnderline(String camel) {
        if (StringUtils.isBlank(camel)) {
            return camel;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * 获取类及其父类的所有字段
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}