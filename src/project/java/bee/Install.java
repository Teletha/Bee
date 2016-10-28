/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package bee;

/**
 * @version 2012/05/10 10:29:33
 */
public class Install extends bee.task.Install {

    /**
     * <p>
     * Install the current Bee into your environment.
     * </p>
     */
    @Override
    public void project() {
        require(Jar.class).merge();

        // super.project();

        BeeInstaller.install(project.locateJar());
    }
}
