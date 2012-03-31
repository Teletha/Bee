/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */

/**
 * @version 2012/03/20 15:45:08
 */
public class Project extends bee.definition.Project {

    private String aetherVersion = "0.9.0-SNAPSHOT";

    {
        require("npc", "sinobu", "0.9.1");
        require("npc", "antibug", "0.2").atTest();
        require("org.eclipse.aether", "aether-api", aetherVersion);
        require("org.eclipse.aether", "aether-util", aetherVersion);
        require("org.eclipse.aether", "aether-impl", aetherVersion);
        require("org.eclipse.aether", "aether-connector-file", aetherVersion);
        require("org.eclipse.aether", "aether-connector-wagon", aetherVersion);
        require("org.apache.maven", "maven-aether-provider", "3.0.4");
        require("org.apache.maven.wagon", "wagon-http-lightweight", "1.0");

        unrequire("org.sonatype.aether", "*");
        unrequire("org.apache.maven.wagon", "wagon-http-shared");
        unrequire("org.codehaus.plexus", "plexus-component-annotations");

        repository("http://oss.sonatype.org/content/repositories/snapshots");
    }
}