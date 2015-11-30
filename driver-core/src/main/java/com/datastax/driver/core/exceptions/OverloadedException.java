/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver.core.exceptions;

import com.datastax.driver.core.utils.Hosts;

import java.net.InetAddress;
import java.net.SocketAddress;

/**
 * Indicates that the contacted host reported itself being overloaded.
 */
public class OverloadedException extends QueryExecutionException implements CoordinatorException {

    private static final long serialVersionUID = 0;

    private final SocketAddress address;

    public OverloadedException(SocketAddress address, String message) {
        super(String.format("Queried host (%s) was overloaded: %s", address, message));
        this.address = address;
    }

    /**
     * Private constructor used solely when copying exceptions.
     */
    private OverloadedException(SocketAddress address, String message, OverloadedException cause) {
        super(message, cause);
        this.address = address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetAddress getHost() {
        return Hosts.getHost(getAddress());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SocketAddress getAddress() {
        return address;
    }

    @Override
    public OverloadedException copy() {
        return new OverloadedException(address, getMessage(), this);
    }
}
