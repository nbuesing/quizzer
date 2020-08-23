package com.github.nbuesing.quiz.requesthandler;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class Ab {

    private static byte[] keyBytes = "foo".getBytes();
    private static Partitioner partitioner = new DefaultPartitioner();
    private static Node node0 = new Node(0, "localhost", 99);
    private static Node node1 = new Node(1, "localhost", 100);
    private static Node node2 = new Node(2, "localhost", 101);
    private static Node[] nodes = new Node[] {node0, node1, node2};
    private static String topic = "xxxxxx";
    // Intentionally make the partition list not in partition order to test the edge cases.
    private static List<PartitionInfo> partitions = asList(new PartitionInfo(topic, 1, null, nodes, nodes),
            new PartitionInfo(topic, 4, node1, nodes, nodes),
            new PartitionInfo(topic, 3, node1, nodes, nodes),
            new PartitionInfo(topic, 5, node1, nodes, nodes),
            new PartitionInfo(topic, 2, node1, nodes, nodes),
            new PartitionInfo(topic, 0, node0, nodes, nodes));
    private static Cluster cluster = new Cluster("clusterId", asList(node0, node1, node2), partitions,
            Collections.<String>emptySet(), Collections.<String>emptySet());



    public static void main(String[] args) {

        System.out.println(partitioner.partition("xxxxxx",  null, "foo".getBytes(), null, null, cluster));
    }
}
