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
package bee.compiler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import bee.FileSet;
import ezbean.I;
import ezbean.model.ClassUtil;

/**
 * @version 2010/12/19 10:20:21
 */
public class JavaCompiler {

    /** The actual java compiler. */
    private final javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    /** The source directories. */
    private final Set<File> sources = new HashSet();

    /** The classpath list. */
    private final Set<File> classpaths = new HashSet();

    /** The annotation processors. */
    private final List<Class> processors = new ArrayList();

    /** The output directory. */
    private File output;

    /** The source version. */
    private SourceVersion sourceVersion = SourceVersion.RELEASE_7;

    /** The target version. */
    private SourceVersion targetVersion = SourceVersion.RELEASE_7;

    public void addSourcePath(File file) {
        if (file != null) {
            sources.add(file);
        }
    }

    public void addClassPath(File file) {

    }

    /**
     * <p>
     * Add the specified annotation processor to compile process. This method bybpasses the default
     * discovery process.
     * </p>
     * <p>
     * </p>
     * <p>
     * Names of the annotation processors to run. This bypasses the default discovery process.
     * </p>
     * 
     * @param processor
     */
    public void addProcessor(Class<? extends Processor> processor) {
        if (processor != null && !processors.contains(processor)) {
            processors.add(processor);
        }
    }

    /**
     * <p>
     * Set the destination directory for class files. If a class is part of a package, javac puts
     * the class file in a subdirectory reflecting the package name, creating directories as needed.
     * For example, if you specify -d c:\myclasses and the class is called com.mypackage.MyClass,
     * then the class file is called c:\myclasses\com\mypackage\MyClass.class.
     * </p>
     * <p>
     * If you don't use this method, this compiler use in-memory storage for compiled classes.
     * </p>
     * <p>
     * Note that the directory specified by this method is not automatically added to your user
     * class path.
     * </p>
     * 
     * @param directory Your output directory. <code>null</code> value will reset to default.
     */
    public void setOutput(File directory) {
        this.output = directory;
    }

    /**
     * <p>
     * Show a description of each use or override of a deprecated member or class. Without
     * -deprecation, javac shows a summary of the source files that use or override deprecated
     * members or classes. -deprecation is shorthand for -Xlint:deprecation.
     * </p>
     * 
     * @param show
     */
    public void setDeprecation(boolean show) {

    }

    /**
     * <p>
     * Set the source file encoding name, such as EUC-JP and UTF-8. If -encoding is not specified,
     * the platform default converter is used.
     * </p>
     * 
     * @param charset
     */
    public void setEncoding(Charset charset) {

    }

    /**
     * <p>
     * Generate all debugging information, including local variables. By default, only line number
     * and source file information is generated.
     * </p>
     * 
     * @param generate
     */
    public void setGenerateDebugInfo(boolean generate) {

    }

    /**
     * <p>
     * Verbose output. This includes information about each class loaded and each source file
     * compiled.
     * </p>
     * 
     * @param verbose
     */
    public void setVerbose(boolean verbose) {

    }

    /**
     * <p>
     * Override the location of installed extensions.
     * </p>
     * 
     * @param directory
     */
    public void setExtensionDirectory(File directory) {

    }

    /**
     * <p>
     * Disable warning messages. This has the same meaning as -Xlint:none.
     * </p>
     */
    public void setNoWarn() {

    }

    /**
     * <p>
     * Set source version and target version for compiling.
     * </p>
     * 
     * @param sourceVersion
     * @param targetVersion
     */
    public void setVersion(SourceVersion sourceVersion, SourceVersion targetVersion) {
        if (sourceVersion != null) {
            this.sourceVersion = sourceVersion;
        }

        if (targetVersion != null) {
            this.targetVersion = targetVersion;
        }
    }

    /**
     * <p>
     * Specify the directory where to place generated source files. The directory must already
     * exist; javac will not create it. If a class is part of a package, the compiler puts the
     * source file in a subdirectory reflecting the package name, creating directories as needed.
     * For example, if you specify -s C:\mysrc and the class is called com.mypackage.MyClass, then
     * the source file will be placed in C:\mysrc\com\mypackage\MyClass.java.
     * </p>
     * 
     * @param directory
     */
    public void setGeneratedSourceDirectory(File directory) {

    }

