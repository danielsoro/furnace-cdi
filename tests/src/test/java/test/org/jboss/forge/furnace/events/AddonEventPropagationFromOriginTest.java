/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.jboss.forge.furnace.events;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDeployment;
import org.jboss.forge.arquillian.AddonDeployments;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.org.jboss.forge.furnace.mocks.event.EventPayload1;
import test.org.jboss.forge.furnace.mocks.event.EventPayload3;
import test.org.jboss.forge.furnace.mocks.event.EventResponseService;
import test.org.jboss.forge.furnace.mocks.event.EventService;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RunWith(Arquillian.class)
public class AddonEventPropagationFromOriginTest
{
   @Deployment(order = 1)
   @AddonDeployments({
            @AddonDeployment(name = "org.jboss.forge.furnace.container:cdi")
   })
   public static AddonArchive getDeployment()
   {
      AddonArchive archive = ShrinkWrap
               .create(AddonArchive.class)
               .addClasses(EventService.class, EventPayload1.class)
               .addBeansXML()
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                        AddonDependencyEntry.create("dependency", "1")
               );

      return archive;
   }

   @Deployment(name = "dependency,1", testable = false, order = 2)
   public static AddonArchive getDependencyDeployment()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class, "dependency.jar")
               .addClasses(EventResponseService.class, EventPayload3.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi", false)
               )
               .addBeansXML();

      return archive;
   }

   @Inject
   private EventService sender;

   @Test
   public void testEventPropagationAcrossContainers() throws Exception
   {
      Assert.assertFalse(sender.isLocalRequestRecieved());
      Assert.assertFalse(sender.isWrongResponseRecieved());
      Assert.assertFalse(sender.isRemoteResponseRecieved());
      sender.firePayload1();
      Assert.assertTrue(sender.isLocalRequestRecieved());
      Assert.assertTrue(sender.isRemoteResponseRecieved());
      Assert.assertFalse(sender.isWrongResponseRecieved());
   }

}