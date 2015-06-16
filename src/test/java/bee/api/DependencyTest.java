/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package bee.api;

import java.util.Set;

import org.junit.Test;

import bee.BlinkProject;
import bee.Null;

/**
 * @version 2015/06/08 16:05:49
 */
public class DependencyTest {

    @Test
    public void empty() {
        BlinkProject project = new BlinkProject();
        Repository repository = new Repository(project, Null.UI);
        Set<Library> dependencies = repository.collectDependency(project, Scope.Compile);
        assert dependencies.size() == 0;
    }

    @Test
    public void external() {
        BlinkProject project = new BlinkProject();
        project.require("org.ow2.asm", "asm", "5.0");

        Repository repository = new Repository(project, Null.UI);
        Set<Library> dependencies = repository.collectDependency(project, Scope.Compile);
        assert dependencies.size() == 1;
    }

    @Test
    public void atTest1() {
        BlinkProject project = new BlinkProject();
        project.require("org.ow2.asm", "asm", "5.0.4").atTest();

        Repository repository = new Repository(project, Null.UI);
        Set<Library> dependencies = repository.collectDependency(project, Scope.Test);
        assert dependencies.size() == 1;
        dependencies = repository.collectDependency(project, Scope.Runtime);
        assert dependencies.size() == 0;
    }

    @Test
    public void atTest2() {
        BlinkProject project = new BlinkProject();
        project.require("org.ow2.asm", "asm-tree", "5.0.4").atTest();

        Repository repository = new Repository(project, Null.UI);
        Set<Library> dependencies = repository.collectDependency(project, Scope.Test);
        assert dependencies.size() == 2;
        dependencies = repository.collectDependency(project, Scope.Runtime);
        assert dependencies.size() == 0;
    }

    @Test
    public void external1() {
        BlinkProject project = new BlinkProject();
        project.require("org.skyscreamer", "jsonassert", "1.2.3");

        Repository repository = new Repository(project, Null.UI);
        Set<Library> dependencies = repository.collectDependency(project, Scope.Runtime);
        assert dependencies.size() == 2;
        dependencies = repository.collectDependency(project, Scope.Test);
        assert dependencies.size() == 2;
    }

    @Test
    public void byLibrary() {
        Repository repo = new Repository(new BlinkProject(), Null.UI);
        Set<Library> dep = repo.collectDependency("org.skyscreamer", "jsonassert", "1.2.3", Scope.Runtime);
        assert dep.size() == 2;

        dep = repo.collectDependency("org.skyscreamer", "jsonassert", "1.2.3", Scope.Test);
        assert dep.size() == 2;
    }
}