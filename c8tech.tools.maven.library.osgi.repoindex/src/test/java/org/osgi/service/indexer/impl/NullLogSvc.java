/**
 * ==========================================================================
 * Copyright © 2015-2018 OSGi Alliance, Cristiano Gavião.
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
package org.osgi.service.indexer.impl;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

@SuppressWarnings("rawtypes")
public class NullLogSvc implements LogService {

    @Override
    public void log(int level, String message) {
    }

    @Override
    public void log(int level, String message, Throwable exception) {
    }

    @Override
    public void log(ServiceReference sr, int level, String message) {
    }

    @Override
    public void log(ServiceReference sr, int level, String message,
            Throwable exception) {
    }

}
