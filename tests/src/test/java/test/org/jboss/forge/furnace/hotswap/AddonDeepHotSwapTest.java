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
public class AddonDeepHotSwapTest
{
   @Deployment(order = 5)
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
               .addBeansXML()
               .addAsAddonDependencies(AddonDependencyEntry.create("dep3", "3"));

      return archive;
   }

   @Deployment(name = "dep3,3", testable = false, order = 3)
   public static AddonArchive getDeploymentDep3()
   {
      AddonArchive archive = ShrinkWrap
               .create(AddonArchive.class)
               .addBeansXML()
               .addAsAddonDependencies(AddonDependencyEntry.create("dep4", "4"));

      return archive;
   }

   @Deployment(name = "dep4,4", testable = false, order = 4)
   public static AddonArchive getDeploymentDep4()
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
      AddonId dep1Id = AddonId.from("dep1", "1");
      AddonId dep2Id = AddonId.from("dep2", "2");
      AddonId dep3Id = AddonId.from("dep3", "3");
      AddonId dep4Id = AddonId.from("dep4", "4");

      Addon dep1 = registry.getAddon(dep1Id);
      Addon dep2 = registry.getAddon(dep2Id);
      Addon dep3 = registry.getAddon(dep3Id);
      Addon dep4 = registry.getAddon(dep4Id);

      ClassLoader dep1Classloader = dep1.getClassLoader();
      ClassLoader dep2Classloader = dep2.getClassLoader();
      ClassLoader dep3Classloader = dep3.getClassLoader();
      ClassLoader dep4Classloader = dep4.getClassLoader();

      Addons.waitUntilStarted(dep1, 10, TimeUnit.SECONDS);
      Addons.waitUntilStarted(dep2, 10, TimeUnit.SECONDS);
      Addons.waitUntilStarted(dep3, 10, TimeUnit.SECONDS);
      Addons.waitUntilStarted(dep4, 10, TimeUnit.SECONDS);

      ((MutableAddonRepository) repository).disable(dep4Id);
      Addons.waitUntilStopped(dep1, 10, TimeUnit.SECONDS);

      ((MutableAddonRepository) repository).enable(dep4Id);
      Addons.waitUntilStarted(dep1, 10, TimeUnit.SECONDS);

      /*
       * Verify existing references are updated.
       */
      Assert.assertNotEquals(dep1Classloader, dep1.getClassLoader());
      Assert.assertNotEquals(dep1Classloader.toString(), dep1.getClassLoader().toString());
      Assert.assertNotEquals(dep2Classloader, dep2.getClassLoader());
      Assert.assertNotEquals(dep2Classloader.toString(), dep2.getClassLoader().toString());

      Assert.assertNotEquals(dep3Classloader, dep3.getClassLoader());
      Assert.assertNotEquals(dep3Classloader.toString(), dep3.getClassLoader().toString());
      Assert.assertNotEquals(dep4Classloader, dep4.getClassLoader());
      Assert.assertNotEquals(dep4Classloader.toString(), dep4.getClassLoader().toString());

      /*
       * Now retrieving fresh references.
       */
      Assert.assertNotEquals(dep1Classloader, registry.getAddon(dep1Id).getClassLoader());
      Assert.assertNotEquals(dep1Classloader.toString(), registry.getAddon(dep1Id).getClassLoader()
               .toString());
      Assert.assertNotEquals(dep2Classloader, registry.getAddon(dep2Id).getClassLoader());
      Assert.assertNotEquals(dep2Classloader.toString(), registry.getAddon(dep2Id).getClassLoader()
               .toString());

      Assert.assertNotEquals(dep3Classloader, registry.getAddon(dep3Id).getClassLoader());
      Assert.assertNotEquals(dep3Classloader.toString(), registry.getAddon(dep3Id).getClassLoader()
               .toString());
      Assert.assertNotEquals(dep4Classloader, registry.getAddon(dep4Id).getClassLoader());
      Assert.assertNotEquals(dep4Classloader.toString(), registry.getAddon(dep4Id).getClassLoader()
               .toString());
   }

}