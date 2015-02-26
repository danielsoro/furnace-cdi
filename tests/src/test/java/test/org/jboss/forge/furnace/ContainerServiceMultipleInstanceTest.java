/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.jboss.forge.furnace;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDeployment;
import org.jboss.forge.arquillian.AddonDeployments;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.org.jboss.forge.furnace.mocks.AbstractImplementation;
import test.org.jboss.forge.furnace.mocks.ExportedInterface;
import test.org.jboss.forge.furnace.mocks.ImplementingClass1;
import test.org.jboss.forge.furnace.mocks.ImplementingClass2;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Ignore
@RunWith(Arquillian.class)
public class ContainerServiceMultipleInstanceTest
{
   @Deployment
   @AddonDeployments({
            @AddonDeployment(name = "org.jboss.forge.furnace.container:cdi")
   })
   public static AddonArchive getDeployment()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addClasses(
                        AbstractImplementation.class,
                        ExportedInterface.class,
                        ImplementingClass1.class,
                        ImplementingClass2.class
               )
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
               )
               .addBeansXML();

      return archive;
   }

   @Inject
   private Instance<ExportedInterface> instanceInterfaceInstance;

   @Test
   public void testRegisteredServices()
   {
      Assert.assertNotNull(instanceInterfaceInstance.get());
      for (ExportedInterface instance : instanceInterfaceInstance)
      {
         Assert.assertNotNull(instance);
      }
      Instance<ImplementingClass1> implementation = instanceInterfaceInstance.select(ImplementingClass1.class);
      Assert.assertTrue(implementation.get() instanceof AbstractImplementation);
   }
}