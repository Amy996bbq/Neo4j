package org.neo4j.learn;

import org.neo4j.graphalgo.*;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UniquenessNode {
    private static final File databaseDirectory = new File("D:\\neo4j\\neo4j-community-3.5.21-windows\\neo4j-community-3.5.21\\data\\databases\\graph.db" );
    private static GraphDatabaseService graphDb;

    public static void main(String[] args) throws IOException {
        UniquenessNode A = new UniquenessNode();
        A.setUp();
        A.creatUniqueConstraint();
        Node b = A.createUniqueNode("Bob");

    }
    public static void setUp() throws IOException {
        FileUtils.deleteRecursively(databaseDirectory);
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory);
        registerShutdownHook();
    }

    //create a unique constraint
    private void creatUniqueConstraint() {
        try(Transaction tx = graphDb.beginTx()) {
            graphDb.schema().constraintFor(Label.label("User"))
                            .assertPropertyIsUnique("name")
                            .create();
            tx.success();
        }
    }

    //create a unique node
    private Node createUniqueNode(String username) {
        Node result = null;
        ResourceIterator<Node> resultIterator = null;
        try ( Transaction tx = graphDb.beginTx() )
        {
            String queryString = "MERGE (n:User {name: {name}}) RETURN n";
            Map<String, Object> parameters = new HashMap<>();
            parameters.put( "name", username );
            resultIterator = graphDb.execute( queryString, parameters ).columnAs( "n" );
            result = resultIterator.next();
            tx.success();
            return result;
        }

    }

    public void shutDown() {
        graphDb.shutdown();
    }
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> graphDb.shutdown()));
    }
}
