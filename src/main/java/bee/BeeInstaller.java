/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bee;

import static bee.Platform.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bee.api.Repository;
import bee.util.JarArchiver;
import filer.Filer;
import kiss.I;
import psychopath.File;
import psychopath.Locator;

/**
 * @version 2015/06/22 11:53:31
 */
public class BeeInstaller {

    /** The date formatter. */
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * <p>
     * Launch Bee.
     * </p>
     */
    public static final void main(String... args) {
        install(Locator.locate(BeeInstaller.class).asFile());
    }

    /**
     * <p>
     * Install Bee into your system.
     * </p>
     * 
     * @param source
     */
    public static final void install(File source) {
        UserInterface ui = I.make(UserInterface.class);

        try {
            String fileName = "bee-" + format.format(new Date(source.lastModified())) + ".jar";
            psychopath.File dest = BeeHome.file(fileName);

            // delete old files
            BeeHome.walkFiles("bee-*.jar").to(jar -> {
                try {
                    jar.delete();
                } catch (Exception e) {
                    // we can't delete current processing jar file.
                }
            });

            if (source.lastModified() != dest.lastModified()) {
                // The current bee.jar is newer.
                // We should copy it to JDK directory.
                // This process is mainly used by Bee users while install phase.
                source.copyTo(dest);
                ui.talk("Install new bee library. [", dest, "]");
            }

            // create bat file
            List<String> bat = new ArrayList();

            if (Bee.getFileName().toString().endsWith(".bat")) {
                // windows
                // use JDK full path to avoid using JRE
                bat.add("@echo off");
                bat.add(JavaHome.resolve("bin/java") + " -Xms256m -Xmx2048m -cp \"" + dest.toString() + "\" " + Bee.class
                        .getName() + " %*");
            } else {
                // linux
                // TODO
            }
            Files.write(Bee, bat, StandardCharsets.UTF_8);

            ui.talk("Write new bat file. [", Bee, "]");

            // create bee-api library and sources
            Path classes = Filer.locateTemporary();
            JarArchiver archiver = new JarArchiver();
            archiver.add(source.asJavaPath(), "bee/**", "!**.java");
            archiver.add(source.asJavaPath(), "META-INF/services/**");
            archiver.pack(classes);

            Path sources = Filer.locateTemporary();
            archiver = new JarArchiver();
            archiver.add(source.asJavaPath(), "bee/**.java");
            archiver.add(source.asJavaPath(), "META-INF/services/**");
            archiver.pack(sources);

            I.make(Repository.class).install(bee.Bee.API, classes, Locator.file(sources), null);
        } catch (

        IOException e) {
            throw I.quiet(e);
        }
    }
}
