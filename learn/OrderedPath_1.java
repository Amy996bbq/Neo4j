package org.neo4j.learn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.neo4j.cypher.internal.v3_4.functions.Exp;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Paths;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.io.fs.FileUtils;

import static org.neo4j.graphdb.RelationshipType.withName;

public class OrderedPath_1
{
    private static final RelationshipType REL1 = withName( "REL1" ), REL2 = withName( "REL2" ),
            REL3 = withName( "REL3" );
    static final File databaseDirectory = new File("D:\\neo4j\\neo4j-community-3.5.21-windows\\neo4j-community-3.5.21\\data\\databases\\graph.db");
    GraphDatabaseService db;
    private long NodeId;

    public OrderedPath_1( GraphDatabaseService db )
    {
        this.db = db;
    }

    public static void main( String[] args ) throws IOException
    {
        FileUtils.deleteRecursively(databaseDirectory);
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase( databaseDirectory );
        OrderedPath_1 op = new OrderedPath_1(db);
        op.createTheGraph();

        System.out.println(op.printPaths());
        //op.shutdownGraph();
    }

    public void createTheGraph()
    {
        try ( Transaction tx = db.beginTx() )
        {
            // tag::createGraph[]
            Node A = db.createNode();
            NodeId = A.getId();
            Node B = db.createNode();
            Node C = db.createNode();
            Node D = db.createNode();

            A.createRelationshipTo( B, REL1 );
            C.createRelationshipTo( D, REL3 );
            A.createRelationshipTo( C, REL2 );
            B.createRelationshipTo( C, REL2 );
            // end::createGraph[]
            A.setProperty( "name", "A" );
            B.setProperty( "name", "B" );
            C.setProperty( "name", "C" );
            D.setProperty( "name", "D" );
            tx.success();
            //return A;
        }
    }

    public void shutdownGraph()
    {
        try
        {
            if ( db != null )
            {
                db.shutdown();
            }
        }
        finally
        {
            db = null;
        }
    }

    public Traverser findPaths( final Node nod)
    {
        // tag::walkOrderedPath[]
        final ArrayList<RelationshipType> orderedPathContext = new ArrayList<>();
        orderedPathContext.add( REL1 );
        orderedPathContext.add( withName( "REL2" ) );
        orderedPathContext.add( withName( "REL3" ) );
        TraversalDescription td = db.traversalDescription()
                .evaluator( new Evaluator()
                {
                    @Override
                    public Evaluation evaluate( final Path path )
                    {
                        if ( path.length() == 0 )
                        {
                            return Evaluation.EXCLUDE_AND_CONTINUE;
                        }
                        RelationshipType expectedType = orderedPathContext.get( path.length() - 1 );
                        System.out.println(expectedType.name());
                        boolean isExpectedType = path.lastRelationship()
                                .isType( expectedType );
                        boolean included = path.length() == orderedPathContext.size() && isExpectedType;
                        boolean continued = path.length() < orderedPathContext.size() && isExpectedType;
                        return Evaluation.of( included, continued ); //continued那个位置是serializable接口--不需要任何方法实现，唯一目的是声明其类是可以被序列化的
                    }
                } )
                .uniqueness( Uniqueness.NODE_PATH );
        // end::walkOrderedPath[]
        return td.traverse( nod );
    }

    private Node getNode()
    {
        return db.getNodeById( NodeId );
    }

    String printPaths(  )
    {
        try ( Transaction transaction = db.beginTx() )
        {
            String output = "";
            // tag::printPath[]
            Traverser traverser = findPaths(getNode());
            PathPrinter pathPrinter = new PathPrinter( "name" );
            for ( Path path : traverser )
            {
                output += Paths.pathToString( path, pathPrinter );
            }
            // end::printPath[]
            output += "\n";
            return output;
        }
    }

    // tag::pathPrinter[]
    static class PathPrinter implements Paths.PathDescriptor<Path>
    {
        private final String nodePropertyKey;

        public PathPrinter( String nodePropertyKey )
        {
            this.nodePropertyKey = nodePropertyKey;
        }

        @Override
        public String nodeRepresentation( Path path, Node node )
        {
            return "(" + node.getProperty( nodePropertyKey, "" ) + ")";
        }

        @Override
        public String relationshipRepresentation( Path path, Node from, Relationship relationship )
        {
            String prefix = "--", suffix = "--";
            if ( from.equals( relationship.getEndNode() ) )
            {
                prefix = "<--";
            }
            else
            {
                suffix = "-->";
            }
            return prefix + "[" + relationship.getType().name() + "]" + suffix;
        }
    }
    // end::pathPrinter[]
}