    /**
     * <p>
     * Invoke compiler with specified options.
     * </p>
     */
    public ClassLoader compile() {
        List<String> args = new ArrayList();

        // Output Directory
        if (output != null) {
            if (output.isFile()) {
                output = output.getParentFile();
            }

            if (!output.exists()) {
                output.mkdirs();
            }

            args.add("-d");
            args.add(output.getAbsolutePath());
        }

        // Annotation Processing Tools
        if (processors.size() == 0) {
            args.add("-proc:none");
        } else {
            StringBuilder processors = new StringBuilder();
            StringBuilder processorPaths = new StringBuilder();

            for (int i = 0, end = this.processors.size(); i < end; i++) {
                Class processor = this.processors.get(i);

                processors.append(processor.getName());
                processorPaths.append('"').append(ClassUtil.getArchive(processor).getAbsolutePath()).append('"');

                if (i < end - 1) {
                    processors.append(',');
                    processorPaths.append(',');
                }
            }

            args.add("-processor");
            args.add(processors.toString());
            args.add("-processorpath");
            args.add(processorPaths.toString());
        }

        List<File> sources = new ArrayList();

        // Java Sources
        for (File source : this.sources) {
            FileSet set = new FileSet(source);

            for (File file : set) {
                sources.add(file);
            }
        }

        // Invocation
        ErrorListener listener = new ErrorListener();
        Manager manager = new Manager(compiler.getStandardFileManager(listener, Locale.getDefault(), I.getEncoding()));

        CompilationTask task = compiler.getTask(null, manager, listener, args, null, manager.getJavaFileObjectsFromFiles(sources));
        boolean result = task.call();
        System.out.println(result);
        return manager;
    }

    /**
     * @version 2010/12/18 11:51:46
     */
    private static class ErrorListener implements DiagnosticListener<JavaFileObject> {

