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

import static com.ashtonit.bumsink.Main.ALL_THREADS;
import static com.ashtonit.bumsink.Main.POP_BACKLOG;
import static com.ashtonit.bumsink.Main.POP_HOST;
import static com.ashtonit.bumsink.Main.POP_PORT;
import static com.ashtonit.bumsink.Main.RUNNING;
import static com.ashtonit.bumsink.Main.SO_TIMEOUT;
import static com.ashtonit.bumsink.Main.info;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


/**
 * @author Bruce Ashton
 * @date 2013-11-05
 */
class PopServer extends Thread {

    private final ServerSocket serverSocket;

    /**
     * @param threadGroup
     * @throws BumsinkException
     */
    PopServer(final ThreadGroup threadGroup) throws BumsinkException {

        super(threadGroup, PopServer.class.getName());
        try {
            final InetAddress inetAddress = InetAddress.getByName(POP_HOST);
            serverSocket = new ServerSocket(POP_PORT, POP_BACKLOG, inetAddress);
            serverSocket.setSoTimeout(SO_TIMEOUT);
        } catch (final UnknownHostException e) {
            throw new BumsinkException(e);
        } catch (final IOException e) {
            throw new BumsinkException(e);
        }
    }

    /**
     * @see Thread#run()
     */
    @Override
    public void run() {

        info("PopServer started.");
        while (RUNNING) {
            try {
                final Socket socket = serverSocket.accept();
                new PopSession(ALL_THREADS, socket).start();
            } catch (final SocketTimeoutException e) {
                // Do nothing.
            } catch (final IOException e) {
                e.printStackTrace();
            } catch (final Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
