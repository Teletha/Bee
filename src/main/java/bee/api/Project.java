/*
 * Copyright (C) 2010 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this Path except in compliance with the License.
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
package bee.api;

import static bee.util.Inputs.*;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.lang.model.SourceVersion;

import org.apache.maven.wagon.PathUtils;
import org.sonatype.aether.graph.Exclusion;
import org.sonatype.aether.repository.RemoteRepository;

import bee.Bee;
import bee.task.AnnotationValidator;
import bee.util.PathPattern;
import bee.util.PathSet;
import kiss.I;
import kiss.XML;
import kiss.model.ClassUtil;

/**
 * @version 2012/04/17 23:50:41
 */
public class Project {

    /** The libraries. */
    final SortedSet<Library> libraries = new TreeSet();

    /** The excluded libraries. */
    final HashSet<Exclusion> exclusions = new HashSet();

    /** The repositories. */
    final ArrayList<RemoteRepository> repositories = new ArrayList();

    /** The project association. */
    final Map<Class, Object> associates = new ConcurrentHashMap();

    /** The project root directory. */
    private Path root;

    /** The product group. */
    private String productGroup = "";

    /** The product name. */
    private String productName = "";

    /** The product version. */
    private String productVersion = "1.0";

    /** The product description. */
    private String description = "";

    /** The requirement of Java version. */
    private SourceVersion requirementJavaVersion;

    /** The license. */
    private License license;

    /** The input base directory. */
    private Path input;

    /** The output base directory. */
    private Path output;

    /**
     * 
     */
    protected Project() {
        Class projectClass = getClass();

        if (projectClass.isMemberClass()) {
            // fabric project
            this.root = I.locate("").toAbsolutePath();
        } else {
            Path archive = ClassUtil.getArchive(projectClass);

            if (Files.isDirectory(archive)) {
                // directory
                this.root = archive.getParent().getParent();
            } else {
                // some archive
                this.root = archive;
            }
        }

        setInput((Path) null);
        setOutput((Path) null);
        set((License) null);
    }

    /**
     * <p>
     * Return project root directory.
     * </p>
     * 
     * @return A root directory of this project.
     */
    public Path getRoot() {
        return root;
    }

    /**
     * <p>
     * Return product group.
     * </p>
     * 
     * @return The product group.
     */
    public String getGroup() {
        return productGroup;
    }

    /**
     * <p>
     * Return product name.
     * </p>
     * 
     * @return The product name.
     */
    public String getProduct() {
        return productName;
    }

    /**
     * <p>
     * Return product version.
     * </p>
     * 
     * @return The product version.
     */
    public String getVersion() {
        return productVersion;
    }

    /**
     * <p>
     * Declare product package, name and version.
     * </p>
     * 
     * @param productPackage A product package name.
     * @param productName A product name.
     * @param productVersion A product version.
     */
    protected final void product(String productPackage, String productName, String productVersion) {
        this.productGroup = normalize(productPackage, "YourPackage");
        this.productName = normalize(productName, "YourProduct");
        this.productVersion = normalize(productVersion, "1.0");
    }

    /**
     * <p>
     * Return product description.
     * </p>
     * 
     * @return The product description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * <p>
     * Declare product description.
     * </p>
     * 
     * @param description A product description.
     */
    protected final void describe(String description) {
        if (description == null) {
            description = "";
        }
        this.description = description.trim();
    }

    /**
     * <p>
     * Resolve all dependencies for the specified scope.
     * </p>
     * 
     * @param scope
     * @return
     */
    public Set<Library> getDependency(Scope scope) {
        return I.make(Repository.class).collectDependency(this, scope);
    }

    /**
     * <p>
     * Find {@link Library} by name.
     * </p>
     * 
     * @param projectName
     * @param productName
     * @param version
     * @return
     */
    public Set<Library> getLibrary(String projectName, String productName, String version) {
        return I.make(Repository.class)
                .collectDependency(new Library(projectName, productName, version), Scope.Runtime);
    }

    /**
     * <p>
     * Declare dependency.
     * </p>
     * 
     * @param projectName A project name.
     * @param productName A product name.
     * @param version A product version.
     * @return A dependency.
     */
    protected final Library require(String projectName, String productName, String version) {
        Library library = new Library(projectName, productName, version);
        libraries.add(library);

        // API definition
        return library;
    }

