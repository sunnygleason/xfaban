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
 * $Id: CycleType.java,v 1.2.8.2 2009/10/05 22:51:41 akara Exp $
 *
 * Copyright 2005-2009 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.faban.driver;

/**
 * CycleType determines whether the cycleTime is to be interpreted as
 * think time or arrival time.
 */
public enum CycleType {

    /**
     * Think time defines the time between the end of the previous operation
     * and the begin of the next operation.
     */
    THINKTIME,

    /**
     * Inter-arrival time defines time between the begin of the previous operation
     * and the begin of the next operation.
     */
    CYCLETIME
}
