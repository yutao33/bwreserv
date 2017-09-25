/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.snlab.eval.impl;

import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.BwreservService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.EvalFuncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.EvalFuncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.EvalFuncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.Tx;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.TxBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.snlab.eval.impl.Util.iid;

public class BwreservProvider implements BwreservService{

    private static final Logger LOG = LoggerFactory.getLogger(BwreservProvider.class);

    private final DataBroker dataBroker;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public BwreservProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        Tx tx = new TxBuilder().setBw(10l).build();
        wt.put(LogicalDatastoreType.OPERATIONAL,iid,tx);
        Util.submit(wt);
        LOG.info("BwreservProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("BwreservProvider Closed");
    }

    @Override
    public Future<RpcResult<EvalFuncOutput>> evalFunc(EvalFuncInput input) {
        EvalFuncOutput output = new EvalFuncOutputBuilder().setResult("result").build();
        RpcResult<EvalFuncOutput> rpcResult = RpcResultBuilder.success(output).build();
        for(int i=0;i<1000;i++){
            executor.submit(new Job());
        }
        return Futures.immediateFuture(rpcResult);
    }


    private class Job implements Runnable {
        @Override
        public void run() {
            ReadWriteTransaction rwt = dataBroker.newReadWriteTransaction();
            try {
                rwt.read(LogicalDatastoreType.OPERATIONAL, iid).checkedGet();
            } catch (ReadFailedException e) {
                LOG.error("rt failed");
            }
            Tx newtx = new TxBuilder().setBw(new Random().nextLong()).build();
            rwt.put(LogicalDatastoreType.OPERATIONAL,iid,newtx);
            Util.submit(rwt);
        }

    }
}