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
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

import javax.inject.Inject;

public class WizardPageTwo extends AbstractUICommand implements UIWizardStep
{
   @Inject
   UIInput<String> lastName;

   @Inject
   UIInput<String> firstName;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.forCommand(WizardPageTwo.class).name("WizardPageTwo");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {

      firstName.setValue((String) builder.getUIContext().getAttributeMap().get("name"));
      builder.add(firstName);

      builder.add(lastName);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      return Results
            .success("Command 'WizardPageTwo' successfully executed! name is " + context.getUIContext().getAttributeMap().get("name"));
   }

   @Override
   public NavigationResult next(UINavigationContext uiNavigationContext) throws Exception {
      return null;
   }
}