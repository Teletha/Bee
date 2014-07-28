/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package bee.task;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import kiss.I;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import bee.TaskFailure;
import bee.api.Command;
import bee.api.Scope;
import bee.api.Task;
import bee.util.Java;
import bee.util.Java.JVM;

/**
 * @version 2012/03/28 9:58:39
 */
public class Test extends Task {

    @Command("Test product code.")
    public void test() {
        Compile compile = require(Compile.class);
        compile.source();
        compile.test();

        try {
            Path report = project.getOutput().resolve("test-reports");
            Files.createDirectories(report);

            Java java = new Java();
            java.addClassPath(project.getClasses());
            java.addClassPath(project.getTestClasses());
            java.addClassPath(project.getDependency(Scope.Test));
            java.addClassPath(loadBee());
            java.enableAssertion();
            java.setWorkingDirectory(project.getRoot());
            java.run(Junit.class, project.getTestClasses(), report);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * @version 2012/04/04 17:51:29
     */
    private static final class Junit extends JVM {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process() {
            Path classes = I.locate(args[0]);
            List<Path> tests = I.walk(classes, "**Test.class");

            if (tests.isEmpty()) {
                ui.talk("Nothing to test");

                return true;
            }

            // execute test classes
            ui.title(" T E S T S");

            int runs = 0;
            int skips = 0;
            long times = 0;
            List<Failure> fails = new ArrayList();
            List<Failure> errors = new ArrayList();

            JUnitCore core = new JUnitCore();

            for (Path path : tests) {
                String fqcn = classes.relativize(path).toString();
                fqcn = fqcn.substring(0, fqcn.length() - 6).replace(File.separatorChar, '.');

                try {
                    ui.talk("Running ", fqcn);

                    Result result = core.run(Class.forName(fqcn));
                    List<Failure> failures = result.getFailures();
                    List<Failure> fail = new ArrayList();
                    List<Failure> error = new ArrayList();

                    for (Failure failure : failures) {
                        if (failure.getException() instanceof AssertionError) {
                            fail.add(failure);
                        } else {
                            error.add(failure);
                        }
                    }

                    ui.talk(buildResult(result.getRunCount(), fail.size(), error.size(), result.getIgnoreCount(), result.getRunTime()));

                    runs += result.getRunCount();
                    skips += result.getIgnoreCount();
                    times += result.getRunTime();
                    fails.addAll(fail);
                    errors.addAll(error);
                } catch (Error e) {
                    ui.talk(buildResult(0, 0, 0, 0, 0));
                } catch (ClassNotFoundException e) {
                    throw I.quiet(e);
                }
            }

            ui.talk("TOTAL");
            ui.talk(buildResult(runs, fails.size(), errors.size(), skips, times));

            if (fails.size() != 0 || errors.size() != 0) {
                TaskFailure failure = new TaskFailure("Test has failed.");
                buildFailure(failure, errors);
                buildFailure(failure, fails);
                throw failure;
            }
            return true;
        }

        /**
         * <p>
         * Build result message.
         * </p>
         */
        private String buildResult(int tests, int fails, int errors, int ignores, long time) {
            StringBuilder builder = new StringBuilder();
            builder.append("Tests run: ").append(tests);
            builder.append("  Failures: ").append(fails);
            builder.append("  Errors: ").append(errors);
            builder.append("  Skipped: ").append(ignores);
            builder.append("  Time elapsed: ").append((float) time / 1000).append("sec");

            if (errors != 0) {
                builder.append(" <<< ERROR!");
            } else if (fails != 0) {
                builder.append(" <<< FAILURE!");
            }
            return builder.toString();
        }

        /**
         * <p>
         * Build {@link TaskFailure}.
         * </p>
         * 
         * @param failure A current resolver.
         * @param list A list of test results.
         */
        private void buildFailure(TaskFailure failure, List<Failure> list) {
            for (Failure fail : list) {
                Description desc = fail.getDescription();
                Class test = desc.getTestClass();
                StackTraceElement element = fail.getException().getStackTrace()[0];
                int line = element.getClassName().equals(test.getName()) ? element.getLineNumber() : 0;

                String target = desc.getClassName() + "." + desc.getMethodName();

                if (line != 0) {
                    target = target + "(" + test.getSimpleName() + ".java:" + line + ")";
                }
                failure.solve("Fix " + target + "  >>>  " + fail.getMessage().trim().split("[\\r\\n]")[0]);
            }
        }
    }
}
