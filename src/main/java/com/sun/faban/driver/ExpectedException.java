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
 * $Id: ExpectedException.java,v 1.4.6.1 2009/09/21 05:01:25 akara Exp $
 *
 * Copyright 2005-2009 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.faban.driver;

/**
 * An ExpectedException, signifies an exception condition which is expected.
 * Such conditions are only logged at the FINER level in order not to clutter
 * up the logs too much. The operation throwing the exception is still
 * considered as a failed operation and will count towards those failed
 * operation count.
 *
 * @author Akara Sucharitakul
 */
public class ExpectedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private boolean logged = false;

    /**
     * Constructs a new expected exception with <code>null</code> as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public ExpectedException() {
    	super();
    }

    /**
     * Constructs a new expected exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public ExpectedException(String message) {
        super(message);
    }

    /**
     * Constructs a new Expected exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this Expected exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public ExpectedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new expected exception with the specified cause and a
     * detail message of <tt>(cause==null ? null : cause.toString())</tt>
     * (which typically contains the class and detail message of
     * <tt>cause</tt>).  This constructor is useful for expected exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public ExpectedException(Throwable cause) {
        super(cause);
    }

    /**
     * Sets the flag indicating this exception has already been logged.
     * This is used to avoid duplicate logging.
     */
    public void setLogged() {
        logged = true;
    }

    /**
     * Checks whether this exception has already been logged.
     * @return true if this exception was already logged, false otherwise.
     */
    public boolean wasLogged() {
        return logged;
    }
}
