package org.buddha.forge.commands.ui;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.parser.java.resources.JavaResourceVisitor;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.visit.VisitContext;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.input.UICompleter;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import javax.batch.api.chunk.*;
import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class BatchNewJobXmlCommand extends AbstractProjectCommand {
    @Inject
    @WithAttributes(label = "ItemReader", type = InputType.JAVA_CLASS_PICKER)
    UIInput<String> reader;

    @Inject
    @WithAttributes(label = "ItemProcessor", type = InputType.JAVA_CLASS_PICKER)
    UIInput<String> processor;

    @Inject
    @WithAttributes(label = "ItemWriter", type = InputType.JAVA_CLASS_PICKER)
    UIInput<String> writer;

//    @Inject
//    UIInput<String> changeTester;

    @Inject
    ProjectFactory projectFactory;

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(BatchNewJobXmlCommand.class).name(
                "Batch New Job Xml");
    }

    @Override
    public void initializeUI(final UIBuilder builder) throws Exception {
        reader.setCompleter(new StringUICompleter(ItemReader.class, AbstractItemReader.class));
        writer.setCompleter(new StringUICompleter(ItemWriter.class, AbstractItemWriter.class));
        processor.setCompleter(new StringUICompleter(ItemProcessor.class, null));
        builder.add(reader)
                .add(processor)
                .add(writer);
//                .add(changeTester);
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        return Results
                .success("Command 'Batch New Job Xml' successfully executed!");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

    @Override
    protected ProjectFactory getProjectFactory() {
        return projectFactory;
    }

    private class BatchUI extends JavaResourceVisitor {
        private final Set<String> result;
        private final Class<?> baseAbstractClass;
        private final Class<?> baseInterfaceClass;

        public BatchUI(Set<String> result,Class<?> baseInterfaceClass, Class<?> baseAbstractClass) {
            this.result = result;
            this.baseAbstractClass = baseAbstractClass;
            this.baseInterfaceClass = baseInterfaceClass;
        }

        @Override
        public void visit(VisitContext context, JavaResource javaResource) {
            try {

                JavaSource javaType = javaResource.getJavaType();
                if (javaType.isClass()) {
                    JavaClassSource javaClassSource = (JavaClassSource) javaType;
                    if (javaClassSource.hasInterface(baseInterfaceClass) || ( baseAbstractClass != null && baseAbstractClass.getName().equals(javaClassSource.getSuperType()))) {
                        result.add(javaType.getQualifiedName());
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private class StringUICompleter implements UICompleter<String> {

        private final Class<?> baseAbstractClass;
        private final Class<?> baseInterfaceClass;

        public StringUICompleter(Class<?> baseInterfaceClass, Class<?> baseAbstractClass) {
            this.baseAbstractClass = baseAbstractClass;
            this.baseInterfaceClass = baseInterfaceClass;
        }

        @Override
        public Iterable<String> getCompletionProposals(UIContext context, InputComponent<?, String> input, String value) {
            final Set<String> result = new HashSet<String>();

            Project selectedProject = getSelectedProject(context);
            JavaSourceFacet facet = selectedProject.getFacet(JavaSourceFacet.class);
            facet.visitJavaSources(new BatchUI(result,baseInterfaceClass,baseAbstractClass));

            return result;
        }
    }
}