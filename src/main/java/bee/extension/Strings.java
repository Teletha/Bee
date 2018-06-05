/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bee.extension;

/**
 * @version 2016/12/14 16:48:47
 */
public class Strings implements Extension {

    /**
     * <p>
     * To camel case.
     * </p>
     * 
     * @param value
     * @return
     */
    @Extension.Method
    public static String toCamel(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    @Extension.Method
    public static boolean tester(String value) {
        return false;
    }
}