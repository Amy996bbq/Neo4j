package org.neo4j.learn;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphalgo.*;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.io.fs.FileUtils;

public class EmbeddedNeo4jWithBolt {
    private static final File databaseDirectory = new File( "D:\\neo4j\\neo4j-community-3.5.21-windows\\neo4j-community-3.5.21\\data\\databases\\graph.db");
    public static GraphDatabaseService graphDb;

    public static void main( final String[] args ) throws IOException
    {
        EmbeddedNeo4jWithBolt A = new EmbeddedNeo4jWithBolt();
        A.setUp();
        A.creatDb();
    }

    public static void setUp() throws IOException {
        FileUtils.deleteRecursively(databaseDirectory);
        System.out.println( "Starting database ..." );

        // tag::startDb[]
        GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector( "0" );
        graphDb = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(databaseDirectory)
                .setConfig( bolt.type, "BOLT" )
                .setConfig( bolt.enabled, "true" )
                .setConfig( bolt.address, "localhost:7687" )
                .newGraphDatabase();
        // end::startDb[]
        registerShutdownHook();
    }

    private void creatDb() {
        try(Transaction tx = graphDb.beginTx()) {
            Node nodeA = graphDb.createNode();
            nodeA.setProperty("name", "Bob");
            Node nodeB = graphDb.createNode();
            nodeB.setProperty("name", "Amy");
            Node nodeC = graphDb.createNode();
            nodeC.setProperty("name", "Sam");
            tx.success();
        }
    }

    public void shutDown() {
        graphDb.shutdown();
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> graphDb.shutdown()));
    }
}