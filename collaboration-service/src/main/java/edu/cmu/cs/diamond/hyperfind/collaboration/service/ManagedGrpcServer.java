/*
 * HyperFind, a search application for the OpenDiamond platform
 *
 * Copyright (c) 2009-2011-2020 Carnegie Mellon University
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

import io.dropwizard.lifecycle.Managed;
import io.grpc.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManagedGrpcServer implements Managed {

    private static final Logger log = LoggerFactory.getLogger(ManagedGrpcServer.class);

    private final Server server;

    public ManagedGrpcServer(Server server) {
        this.server = server;
    }

    @Override
    public void start() throws Exception {
        log.info("Starting gRPC server");
        server.start();
        log.info("gRPC server started on port {}", server.getPort());
    }

    @Override
    public void stop() {
        log.info("Stopping gRPC server on port {}", server.getPort());
        server.shutdown();
    }

}
