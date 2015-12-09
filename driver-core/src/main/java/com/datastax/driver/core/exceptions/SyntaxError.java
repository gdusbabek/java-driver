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
 * Indicates a syntax error in a query.
 */
public class SyntaxError extends QueryValidationException implements CoordinatorException {

    private static final long serialVersionUID = 0;

    private final SocketAddress address;

    /**
     * @deprecated This constructor is kept for backwards compatibility.
     */
    @Deprecated
    public SyntaxError(String msg) {
        this(null, msg);
    }

    public SyntaxError(SocketAddress address, String msg) {
        super(msg);
        this.address = address;
    }

    private SyntaxError(SocketAddress address, String msg, Throwable cause) {
        super(msg, cause);
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
    public SyntaxError copy() {
        return new SyntaxError(getAddress(), getMessage(), this);
    }
}
