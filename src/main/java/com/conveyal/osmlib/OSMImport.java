package com.conveyal.osmlib;

import com.conveyal.osmlib.serializer.VarInt;
import org.mapdb.DataIO;

import java.io.*;
import java.util.Map;
import java.util.Set;

/**
 * Converts OSM-LIB storage  from MapDB 1.0 format to 2.0 format
 */
public class OSMImport {

    static class NodeSerializerReplacement implements org.mapdb10.Serializer<Node>,Serializable {

        @Override
        public void serialize(DataOutput out, Node node) throws IOException {
            out.writeInt(node.fixedLat);
            out.writeInt(node.fixedLon);
            VarInt.writeTags(out, node);
        }

        @Override
        public Node deserialize(DataInput in, int avail) throws IOException {
            Node node = new Node();
            node.fixedLat = in.readInt();
            node.fixedLon = in.readInt();
            VarInt.readTags(in, node);
            return node;
        }

        @Override
        public int fixedSize() {
            return -1;
        }
    }

    static  class WaySerializerReplacement implements org.mapdb10.Serializer<Way>, Serializable {

        /** Delta-code the series of node references, and write out all values as varints. */
        @Override
        public void serialize(DataOutput out, Way way) throws IOException {
            VarInt.writeRawVarint32(out, way.nodes.length);
            long lastNodeId = 0;
            for (int i = 0; i < way.nodes.length; i++) {
                long delta = way.nodes[i] - lastNodeId;
                VarInt.writeSInt64NoTag(out, delta);
                lastNodeId = way.nodes[i];
            }
            VarInt.writeTags(out, way);
        }

        @Override
        public Way deserialize(DataInput in, int available) throws IOException {
            Way way = new Way();
            int nNodes = VarInt.readRawVarint32(in);
            way.nodes = new long[nNodes];
            long lastNodeId = 0;
            for (int i = 0; i < nNodes; i++) {
                lastNodeId += VarInt.readSInt64(in);
                way.nodes[i] = lastNodeId;
            }
            VarInt.readTags(in, way);
            return way;
        }

        @Override
        public int fixedSize() {
            return -1;
        }

    }


    public static void main(String[] args) {
        if(args==null || args.length!=2){
            System.out.println("Parameters: input (MapDB 1 file)   output (MapDB 2 file)");
            return;
        }
        File db1File = new File(args[0]);
        if(!db1File.exists()){
            System.out.println("Input MapDB 1 file does not exists");
            return;
        }

        File db2File = new File(args[1]);
        if(!db2File.exists()){
            System.out.println("Output MapDB 2 file already exists");
            return;
        }

        org.mapdb10.DB db1 = org.mapdb10.DBMaker
                .newFileDB(db1File)
                .compressionEnable()
                .make();

        //override Node serializer, it will be rolledback with tx
        db1.getCatalog().put("nodes.valueSerializer", new NodeSerializerReplacement());
        db1.getCatalog().put("ways.valueSerializer", new WaySerializerReplacement());

        OSM osm = new OSM(db2File.getPath());

        {
            long counter = 0;
            System.out.println("Nodes: started");
            Map<Long,Node> nodes1 = db1.getTreeMap("nodes");
            for(Map.Entry<Long,Node> e:nodes1.entrySet()){
                counter++;
                osm.nodes.put(e.getKey(), e.getValue());
            }
            System.out.println("Nodes: done, "+counter+" imported");
        }

        {
            long counter = 0;
            System.out.println("Ways: started");
            Map<Long,Way> ways = db1.getTreeMap("ways");
            for(Map.Entry<Long,Way> e:ways.entrySet()){
                counter++;
                osm.ways.put(e.getKey(), e.getValue());
            }
            System.out.println("Ways: done, "+counter+" imported");
        }

        {
            long counter = 0;
            System.out.println("Relations: started");
            Map<Long,Relation> relations = db1.getTreeMap("relations");
            for(Map.Entry<Long,Relation> e:relations.entrySet()){
                counter++;
                osm.relations.put(e.getKey(), e.getValue());
            }
            System.out.println("Relations: done, "+counter+" imported");
        }

        {
            long counter = 0;
            System.out.println("Spatial_index: started");
            Set<org.mapdb10.Fun.Tuple3<Integer,Integer,Long>> spatial_index = db1.getTreeSet("spatial_index");
            for(org.mapdb10.Fun.Tuple3<Integer,Integer,Long> e:spatial_index){
                byte[] b = new byte[16];
                OSM.setInt(b, 0, e.a);
                OSM.setInt(b, 4, e.b);
                DataIO.putLong(b,8,e.c);
                counter++;
                osm.index.add(b);
            }
            System.out.println("Spatial_index: done, "+counter+" imported");
        }

        {
            System.out.println("Timestamp");
            long timestampVal = db1.getAtomicLong("timestamp").get();
            osm.timestamp.set(timestampVal);
        }

        {
            System.out.println("Sequence_number");
            long sequence_numberVal = db1.getAtomicLong("sequence_number").get();
            osm.sequenceNumber.set(sequence_numberVal);
        }


        db1.rollback();
        db1.close();
        new File(db1File.getPath()+".t").delete();
        osm.close();

    }
}