    /**
     * <p>
     * Returns Java version requirement.
     * </p>
     * 
     * @return A Java version requirement.
     */
    public String getJavaVersion() {
        return normalize(requirementJavaVersion);
    }

    /**
     * <p>
     * Declare Java version requirement.
     * </p>
     * 
     * @param version A Java version to require.
     */
    protected final void require(SourceVersion version) {
        this.requirementJavaVersion = version;
    }

    /**
     * <p>
     * Exclude the specified library from transitive dependency resolution.
     * </p>
     * 
     * @param projectName A project name.
     * @param productName A product name.
     */
    protected final void unrequire(String projectName, String productName) {
        exclusions.add(new Exclusion(projectName, productName, "", "jar"));
    }

    /**
     * <p>
     * Add new repository by URI.
     * </p>
     * 
     * @param uri
     */
    protected final void repository(String uri) {
        if (uri != null && !uri.isEmpty()) {
            repositories.add(new RemoteRepository(PathUtils.host(uri), "default", uri));
        }
    }

    /**
     * Get the bese directory of input.
     * 
     * @return The base input directory.
     */
    public Path getInput() {
        return input;
    }

    /**
     * <p>
     * Set base directory of input. <code>null</code> will set default directory. A relative path
     * will be resolved from project root directory.
     * </p>
     * 
     * @param input The base input directory to set.
     */
    protected final void setInput(Path input) {
        if (input == null) {
            input = getRoot().resolve("src");
        }

        if (!input.isAbsolute()) {
            input = getRoot().resolve(input);
        }
        this.input = input;
    }

    /**
     * <p>
     * Set base directory of input. <code>null</code> will set default directory. A relative path
     * will be resolved from project root directory.
     * </p>
     * 
     * @param input The base input directory to set.
     */
    protected final void setInput(String input) {
        if (input == null) {
            input = "src";
        }
        setInput(getRoot().resolve(input));
    }

    /**
     * Get the bese directory of output.
     * 
     * @return The base output directory.
     */
    public Path getOutput() {
        return output;
    }

    /**
     * <p>
     * Set base directory of output. <code>null</code> will set default directory. A relative path
     * will be resolved from project root directory.
     * </p>
     * 
     * @param output The base output directory to set.
     */
    protected final void setOutput(Path output) {
        if (output == null) {
            output = getRoot().resolve("target");
        }

        if (!output.isAbsolute()) {
            output = getRoot().resolve(output);
        }
        this.output = output;
    }

    /**
     * <p>
     * Set base directory of output. <code>null</code> will set default directory. A relative path
     * will be resolved from project root directory.
     * </p>
     * 
     * @param output The base output directory to set.
     */
    protected final void setOutput(String output) {
        if (output == null) {
            output = "target";
        }
        setOutput(getRoot().resolve(output));
    }

    /**
     * <p>
     * Returns source directories.
     * </p>
     * 
     * @return
     */
    public PathSet getSources() {
        PathSet set = new PathSet();

        for (Path path : I.walkDirectory(input.resolve("main"), "*")) {
            set.add(path);
        }
        return set;
    }

    /**
     * <p>
     * Returns class directory.
     * </p>
     * 
     * @return
     */
    public PathPattern getClasses() {
        return new PathPattern(output.resolve("classes"));
    }

    /**
     * <p>
     * Returns test source directories.
     * </p>
     * 
     * @return
     */
    public PathSet getTestSources() {
        PathSet set = new PathSet();

        for (Path path : I.walkDirectory(input.resolve("test"), "*")) {
            set.add(path);
        }
        return set;
    }

    /**
     * <p>
     * Returns test class directory.
     * </p>
     * 
     * @return
     */
    public Path getTestClasses() {
        return output.resolve("test-classes");
    }

    /**
     * <p>
     * Returns project source directories.
     * </p>
     * 
     * @return
     */
    public PathSet getProjectSources() {
        PathSet set = new PathSet();

        for (Path path : I.walkDirectory(input.resolve("project"), "*")) {
            set.add(path);
        }
        return set;
    }

