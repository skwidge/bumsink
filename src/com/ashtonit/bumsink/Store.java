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

import static com.ashtonit.bumsink.Main.MAIL_DIR;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author Bruce Ashton
 * @date 2013-11-05
 */
class Store {

    private static final Object LOCK = new Object();
    private static Store store;

    private final File directory;
    private final List<Message> messages = Collections.synchronizedList(new ArrayList<Message>());

    private Store(final String dirName) throws BumsinkException {

        directory = new File(dirName);
        if (!directory.exists()) {
            directory.mkdir();
        }
        if (!directory.isDirectory()) {
            throw new BumsinkException("Not a directory: " + directory.getAbsolutePath());
        }
        if (!directory.canRead()) {
            throw new BumsinkException("Cannot read directory " + directory.getAbsolutePath());
        }
        if (!directory.canWrite()) {
            throw new BumsinkException("Cannot write to directory " + directory.getAbsolutePath());
        }
        for (File file : directory.listFiles()) {
            messages.add(new Message(file));
        }
    }

    static Store getInstance() throws BumsinkException {

        synchronized (LOCK) {
            if (store == null) {
                store = new Store(MAIL_DIR);
            }
        }
        return store;
    }

    Message getMessage(final int msgNum) throws BumsinkException {

        final Message message = messages.get(msgNum - 1);
        if (message == null) {
            throw new BumsinkException("No message found");
        }
        return message;
    }

    int getMsgCount() {

        return messages.size();
    }

    long getMsgOctets() {

        int total = 0;
        for (final Message message : messages) {
            try {
                total += message.getOctets();
            } catch (BumsinkException e) {
                e.printStackTrace();
            }
        }
        return total;
    }

    void quit() {

        for (final Message message : messages) {
            if (message.isDeleted()) {
                message.purge();
            }
        }
    }

    void reset() {

        for (final Message message : messages) {
            message.setDeleted(false);
        }
    }

    void save(final String email) throws IOException {

        String filename = Integer.toString(email.hashCode());
        File file = new File(directory, filename);
        int idx = 0;
        while (file.exists()) {
            filename = filename.split("_")[0] + "_" + idx;
            file = new File(filename);
            idx++;
        }
        final Writer writer = new FileWriter(file);
        writer.write(email);
        writer.flush();
        writer.close();
        messages.add(new Message(file));
    }
}
