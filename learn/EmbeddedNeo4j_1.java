package org.neo4j.learn;

import java.io.File;
import java.io.IOException;

import org.neo4j.examples.EmbeddedNeo4j;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;



public class EmbeddedNeo4j_1 {
    //设置DataBase地址
    private static final File databaseDirectory = new File("D:\\neo4j\\neo4j-community-3.5.21-windows\\neo4j-community-3.5.21\\data\\databases\\graph.db");
    //准备参数
    GraphDatabaseService graphDb;
    Node firstNode;
    Node secondNode;
    Relationship relationship;
    public static void main(String[] args) throws IOException{
        EmbeddedNeo4j_1 HW = new EmbeddedNeo4j_1();
        HW.createData();
        HW.addData();
        //HW.removeData();
        //HW.shutDown();
    }

    //创建关系类型
    private static enum RelTypes implements RelationshipType {
        KNOWS
    }

    //添加数据
    void createData() throws IOException{
        //删除Db文件夹之前的database
        FileUtils.deleteRecursively(databaseDirectory);

        //启动数据库服务（如果给定的数据库目录不存在则会自动创建）
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory);
        //registerShutdownHook是保证Database正常关闭的钩子
        registerShutdownHook(graphDb);
    }

    void addData() {
        //在一个事务中完成多次写数据库操作
        try (Transaction tx = graphDb.beginTx()) {
            //Updating operations go here
            firstNode = graphDb.createNode();
            firstNode.setProperty("message","Hello, ");
            secondNode = graphDb.createNode();
            secondNode.setProperty("message","World!");

            relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS);
            relationship.setProperty("message","braveNeo4j ");

            //readData
            System.out.println(firstNode.getProperty("message"));
            System.out.println(relationship.getProperty("message"));
            System.out.println(secondNode.getProperty("message"));

            tx.success();
        }
        //finally {
        //  tx.close();
        //}
    }

    //删除Database中的内容
    void removeData() {
        try (Transaction tx = graphDb.beginTx()) {
            firstNode.getSingleRelationship(RelTypes.KNOWS, Direction.OUTGOING).delete();
            firstNode.delete();
            secondNode.delete();

            tx.success();
        }
    }

    //关闭Database
    void shutDown() {
        System.out.println();
        System.out.println("Shutting down database...");
        graphDb.shutdown();
    }

    //保证Database正常关闭的钩子 -- private static void & final Db
    private static void registerShutdownHook(final GraphDatabaseService graphDb){
        Runtime.getRuntime().addShutdownHook( new Thread(){
                        @Override
                        public void run() {
                            graphDb.shutdown();
                        }
                });
    }
}

