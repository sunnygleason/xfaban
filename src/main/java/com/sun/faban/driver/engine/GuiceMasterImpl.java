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

import java.io.File;
import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Module;
import com.sun.faban.driver.engine.RunInfo.DriverConfig;
import com.sun.faban.driver.util.Timer;

/**
 * A Guice-Enabled Faban Master Impl.
 */
public class GuiceMasterImpl extends MasterImpl implements Runnable {
    private static final long serialVersionUID = 1L;
    private volatile boolean isAlreadyStarted = false;

    /**
     * Creates and exports a new Master.
     * 
     * @throws RemoteException
     *             if failed to export object
     */
    protected GuiceMasterImpl() throws RemoteException {
        super();
        System.setProperty("guice.enabled", "true");
    }

    @Override
    public void run() {
        try {
            runBenchmark();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs the benchmark from begin to end.
     * 
     * @throws Exception
     *             Any error that had occurred during the run.
     */
    @SuppressWarnings("unchecked")
    public void runBenchmark() throws Exception {
        synchronized (this) {
            if (this.isAlreadyStarted) {
                throw new IllegalStateException("MasterImpl already started!");
            }

            this.isAlreadyStarted = true;
        }

        Class<Module> moduleClass = (Class<Module>) Class.forName(GuiceContext
                .getNamedProperty("driverModule"));

        Module module = moduleClass.newInstance();
        GuiceContext.configure(module);

        Class<?> driverClass = GuiceContext.getDriverInstance().getClass();

        benchDef = GuiceBenchmarkDefinition.read(driverClass);
        runInfo = new RunInfo();

        runInfo.runtimeStatsEnabled = Boolean.valueOf(GuiceContext
                .getNamedProperty("runtimeStatsEnabled", "true"));
        runInfo.runtimeStatsInterval = Integer.parseInt(GuiceContext
                .getNamedProperty("graphInterval", "1"));
        runInfo.graphInterval = Integer.parseInt(GuiceContext.getNamedProperty(
                "graphInterval", "1"));

        runInfo.resultsDir = GuiceContext.getNamedProperty("outputDir");

        runInfo.rampUp = Integer.parseInt(GuiceContext
                .getNamedProperty("rampUp"));
        runInfo.stdyState = Integer.parseInt(GuiceContext
                .getNamedProperty("steadyState"));
        runInfo.rampDown = Integer.parseInt(GuiceContext
                .getNamedProperty("rampDown"));
        runInfo.msBetweenThreadStart = Integer.parseInt(GuiceContext
                .getNamedProperty("msBetweenThreadStart", "0"));

        runInfo.simultaneousStart = Boolean.valueOf(GuiceContext
                .getNamedProperty("simultaneousStart", "true"));
        runInfo.parallelAgentThreadStart = Boolean.valueOf(GuiceContext
                .getNamedProperty("parallelAgentThreadStart", "true"));

        runInfo.driverConfigs = new DriverConfig[benchDef.drivers.length];
        for (int i = 0; i < benchDef.drivers.length; i++) {
            DriverConfig driverConfig = new DriverConfig(benchDef.drivers[i]);
            driverConfig.numThreads = Integer.parseInt(GuiceContext
                    .getNamedProperty("threads", "1"));
            driverConfig.runControl = benchDef.runControl;
            driverConfig.graphInterval = runInfo.graphInterval;
            driverConfig.mix[0].normalize();
            runInfo.driverConfigs[i] = driverConfig;
        }

        // When run form the harness, the outputdir may be the runId.
        // In that case the faban.outputdir.unique property must be set to true.
        boolean uniqueDir = false;
        String uniqueDirString = System.getProperty("faban.outputdir.unique");
        if (uniqueDirString != null) {
            uniqueDir = RunInfo.ConfigurationReader
                    .relaxedParseBoolean(uniqueDirString);
        }

        if (uniqueDir) {
            // Ensure separator is not at end.
            if (runInfo.resultsDir.endsWith(fs)) {
                runInfo.resultsDir = runInfo.resultsDir.substring(0,
                        runInfo.resultsDir.length() - fs.length());
            }

            // Then take the innermost directory name.
            int idx = runInfo.resultsDir.lastIndexOf(fs);
            ++idx;
            runInfo.runId = runInfo.resultsDir.substring(idx);
        } else {
            // Gets the ID for this run from the sequence file.
            try {
                runInfo.runId = getRunID(true);
            } catch (Exception e) {
                logger.severe("Cannot read the run id");
                logger.throwing(className, "<init>", e);
                throw e;
            }
        }
        logger.info("RunID for this run is : " + runInfo.runId);

        String runOutputDir = runInfo.resultsDir;
        if (!uniqueDir) {
            runOutputDir = runInfo.resultsDir + fs + runInfo.runId;
        }

        // make a new directory for the run.
        File runDirFile = null;
        runDirFile = new File(runOutputDir);
        if (!runDirFile.exists()) {
            if (!runDirFile.mkdirs()) {
                throw new IOException("Could not create the new "
                        + "Run Directory: " + runOutputDir);
            }
        }

        logger.info("Output directory for this run is : " + runOutputDir);
        runInfo.resultsDir = runOutputDir;

        configureLogger(runOutputDir);

        timer = new Timer();

        agentRefs = new Agent[benchDef.drivers.length][];
        agentThreads = new int[benchDef.drivers.length];
        remainderThreads = new int[benchDef.drivers.length];

        scheduler = new java.util.Timer("Scheduler", false);
        try {
            int agentCnt = configure();
            if (agentCnt > 0) {
                for (int i = 0; i < benchDef.drivers.length && !runAborted; i++) {
                    configureAgents(i);
                }
                for (int i = 0; i < benchDef.drivers.length && !runAborted; i++) {
                    startThreads(i);
                }
                logger.config("Detected " + agentCnt + " Remote Agents.");
            } else {
                configureLocal();
            }
        } catch (ConnectException e) {
            configureLocal();
        } catch (NotBoundException e) {
            configureLocal();
        } catch (RemoteException e) {
            Throwable t = e.getCause();
            Throwable tt;
            while ((tt = t.getCause()) != null) {
                t = tt;
            }
            logger.log(Level.WARNING, "Error acccessing registry or agent!", t);
            configureLocal();
        }
        changeState(MasterState.STARTING);
        super.executeRun();
    }

    /**
     * The main method to start the master. No arguments are required. The
     * -noexit argument will cause the master to wait. The only actual
     * expectation is the benchmark.properties property pointing to the
     * properties file.
     * 
     * @param args
     *            The command line arguments are ignored.
     */
    public static void main(String[] args) {
        // Check whether -noexit is set.
        boolean normalExit = true;
        for (String arg : args) {
            if ("-noexit".equals(arg)) {
                normalExit = false;
                break;
            }
        }
        GuiceMasterImpl m = null;
        try {
            m = new GuiceMasterImpl();
        } catch (RemoteException e) {
            // We have no master so we have no logger, create a new one
            // for logging this message.
            Logger logger = Logger.getLogger(Master.class.getName());
            logger.log(Level.SEVERE, "Cannot initialize remote object, "
                    + "stubs may not be generated properly.", e);
            System.exit(1);
        }
        try {
            m.runBenchmark();
            if (normalExit) {
                System.exit(0);
            }
        } catch (Throwable t) {
            m.logger.log(Level.SEVERE, "Master terminated with errors.", t);
            System.exit(1);
        }
    }
}
