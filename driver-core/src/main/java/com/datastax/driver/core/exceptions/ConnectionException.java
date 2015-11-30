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
 * Indicates that a connection to a host has encountered a problem
 * and that it should be closed.
 */
public class ConnectionException extends DriverException implements CoordinatorException {

    private static final long serialVersionUID = 0;

    public final SocketAddress address;
    
    public ConnectionException(SocketAddress address, String msg, Throwable cause) {
        super(msg, cause);
        this.address = address;
    }
    
    public ConnectionException(SocketAddress address, String msg) {
        super(msg);
        this.address = address;
    }

    @Override
    public InetAddress getHost() {
        return Hosts.getHost(getAddress());
    }

    @Override
    public SocketAddress getAddress() {
        return address;
    }

    @Override
    public String getMessage() {
        return String.format("[%s] %s", getHost(), getRawMessage());
    }

    @Override
    public ConnectionException copy() {
        return new ConnectionException(address, getRawMessage(), this);
    }

    String getRawMessage() {
        return super.getMessage();
    }

}
