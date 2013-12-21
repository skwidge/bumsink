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
import static com.ashtonit.bumsink.Main.VERSION;
import static com.ashtonit.bumsink.Main.err;
import static com.ashtonit.bumsink.Main.info;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.Socket;


/**
 * A Pop connection with a single client.
 * 
 * @author Bruce Ashton
 * @date 2013-11-05
 */
class PopSession extends Thread {

    private static final String CMD_APOP = "APOP";
    private static final String CMD_DELE = "DELE";
    private static final String CMD_LIST = "LIST";
    private static final String CMD_NOOP = "NOOP";
    private static final String CMD_PASS = "PASS";
    private static final String CMD_QUIT = "QUIT";
    private static final String CMD_RETR = "RETR";
    private static final String CMD_RSET = "RSET";
    private static final String CMD_STAT = "STAT";
    private static final String CMD_TOP = "TOP";
    private static final String CMD_UIDL = "UIDL";
    private static final String CMD_USER = "USER";

    private static final String EOM = ".";
    private static final String ERR = "-ERR ";
    private static final String OK = "+OK ";

    private final BufferedReader reader;
    private final Socket socket;
    private final Store store;

    private final BufferedWriter writer;

    /**
     * @param threadGroup
     * @param socket
     * @throws BumsinkException
     */
    PopSession(final ThreadGroup threadGroup, final Socket socket) throws BumsinkException {

        super(threadGroup, socket.toString());
        this.socket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (final IOException e) {
            throw new BumsinkException(e);
        }
        this.store = Store.getInstance();
        setDaemon(true);
    }

