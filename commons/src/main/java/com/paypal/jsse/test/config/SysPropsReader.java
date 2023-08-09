package com.paypal.jsse.test.config;

public interface SysPropsReader {

    static <T> T readProperty(final String propertyName,
                              final Class<T> targetType,
                              final T defaultValue) {
        final String propertyValue = System.getProperty(propertyName, String.valueOf(defaultValue));
        return convertToSpecificType(propertyValue, targetType);
    }

    @SuppressWarnings("unchecked")
    static  <T> T convertToSpecificType(final String propertyValue,
                                       final Class<T> targetType) {
        if (targetType == null || targetType.equals(String.class)) {
            return (T) propertyValue;
        } else if (targetType.equals(Boolean.class)) {
            return (T) Boolean.valueOf(propertyValue);
        } else if (targetType.equals(Integer.class)) {
            return (T) Integer.valueOf(propertyValue);
        } else if (targetType.equals(Double.class)) {
            return (T) Double.valueOf(propertyValue);
        }
        throw new RuntimeException("Unsupported target type: " + targetType);
    }
}
