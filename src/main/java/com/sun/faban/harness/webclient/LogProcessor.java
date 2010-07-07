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
 * $Id: Metrics.java,v 1.26 2008/06/17 20:43:58 akara Exp $
 *
 * Copyright 2005-2009 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.faban.harness.webclient;

import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * A command line utility to process large log files into some text
 * format that we can grep or awk on. This class really has no other
 * functionality in Faban and is subject to removal or replaced
 * by options of the fabancli.
 *
 * @author Akara Sucharitakul
 */
public class LogProcessor {

    /**
     * Runs the log processor.
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        LogOutputHandler handler = new LogOutputHandler(
                new PrintWriter(System.out));
        InputStream logInput = System.in;
        try {
            SAXParserFactory sFact = SAXParserFactory.newInstance();
            sFact.setFeature("http://xml.org/sax/features/validation", false);
            sFact.setFeature("http://apache.org/xml/features/" +
                    "allow-java-encodings", true);
            sFact.setFeature("http://apache.org/xml/features/nonvalidating/" +
                    "load-dtd-grammar", false);
            sFact.setFeature("http://apache.org/xml/features/nonvalidating/" +
                    "load-external-dtd", false);
            SAXParser parser = sFact.newSAXParser();
            parser.parse(logInput, handler);
            handler.xmlComplete = true; // If we get here, the XML is good.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class LogOutputHandler extends LogParseHandler
            implements XMLInputStream.EOFListener {

        private PrintWriter out;

        LogRecordDetail detail = new LogRecordDetail();
        ExceptionRecord exception = new ExceptionRecord();
        StackFrame frame = new StackFrame();
        ArrayList stackFrames = new ArrayList();

        LogOutputHandler(PrintWriter out) {
            super(null, null, null);
            this.out = out;
        }

        private void flush() {
            out.flush();
        }

        /**
         * The processRecord method allows subclasses to define
         * how a record should be processed.
         *
         * @throws org.xml.sax.SAXException If the processing should stop.
         */
        public void processRecord() throws SAXException {
                printRecord(logRecord);
        }

        /**
         * Formats a multi-line message into html line breaks
         * for readability.
         *
         * @param message The message to be formatted.
         * @return The new formatted message.
         */
        @Override String formatMessage(String message) {
            int idx = message.indexOf("<br>");
            if (idx == -1) // If there's no <br>, don't even hassle.
                return message;
            StringBuffer msg = new StringBuffer(message);
            String crlf = "\n";
            while (idx != -1) {
                msg.replace(idx, idx + 4, crlf);
                idx = msg.indexOf("<br>", idx + crlf.length());
            }
            return msg.toString();
        }



        /**
         * The processDetail method allows subclasses to process
         * the exceptions not processed by default. This is called
         * from endElement.
         *
         * @param qName The element qName
         * @throws org.xml.sax.SAXException If the processing should stop.
         */
        public void processDetail(String qName) throws SAXException {
            if ("millis".equals(qName))
                detail.millis = buffer.toString().trim();
            else if ("sequence".equals(qName))
                detail.sequence = buffer.toString().trim();
            else if ("logger".equals(qName))
                detail.logger = buffer.toString().trim();
            else if ("message".equals(qName))
                exception.message = buffer.toString().trim();
            else if ("class".equals(qName))
                frame.clazz = buffer.toString().trim();
            else if ("method".equals(qName))
                frame.method = buffer.toString().trim();
            else if ("line".equals(qName))
                frame.line = buffer.toString().trim();
            else if ("frame".equals(qName)) {
                stackFrames.add(frame);
                frame = new RecordHandler.StackFrame();
            } else if ("exception".equals(qName)) {
                RecordHandler.StackFrame[] frameArray =
                        new RecordHandler.StackFrame[stackFrames.size()];
                exception.stackFrames =
                        (RecordHandler.StackFrame[]) stackFrames.toArray(frameArray);
                stackFrames.clear();
                logRecord.exceptionFlag = true;
                logRecord.exception = exception;
                exception = new ExceptionRecord();
            }
        }

        /**
         * Prints the html result of the parsing to the servlet output.
         */
        public void printHtml() {
            // We never print in html. So this is a noop here.
        }

        /**
         * Gets called if and when eof is hit.
         */
        public void eof() {
            flush();
        }

        private void printRecord(LogRecord r) {
            // Print only the time, not the date.
            int timeIdx = r.date.indexOf('T') + 1;
            out.println(r.date.substring(timeIdx) + ":thread[" + r.thread +
                    "]:" + r.level + ':' + r.message);
            if (r.exception != null) {
                out.println(r.exception.message);
                for (StackFrame s : r.exception.stackFrames) {
                    out.println("    at " + s.clazz + '.' + s.method +
                                " (" + s.line + ')');
                }
                r.exception = null;
            }
        }
    }

}
