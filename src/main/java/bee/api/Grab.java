/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package bee.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Repeatable(GrabSet.class)
@Retention(RUNTIME)
@Target(TYPE)
public @interface Grab {
    /**
     * Specify project name.
     * 
     * @return
     */
    String group();

    /**
     * Specify module name.
     * 
     * @return
     */
    String module();

    /**
     * Specify module version.
     * 
     * @return
     */
    String version() default "LATEST";
}
