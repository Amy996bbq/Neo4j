package org.neo4j.learn;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.io.fs.FileUtils;

public class Uniqueness_pet {
    private static final File databaseDirectory = new File("D:\\neo4j\\neo4j-community-3.5.21-windows\\neo4j-community-3.5.21\\data\\databases\\graph.db");
    private GraphDatabaseService graphDb;
    private long pet0NodeId, P1NodeId;
    public enum RelTypes implements RelationshipType {
        OWNS,DESCENDENT;
    }

    public static void main(String[] args) throws IOException{
        Uniqueness_pet A = new Uniqueness_pet();
        A.setUp();
        A.createDb();
        System.out.println(A.printNode());
    }


    public void setUp() throws IOException {
        FileUtils.deleteRecursively(databaseDirectory);
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory);
        registerShutdownHook();
    }

    private void createDb() {
        try(Transaction tx = graphDb.beginTx()) {
            Node pet3 = graphDb.createNode();
            Node P1 = graphDb.createNode();
            Node pet0 = graphDb.createNode();
            Node P2 = graphDb.createNode();
            Node pet2 = graphDb.createNode();
            Node pet1 = graphDb.createNode();

            pet0.setProperty("name", "pet0");
            pet1.setProperty("name", "pet1");
            pet2.setProperty("name", "pet2");
            pet3.setProperty("name", "pet3");
            P1.setProperty("name", "principal1");
            P2.setProperty("name", "principal2");

            P1.createRelationshipTo(pet1, RelTypes.OWNS);
            P1.createRelationshipTo(pet3, RelTypes.OWNS);
            P2.createRelationshipTo(pet2, RelTypes.OWNS);
            pet0.createRelationshipTo(pet1, RelTypes.DESCENDENT);
            pet0.createRelationshipTo(pet2, RelTypes.DESCENDENT);
            pet0.createRelationshipTo(pet3, RelTypes.DESCENDENT);

            pet0NodeId = pet0.getId();
            P1NodeId = P1.getId();
            tx.success();
        }
    }

    private Traverser getPets(final Node startNode) {
        Node endNode = graphDb.getNodeById(P1NodeId);
        TraversalDescription td = graphDb.traversalDescription()
                .breadthFirst()
                .relationships(RelTypes.DESCENDENT,Direction.OUTGOING)
                .relationships(RelTypes.OWNS,Direction.INCOMING)
                .uniqueness( Uniqueness.NODE_GLOBAL )
                .evaluator(Evaluators.includeWhereEndNodeIs(endNode));
        return td.traverse(startNode);
    }

    private String printNode() {
        try (Transaction tx1 = graphDb.beginTx()) {
            int count = 0;
            Node startNode = graphDb.getNodeById(pet0NodeId);
            //String output = "Principal1 owns" + "\n";
            String output = "";
            Traverser ownerTraverser = getPets(startNode);
            System.out.println(ownerTraverser);
            for (Path petpath : ownerTraverser) {
                output += petpath.toString() + "\n";
                count++;
            }
            return output;
        }
    }

    private void shutdown() {
        graphDb.shutdown();
    }
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(()->graphDb.shutdown()));
    }
}

