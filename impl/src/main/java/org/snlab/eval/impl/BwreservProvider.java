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
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.BwreservService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.EvalFuncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.EvalFuncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.EvalFuncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.Graph;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.GraphBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.LinkIdType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.NodeIdType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.Tx;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.TxBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.graph.Link;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.graph.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.graph.LinkKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.graph.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.graph.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bwreserv.rev150105.graph.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.snlab.eval.impl.Util.graphiid;
import static org.snlab.eval.impl.Util.txiid;

public class BwreservProvider implements BwreservService{

    private static final Logger LOG = LoggerFactory.getLogger(BwreservProvider.class);

    private final DataBroker dataBroker;

    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    private List<Node> nodes;

    public BwreservProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
//        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
//        Tx tx = new TxBuilder().setBw(10L).build();
//        wt.put(LogicalDatastoreType.OPERATIONAL, txiid,tx);
//        Util.submit(wt);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        inittopo();
        LOG.info("BwreservProvider Session Initiated");
        System.out.println("BwreservProvider Session Initiated");
    }

    private void inittopo() {
        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        nodes=new ArrayList<>();
        List<Link> links=new ArrayList<>();

        Map<String,List<LinkIdType>> nodelinks=new HashMap<>();

        LinkBuilder linkBuilder = new LinkBuilder();
        String[] ls = PredefTopo.links;
        for (int i=0;i<ls.length;i+=2){
            String from=ls[i];
            String to=ls[i+1];
            String linkidstr=from + "-" + to;
            LinkIdType linkid = new LinkIdType(linkidstr);
            Link link = linkBuilder.setFrom(new NodeIdType(from))
                    .setTo(new NodeIdType(to))
                    .setLinkid(linkid)
                    .setBw(1000000000000L).build();
            links.add(link);
            List<LinkIdType> ll = nodelinks.get(from);
            if(ll==null){
                ll=new ArrayList<>();
                nodelinks.put(from,ll);
            }
            ll.add(linkid);
        }

        NodeBuilder nodebuilder = new NodeBuilder();

        for (Map.Entry<String, List<LinkIdType>> entry : nodelinks.entrySet()) {
            String nodeidstr = entry.getKey();
            Node node = nodebuilder.setNodeid(new NodeIdType(nodeidstr))
                    .setNodeLinks(entry.getValue())
                    .build();
            nodes.add(node);
        }

//        for (String prenode : PredefTopo.nodes) {
//            Node n = nodebuilder.setNodeid(new NodeIdType(prenode)).setNodeLinks().build();
//            nodes.add(n);
//        }

        Graph graph = new GraphBuilder().setNode(nodes).setLink(links).build();
        wt.put(LogicalDatastoreType.OPERATIONAL,graphiid,graph);
        Util.submit(wt);
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
        Random random = new Random();
        Object[] array = nodes.toArray();
        int len=array.length;

        Long parm = input.getParm();

        if(parm==-1){
            Util.rn.set(0);
            Util.wn.set(0);
        } else if (parm==-2){
            System.out.println("read failed="+Util.rn.get());
            System.out.println("write failed="+Util.wn.get());
            System.out.println("time="+Util.time);
        } else {

            Util.startcount(parm);

            for (int i = 0; i < parm; i++) {
                int src = Math.abs(random.nextInt()) % len;
                int dst;
                while ((dst = (Math.abs(random.nextInt()) % len)) == src) ;
                NodeIdType srcnode = ((Node) array[src]).getNodeid();
                NodeIdType dstnode = ((Node) array[dst]).getNodeid();
                int bw = random.nextInt() % 10;
                executor.submit(new BRJob(srcnode, dstnode, bw));
            }
        }
        return Futures.immediateFuture(rpcResult);
    }


//    private class Job implements Runnable {
//        @Override
//        public void run() {
//            ReadWriteTransaction rwt = dataBroker.newReadWriteTransaction();
//            try {
//                rwt.read(LogicalDatastoreType.OPERATIONAL, txiid).checkedGet();
//            } catch (ReadFailedException e) {
//                LOG.error("rt failed");
//            }
//            Tx newtx = new TxBuilder().setBw(new Random().nextLong()).build();
//            rwt.put(LogicalDatastoreType.OPERATIONAL, txiid,newtx);
//            Util.submit(rwt);
//        }
//
//    }

    private class BRJob implements Runnable {

        private NodeIdType src,dst;
        private int bw;

        private BRJob(NodeIdType src, NodeIdType dst, int bw) {
            this.src = src;
            this.dst = dst;
            this.bw = bw;
        }

        @Override
        public void run() {
            ReadWriteTransaction rwt = dataBroker.newReadWriteTransaction();
            Map<NodeIdType,Integer> dist=new HashMap<>();
            Queue<NodeIdType> queue=new LinkedList<>();
            Map<NodeIdType,LinkIdType> lasthop=new HashMap<>();

            try {

                Graph graph = rwt.read(LogicalDatastoreType.OPERATIONAL, graphiid).checkedGet().get();

                LOG.info("graph read success");

                Map<NodeIdType,Node> nodeMap=new HashMap<>();
                Map<LinkIdType,Link> linkMap=new HashMap<>();
                for (Node node : graph.getNode()) {
                    nodeMap.put(node.getNodeid(),node);
                }
                for (Link link : graph.getLink()) {
                    linkMap.put(link.getLinkid(),link);
                }

                dist.put(src,0);
                queue.add(src);
                while(!queue.isEmpty()){
                    NodeIdType u = queue.poll();
                    if(u.equals(dst)){
                        LOG.info("got it");
                        StringBuilder sb=new StringBuilder(u.getValue());
                        NodeIdType n = u;
                        while(!n.equals(src)){
                            LinkIdType e = lasthop.get(n);
                            Link oldlink = linkMap.get(e);
                            Link newlink = new LinkBuilder(oldlink).setBw(oldlink.getBw() - bw).build();
                            InstanceIdentifier<Link> linkiid = graphiid.builder().child(Link.class, new LinkKey(e)).build();
                            rwt.put(LogicalDatastoreType.OPERATIONAL,linkiid,newlink);
                            sb.append(oldlink.getFrom());
                            n=oldlink.getFrom();
                        }
                        CheckedFuture<Void, TransactionCommitFailedException> future = rwt.submit();
                        Futures.addCallback(future, new FutureCallback<Void>() {
                            @Override
                            public void onSuccess(@Nullable Void aVoid) {
                                LOG.info("wt success");
                                Util.count_stop();
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                LOG.error("wt failed");
                                Util.wn.incrementAndGet();
                                Util.count_stop();
                            }
                        });
                        String path = sb.toString();
                        LOG.info(path);
                        return;
                    }
                    Node node = nodeMap.get(u);
                    List<LinkIdType> nodeLinks = node.getNodeLinks();
                    for (LinkIdType e : nodeLinks) {
                        NodeIdType to = linkMap.get(e).getTo();
                        if(!dist.containsKey(to)){
                            if(geq(e)){
                                dist.put(to,dist.get(u)+1);
                                queue.add(to);
                                lasthop.put(to,e);
                            } else {
                                dist.put(to,Integer.MAX_VALUE);
                            }
                        }
                    }
                }

            } catch (ReadFailedException e) {
                LOG.error("rt failed");
                Util.rn.incrementAndGet();
            }

            Util.count_stop();

        }

        private boolean geq(LinkIdType e) {
            return true;
        }

    }

}