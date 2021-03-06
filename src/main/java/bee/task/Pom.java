/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bee.task;

import bee.Task;
import bee.api.Command;

public class Pom extends Task {

    @Command("Generate pom file.")
    public void build() {
        makeFile("pom.xml", project.toString());
    }
}