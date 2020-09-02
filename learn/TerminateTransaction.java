package org.neo4j.learn;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionTerminatedException;

import org.neo4j.io.fs.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;

public class TerminateTransaction {
    private static final File databaseDirectory = new File("D:\\neo4j\\neo4j-community-3.5.21-windows\\neo4j-community-3.5.21\\data\\databases\\graph.db");
    public static GraphDatabaseService graphDb;
    public static long millis = 1000;

    public static void main(String[] args) throws IOException{
        System.out.println( new TerminateTransaction().run() );
    }

    public String run() throws IOException {
        FileUtils.deleteRecursively(databaseDirectory);

        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory);
        RelationshipType relType = RelationshipType.withName("CHILD");
        Queue<Node> nodes = new LinkedList<>();
        int depth = 1;

        try(Transaction tx = graphDb.beginTx()) {
            Node rootNode = graphDb.createNode();
            nodes.add(rootNode);

            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    long startTime = System.currentTimeMillis();
                    do {
                        try {
                            Thread.sleep(millis);
                        }
                        catch(InterruptedException ignored) {
                            //terminated while sleeping
                        }
                    }
                    while((System.currentTimeMillis() - startTime) < millis);

                    tx.terminate();
                }
            });

            for(;true;depth++) {
                int nodesToExpand = nodes.size();
                for (int i=0; i<nodesToExpand; ++i) {
                    Node parent = nodes.remove();
                    Node left = graphDb.createNode();
                    Node right = graphDb.createNode();

                    parent.createRelationshipTo(left, relType);
                    parent.createRelationshipTo(right, relType);

                    nodes.add(left);
                    nodes.add(right);
                }
            }
        }
        catch (TransactionTerminatedException ignored) {
            return String.format("Created tree up to depth %s in 1 sec", depth);
        }
        finally {
            graphDb.shutdown();
        }
    }
}
