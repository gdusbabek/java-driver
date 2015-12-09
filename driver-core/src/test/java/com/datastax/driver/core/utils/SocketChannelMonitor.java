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

import com.datastax.driver.core.NettyOptions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility that observes {@link Channel}s.  Helpful for ensuring that Sockets are actually closed
 * when they should be.  Utilizes {@link NettyOptions} to monitor created {@link SocketChannel}s.
 */
public class SocketChannelMonitor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SocketChannelMonitor.class);

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("SocketMonitor-%d").build());

    // use a weak set so channels may be garbage collected.
    private final Collection<Channel> channels = Collections.newSetFromMap(
            new MapMaker().weakKeys().<Channel, Boolean>makeMap());

    private final AtomicLong channelsCreated = new AtomicLong(0);

    private final NettyOptions nettyOptions = new NettyOptions() {
        @Override
        public void afterChannelInitialized(Channel channel) throws Exception {
            channels.add(channel);
            channelsCreated.incrementAndGet();
        }

        @Override
        public void onClusterClose(EventLoopGroup eventLoopGroup) {
            eventLoopGroup.shutdownGracefully(0, 15, TimeUnit.SECONDS).syncUninterruptibly();
        }
    };

    @Override
    public void run() {
        try {
            report();
        } catch (Exception e) {
            logger.error("Error countered.", e);
        }
    }

    public void stop() {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
    }

    /**
     * @return A custom {@link NettyOptions} instance that hooks into afterChannelInitialized added channels may be
     * monitored.
     */
    public NettyOptions nettyOptions() {
        return nettyOptions;
    }

    public static Predicate<Channel> openChannels = new Predicate<Channel>() {
        @Override
        public boolean apply(Channel input) {
            return input.isOpen();
        }
    };

    /**
     * Schedules a {@link #report()} to be called every configured interval.
     *
     * @param interval how often to report.
     * @param timeUnit at what time precision to report at.
     */
    public void reportAtFixedInterval(int interval, TimeUnit timeUnit) {
        executor.scheduleAtFixedRate(this, interval, interval, timeUnit);
    }

    /**
     * Reports for all sockets.
     */
    public void report() {
        report(Predicates.<Channel>alwaysTrue());
    }

    /**
     * <p/>
     * Report for all sockets matching the given predicate.  The report format reflects the number of open, closed,
     * live and total sockets created.  This is logged at DEBUG if enabled.
     * <p/>
     * <p/>
     * If TRACE is enabled, each individual socket will be logged as well.
     *
     * @param channelFilter used to determine which sockets to report on.
     */
    public void report(Predicate<Channel> channelFilter) {
        if (logger.isDebugEnabled()) {
            Iterable<Channel> channels = matchingChannels(channelFilter);
            Iterable<Channel> open = Iterables.filter(channels, openChannels);
            Iterable<Channel> closed = Iterables.filter(channels, Predicates.not(openChannels));

            logger.debug("Channel states: {} open, {} closed, live {}, total sockets created " +
                            "(including those that don't match filter) {}.",
                    Iterables.size(open),
                    Iterables.size(closed),
                    Iterables.size(channels),
                    channelsCreated.get());

            if (logger.isTraceEnabled()) {
                logger.trace("Open channels {}.", open);
                logger.trace("Closed channels {}.", closed);
            }
        }
    }

    private static Comparator<Channel> BY_REMOTE_ADDRESS = new Comparator<Channel>() {
        @Override
        public int compare(Channel t0, Channel t1) {
            // Should not be null as these are filtered previously in matchingChannels.
            assert t0 != null && t0.remoteAddress() != null;
            assert t1 != null && t1.remoteAddress() != null;
            ;
            return t0.remoteAddress().toString().compareTo(t1.remoteAddress().toString());
        }
    };

    /**
     * @param addresses The addresses to include.
     * @return Open channels matching the given socket addresses.
     */
    public Collection<Channel> openChannels(final Collection<InetSocketAddress> addresses) {
        List<Channel> channels = Lists.newArrayList(matchingChannels(new Predicate<Channel>() {
            @Override
            public boolean apply(Channel input) {
                return input.isOpen() && input.remoteAddress() != null && addresses.contains(input.remoteAddress());
            }
        }));

        Collections.sort(channels, BY_REMOTE_ADDRESS);

        return channels;
    }

    /**
     * @param channelFilter {@link Predicate} to use to determine whether or not a socket shall be considered.
     * @return Channels matching the given {@link Predicate}.
     */
    public Iterable<Channel> matchingChannels(final Predicate<Channel> channelFilter) {
        return Iterables.filter(Lists.newArrayList(channels), Predicates.and(Predicates.notNull(), channelFilter));
    }
}
