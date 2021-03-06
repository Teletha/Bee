/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package bee.task;

import static bee.Platform.*;

import java.util.List;
import java.util.StringJoiner;

import bee.Task;
import bee.api.Command;
import bee.util.Inputs;
import kiss.I;
import psychopath.File;

public class Ci extends Task {

    @Command(value = "Generate CI/CD configuration files for GitHub.", defaults = true)
    public void github() {
        require(Ci::gitignore);

        String mavenCI = """
                name: Java CI with Maven

                on:
                  push:
                    branches: [master, main]
                  pull_request:
                    branches: [master, main]
                  workflow_dispatch:

                jobs:
                  build:
                    runs-on: ubuntu-latest
                    steps:
                    - name: Check out repository
                      uses: actions/checkout@v2

                    - name: Set up JDK
                      uses: actions/setup-java@v1
                      with:
                        java-version: %s

                    - name: Build with Maven
                      run: mvn -B package --file pom.xml
                """;

        String releasePlease = """
                name: Release Please

                on:
                  push:
                    branches: [master, main]

                jobs:
                  release-please:
                    runs-on: ubuntu-latest
                    steps:
                      - uses: GoogleCloudPlatform/release-please-action@v2
                        with:
                          release-type: simple
                          package-name: %s
                """;

        String jitPack = """
                before_install:
                  - source "$HOME/.sdkman/bin/sdkman-init.sh"
                  - sdk install java %s-open
                  - sdk use java %s-open
                """;

        String sourceVersion = Inputs.normalize(project.getJavaClassVersion());
        String testVersion = Inputs.normalize(project.getJavaTestClassVersion());

        makeFile(".github/workflows/java-ci-with-maven.yml", String.format(mavenCI, testVersion));
        makeFile(".github/workflows/release-please.yml", String.format(releasePlease, project.getProduct()));
        makeFile("jitpack.yml", String.format(jitPack, sourceVersion, sourceVersion));
        makeFile("version.txt", project.getVersion());
        makeFile(project.getProjectDefinition(), line -> {
            if (line.trim().startsWith("product(")) {
                return line.replaceAll(",[^,]+\\);", ", ref(\"version.txt\"));");
            } else {
                return line;
            }
        });
    }

    @Command(value = "Generate .gitignore file.")
    public void gitignore() {
        File ignore = project.getRoot().file(".gitignore");

        makeFile(ignore, update(ignore.lines().toList()));
    }

    /**
     * Update gitignore configuration.
     * 
     * @param lines Lines to update.
     * @return An updated lines.
     */
    List<String> update(List<String> lines) {
        StringJoiner uri = new StringJoiner(",", "https://www.gitignore.io/api/", "").add("Java").add("Maven");

        // OS
        if (isWindows()) uri.add("Windows");
        if (isLinux()) uri.add("Linux");

        // IDE
        for (IDESupport ide : I.find(IDESupport.class)) {
            if (ide.exist(project)) {
                uri.add(ide.toString());
            }
        }

        return I.http(uri.toString(), String.class)
                .waitForTerminate()
                .flatArray(rule -> rule.split(EOL))
                .startWith(".*", "!/.gitignore", "!/.github")
                .startWith(lines)
                .distinct()
                .toList();
    }
}
