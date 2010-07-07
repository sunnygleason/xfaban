/* 
 * The contents of this file are subject to the terms
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
 * $Id: MasterState.java,v 1.2.6.1 2009/09/21 05:01:21 akara Exp $
 *
 * Copyright 2005-2009 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.faban.driver.engine;


/**
 * MasterState allows querying the state of the master.
 *
 * @author Akara Sucharitakul
 */
public enum MasterState {

    /** Not Started. */
    NOT_STARTED,

    /** Configuring. */
    CONFIGURING,

    /** Starting. */
    STARTING,

    /** Ramp up. */
    RAMPUP,

    /** Steady State. */
    STEADYSTATE,

    /** Ramp down. */
    RAMPDOWN,

    /** Results. */
    RESULTS,

    /** Aborted. */
    ABORTED   
}