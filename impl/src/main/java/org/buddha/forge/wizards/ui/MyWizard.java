package org.buddha.forge.wizards.ui;

import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizard;

import javax.inject.Inject;

public class MyWizard extends AbstractUICommand implements UIWizard
{
   @Inject
   UIInput<String> name;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.forCommand(MyWizard.class).name("MyWizard");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      builder.add(name);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      return Results.success("Command 'MyWizard' successfully executed!");
   }

   @Override
   public NavigationResult next(UINavigationContext uiNavigationContext) throws Exception {
      uiNavigationContext.getUIContext().getAttributeMap().put("name",name.getValue());
      return Results.navigateTo(WizardPageTwo.class);
   }
}