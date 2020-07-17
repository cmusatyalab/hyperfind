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

package edu.cmu.cs.diamond.hyperfind.collaboration.service;

import edu.cmu.cs.diamond.hyperfind.collaboration.service.config.CollaborationServiceConfig;
import edu.cmu.cs.diamond.hyperfind.collaboration.service.config.ServerSslConfig;
import edu.cmu.cs.diamond.hyperfind.connection.api.Connection;
import edu.cmu.cs.diamond.hyperfind.connection.diamond.DiamondConnection;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import java.io.File;
import java.nio.file.Paths;
import javax.net.ssl.SSLException;

public final class CollaborationService extends Application<CollaborationServiceConfig> {

    @Override
    public void run(CollaborationServiceConfig config, Environment environment) {
        io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder serverBuilder
                = io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder.forPort(config.port());

        config.ssl().ifPresent(s -> configureSslContext(serverBuilder, s));

        Connection connection = new DiamondConnection(config.bundleDirs(), config.filterDirs());
        serverBuilder.addService(new CollaborationResource(connection));

        environment.lifecycle().manage(new ManagedGrpcServer(serverBuilder.build()));
    }

    private static void configureSslContext(
            io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder serverBuilder,
            ServerSslConfig config) {
        File certChain = Paths.get(config.certChain()).toFile();
        File privateKey = Paths.get(config.privateKey()).toFile();

        try {
            if (config.password().isPresent()) {
                serverBuilder.sslContext(io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts.forServer(
                        certChain, privateKey, config.password().get()).build());
            } else {
                serverBuilder.sslContext(io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts.forServer(
                        certChain, privateKey).build());
            }
        } catch (SSLException e) {
            throw new RuntimeException("Failed to create ssl context", e);
        }
    }

    public static void main(String[] args) throws Exception {
        new CollaborationService().run(args);
    }
}
