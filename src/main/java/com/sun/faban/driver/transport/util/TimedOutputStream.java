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
 * $Id: TimedOutputStream.java,v 1.7.2.2 2009/09/23 21:48:53 akara Exp $
 *
 * Copyright 2005-2009 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.faban.driver.transport.util;

import com.sun.faban.driver.engine.DriverContext;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * A pass-through output stream that records the time of the output.
 * Note that the client-side time recording for writes is
 * always before the write happens.
 *
 * @author Akara Sucharitakul
 */
public class TimedOutputStream extends FilterOutputStream {

    DriverContext ctx;

    /**
     * Creates an output stream filter built on top of the specified
     * underlying output stream.
     *
     * @param out the underlying output stream to be assigned to
     *            the field <tt>this.out</tt> for later use, or
     *            <code>null</code> if this instance is to be
     *            created without an underlying stream.
     */
    public TimedOutputStream(OutputStream out) {
        super(out);
        ctx = DriverContext.getContext();
    }

    /**
     * Writes the specified <code>byte</code> to this output stream.
     * <p/>
     * The <code>write</code> method of <code>FilterOutputStream</code>
     * calls the <code>write</code> method of its underlying output stream,
     * that is, it performs <tt>out.write(b)</tt>.
     * <p/>
     * Implements the abstract <tt>write</tt> method of <tt>OutputStream</tt>.
     *
     * @param b the <code>byte</code>.
     * @throws IOException {@link IOException} if an I/O error occurs.
     */
    @Override
	public void write(int b) throws IOException {
        if (ctx != null)
            ctx.recordStartTime();
        super.write(b);
    }

    /**
     * Writes <code>b.length</code> bytes to this output stream.
     * <p/>
     * The <code>write</code> method of <code>FilterOutputStream</code>
     * calls its <code>write</code> method of three arguments with the
     * arguments <code>b</code>, <code>0</code>, and
     * <code>b.length</code>.
     * <p/>
     * Note that this method does not call the one-argument
     * <code>write</code> method of its underlying stream with the single
     * argument <code>b</code>.
     *
     * @param b the data to be written.
     * @throws IOException {{@link IOException} if an I/O error occurs.
     * @see java.io.FilterOutputStream#write(byte[], int, int)
     */
    @Override
	public void write(byte b[]) throws IOException {
        if (ctx != null && b.length > 0)
            ctx.recordStartTime();
        super.write(b);
    }

    /**
     * Writes <code>len</code> bytes from the specified
     * <code>byte</code> array starting at offset <code>off</code> to
     * this output stream.
     * <p/>
     * As suggested by the FilterOutputStream superclass, this method
     * implements as more efficient task by calling the underlying output
     * stream directly.
     * @param b The bytes to write
     * @param off The offset into the byte array
     * @param len The length to write
     * @throws IOException If there is an error writing
     */
    @Override
	public void write(byte b[], int off, int len) throws IOException {
        if (ctx != null && b.length > 0 && len > 0)
            ctx.recordStartTime();
        out.write(b, off, len);
    }
}
