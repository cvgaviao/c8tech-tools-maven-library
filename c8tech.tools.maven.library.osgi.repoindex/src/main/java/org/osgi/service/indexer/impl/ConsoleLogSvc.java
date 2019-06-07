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
package org.osgi.service.indexer.impl;

/*
 * Part of this code was borrowed from BIndex project (https://github.com/osgi/bindex) 
 * and it is released under OSGi Specification License, VERSION 2.0
 */
import java.io.PrintStream;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

@SuppressWarnings("rawtypes")
public class ConsoleLogSvc implements LogService {

    @Override
    public void log(int level, String message) {
        log(null, level, message, null);
    }

    @Override
    public void log(int level, String message, Throwable exception) {
        log(null, level, message, exception);
    }

    @Override
    public void log(ServiceReference sr, int level, String message) {
        log(sr, level, message, null);
    }

    @Override
    public void log(ServiceReference sr, int level, String message,
            Throwable exception) {
        PrintStream out = level <= LOG_WARNING ? System.err : System.out; //NOSONAR

        StringBuilder builder = new StringBuilder();
        switch (level) {
        case LOG_DEBUG:
            builder.append("DEBUG");
            break;
        case LOG_INFO:
            builder.append("INFO");
            break;
        case LOG_WARNING:
            builder.append("WARNING");
            break;
        case LOG_ERROR:
            builder.append("ERROR");
            break;
        default:
            builder.append("<<unknown>>");
        }
        builder.append(": ");
        builder.append(message);

        if (exception != null) {
            builder.append(" [");
            builder.append(exception.getLocalizedMessage());
            builder.append("]");
        }

        out.println(builder.toString());

        if (exception != null)
            exception.printStackTrace(out);
    }

}
