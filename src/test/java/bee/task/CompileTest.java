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

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import bee.BlinkProject;

/**
 * @version 2012/04/02 9:37:56
 */
public class CompileTest {

    @Test
    public void compileMainSource() throws Exception {
        BlinkProject project = new BlinkProject();
        project.source("A");
        project.source("test.B");
        project.resource("C");

        Path A = project.locateMainOutput("A.class");
        Path B = project.locateMainOutput("test/B.class");
        Path C = project.locateMainOutput("C");

        assert Files.notExists(A);
        assert Files.notExists(B);
        assert Files.notExists(C);

        Compile compile = new Compile();
        compile.source();

        assert Files.exists(A);
        assert Files.exists(B);
        assert Files.exists(C);
    }

    @Test
    public void compileTestSource() throws Exception {
        BlinkProject project = new BlinkProject();
        project.sourceTest("A");
        project.sourceTest("test.B");
        project.resourceTest("C");

        Path A = project.locateTestOutput("A.class");
        Path B = project.locateTestOutput("test/B.class");
        Path C = project.locateTestOutput("C");

        assert Files.notExists(A);
        assert Files.notExists(B);
        assert Files.notExists(C);

        Compile compile = new Compile();
        compile.test();

        assert Files.exists(A);
        assert Files.exists(B);
        assert Files.exists(C);
    }

    @Test
    public void compileProjectSource() throws Exception {
        BlinkProject project = new BlinkProject();
        project.sourceProject("A");
        project.sourceProject("test.B");
        project.resourceProject("C");

        Path A = project.locateProjectOutput("A.class");
        Path B = project.locateProjectOutput("test/B.class");
        Path C = project.locateProjectOutput("C");

        assert Files.notExists(A);
        assert Files.notExists(B);
        assert Files.notExists(C);

        Compile compile = new Compile();
        compile.project();

        assert Files.exists(A);
        assert Files.exists(B);
        assert Files.exists(C);
    }

    @Test(expected = Throwable.class)
    public void compileInvalidSource() throws Exception {
        BlinkProject project = new BlinkProject();
        project.source("A", "invalid");

        Compile compile = new Compile();
        compile.source();
    }
}