    /**
     * @see Thread#run()
     */
    @Override
    public void run() {

        try {
            write(OK + "BUMSink POP3 server version " + VERSION + " ready");
            String line = reader.readLine();
            while (line != null) {
                if (DEBUG) {
                    info("read: " + line);
                }
                if (line.startsWith(CMD_APOP)) {
                    handleApop(line);
                } else if (line.startsWith(CMD_DELE)) {
                    handleDele(line);
                } else if (line.startsWith(CMD_LIST)) {
                    handleList(line);
                } else if (line.startsWith(CMD_NOOP)) {
                    handleNoop();
                } else if (line.startsWith(CMD_QUIT)) {
                    handleQuit(line);
                } else if (line.startsWith(CMD_RSET)) {
                    handleRset(line);
                } else if (line.startsWith(CMD_RETR)) {
                    handleRetr(line);
                } else if (line.startsWith(CMD_STAT)) {
                    handleStat();
                } else if (line.startsWith(CMD_USER)) {
                    handleUser(line);
                } else if (line.startsWith(CMD_PASS)) {
                    handlePass(line);
                } else if (line.startsWith(CMD_TOP)) {
                    handleTop(line);
                } else if (line.startsWith(CMD_UIDL)) {
                    handleUidl(line);
                } else
                    handleWtf(line);
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

    private void handleApop(final String line) throws IOException {

        write(OK + "Hello " + line.split(" ")[0]);
    }

    private void handleDele(final String line) throws IOException {

        final String[] words = line.split("\\s+");
        if (words.length < 2) {
            err(ERR + "not enough arguments");
            write(ERR + "not enough arguments");
        } else {
            final int count = store.getMsgCount();
            final int msgNum = Integer.parseInt(words[1]);
            if (msgNum < 1 || msgNum > count) {
                err(ERR + msgNum + " no such message");
                write(ERR + msgNum + " no such message");
            } else {
                try {
                    store.getMessage(msgNum).setDeleted(true);
                    write(OK + "message " + msgNum + " deleted");
                } catch (BumsinkException e) {
                    err(ERR + msgNum + " no such message");
                    e.printStackTrace();
                    write(ERR + msgNum + " no such message");
                }
            }
        }
    }

    private void handleList(final String line) throws IOException {

        final int count = store.getMsgCount();
        final String[] words = line.split("\\s+");
        if (words.length < 2) {
            write(OK + "scan listing follows");
            for (int i = 1; i <= count; i++) {
                try {
                    final Message message = store.getMessage(i);
                    final long octets = message.getOctets();
                    if (!message.isDeleted()) {
                        write(i + " " + octets);
                    }
                } catch (BumsinkException e) {
                    e.printStackTrace();
                }
            }
            write(EOM);
        } else {
            final int msgNum = Integer.parseInt(words[1]);
            if (msgNum < 1 || msgNum > count) {
                err(ERR + msgNum + "no such message");
                write(ERR + msgNum + "no such message");
            } else {
                try {
                    final Message message = store.getMessage(msgNum);
                    final long octets = message.getOctets();
                    write(OK + msgNum + " " + octets);
                } catch (BumsinkException e) {
                    err(ERR + msgNum + " could not read message from file");
                    e.printStackTrace();
                    write(ERR + msgNum + " could not read message from file");
                }
            }
        }
    }

    private void handleNoop() throws IOException {

        write(OK);
    }

    private void handlePass(final String line) throws IOException {

        write(OK + "Seems legit");
    }

    private void handleQuit(final String line) throws IOException {

        store.quit();
        write(OK + "BUMSink POP3 signing off");
        socket.close();
    }

    private void handleRetr(final String line) throws IOException {

        final String[] words = line.split("\\s+");
        if (words.length < 2) {
            err(ERR + "no such message");
            write(ERR + "no such message");
        } else {
            final int count = store.getMsgCount();
            final int msgNum = Integer.parseInt(words[1]);
            if (msgNum < 1 || msgNum > count) {
                err(ERR + msgNum + " no such message");
                write(ERR + msgNum + " no such message");
            } else {
                try {
                    final Message message = store.getMessage(msgNum);
                    final long octets = message.getOctets();
                    final Reader reader = message.getReader();
                    write(OK + octets + " octets");
                    int chr = reader.read();
                    while (chr > -1) {
                        writer.write(chr);
                        chr = reader.read();
                    }
                    write(EOM);
                } catch (final IOException e) {
                    err(ERR + msgNum + " could not read message from file");
                    e.printStackTrace();
                    write(ERR + msgNum + " could not read message from file");
                } catch (final BumsinkException e) {
                    err(ERR + msgNum + " could not read message from file");
                    e.printStackTrace();
                    write(ERR + msgNum + " could not read message from file");
                }
            }
        }
    }

    private void handleRset(final String line) throws IOException {

        store.reset();
        write(OK + store.getMsgCount() + " " + store.getMsgOctets());
    }

    private void handleStat() throws IOException {

        write(OK + store.getMsgCount() + " " + store.getMsgOctets());
    }

    private void handleTop(final String line) throws IOException {

        final String[] words = line.split("\\s+");
        if (words.length < 3) {
            err(ERR + "not enough arguments");
            write(ERR + "not enough arguments");
        } else {
            final int count = store.getMsgCount();
            final int msgNum = Integer.parseInt(words[1]);
            final int lines = Integer.parseInt(words[2]);
            if (msgNum < 1 || msgNum > count) {
                err(ERR + msgNum + " no such message");
                write(ERR + msgNum + " no such message");
            } else {
                try {
                    final Message message = store.getMessage(msgNum);
                    final long octets = message.getOctets();
                    final String top = message.getTop(lines);
                    write(OK + octets + " octets");
                    write(top);
                    write(EOM + EOL);
                } catch (final BumsinkException e) {
                    err(ERR + msgNum + " could not read message from file");
                    e.printStackTrace();
                    write(ERR + msgNum + " could not read message from file");
                }
            }
        }
    }

    private void handleUidl(final String line) throws IOException {

        final int count = store.getMsgCount();
        final String[] words = line.split("\\s+");
        if (words.length < 2) {
            write(OK + "UIDL listing follows");
            for (int i = 1; i <= count; i++) {
                try {
                    final Message message = store.getMessage(i);
                    if (!message.isDeleted()) {
                        final String uidl = message.getUidl();
                        write(i + " " + uidl);
                    }
                } catch (BumsinkException e) {
                    e.printStackTrace();
                }
            }
            write(EOM);
        } else {
            final int msgNum = Integer.parseInt(words[1]);
            if (msgNum < 1 || msgNum > count) {
                err(ERR + msgNum + "no such message");
                write(ERR + msgNum + "no such message");
            } else {
                try {
                    final Message message = store.getMessage(msgNum);
                    final String uidl = message.getUidl();
                    write(OK + msgNum + " " + uidl);
                } catch (BumsinkException e) {
                    e.printStackTrace();
                    err(ERR + msgNum + " could not read uidl from file");
                    write(ERR + msgNum + " could not read uidl from file");
                }
            }
        }
    }

    private void handleUser(final String line) throws IOException {

        write(OK + "Hello " + line);
    }

    private void handleWtf(String line) throws IOException {

        err(ERR + "Unknown command: " + line);
        write(ERR + "Unknown command: " + line);
    }

    private void write(final String line) throws IOException {

        if (DEBUG) {
            info("write: " + line);
        }
        writer.write(line + EOL);
        writer.flush();
    }
}