    /**
     * <p>
     * Returns project class directory.
     * </p>
     * 
     * @return
     */
    public Path getProjectClasses() {
        return output.resolve("project-classes");
    }

    /**
     * <p>
     * Returns project source file.
     * </p>
     * 
     * @return
     */
    public Path getProjectDefinition() {
        return input.resolve("project/java/Project.java");
    }

    /**
     * <p>
     * Returns project source file.
     * </p>
     * 
     * @return
     */
    public Path getProjectDefintionClass() {
        return getProjectClasses().resolve("Project.class");
    }

    /**
     * <p>
     * Resolve all annotation processor for this project.
     * </p>
     * 
     * @return
     */
    public Set<Path> getAnnotationProcessors() {
        // search javax.annotation.processing.Processor file in libraries
        Set<Path> libraries = new HashSet();

        try {
            for (Library library : getDependency(Scope.Runtime)) {
                Path file = FileSystems.newFileSystem(library.getJar(), ClassLoader.getSystemClassLoader())
                        .getPath("/")
                        .resolve("META-INF/services/javax.annotation.processing.Processor");

                if (Files.exists(file)) {
                    libraries.add(library.getJar());
                }
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }

        // search AnnotationValidator in classpath
        List<AnnotationValidator> validators = I.find(AnnotationValidator.class);

        if (!validators.isEmpty()) {
            libraries.add(ClassUtil.getArchive(Bee.class));
        }

        return libraries;
    }

    /**
     * <p>
     * Returns project license.
     * </p>
     * 
     * @return
     */
    public License getLicense() {
        return license;
    }

    /**
     * <p>
     * Set product license.
     * </p>
     * 
     * @param license
     */
    protected void set(License license) {
        if (license == null) {
            license = License.MIT;
        }
        this.license = license;
    }

    /**
     * <p>
     * Locate product jar file.
     * </p>
     * 
     * @return
     */
    public final Path locateJar() {
        return getOutput().resolve(getProduct() + "-" + getVersion() + ".jar");
    }

    /**
     * <p>
     * Locate product source jar file.
     * </p>
     * 
     * @return
     */
    public final Path locateSourceJar() {
        return getOutput().resolve(getProduct() + "-" + getVersion() + "-sources.jar");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((productGroup == null) ? 0 : productGroup.toLowerCase().hashCode());
        result = prime * result + ((productName == null) ? 0 : productName.toLowerCase().hashCode());
        result = prime * result + ((productVersion == null) ? 0 : productVersion.toLowerCase().hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Project) {
            Project project = (Project) obj;

            return productGroup.equalsIgnoreCase(project.productGroup) && productName
                    .equalsIgnoreCase(project.productName) && productVersion.equalsIgnoreCase(project.productVersion);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        XML pom = I.xml("project");
        pom.child("modelVersion").text("4.0.0");
        pom.child("groupId").text(getGroup());
        pom.child("artifactId").text(getProduct());
        pom.child("version").text(getVersion());

        XML dependencies = pom.child("dependencies");

        for (Library library : libraries) {
            XML dependency = dependencies.child("dependency");
            dependency.child("groupId").text(library.group);
            dependency.child("artifactId").text(library.name);
            dependency.child("version").text(library.version);
            dependency.child("scope").text(library.scope.name());

            XML exclusions = dependency.child("exclusions");

            for (Exclusion e : this.exclusions) {
                XML exclusion = exclusions.child("exclusion");
                exclusion.child("groupId").text(e.getGroupId());
                exclusion.child("artifactId").text(e.getArtifactId());
            }
        }

        // write as pom
        return pom.toString();
    }

    /**
     * <p>
     * Returns literal project definition.
     * </p>
     * 
     * @return
     */
    public List<String> toDefinition() {
        List<String> code = new ArrayList();
        code.addAll(license.forJava());
        code.add("public class Project extends " + Project.class.getName() + " {");
        code.add("");
        code.add("  {");
        code.add("      product(\"" + productGroup + "\", \"" + productName + "\", \"" + productVersion + "\");");
        code.add("  }");
        code.add("}");

        return code;
    }
}
