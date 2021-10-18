package org.smartframework.cloud.starter.mp.shardingjdbc.handler;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class ShardingsphereEnumTypeHandler<E extends Enum<E>> extends MybatisEnumTypeHandler<E> {

    private static final ReflectorFactory REFLECTOR_FACTORY = new DefaultReflectorFactory();
    private final Class<E> enumClassType;
    private final Class<?> propertyType;
    private final Invoker getInvoker;

    public ShardingsphereEnumTypeHandler(Class<E> enumClassType) {
        super(enumClassType);
        if (enumClassType == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.enumClassType = enumClassType;
        MetaClass metaClass = MetaClass.forClass(enumClassType, REFLECTOR_FACTORY);
        String name = "value";
        if (!IEnum.class.isAssignableFrom(enumClassType)) {
            name = findEnumValueFieldName(this.enumClassType).orElseThrow(() -> new IllegalArgumentException(String.format("Could not find @EnumValue in Class: %s.", this.enumClassType.getName())));
        }
        this.propertyType = ReflectionKit.resolvePrimitiveIfNecessary(metaClass.getGetterType(name));
        this.getInvoker = metaClass.getGetInvoker(name);
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // TODO: 修改以便shardingsphere支持枚举----start
        if (rs instanceof ShardingSphereResultSet) {
            ShardingSphereResultSet shardingSphereResultSet = (ShardingSphereResultSet) rs;
            if (Byte.class.equals(propertyType)) {
                return this.valueOf(shardingSphereResultSet.getByte(columnName));
            }
            if (Short.class.equals(propertyType)) {
                return this.valueOf(shardingSphereResultSet.getShort(columnName));
            }
            if (Integer.class.equals(propertyType)) {
                return this.valueOf(shardingSphereResultSet.getInt(columnName));
            }
            if (Long.class.equals(propertyType)) {
                return this.valueOf(shardingSphereResultSet.getLong(columnName));
            }
            if (Double.class.equals(propertyType)) {
                return this.valueOf(shardingSphereResultSet.getDouble(columnName));
            }
            if (Float.class.equals(propertyType)) {
                return this.valueOf(shardingSphereResultSet.getFloat(columnName));
            }
            if (String.class.equals(propertyType)) {
                return this.valueOf(shardingSphereResultSet.getString(columnName));
            }
        }
        // TODO: 修改以便shardingsphere支持枚举----end

        return super.getNullableResult(rs, columnName);
    }

    private E valueOf(Object value) {
        E[] es = this.enumClassType.getEnumConstants();
        return Arrays.stream(es).filter((e) -> equalsValue(value, getValue(e))).findAny().orElse(null);
    }

    private Object getValue(Object object) {
        try {
            return this.getInvoker.invoke(object, new Object[0]);
        } catch (ReflectiveOperationException e) {
            throw ExceptionUtils.mpe(e);
        }
    }

}