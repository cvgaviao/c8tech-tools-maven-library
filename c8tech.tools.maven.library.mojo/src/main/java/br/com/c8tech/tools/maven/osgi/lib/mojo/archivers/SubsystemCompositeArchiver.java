/**
 * ======================================================================
 * Copyright © 2015-2019, Cristiano V. Gavião.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * =======================================================================
 */
package br.com.c8tech.tools.maven.osgi.lib.mojo.archivers;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.archiver.Archiver;

import br.com.c8tech.tools.maven.osgi.lib.mojo.CommonMojoConstants;

/**
 * 
 * @author Cristiano Gavião
 * 
 */
@Named(CommonMojoConstants.OSGI_SUBSYSTEM_PACKAGING_COMPOSITE)
@Typed(Archiver.class)
@Singleton
public class SubsystemCompositeArchiver extends AbstractSubsystemArchiver {
}
