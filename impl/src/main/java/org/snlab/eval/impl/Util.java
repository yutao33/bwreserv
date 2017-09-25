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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.Tx;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class Util {
    public static final Logger LOG = LoggerFactory.getLogger(Util.class);

    public static final InstanceIdentifier<Tx> iid = InstanceIdentifier.create(Tx.class);

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
            }
        });
    }
}
