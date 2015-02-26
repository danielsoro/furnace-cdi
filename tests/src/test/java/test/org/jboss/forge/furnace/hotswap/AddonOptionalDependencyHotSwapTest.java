/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.jboss.forge.furnace.hotswap;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDeployment;
import org.jboss.forge.arquillian.AddonDeployments;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.repositories.AddonRepository;
import org.jboss.forge.furnace.repositories.MutableAddonRepository;
import org.jboss.forge.furnace.util.Addons;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RunWith(Arquillian.class)
public class AddonOptionalDependencyHotSwapTest
{
   @Deployment(order = 3)
   @AddonDeployments({
            @AddonDeployment(name = "org.jboss.forge.furnace.container:cdi")
   })
   public static AddonArchive getDeployment()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
               )
               .addBeansXML();

      return archive;
   }

   @Deployment(name = "dep1,1", testable = false, order = 2)
   public static AddonArchive getDeploymentDep1()
   {
      AddonArchive archive = ShrinkWrap
               .create(AddonArchive.class)
               .addBeansXML()
               .addAsAddonDependencies(AddonDependencyEntry.create("dep2", "2", false, true));

      return archive;
   }

   @Deployment(name = "dep2,2", testable = false, order = 1)
   public static AddonArchive getDeploymentDep2()
   {
      AddonArchive archive = ShrinkWrap
               .create(AddonArchive.class)
               .addBeansXML();

      return archive;
   }

   @Inject
   private AddonRegistry registry;

   @Inject
   private AddonRepository repository;

   @Test
   public void testHotSwap() throws Exception
   {
      AddonId depOneId = AddonId.from("dep1", "1");
      AddonId depTwoId = AddonId.from("dep2", "2");

      Addon depOne = registry.getAddon(depOneId);
      Addon depTwo = registry.getAddon(depTwoId);

      ClassLoader depOneClassloader = depOne.getClassLoader();
      ClassLoader depTwoClassloader = depTwo.getClassLoader();

      ((MutableAddonRepository) depTwo.getRepository()).disable(depTwoId);
      Addons.waitUntilStopped(depTwo, 10, TimeUnit.SECONDS);
      Addons.waitUntilStarted(depOne, 10, TimeUnit.SECONDS);

      Assert.assertNotNull(depOne.getClassLoader());
      Assert.assertNotEquals(depOneClassloader, depOne.getClassLoader());
      depOneClassloader = depOne.getClassLoader();

      ((MutableAddonRepository) repository).enable(depTwoId);
      Addons.waitUntilStarted(depTwo, 10, TimeUnit.SECONDS);
      Thread.sleep(1000);

      Assert.assertNotEquals(depOneClassloader, depOne.getClassLoader());
      Assert.assertNotEquals(depOneClassloader.toString(), depOne.getClassLoader().toString());
      Assert.assertNotEquals(depTwoClassloader, depTwo.getClassLoader());
      Assert.assertNotEquals(depTwoClassloader.toString(), depTwo.getClassLoader().toString());
   }

}