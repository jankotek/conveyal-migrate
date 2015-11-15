package com.conveyal.osmlib;

/*
import org.mapdb.Fun;

import java.io.File;

//
// Creates test data in 1.0 format
// Is commented out to prevent compilation errors
//
public class CreateImportTestData {

    final static long TIMESTAMP = 123909190213L;
    final static long SEQUENCE_NUMBER = 43289094023904L;

    public static void main(String[] args) {
        File f = new File(args[0]);
        if(f.exists())
            throw new RuntimeException("File already exists "+f);


        OSM osm = new OSM(f.getPath());

        osm.timestamp.set(TIMESTAMP);
        osm.sequenceNumber.set(SEQUENCE_NUMBER);

        osm.nodes.put(11L, new Node(11,11));
        osm.nodes.put(22L, new Node(22,22));
        osm.nodes.put(33L, new Node(33,33));

        Way way = new Way();
        way.nodes = new long[]{5,55,555};
        osm.ways.put(55L, way);
        way = new Way();
        way.nodes = new long[]{6,66,666};
        osm.ways.put(66L, way);

        Relation relation = new Relation();
        Relation.Member member = new Relation.Member();
        member.id=111;
        member.type = OSMEntity.Type.NODE;
        member.role = "ROLE";
        relation.members.add(member);
        osm.relations.put(33L, relation);

        osm.index.add(new Fun.Tuple3(1,2,3L));
        osm.index.add(new Fun.Tuple3(4,5,6L));

        osm.db.commit();
        osm.db.close();

    }
}
*/
