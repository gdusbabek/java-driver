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
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Indicates an error during the authentication phase while connecting to a node.
 */
public class AuthenticationException extends DriverException {

    private static final long serialVersionUID = 0;

    private final SocketAddress address;

    public AuthenticationException(SocketAddress address, String message) {
        super(String.format("Authentication error on host %s: %s", address, message));
        this.address = address;
    }

    private AuthenticationException(String message, Throwable cause, SocketAddress address)
    {
        super(message, cause);
        this.address = address;
    }

    /**
     * The host for which the authentication failed.
     * <p>
     * This is a shortcut for {@code getAddress().getAddress()}.
     *
     * @return the host for which the authentication failed.
     */
    public InetAddress getHost() {
        return Hosts.getHost(address);
    }

    /**
     * The full address of the host for which the authentication failed.
     *
     * @return the host for which the authentication failed.
     */
    public SocketAddress getAddress() {
        return address;
    }

    @Override
    public DriverException copy() {
        return new AuthenticationException(getMessage(), this, address);
    }
}
