/**
 * ======================================================================
 * Copyright © 2015-2019, OSGi Alliance, Cristiano V. Gavião.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * =======================================================================
 */
package org.osgi.service.indexer.osgi;

import java.io.PrintStream;
import java.util.Date;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

class LogTracker extends ServiceTracker<LogService, LogService>
        implements LogService {

    public LogTracker(BundleContext context) {
        super(context, LogService.class.getName(), null);
    }

    @Override
    public void log(int level, String message) {
        log(null, level, message, null);
    }

    @Override
    public void log(int level, String message, Throwable exception) {
        log(null, level, message, exception);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void log(ServiceReference sr, int level, String message) {
        log(sr, level, message, null);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void log(ServiceReference sr, int level, String message,
            Throwable exception) {
        LogService log = getService();

        if (log != null)
            log.log(sr, level, message, exception);
        else {
            PrintStream stream = (level <= LogService.LOG_WARNING) ? System.err : System.out;//NOSONAR
            if (message == null)
                message = "";
            Date now = new Date();
            stream.println(String.format("[%-7s] %tF %tT: %s",
                    LogUtils.formatLogLevel(level), now, now, message));
            if (exception != null)
                exception.printStackTrace(stream);
        }
    }

}
