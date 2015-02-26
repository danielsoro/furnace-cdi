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
public class AddonHotSwapTest
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
               .addAsAddonDependencies(AddonDependencyEntry.create("dep2", "2"));

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

      ((MutableAddonRepository) repository).disable(depTwoId);
      Addons.waitUntilStopped(depOne, 10, TimeUnit.SECONDS);

      ((MutableAddonRepository) repository).enable(depTwoId);
      Addons.waitUntilStarted(depOne, 10, TimeUnit.SECONDS);

      /*
       * Verify existing references are updated.
       */
      Assert.assertNotEquals(depOneClassloader, depOne.getClassLoader());
      Assert.assertNotEquals(depOneClassloader.toString(), depOne.getClassLoader().toString());
      Assert.assertNotEquals(depTwoClassloader, depTwo.getClassLoader());
      Assert.assertNotEquals(depTwoClassloader.toString(), depTwo.getClassLoader().toString());

      /*
       * Now retrieving fresh references.
       */
      Assert.assertNotEquals(depOneClassloader, registry.getAddon(depOneId).getClassLoader());
      Assert.assertNotEquals(depOneClassloader.toString(), registry.getAddon(depOneId).getClassLoader()
               .toString());
      Assert.assertNotEquals(depTwoClassloader, registry.getAddon(depTwoId).getClassLoader());
      Assert.assertNotEquals(depTwoClassloader.toString(), registry.getAddon(depTwoId).getClassLoader()
               .toString());
   }

}