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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;


/**
 * A test mail server.
 * 
 * @author Bruce Ashton
 * @date 2013-11-05
 */
public class Main {

    static final ThreadGroup ALL_THREADS = new ThreadGroup("BUMSink");
    static boolean DEBUG = false;
    static final String EOL = "\r\n";
    static String MAIL_DIR = "mail";
    static int POP_BACKLOG = 0;
    static String POP_HOST = "localhost";
    static int POP_PORT = 110;
    static boolean RUNNING = true;
    static int SMTP_BACKLOG = 0;
    static String SMTP_HOST = "localhost";
    static int SMTP_PORT = 25;
    static int SO_TIMEOUT = 10000;
    static final String VERSION = "0.1";

    private static final String DEBUG_KEY = "debug";
    private static final String MAIL_DIR_KEY = "mail.dir";
    private static final String POP_BACKLOG_KEY = "pop.backlog";
    private static final String POP_HOST_KEY = "pop.host";
    private static final String POP_PORT_KEY = "pop.port";
    private static final String SMTP_BACKLOG_KEY = "smtp.backlog";
    private static final String SMTP_HOST_KEY = "smtp.host";
    private static final String SMTP_PORT_KEY = "smtp.port";
    private static final String SO_TIMEOUT_KEY = "so.timeout";

    /**
     * @param args
     * @throws BumsinkException
     */
    public static void main(final String[] args) throws BumsinkException {

        if (args.length > 0) {
            loadProperties(args[0]);
        } else {
            System.out.println("Usage: java -jar bumsink.jar bumsink.properties");
            System.exit(0);
        }
        new SmtpServer(ALL_THREADS).start();
        new PopServer(ALL_THREADS).start();
        info("Bumsink started.");
    }

    static void err(final String message) {

        log(System.err, message);
    }

    static void info(final String message) {

        log(System.out, message);
    }

    private static void loadProperties(final String filename) throws BumsinkException {

        if (filename == null) {
            return;
        }

        final File file = new File(filename);
        if (!file.exists()) {
            throw new BumsinkException("No such file: " + filename);
        } else if (!file.isFile()) {
            throw new BumsinkException("Not a regular file: " + filename);
        } else if (!file.canRead()) {
            throw new BumsinkException("Cannot read file: " + filename);
        }

        try {
            final Properties properties = new Properties();
            final FileInputStream inStream = new FileInputStream(file);
            properties.load(inStream);

            DEBUG = Boolean.valueOf(properties.getProperty(DEBUG_KEY, Boolean.toString(DEBUG)));
            MAIL_DIR = properties.getProperty(MAIL_DIR_KEY, MAIL_DIR);
            POP_BACKLOG = Integer.parseInt(properties.getProperty(POP_BACKLOG_KEY, Integer.toString(POP_BACKLOG)));
            POP_HOST = properties.getProperty(POP_HOST_KEY, POP_HOST);
            POP_PORT = Integer.parseInt(properties.getProperty(POP_PORT_KEY, Integer.toString(POP_PORT)));
            SMTP_BACKLOG = Integer.parseInt(properties.getProperty(SMTP_BACKLOG_KEY, Integer.toString(SMTP_BACKLOG)));
            SMTP_HOST = properties.getProperty(SMTP_HOST_KEY, SMTP_HOST);
            SMTP_PORT = Integer.parseInt(properties.getProperty(SMTP_PORT_KEY, Integer.toString(SMTP_PORT)));
            SO_TIMEOUT = Integer.parseInt(properties.getProperty(SO_TIMEOUT_KEY, Integer.toString(SO_TIMEOUT)));
        } catch (final FileNotFoundException e) {
            throw new BumsinkException(e);
        } catch (final IOException e) {
            throw new BumsinkException(e);
        }
    }

    private static void log(final PrintStream writer, final String message) {

        final StringBuilder builder = new StringBuilder(Long.toString(System.currentTimeMillis()));
        builder.append(' ');
        builder.append(Thread.currentThread().getName());
        builder.append(' ');
        builder.append(message);
        writer.println(builder.toString());
    }
}
