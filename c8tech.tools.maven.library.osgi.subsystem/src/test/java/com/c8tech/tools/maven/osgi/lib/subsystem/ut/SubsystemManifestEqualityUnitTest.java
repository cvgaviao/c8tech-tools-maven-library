/**
 * ==========================================================================
 * Copyright © 2015-2018 Cristiano Gavião, C8 Technology ME.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Cristiano Gavião (cvgaviao@c8tech.com.br)- initial API and implementation
 * ==========================================================================
 */
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.c8tech.tools.maven.osgi.lib.subsystem.ut;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * #%L
 * Library for OSGi Subsystem Archive Handling
 * %%
 * Copyright (C) 2012 - 2017 Cristiano Gavião, C8 Technology ME
 * %%
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * #L%
 */

import java.io.IOException;

import org.apache.aries.subsystem.core.archive.BundleSymbolicNameHeader;
import org.apache.aries.subsystem.core.archive.DeployedContentHeader;
import org.apache.aries.subsystem.core.archive.DynamicImportPackageHeader;
import org.apache.aries.subsystem.core.archive.ExportPackageHeader;
import org.apache.aries.subsystem.core.archive.FragmentHostHeader;
import org.apache.aries.subsystem.core.archive.ImportPackageHeader;
import org.apache.aries.subsystem.core.archive.PreferredProviderHeader;
import org.apache.aries.subsystem.core.archive.ProvideCapabilityHeader;
import org.apache.aries.subsystem.core.archive.ProvisionResourceHeader;
import org.apache.aries.subsystem.core.archive.RequireBundleHeader;
import org.apache.aries.subsystem.core.archive.RequireCapabilityHeader;
import org.apache.aries.subsystem.core.archive.SubsystemContentHeader;
import org.apache.aries.subsystem.core.archive.SubsystemExportServiceHeader;
import org.apache.aries.subsystem.core.archive.SubsystemImportServiceHeader;
import org.apache.aries.subsystem.core.archive.SubsystemManifest;
import org.apache.aries.subsystem.core.archive.SubsystemSymbolicNameHeader;
import org.apache.aries.subsystem.core.archive.SubsystemTypeHeader;
import org.apache.aries.subsystem.core.archive.SubsystemVersionHeader;
import org.apache.aries.subsystem.core.archive.SymbolicNameHeader;
import org.junit.jupiter.api.Test;

public class SubsystemManifestEqualityUnitTest {

