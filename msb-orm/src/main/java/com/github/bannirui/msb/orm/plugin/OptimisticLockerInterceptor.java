package com.github.bannirui.msb.orm.plugin;

import com.github.bannirui.msb.ex.FrameworkException;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import tk.mybatis.mapper.annotation.Version;
import tk.mybatis.mapper.entity.Example;

@Intercepts({@Signature(
    type = Executor.class,
    method = "update",
    args = {MappedStatement.class, Object.class}
)})
public class OptimisticLockerInterceptor implements Interceptor {
    private Map<Class<?>, Field> fieldCache = new HashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement)args[0];
        if (SqlCommandType.UPDATE != ms.getSqlCommandType()) {
            return invocation.proceed();
        } else {
            String msId = ms.getId();
            if (msId != null && msId.endsWith("updateByExampleAndVersionSelective")) {
                Object param = args[1];
                if (param instanceof MapperMethod.ParamMap map) {
                    Object record = map.get("record");
                    Example example = (Example)map.get("example");
                    Field versionField = this.getVersionField(record.getClass());
                    if (versionField == null || versionField.get(record) == null) {
                        throw FrameworkException.getInstance("not find @Version,Class [{1}]", new Object[]{record.getClass().getName()});
                    }
                    Object originalVersionVal = versionField.get(record);
                    versionField.set(record, this.getUpdatedVersionVal(originalVersionVal));
                    List<Example.Criteria> oredCriteria = example.getOredCriteria();
                    oredCriteria.forEach(c -> c.andEqualTo(versionField.getName(), originalVersionVal));
                }
                return invocation.proceed();
            } else {
                return invocation.proceed();
            }
        }
    }

    protected Object getUpdatedVersionVal(Object originalVersionVal) {
        Class<?> versionValClass = originalVersionVal.getClass();
        if (Long.TYPE.equals(versionValClass)) {
            return (Long)originalVersionVal + 1L;
        } else if (Long.class.equals(versionValClass)) {
            return (Long)originalVersionVal + 1L;
        } else if (Integer.TYPE.equals(versionValClass)) {
            return (Integer)originalVersionVal + 1;
        } else if (Integer.class.equals(versionValClass)) {
            return (Integer)originalVersionVal + 1;
        } else if (Date.class.equals(versionValClass)) {
            return new Date();
        } else {
            return Timestamp.class.equals(versionValClass) ? new Timestamp(System.currentTimeMillis()) : originalVersionVal;
        }
    }

    private Field getVersionField(Class<?> parameterClass) {
        if (this.fieldCache.containsKey(parameterClass)) {
            return this.fieldCache.get(parameterClass);
        } else {
            synchronized(this.fieldCache) {
                if (this.fieldCache.containsKey(parameterClass)) {
                    return this.fieldCache.get(parameterClass);
                } else {
                    Field field = this.getVersionFieldRegular(parameterClass);
                    this.fieldCache.put(parameterClass, field);
                    return field;
                }
            }
        }
    }

    private Field getVersionFieldRegular(Class<?> parameterClass) {
        if (parameterClass != Object.class && parameterClass != null) {
            Field[] fields = parameterClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Version.class)) {
                    field.setAccessible(true);
                    return field;
                }
            }
            return this.getVersionFieldRegular(parameterClass.getSuperclass());
        } else {
            return null;
        }
    }

    @Override
    public Object plugin(Object target) {
        return target instanceof Executor ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
