package org.buddha.forge.commands.ui;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.DependencyQuery;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.dependencies.util.NonSnapshotDependencyFilter;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;

import javax.inject.Inject;
import java.lang.Override;
import java.lang.Exception;
import java.util.List;

public class AddJUnitDependencyCommand extends AbstractProjectCommand
{
   @Inject
   ProjectFactory projectFactory;

   @Inject
   DependencyInstaller dependencyInstaller;

   @Inject
   DependencyResolver dependencyResolver;

   @Inject @WithAttributes(label = "Version", required = true, description = "Select the version of JUnit")
   private UISelectOne<Coordinate> version;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception {

      DependencyQuery query = DependencyQueryBuilder
              .create("junit:junit")
              .setFilter(new NonSnapshotDependencyFilter());

      List<Coordinate> coordinates = dependencyResolver.resolveVersions(query);
      version.setValueChoices(coordinates);
      builder.add(version);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      DependencyBuilder builder = DependencyBuilder.create();
      builder.setCoordinate(version.getValue());

      dependencyInstaller.install(getSelectedProject(context), builder);

      return Results.success("Added Dependency to the selected project");
   }

   @Override
   protected boolean isProjectRequired() {
      return true;
   }

   @Override
   protected ProjectFactory getProjectFactory() {
      return projectFactory;
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.forCommand(AddJUnitDependencyCommand.class)
            .name("Add JUnit Dependency")
            .category(Categories.create("Play Ground", "Project"));
   }


}