        /**
         * @see javax.tools.DiagnosticListener#report(javax.tools.Diagnostic)
         */
        @Override
        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
            System.out.println(diagnostic);
        }
    }

    /**
     * @version 2010/12/19 10:20:30
     */
    private class Manager extends SecureClassLoader implements StandardJavaFileManager {

        /** The actual file manager. */
        private final StandardJavaFileManager manager;

        /** The mapping for defined classes. */
        private final Map<String, Class> classes = new HashMap();

        /**
         * @param fileManager
         */
        public Manager(StandardJavaFileManager manager) {
            this.manager = manager;
        }

        /**
         * @see javax.tools.StandardJavaFileManager#getJavaFileObjectsFromFiles(java.lang.Iterable)
         */
        @Override
        public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
            return manager.getJavaFileObjectsFromFiles(files);
        }

        /**
         * @see javax.tools.StandardJavaFileManager#getJavaFileObjects(java.io.File[])
         */
        @Override
        public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
            return manager.getJavaFileObjects(files);
        }

        /**
         * @see javax.tools.StandardJavaFileManager#getJavaFileObjectsFromStrings(java.lang.Iterable)
         */
        @Override
        public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
            return manager.getJavaFileObjectsFromStrings(names);
        }

        /**
         * @see javax.tools.StandardJavaFileManager#getJavaFileObjects(java.lang.String[])
         */
        @Override
        public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
            return manager.getJavaFileObjects(names);
        }

        /**
         * @see javax.tools.StandardJavaFileManager#setLocation(javax.tools.JavaFileManager.Location,
         *      java.lang.Iterable)
         */
        @Override
        public void setLocation(Location location, Iterable<? extends File> path) throws IOException {
            manager.setLocation(location, path);
        }

        /**
         * @see javax.tools.StandardJavaFileManager#getLocation(javax.tools.JavaFileManager.Location)
         */
        @Override
        public Iterable<? extends File> getLocation(Location location) {
            return manager.getLocation(location);
        }

        /**
         * @see javax.tools.OptionChecker#isSupportedOption(java.lang.String)
         */
        @Override
        public int isSupportedOption(String option) {
            return manager.isSupportedOption(option);
        }

        /**
         * @see javax.tools.JavaFileManager#list(javax.tools.JavaFileManager.Location,
         *      java.lang.String, java.util.Set, boolean)
         */
        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
                throws IOException {
            return manager.list(location, packageName, kinds, recurse);
        }

        /**
         * @see javax.tools.JavaFileManager#inferBinaryName(javax.tools.JavaFileManager.Location,
         *      javax.tools.JavaFileObject)
         */
        @Override
        public String inferBinaryName(Location location, JavaFileObject file) {
            return manager.inferBinaryName(location, file);
        }

        /**
         * @see javax.tools.StandardJavaFileManager#isSameFile(javax.tools.FileObject,
         *      javax.tools.FileObject)
         */
        @Override
        public boolean isSameFile(FileObject a, FileObject b) {
            return manager.isSameFile(a, b);
        }

        /**
         * @see javax.tools.JavaFileManager#handleOption(java.lang.String, java.util.Iterator)
         */
        @Override
        public boolean handleOption(String current, Iterator<String> remaining) {
            return manager.handleOption(current, remaining);
        }

        /**
         * @see javax.tools.JavaFileManager#hasLocation(javax.tools.JavaFileManager.Location)
         */
        @Override
        public boolean hasLocation(Location location) {
            return manager.hasLocation(location);
        }

        /**
         * @see javax.tools.JavaFileManager#getClassLoader(javax.tools.JavaFileManager.Location)
         */
        @Override
        public ClassLoader getClassLoader(Location location) {
            return this;
        }

        /**
         * @see javax.tools.JavaFileManager#getJavaFileForInput(javax.tools.JavaFileManager.Location,
         *      java.lang.String, javax.tools.JavaFileObject.Kind)
         */
        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
            return manager.getJavaFileForInput(location, className, kind);
        }

        /**
         * @see javax.tools.JavaFileManager#getJavaFileForOutput(javax.tools.JavaFileManager.Location,
         *      java.lang.String, javax.tools.JavaFileObject.Kind, javax.tools.FileObject)
         */
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling)
                throws IOException {
            if (output == null) {
                return new Bytecode(className);
            } else {
                return manager.getJavaFileForOutput(location, className, kind, sibling);
            }
        }

        /**
         * @see javax.tools.JavaFileManager#getFileForInput(javax.tools.JavaFileManager.Location,
         *      java.lang.String, java.lang.String)
         */
        @Override
        public FileObject getFileForInput(Location location, String packageName, String relativeName)
                throws IOException {
            return manager.getFileForInput(location, packageName, relativeName);
        }

        /**
         * @see javax.tools.JavaFileManager#getFileForOutput(javax.tools.JavaFileManager.Location,
         *      java.lang.String, java.lang.String, javax.tools.FileObject)
         */
        @Override
        public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling)
                throws IOException {
            return manager.getFileForOutput(location, packageName, relativeName, sibling);
        }

        /**
         * @see javax.tools.JavaFileManager#flush()
         */
        @Override
        public void flush() throws IOException {
            manager.flush();

            // clean up
            classes.clear();
        }

        /**
         * @see javax.tools.JavaFileManager#close()
         */
        @Override
        public void close() throws IOException {
            manager.close();

            // clean up
            classes.clear();
        }

        /**
         * @see java.lang.ClassLoader#findClass(java.lang.String)
         */
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            Class clazz = classes.get(name);

            return clazz != null ? clazz : super.findClass(name);
        }

        /**
         * @version 2010/12/18 22:45:13
         */
        private class Bytecode extends ByteArrayOutputStream implements JavaFileObject {

            /** The class name. */
            private final String name;

            /**
             * @param name
             */
            public Bytecode(String name) {
                this.name = name;
            }

            /**
             * @see javax.tools.FileObject#toUri()
             */
            @Override
            public URI toUri() {
                return null;
            }

            /**
             * @see javax.tools.FileObject#getName()
             */
            @Override
            public String getName() {
                return null;
            }

            /**
             * @see javax.tools.FileObject#openInputStream()
             */
            @Override
            public InputStream openInputStream() throws IOException {
                return null;
            }

            /**
             * @see javax.tools.FileObject#openReader(boolean)
             */
            @Override
            public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
                return null;
            }

            /**
             * @see javax.tools.FileObject#getCharContent(boolean)
             */
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                return null;
            }

            /**
             * @see javax.tools.FileObject#openWriter()
             */
            @Override
            public Writer openWriter() throws IOException {
                return null;
            }

            /**
             * @see javax.tools.FileObject#getLastModified()
             */
            @Override
            public long getLastModified() {
                return 0;
            }

            /**
             * @see javax.tools.FileObject#delete()
             */
            @Override
            public boolean delete() {
                return false;
            }

            /**
             * @see javax.tools.JavaFileObject#getKind()
             */
            @Override
            public Kind getKind() {
                return null;
            }

            /**
             * @see javax.tools.JavaFileObject#isNameCompatible(java.lang.String,
             *      javax.tools.JavaFileObject.Kind)
             */
            @Override
            public boolean isNameCompatible(String simpleName, Kind kind) {
                return false;
            }

            /**
             * @see javax.tools.JavaFileObject#getNestingKind()
             */
            @Override
            public NestingKind getNestingKind() {
                return null;
            }

            /**
             * @see javax.tools.JavaFileObject#getAccessLevel()
             */
            @Override
            public Modifier getAccessLevel() {
                return null;
            }

            /**
             * @see javax.tools.SimpleJavaFileObject#openOutputStream()
             */
            @Override
            public OutputStream openOutputStream() throws IOException {
                return this;
            }

            /**
             * @see java.io.OutputStream#close()
             */
            @Override
            public void close() throws IOException {
                // close stream
                super.close();

                // define class
                byte[] bytes = toByteArray();

                classes.put(name, defineClass(name, bytes, 0, bytes.length));
            }
        }
    }
}
