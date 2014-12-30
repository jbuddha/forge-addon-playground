package org.buddha.forge.commands.ui;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.parser.java.resources.JavaResourceVisitor;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.resource.URLResource;
import org.jboss.forge.addon.resource.visit.VisitContext;
import org.jboss.forge.addon.templates.Template;
import org.jboss.forge.addon.templates.TemplateFactory;
import org.jboss.forge.addon.templates.freemarker.FreemarkerTemplate;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.input.UICompleter;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.validate.UIValidator;
import org.jboss.forge.roaster.model.Annotation;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.util.Strings;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.batchXML10.BatchXMLDescriptor;

import javax.batch.api.chunk.*;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    @Inject
    @WithAttributes(label = "JobXML")
    UIInput<String> jobXml;

    @Inject
    ProjectFactory projectFactory;

    @Inject
    TemplateFactory templateFactory;

    @Inject
    ResourceFactory resourceFactory;

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

        jobXml.addValidator(new UIValidator() {
            @Override
            public void validate(UIValidationContext context) {
                String jobXmlValue = (String) context.getCurrentInputComponent().getValue();

                FileResource<?> fileResource = getBatchXmlResource(context.getUIContext(), jobXml.getValue());

                if(fileResource.exists()){
                    context.addValidationError(context.getCurrentInputComponent(),fileResource.getFullyQualifiedName()+" already exists");
                }
            }
        });

        builder.add(reader)
                .add(processor)
                .add(writer)
                .add(jobXml);
    }

    private FileResource<?> getBatchXmlResource(UIContext context, String value) {
        ResourcesFacet resourcesFacet = getSelectedProject(context).getFacet(ResourcesFacet.class);

        return resourcesFacet.getResource("META-INF" + File.separator + "batch-jobs" + File.separator + value);
    }

    @Override
    public Result execute(UIExecutionContext context)  {

        FileResource<?> jobXmlResource = getBatchXmlResource(context.getUIContext(),jobXml.getValue());
        BatchXMLDescriptor descriptor = Descriptors.create(BatchXMLDescriptor.class);

        Resource<URL> templateJobXml = resourceFactory.create(getClass().getResource("/templates" + File.separator + "job.ftl")).reify(URLResource.class);
        Template template = templateFactory.create(templateJobXml, FreemarkerTemplate.class);

        Map<String, Object> templateContext = new HashMap<String,Object>();
        try {
            String readerName = getCDIBeanName(context, reader.getValue());
            String writerName = getCDIBeanName(context, writer.getValue());
            String processorName = getCDIBeanName(context, processor.getValue());

            templateContext.put("readerBeanName",readerName);
            templateContext.put("writerBeanName",writerName);
            templateContext.put("processorBeanName",processorName);
            jobXmlResource.createNewFile();
            jobXmlResource.setContents(template.process(templateContext));
        }catch (IOException e){
            return Results.fail(e.getMessage(),e);
        }
        return Results.success("Command 'Batch New Job Xml' successfully executed!");
    }

    private String getCDIBeanName(UIExecutionContext context, String value) throws FileNotFoundException {

        Project project = getSelectedProject(context);
        JavaSourceFacet facet = project.getFacet(JavaSourceFacet.class);
        JavaResource javaResource = facet.getJavaResource(value);
        JavaType<?> javaType = javaResource.getJavaType();
        Annotation<? extends JavaType<?>> named = javaType.getAnnotation(Named.class);

        if(named != null){
            return named.getStringValue();
        }
        return Strings.uncapitalize(javaType.getName());
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