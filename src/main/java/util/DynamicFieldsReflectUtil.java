package util;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * @Desc: 提供给某个类动态添加属性的工具类
 */
public class DynamicFieldsReflectUtil {


    private static Logger logger = LoggerFactory.getLogger(DynamicFieldsReflectUtil.class);

    /***
     * 动态添加属性
     * @param dest 源对象
     * @param addProperties 需要添加的属性
     * @return
     */
    public static Object getTarget(Object dest, Map<String, Object> addProperties) {
        if (MapUtils.isEmpty(addProperties)) {
            return dest;
        }
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        PropertyDescriptor[] descriptors = propertyUtilsBean.getPropertyDescriptors(dest);
        Map<String, Class> propertyMap = new HashMap<>();
        for (PropertyDescriptor d : descriptors) {
            if (!"class".equalsIgnoreCase(d.getName())) {
                propertyMap.put(d.getName(), d.getPropertyType());
            }
        }
        // add extra properties
        addProperties.forEach((k, v) -> propertyMap.put(k, v.getClass()));
        // new dynamic bean
        DynamicBean dynamicBean = new DynamicBean(dest.getClass(), propertyMap);
        // add old value
        propertyMap.forEach((k, v) -> {
            try {
                // filter extra properties
                if (!addProperties.containsKey(k)) {
                    dynamicBean.setValue(k, propertyUtilsBean.getNestedProperty(dest, k));
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });
        // add extra value
        addProperties.forEach((k, v) -> {
            try {
                dynamicBean.setValue(k, v);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });
        Object target = dynamicBean.getTarget();
        return target;
    }
}
