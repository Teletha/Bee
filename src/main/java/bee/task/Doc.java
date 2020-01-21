/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bee.task;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.tools.DocumentationTool;
import javax.tools.DocumentationTool.DocumentationTask;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import antibug.doc.Javadoc;
import bee.Platform;
import bee.Task;
import bee.UserInterface;
import bee.api.Command;
import bee.api.Scope;
import kiss.I;
import psychopath.Directory;
import psychopath.File;
import psychopath.Location;

public class Doc extends Task {

    /**
     * Generate javadoc with the specified doclet.
     */
    @Command("Generate product javadoc.")
    public Directory javadoc() {
        if (false) {
            Directory out = project.getOutput().directory("new-api");

            List<Location> list = I.signal(project.getDependency(Scope.Test, Scope.Compile))
                    .map(library -> library.getLocalJar())
                    .as(Location.class)
                    .toList();

            Javadoc.with.sources(project.getSourceSet().toList())
                    .output(out)
                    .product(project.getProduct())
                    .project(project.getGroup())
                    .version(project.getVersion())
                    .classpath(list)
                    .useExternalJDKDoc()
                    .build();
            return out;
        }

        // specify output directory
        Directory output = project.getOutput().directory("api").create();

        List<String> options = new CopyOnWriteArrayList();

        // lint
        options.add("-Xdoclint:none");
        options.add("-Xmaxwarns");
        options.add("1");
        options.add("-Xmaxerrs");
        options.add("1");

        // format
        options.add("-html5");
        options.add("-javafx");

        // external links
        options.add("-link");
        options.add("https://docs.oracle.com/en/java/javase/12/docs/api/");

        try {
            DocumentationTool doc = ToolProvider.getSystemDocumentationTool();
            StandardJavaFileManager manager = doc.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8);
            manager.setLocationFromPaths(DocumentationTool.Location.DOCUMENTATION_OUTPUT, I.list(output.asJavaPath()));
            manager.setLocationFromPaths(StandardLocation.SOURCE_PATH, project.getSourceSet()
                    .map(Location::asJavaPath)
                    // .merge(I.signal(project.getDependency(Scope.Compile)).map(lib ->
                    // lib.getLocalSourceJar().asJavaPath()))
                    .toList());
            manager.setLocationFromPaths(StandardLocation.CLASS_PATH, I.signal(project.getDependency(Scope.Test, Scope.Compile))
                    .map(library -> library.getLocalJar().asJavaPath())
                    .toList());

            DocumentationTask task = doc
                    .getTask(new UIWriter(ui), manager, null, null, options, manager.getJavaFileObjectsFromPaths(project.getSourceSet()
                            .flatMap(dir -> dir.walkFile("**.java"))
                            .map(File::asJavaPath)
                            .toList()));

            if (task.call()) {
                ui.talk("Build javadoc : " + output);
            } else {
                ui.talk("Fail building javadoc.");
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }
        return output;
    }

    /**
     * @version 2018/04/04 11:25:57
     */
    private static class UIWriter extends Writer {

        private UserInterface ui;

        /**
         * @param ui
         */
        private UIWriter(UserInterface ui) {
            this.ui = ui;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            String message = String.valueOf(cbuf, off, len).trim();

            if (message.endsWith(Platform.EOL)) {
                message = message.substring(0, message.length() - Platform.EOL.length());
            }

            if (message.length() != 0) {
                ui.talk(message + "\r");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void flush() throws IOException {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
        }
    }
}
