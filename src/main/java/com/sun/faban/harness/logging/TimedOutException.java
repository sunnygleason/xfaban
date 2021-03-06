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
 * $Id: TimedOutException.java,v 1.2.8.1 2009/09/21 05:02:14 akara Exp $
 *
 * Copyright 2005-2009 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.faban.harness.logging;

/**
 * Signals a time out. Generally this exception is used on reading
 * request headers and data.
 *
 * @author Akara Sucharitakul
 */
public class TimedOutException extends Exception {

    /**
     * Constructs a new com.sun.faban.harness.logging.TimedOutException with <code>null</code>
     * as its detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     */
    public TimedOutException() {
    }

    /**
     * Constructs a new com.sun.faban.harness.logging.TimedOutException with the specified detail
     * message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public TimedOutException(String message) {
        super(message);
    }

    /**
     * Constructs a new com.sun.faban.harness.logging.TimedOutException with the specified
     * detail message and cause.  <p>Note that the detail message associated
     * with * <code>cause</code> is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since  1.4
     */
    public TimedOutException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new com.sun.faban.harness.logging.TimedOutException with the specified cause
     *  and a detail message of
     * <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * java.security.PrivilegedActionException}).
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since  1.4
     */
    public TimedOutException(Throwable cause) {
        super(cause);
    }
}
