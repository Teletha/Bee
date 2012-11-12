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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;

import kiss.I;
import kiss.model.ClassUtil;

import org.junit.Test;

import bee.BlinkProject;
import bee.api.Project;
import bee.sample.Bean;
import bee.sample.Enum;
import bee.sample.Interface;
import bee.sample.annotation.SourceAnnotation;
import bee.task.AnnotationProcessor.ProjectInfo;
import bee.util.JavaCompiler;

/**
 * @version 2012/11/11 16:26:26
 */
public class AnnotationValidatorTest {

    @Test
    public void classNameForClass() throws Exception {
        BlinkProject project = new BlinkProject();
        project.importBy(Bean.class);

        compileWith(new AnnotationValidator<SourceAnnotation>() {

            /**
             * {@inheritDoc}
             */
            @Override
            protected void validate(SourceAnnotation annotation) {
                assert getClassName().equals(Bean.class.getName());
            }
        });
    }

    @Test
    public void classNameForEnum() throws Exception {
        BlinkProject project = new BlinkProject();
        project.importBy(Enum.class);

        compileWith(new AnnotationValidator<SourceAnnotation>() {

            /**
             * {@inheritDoc}
             */
            @Override
            protected void validate(SourceAnnotation annotation) {
                assert getClassName().equals(Enum.class.getName());
            }
        });
    }

    @Test
    public void classNameForInterface() throws Exception {
        BlinkProject project = new BlinkProject();
        project.importBy(Interface.class);

        compileWith(new AnnotationValidator<SourceAnnotation>() {

            /**
             * {@inheritDoc}
             */
            @Override
            protected void validate(SourceAnnotation annotation) {
                assert getClassName().equals(Interface.class.getName());
            }
        });
    }

    @Test
    public void classNameForAnnotation() throws Exception {
        BlinkProject project = new BlinkProject();
        project.importBy(bee.sample.Annotation.class);

        compileWith(new AnnotationValidator<SourceAnnotation>() {

            /**
             * {@inheritDoc}
             */
            @Override
            protected void validate(SourceAnnotation annotation) {
                assert getClassName().equals(bee.sample.Annotation.class.getName());
            }
        });
    }

    @Test
    public void getSourceFile() throws Exception {
        BlinkProject project = new BlinkProject();
        final Path source = project.importBy(Bean.class);

        compileWith(new AnnotationValidator<SourceAnnotation>() {

            /**
             * {@inheritDoc}
             */
            @Override
            protected void validate(SourceAnnotation annotation) {
                try {
                    assert Files.isSameFile(source, getSourceFile());
                } catch (IOException e) {
                    throw I.quiet(e);
                }
            }
        });
    }

    /**
     * <p>
     * Compile project sources.
     * </p>
     * 
     * @param project
     */
    private void compileWith(AnnotationValidator validator) {
        Project project = I.make(Project.class);
        TestableProcessor processor = new TestableProcessor(validator);

        JavaCompiler compiler = new JavaCompiler();
        compiler.addSourceDirectory(project.getSources());
        compiler.setOutput(project.getClasses());
        compiler.addProcessor(processor);
        compiler.addProcessorOption(new ProjectInfo(project));
        compiler.compile();

        assert processor.isCalled;
    }

    /**
     * @version 2012/11/12 11:19:18
     */
    private static class TestableProcessor<T extends Annotation> extends AnnotationProcessor {

        /** The validator to test. */
        private final AnnotationValidator<T> validator;

        /** The validation target annotation. */
        private final Class annotation;

        /** The flag for confirmation. */
        private boolean isCalled = false;

        /**
         * @param validator
         */
        private TestableProcessor(AnnotationValidator<T> validator) {
            this.validator = validator;
            this.annotation = ClassUtil.getParameter(validator.getClass(), AnnotationValidator.class)[0];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected AnnotationValidator<T> find(Class annotation) {
            if (this.annotation != annotation) {
                return null;
            } else {
                isCalled = true;

                return validator;
            }
        }
    }
}
