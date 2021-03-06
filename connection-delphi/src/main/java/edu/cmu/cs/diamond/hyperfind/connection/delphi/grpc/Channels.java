/*
 * HyperFind, a search application for the OpenDiamond platform
 *
 * Copyright (c) 2009-2020 Carnegie Mellon University
 * All rights reserved.
 *
 * HyperFind is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2.
 *
 * HyperFind is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HyperFind. If not, see <http://www.gnu.org/licenses/>.
 *
 * Linking HyperFind statically or dynamically with other modules is
 * making a combined work based on HyperFind. Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * In addition, as a special exception, the copyright holders of
 * HyperFind give you permission to combine HyperFind with free software
 * programs or libraries that are released under the GNU LGPL, the
 * Eclipse Public License 1.0, or the Apache License 2.0. You may copy and
 * distribute such a system following the terms of the GNU GPL for
 * HyperFind and the licenses of the other code concerned, provided that
 * you include the source code of that other code when and as the GNU GPL
 * requires distribution of source code.
 *
 * Note that people who make modified versions of HyperFind are not
 * obligated to grant this special exception for their modified versions;
 * it is their choice whether to do so. The GNU General Public License
 * gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version
 * which carries forward this exception.
 */

package edu.cmu.cs.diamond.hyperfind.connection.delphi.grpc;

import edu.cmu.cs.diamond.hyperfind.connection.delphi.DelphiConfiguration;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Channels {

    private static final Logger log = LoggerFactory.getLogger(Channels.class);

    private Channels() {

    }

    public static ManagedChannel create(DelphiConfiguration config, String host) {
        return config.useSsl()
                ? edu.cmu.cs.diamond.hyperfind.connection.delphi.grpc.Channels.createSslChannel(
                host,
                config.port(),
                !config.truststorePath().isBlank() ? Optional.of(config.truststorePath()) : Optional.empty())
                : ManagedChannelBuilder.forAddress(host, config.port())
                        .maxInboundMessageSize(Integer.MAX_VALUE)
                        .usePlaintext()
                        .build();
    }

    public static void shutdown(String host, ManagedChannel channel) {
        channel.shutdown();
        try {
            channel.awaitTermination(5, TimeUnit.SECONDS);
            return;
        } catch (InterruptedException e) {
            log.warn("Failed to shutdown channel to {} gracefully - attempting forced shutdown", host, e);
        }

        channel.shutdownNow();
        try {
            channel.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Failed to force shutdown channel to {} gracefully", host, e);
        }
    }

    private static ManagedChannel createSslChannel(String host, int port, Optional<String> trustStorePath) {
        io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder sslContextBuilder
                = io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts.forClient();
        trustStorePath.ifPresent(t -> sslContextBuilder.trustManager(Paths.get(t).toFile()));

        try {
            return io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder.forAddress(host, port)
                    .sslContext(sslContextBuilder.build())
                    .maxInboundMessageSize(Integer.MAX_VALUE)
                    .build();
        } catch (SSLException e) {
            throw new RuntimeException("Failed to create channel", e);
        }
    }
}
