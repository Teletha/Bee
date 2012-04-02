/*
 * Copyright (C) 2010 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bee.projects;

import org.junit.Rule;
import org.junit.Test;

import bee.definition.Project;
import bee.definition.TemporaryProjectBuilder;
import bee.projects.noconfig.project.java.NoConfigProject;

/**
 * @version 2010/10/06 7:42:12
 */
public class NoConfigTest {

    @Rule
    public static final TemporaryProjectBuilder dummy = new TemporaryProjectBuilder(NoConfigProject.class);

    @Test
    public void project() throws Exception {
        Project project = dummy.project;

        assert project != null;
        assert project.getProject().equals("no");
        assert project.getProduct().equals("config");
        assert project.root.equals(dummy.moduleForProject.path);

    }
}
