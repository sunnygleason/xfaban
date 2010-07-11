/* The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html or
 * install_dir/legal/LICENSE
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at install_dir/legal/LICENSE.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.faban.driver.engine;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.sun.faban.driver.BenchmarkDriver;
import com.sun.faban.driver.ConfigurationException;
import com.sun.faban.driver.DefinitionException;

/**
 * Enables configuration via a class reference.
 */
public class GuiceBenchmarkDefinition extends BenchmarkDefinition {
    private static final long serialVersionUID = 1L;

    static BenchmarkDefinition read(Class<?> defClass)
            throws DefinitionException, ConfigurationException {
        BenchmarkDefinition def = new BenchmarkDefinition();
        String className = def.getClass().getName();
        Logger logger = Logger.getLogger(className);
        String defClassName = defClass.getName();

        if (!defClass
                .isAnnotationPresent(com.sun.faban.driver.BenchmarkDefinition.class)) {
            String msg = "Class " + defClassName
                    + " is not a benchmark definition.";
            logger.severe(msg);
            throw new ConfigurationException(msg);
        }

        com.sun.faban.driver.BenchmarkDefinition benchDefAnnotation = defClass
                .getAnnotation(com.sun.faban.driver.BenchmarkDefinition.class);

        def.name = benchDefAnnotation.name();
        def.version = benchDefAnnotation.version();
        def.runControl = benchDefAnnotation.runControl();
        def.metric = benchDefAnnotation.metric();
        def.scaleName = benchDefAnnotation.scaleName();
        def.scaleUnit = benchDefAnnotation.scaleUnit();
        def.configPrecedence = benchDefAnnotation.configPrecedence();

        ArrayList<Class<?>> driverClassList = new ArrayList<Class<?>>();

        // Get all the driver classes
        for (Class<?> driverClass : benchDefAnnotation.drivers()) {
            if (driverClass != Object.class
                    && driverClass.isAnnotationPresent(BenchmarkDriver.class)) {
                driverClassList.add(driverClass);
            }
        }

        // If defClass is not in list and is a driver, prepend
        if (driverClassList.indexOf(defClass) < 0
                && defClass.isAnnotationPresent(BenchmarkDriver.class)) {
            driverClassList.add(0, defClass);
        }

        // Check that we have at least one driver
        if (driverClassList.size() <= 0) {
            String msg = "No driver classes found";
            logger.severe(msg);
            throw new DefinitionException(msg);
        }

        // Transfer the classes to an array
        Class<?>[] driverClasses = new Class<?>[driverClassList.size()];
        driverClasses = driverClassList.toArray(driverClasses);

        def.drivers = new Driver[driverClasses.length];

        // Obtain all driver and driver class names
        for (int i = 0; i < driverClasses.length; i++) {
            BenchmarkDriver benchDriver = driverClasses[i]
                    .getAnnotation(BenchmarkDriver.class);
            def.drivers[i] = new Driver();
            def.drivers[i].name = benchDriver.name();
            def.drivers[i].metric = benchDriver.metric();
            def.drivers[i].opsUnit = benchDriver.opsUnit();
            def.drivers[i].threadPerScale = benchDriver.threadPerScale();
            def.drivers[i].responseTimeUnit = benchDriver.responseTimeUnit();
            if (def.drivers[i].responseTimeUnit.equals(TimeUnit.NANOSECONDS))
                throw new DefinitionException("@BenchmarkDriver "
                        + "responseTimeUnit must not be NANOSECONDS");
            def.drivers[i].className = driverClasses[i].getName();
            populatePrePost(driverClasses[i], def.drivers[i]);
            getBackground(driverClasses[i], def.drivers[i]);
            def.drivers[i].mix[0] = Mix.getMix(driverClasses[i]);
            def.drivers[i].initialDelay[0] = getInitialDelay(driverClasses[i]);
            int totalOps = def.drivers[i].mix[0].operations.length;
            if (def.drivers[i].mix[1] != null) {
                totalOps += def.drivers[i].mix[1].operations.length;
            }

            // Copy operation references into a flat array.
            def.drivers[i].operations = new BenchmarkDefinition.Operation[totalOps];
            for (int j = 0; j < def.drivers[i].mix[0].operations.length; j++) {
                def.drivers[i].operations[j] = def.drivers[i].mix[0].operations[j];
            }
            if (def.drivers[i].mix[1] != null) {
                for (int j = 0; j < def.drivers[i].mix[1].operations.length; j++) {
                    def.drivers[i].operations[j
                            + def.drivers[i].mix[0].operations.length] = def.drivers[i].mix[1].operations[j];
                }
            }

            def.drivers[i].driverClass = driverClasses[i];
        }
        return def;
    }
}
