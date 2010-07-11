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

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.sun.faban.driver.BenchmarkDriver;

/**
 * Supporting class for a quick experiment in making Faban more configurable by
 * external system properties and Guice Modules. This interface is ugly, gets
 * the job done and is subject to change.
 */
public class GuiceContext {
    protected static volatile Injector instance;

    public static boolean isGuiceEnabled() {
        return Boolean.valueOf(System.getProperty("guice.enabled"));
    }

    public static void configure(Module... modules) {
        instance = Guice.createInjector(modules);
    }

    public static <T> T getInstance(Class<T> clazz) {
        return instance.getInstance(clazz);
    }

    public static Object getDriverInstance() {
        return instance
                .getBinding(Key.get(Object.class, BenchmarkDriver.class))
                .getProvider().get();
    }

    public static String getNamedProperty(String name) {
        String prop = System.getProperty(name);
        if (prop != null) {
            return prop;
        }

        return instance.getInstance(Key.get(String.class, Names.named(name)));
    }

    public static String getNamedProperty(String name, String defaultValue) {
        String prop = System.getProperty(name);

        if (prop != null) {
            return prop;
        }

        try {
            return instance.getInstance(Key
                    .get(String.class, Names.named(name)));
        } catch (ConfigurationException e) {
            return defaultValue;
        }
    }
}
