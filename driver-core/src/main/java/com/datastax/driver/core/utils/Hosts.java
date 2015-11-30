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

package com.datastax.driver.core.utils;

import io.netty.channel.unix.DomainSocketAddress;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Hosts {
    
    public static InetAddress getHost(SocketAddress address) {
        if (address instanceof InetSocketAddress) {
            return ((InetSocketAddress)address).getAddress();
        } else {
            try {
                //return InetAddress.getLoopbackAddress(); // mvn...
                return InetAddress.getLocalHost();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    public static String toString(SocketAddress address) {
        if (address instanceof InetSocketAddress) {
            return Hosts.getHost(address).toString();
        } else if (address instanceof DomainSocketAddress) {
            return String.format("Domain Socket: %s", ((DomainSocketAddress)address).path());
        } else {
            return "UNKNOWN ADDRESS";
        }
    }
}
