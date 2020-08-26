package org.neo4j.learn;

import org.neo4j.examples.EmbeddedNeo4j;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.Transaction;
import org.neo4j.io.fs.FileUtils;


import java.io.File;
import java.io.IOException;


public class Node_1 {
    public static final File databaseDirectory = new File("D:\\neo4j\\neo4j-community-3.5.21-windows\\neo4j-community-3.5.21\\data\\databases\\graph.db");
    private static GraphDatabaseService graphDb;
    Node node1;
    Node node2;
    private static Index<Node>nodeIndex;

    public static void main(String[] args) throws IOException{
        Node_1 A = new Node_1();
        A.createDb();
        A.setNode();
    }

    void createDb() throws IOException {
        FileUtils.deleteRecursively(databaseDirectory);
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory);
        registerShutdownHook(graphDb); //注意要传参数进去的啊 不能记得函数定义又忘记调用函数时传入参数

        try ( Transaction tx = graphDb.beginTx() )

        {
            // Database operations go here
            // end::transaction[]
            // tag::addData[]

            node1 = graphDb.createNode();
            node1.setProperty("name","easypoint");
            node2 = graphDb.createNode();
            node2.setProperty("name","csdn");

            nodeIndex = graphDb.index().forNodes( "nodes" );
            nodeIndex.add(node1,"name",node1.getProperty("name"));
            nodeIndex.add(node1,"name","haha");
            nodeIndex.add(node2,"name",node2.getProperty("name"));
            nodeIndex.add(node2,"name","gugu");

            for(Node node :nodeIndex.get("name","haha")){

                System.out.println(node.getProperty("name"));

            }
            // tag::transaction[]

            tx.success();
        }
        // end::transaction[]
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb){
        Runtime.getRuntime().addShutdownHook( new Thread(){
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }
    void shutDown() {
        System.out.println();
        System.out.println("Shutting down database now");
        graphDb.shutdown();
    }

    void setNode() {


    }
}