    @Test
    public void testSubsystemContentEquality() {
        String headerStr = "org.aries.bundle;start-order:=0;type=osgi.bundle;version=\"1.8.4\";resolution:=mandatory";
        SubsystemContentHeader header1 = new SubsystemContentHeader(headerStr);
        SubsystemContentHeader header2 = new SubsystemContentHeader(headerStr);
        assertEquals(header1, header2);

        String headerStr1 = "org.aries.bundle;start-order:=0;type=osgi.bundle;version=\"1.8.4\";resolution:=mandatory";
        String headerStr2 = "org.aries.bundle;type=osgi.bundle;resolution:=mandatory;version=\"1.8.4\";start-order:=0";
        header1 = new SubsystemContentHeader(headerStr1);
        header2 = new SubsystemContentHeader(headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testDynamicImportHeaderEquality() {
        String headerStr1 = "org.eclipse.jetty.*;version=\"[9.0,10.0)\",*;JavaServlet=contract";
        String headerStr2 = "*;JavaServlet=contract,org.eclipse.jetty.*;version=\"[9.0,10.0)\"";
        DynamicImportPackageHeader header1 = new DynamicImportPackageHeader(
                headerStr1);
        DynamicImportPackageHeader header2 = new DynamicImportPackageHeader(
                headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testExportPackageHeaderEquality() {
        String headerStr1 = "javax.servlet;version=\"2.5\",javax.servlet.http; version=\"2.5\"";
        String headerStr2 = "javax.servlet.http; version=\"2.5\",javax.servlet;version=\"2.5\"";
        ExportPackageHeader header1 = new ExportPackageHeader(headerStr1);
        ExportPackageHeader header2 = new ExportPackageHeader(headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testFragmentHostHeaderEquality() {
        String headerStr1 = "the.parent.bundle;bundle-version=1.2.3";
        String headerStr2 = "the.parent.bundle;bundle-version=1.2.3";
        FragmentHostHeader header1 = new FragmentHostHeader(headerStr1);
        FragmentHostHeader header2 = new FragmentHostHeader(headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testImportPackageHeaderEquality() {
        String headerStr1 = "javax.servlet;version=\"2.6.0\", javax.servlet.resources;version=\"2.6.0\"";
        String headerStr2 = "javax.servlet.resources;version=\"2.6.0\",javax.servlet;version=\"2.6.0\"";
        ImportPackageHeader header1 = new ImportPackageHeader(headerStr1);
        ImportPackageHeader header2 = new ImportPackageHeader(headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testPreferredProviderHeaderEquality() {
        String headerStr1 = "org.aries.kernel;version=\"1.0.4\";type=osgi.subsystem.composite";
        String headerStr2 = "org.aries.kernel;type=osgi.subsystem.composite;version=\"1.0.4\"";
        PreferredProviderHeader header1 = new PreferredProviderHeader(
                headerStr1);
        PreferredProviderHeader header2 = new PreferredProviderHeader(
                headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testProvideCapabilityHeaderEquality() {
        String headerStr1 = "osgi.contract;osgi.contract=JavaServlet;version:Version=2.5;uses:=\"javax.servlet,javax.servlet.http\"";
        String headerStr2 = "osgi.contract;uses:=\"javax.servlet,javax.servlet.http\";osgi.contract=JavaServlet;version:Version=2.5";
        ProvideCapabilityHeader header1 = new ProvideCapabilityHeader(
                headerStr1);
        ProvideCapabilityHeader header2 = new ProvideCapabilityHeader(
                headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testProvisionResourceHeaderEquality() {
        String headerStr1 = "com.acme.logging;type=osgi.bundle;deployed-version=1.0.0";
        String headerStr2 = "com.acme.logging;deployed-version=1.0.0;type=osgi.bundle";
        ProvisionResourceHeader header1 = new ProvisionResourceHeader(
                headerStr1);
        ProvisionResourceHeader header2 = new ProvisionResourceHeader(
                headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testRequireBundleHeaderEquality() {
        String headerStr1 = "com.example.acme,com.acme.logging;bundle-version=\"[1.0, 1.1)\"";
        String headerStr2 = "com.acme.logging;bundle-version=\"[1.0, 1.1)\",com.example.acme";
        RequireBundleHeader header1 = new RequireBundleHeader(headerStr1);
        RequireBundleHeader header2 = new RequireBundleHeader(headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testRequireCapabilityHeaderEquality() {
        String headerStr1 = "osgi.ee; filter:=\"(osgi.ee=*)\",screen.size; filter:=\"(&(width>=800)(height>=600))\"";
        String headerStr2 = "screen.size; filter:=\"(&(width>=800)(height>=600))\",osgi.ee; filter:=\"(osgi.ee=*)\"";
        RequireCapabilityHeader header1 = new RequireCapabilityHeader(
                headerStr1);
        RequireCapabilityHeader header2 = new RequireCapabilityHeader(
                headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testSubsystemExportServiceHeaderEquality() {
        String headerStr1 = "com.acme.service.Logging";
        String headerStr2 = "com.acme.service.Logging";
        SubsystemExportServiceHeader header1 = new SubsystemExportServiceHeader(
                headerStr1);
        SubsystemExportServiceHeader header2 = new SubsystemExportServiceHeader(
                headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testSubsystemImportServiceHeaderEquality() {
        String headerStr1 = "com.acme.service.Logging";
        String headerStr2 = "com.acme.service.Logging";
        SubsystemImportServiceHeader header1 = new SubsystemImportServiceHeader(
                headerStr1);
        SubsystemImportServiceHeader header2 = new SubsystemImportServiceHeader(
                headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testSubsystemManifestEquality() throws IOException {

        SubsystemManifest subsystemManifest1 = new SubsystemManifest(
                getClass().getResourceAsStream("/files/SUBSYSTEM.MF.1"));
        SubsystemManifest subsystemManifest2 = new SubsystemManifest(
                getClass().getResourceAsStream("/files/SUBSYSTEM.MF.2"));
        assertEquals(subsystemManifest1, subsystemManifest2);
    }

    @Test
    public void testSubsystemTypeHeaderEquality() {
        String headerStr1 = "osgi.subsystem.composite";
        String headerStr2 = "osgi.subsystem.composite";
        SubsystemTypeHeader header1 = new SubsystemTypeHeader(headerStr1);
        SubsystemTypeHeader header2 = new SubsystemTypeHeader(headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testSubsystemVersionHeaderEquality() {
        String headerStr1 = "1.0.0";
        String headerStr2 = "1.0.0";
        SubsystemVersionHeader header1 = new SubsystemVersionHeader(headerStr1);
        SubsystemVersionHeader header2 = new SubsystemVersionHeader(headerStr2);
        assertEquals(header1, header2);

        headerStr2 = "1";
        header2 = new SubsystemVersionHeader(headerStr2);
        assertEquals(header1, header2, "Equivalent versions should be equal");
    }

    @Test
    public void testBundleSymbolicNameHeaderEquality() {
        String headerStr1 = "com.example.acme;singleton:=true";
        String headerStr2 = "com.example.acme;singleton:=true";
        SymbolicNameHeader header1 = new BundleSymbolicNameHeader(headerStr1);
        SymbolicNameHeader header2 = new BundleSymbolicNameHeader(headerStr2);
        assertEquals(header1, header2);

        headerStr1 = "com.example.acme;fragment-attachment:=never;singleton:=true";
        headerStr2 = "com.example.acme;singleton:=true;fragment-attachment:=never";
        header1 = new BundleSymbolicNameHeader(headerStr1);
        header2 = new BundleSymbolicNameHeader(headerStr2);
        assertEquals(header1, header2, "Equivalent clauses should be equal");
    }

    @Test
    public void testSubsystemSymbolicNameHeaderEquality() {
        String headerStr1 = "org.acme.billing;category=banking";
        String headerStr2 = "org.acme.billing;category=banking";
        SymbolicNameHeader header1 = new SubsystemSymbolicNameHeader(
                headerStr1);
        SymbolicNameHeader header2 = new SubsystemSymbolicNameHeader(
                headerStr2);
        assertEquals(header1, header2);
    }

    @Test
    public void testDeployedContentHeaderEquality() {
        String headerStr1 = "com.acme.logging;type=osgi.bundle;deployed-version=1.0.0";
        String headerStr2 = "com.acme.logging;type=osgi.bundle;deployed-version=1.0.0";
        DeployedContentHeader header1 = new DeployedContentHeader(headerStr1);
        DeployedContentHeader header2 = new DeployedContentHeader(headerStr2);
        assertEquals(header1, header2);
    }
}
