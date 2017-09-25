/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.snlab.eval.impl;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.Graph;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.Tx;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Time;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Util {
    public static final Logger LOG = LoggerFactory.getLogger(Util.class);

    public static final InstanceIdentifier<Tx> txiid = InstanceIdentifier.create(Tx.class);

    public static final InstanceIdentifier<Graph> graphiid = InstanceIdentifier.create(Graph.class);

    public static void submit(WriteTransaction wt){
        CheckedFuture<Void, TransactionCommitFailedException> future = wt.submit();
        Futures.addCallback(future, new FutureCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void aVoid) {
                LOG.info("wt success");
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error("wt failed");
                wn.incrementAndGet();
            }
        });
    }



    public static AtomicInteger rn=new AtomicInteger(0);
    public static AtomicInteger wn=new AtomicInteger(0);


    public static AtomicLong countdown=new AtomicLong(0);
    public static long time;
    public static void startcount(long parm){
        countdown.set(parm);
        time=System.currentTimeMillis();
    }
    public static void count_stop(){
        long l = countdown.decrementAndGet();
        if(l<=0){
            time=System.currentTimeMillis()-time;
        }
    }


}
