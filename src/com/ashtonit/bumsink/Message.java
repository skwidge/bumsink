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

import static com.ashtonit.bumsink.Main.EOL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


/**
 * @author Bruce Ashton
 * @date 2013-11-10
 */
class Message {

    private boolean deleted;
    private final File file;

    /**
     * @param file
     */
    Message(final File file) {

        this.file = file;
    }

    long getOctets() throws BumsinkException {

        if (file == null) {
            throw new BumsinkException("No file found");
        }
        return file.length();
    }

    Reader getReader() throws IOException, BumsinkException {

        if (file == null) {
            throw new BumsinkException("No file found");
        }
        return new FileReader(file);
    }

    String getTop(final int lines) throws BumsinkException {

        final StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            for (int i = 0; i < lines; i++) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                builder.append(line);
                builder.append(EOL);
            }
        } catch (IOException e) {
            throw new BumsinkException("Could not read file", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Do nothing
                }
            }
        }
        return builder.toString();
    }

    String getUidl() throws BumsinkException {

        if (file == null) {
            throw new BumsinkException("No file found");
        }
        return file.getName().split("_")[0];
    }

    /**
     * Returns the deleted.
     * 
     * @return the deleted
     */
    boolean isDeleted() {

        return deleted;
    }

    void purge() {

        if (file != null) {
            file.delete();
        }
    }

    /**
     * Set the value of deleted.
     * 
     * @param deleted the deleted to set
     */
    void setDeleted(boolean deleted) {

        this.deleted = deleted;
    }
}
