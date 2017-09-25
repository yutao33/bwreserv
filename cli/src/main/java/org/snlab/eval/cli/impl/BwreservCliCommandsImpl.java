/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.snlab.eval.cli.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.BwreservService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.EvalFuncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.EvalFuncInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.EvalFuncOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snlab.eval.cli.api.BwreservCliCommands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BwreservCliCommandsImpl implements BwreservCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(BwreservCliCommandsImpl.class);
    private final DataBroker dataBroker;
    private final BwreservService bwreservService;

    public BwreservCliCommandsImpl(final DataBroker db, final RpcProviderRegistry rpcRegistry) {
        this.dataBroker = db;
        bwreservService = rpcRegistry.getRpcService(BwreservService.class);
        LOG.info("BwreservCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        long parm = Long.valueOf(testArgument.toString());
        EvalFuncInput input = new EvalFuncInputBuilder().setParm(parm).build();
        Future<RpcResult<EvalFuncOutput>> future = bwreservService.evalFunc(input);
        String ret="failed";
        try {
            ret = future.get().getResult().getResult();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return ret;
    }
}