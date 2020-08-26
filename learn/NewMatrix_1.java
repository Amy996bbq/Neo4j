package org.neo4j.learn;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Path;
//import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.io.fs.FileUtils;

public class NewMatrix_1 {

    public enum RelTypes implements RelationshipType {
        NEO_NODE,  KNOWS, CODED_BY
    }
    private static final File databaseDirectory = new File("D:\\neo4j\\neo4j-community-3.5.21-windows\\neo4j-community-3.5.21\\data\\databases\\graph.db");
    private GraphDatabaseService graphDb;
    private long matrixNodeID;

    public static void main(String[] args) throws IOException {
        NewMatrix_1 A = new NewMatrix_1();
        A.setUp();
        System.out.println(A.printNeoFriends());
        System.out.println(A.printMatrixHackers());
    }

    public void setUp() throws IOException{
        FileUtils.deleteRecursively(databaseDirectory);
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory);
        registerShutdownHook();
        createNodespace();
    }

    private void createNodespace() {
        try (Transaction tx = graphDb.beginTx()) {
            Node matrix = graphDb.createNode();
            matrixNodeID = matrix.getId();

            System.out.println(matrixNodeID);

            Node thomas = graphDb.createNode();
            //matrixNodeID = thomas.getId();
            thomas.setProperty("name", "Thomas Anderson");
            thomas.setProperty("age", "29");
            matrix.createRelationshipTo(thomas, RelTypes.NEO_NODE);

            Node trinity = graphDb.createNode();
            //matrixNodeID = trinity.getId();
            trinity.setProperty("name", "Trinity");
            Relationship rel = thomas.createRelationshipTo(trinity, RelTypes.KNOWS);
            rel.setProperty("age", "3 days");

            Node morpheus = graphDb.createNode();
            //matrixNodeID = morpheus.getId();
            morpheus.setProperty("name", "Morpheus");
            morpheus.setProperty("occupation", "Total badass");
            morpheus.setProperty("rank", "Captain");
            Relationship rel1 = morpheus.createRelationshipTo(trinity, RelTypes.KNOWS);
            rel.setProperty("age", "12 years");
            thomas.createRelationshipTo(morpheus, RelTypes.KNOWS);

            Node cypher = graphDb.createNode();
            //matrixNodeID = cypher.getId();
            cypher.setProperty("last name", "Reagan");
            cypher.setProperty("name", "Cypher");
            Relationship rel2 = morpheus.createRelationshipTo(cypher, RelTypes.KNOWS);
            rel.setProperty("disclosure", "public");

            Node smith = graphDb.createNode();
            //matrixNodeID = smith.getId();
            smith.setProperty("language", "C++");
            smith.setProperty("name", "Agent Smith");
            smith.setProperty("version", "1.0b");
            Relationship rel3 = cypher.createRelationshipTo(smith, RelTypes.KNOWS);
            rel3.setProperty("age", "6 months");
            rel3.setProperty("disclosure", "secret");

            Node architect = graphDb.createNode();
            //matrixNodeID = architect.getId();
            architect.setProperty("name", "The Architect");
            smith.createRelationshipTo(architect, RelTypes.CODED_BY);

            System.out.println(matrixNodeID + "2th");
            tx.success();
        }
    }

    //return the Neo Node
    private Node getNeoNode() {
        return graphDb.getNodeById(matrixNodeID)
                .getSingleRelationship(RelTypes.NEO_NODE, Direction.OUTGOING)
                .getEndNode();
    }

    public String printNeoFriends() {
        try ( Transaction tx = graphDb.beginTx()) {
            Node neoNode = getNeoNode();
            int numOfFriends = 0;
            String output = neoNode.getProperty("name") + "'s friends:\n";
            Traverser friendsTraverser = getFriends(neoNode);
            //System.out.println(friendsTraverser);
            for (Path friendPath : friendsTraverser) {
                output += "At depth" + friendPath.length() + "=>"
                        +friendPath.endNode()
                        .getProperty("name") + "\n";
                numOfFriends++;
            }
            output += "Number of friends found " + numOfFriends + "\n";
            return output;
        }
    }

    private Traverser getFriends(final Node person) {
        TraversalDescription td = graphDb.traversalDescription()
                .breadthFirst()
                .relationships(RelTypes.KNOWS, Direction.OUTGOING)
                .evaluator(Evaluators.excludeStartPosition());
        return td.traverse(person);
    }

    public String printMatrixHackers() {
        try (Transaction tx = graphDb.beginTx()) {
            String output = "Hackers:\n";
            int numOfHackers = 0;
            Traverser traverser = findHackers(getNeoNode());
            for(Path hackerPath : traverser) {
                output += "At depth " + hackerPath.length() + "=>"
                        + hackerPath.endNode()
                        .getProperty("name") + "\n";
                numOfHackers++;
            }
            output += "Nmuber of hackers found: " + numOfHackers + "\n";
            return output;
        }
    }

    private Traverser findHackers(final Node startNode) {
            TraversalDescription td = graphDb.traversalDescription()
                    .breadthFirst()
                    .relationships(RelTypes.CODED_BY, Direction.OUTGOING)
                    .relationships(RelTypes.KNOWS, Direction.OUTGOING)
                    .evaluator(
                            Evaluators.includeWhereLastRelationshipTypeIs(RelTypes.CODED_BY));
            return td.traverse(startNode);
    }

    public void shutdown() {
        graphDb.shutdown();
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(()->graphDb.shutdown()));
    }
}
