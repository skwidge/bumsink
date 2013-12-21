/*
 *  Copyright 2013 Bruce Ashton
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ashtonit.bumsink;

import static com.ashtonit.bumsink.Main.DEBUG;
import static com.ashtonit.bumsink.Main.EOL;
import static com.ashtonit.bumsink.Main.SMTP_HOST;
import static com.ashtonit.bumsink.Main.VERSION;
import static com.ashtonit.bumsink.Main.err;
import static com.ashtonit.bumsink.Main.info;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


/**
 * A SMTP connection with a single client.
 * 
 * @author Bruce Ashton
 * @date 2013-11-05
 */
class SmtpSession extends Thread {

    private static final String DATA = "DATA";
    private static final String EOM = ".";
    private static final String FROM = "MAIL FROM:";
    private static final String GREET = "220 BUMSink version " + VERSION;
    private static final String HELO = "HELO";
    private static final String INT = "354 Intermediate";
    private static final String NOOP = "NOOP";
    private static final String OK = "250 OK";
    private static final String QUIT = "QUIT";
    private static final String RCPT = "RCPT TO:";
    private static final String RSET = "RSET";
    private static final String WTF = "500 Command not recognized";

    private final StringBuilder email = new StringBuilder();
    private boolean inData = false;
    private final BufferedReader reader;
    private final Socket socket;
    private final Store store;
    private final BufferedWriter writer;

    /**
     * @param threadGroup
     * @param socket
     * @throws BumsinkException
     * @throws IOException
     */
    SmtpSession(final ThreadGroup threadGroup, final Socket socket) throws BumsinkException {

        super(threadGroup, socket.toString());
        this.socket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (final IOException e) {
            throw new BumsinkException(e);
        }
        store = Store.getInstance();
        setDaemon(true);
    }

    /**
     * @see Thread#run()
     */
    @Override
    public void run() {

        try {
            write(GREET);
            String line = reader.readLine();
            while (line != null) {
                if (inData) {
                    handleData(line);
                } else {
                    if (DEBUG) {
                        info("read: " + line);
                    }
                    handle(line);
                }
                line = reader.readLine();
            }
        } catch (final IOException e) {
            // Don't care
        } finally {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (final IOException e) {
                    // Don't care
                }
            }
        }
    }

    private void data() throws IOException {

        inData = true;
        write(INT);
    }

    private void from() throws IOException {

        write(OK);
    }

    private void handle(final String line) throws IOException {

        if (line.startsWith(HELO)) {
            helo();
        } else if (line.startsWith(FROM)) {
            from();
        } else if (line.startsWith(RCPT)) {
            rcpt();
        } else if (line.startsWith(DATA)) {
            data();
        } else if (line.startsWith(NOOP)) {
            noop();
        } else if (line.startsWith(RSET)) {
            rset();
        } else if (line.startsWith(QUIT)) {
            quit();
        } else {
            wtf(line);
        }
    }

    private void handleData(final String line) throws IOException {

        if (EOM.equals(line)) {
            inData = false;
            write(OK);
            if (email.length() > 0) {
                store.save(email.toString());
            }
            email.setLength(0);
        } else {
            email.append(line);
            email.append(EOL);
        }
    }

    private void helo() throws IOException {

        write("250 " + SMTP_HOST);
    }

    private void noop() throws IOException {

        write(OK);
    }

    private void quit() throws IOException {

        write("221 OK");
        socket.close();
    }

    private void rcpt() throws IOException {

        write(OK);
    }

    private void rset() throws IOException {

        email.setLength(0);
        write(OK);
    }

    private void write(final String line) throws IOException {

        if (DEBUG) {
            info("write: " + line);
        }
        writer.write(line + EOL);
        writer.flush();
    }

    private void wtf(final String line) throws IOException {

        err("wtf: " + line);
        write(WTF);
    }
}
