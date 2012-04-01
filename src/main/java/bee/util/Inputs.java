/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package bee.util;

import javax.lang.model.SourceVersion;

/**
 * @version 2012/04/01 15:57:20
 */
public class Inputs {

    /**
     * <p>
     * Normalize user input.
     * </p>
     * 
     * @param input A user input.
     * @param defaultValue A default value.
     * @return A normalized input.
     */
    public static String normalize(String input, String defaultValue) {
        if (input == null) {
            input = defaultValue;
        }

        // trim whitespcae
        input = input.trim();

        if (input.length() == 0) {
            input = defaultValue;
        }

        // API definition
        return input;
    }

    /**
     * <p>
     * Normalize {@link SourceVersion} to human-readable version number.
     * </p>
     * 
     * @param version A target version.
     * @return A version number.
     */
    public static String normalize(SourceVersion version) {
        if (version == null) {
            version = SourceVersion.latest();
        }

        switch (version) {
        case RELEASE_0:
            return "1.0";

        case RELEASE_1:
            return "1.1";

        case RELEASE_2:
            return "1.2";

        case RELEASE_3:
            return "1.3";

        case RELEASE_4:
            return "1.4";

        case RELEASE_5:
            return "1.5";

        case RELEASE_6:
            return "1.6";

        default:
            return "1.7";
        }
    }
}
