package com.conveyal.osmlib;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class OSMImportTest {


    @Test
    public void test_import() throws IOException {
        File t = File.createTempFile("mapdb","osm");
        t.deleteOnExit();

        //run import
        OSMImport.main(new String[]{"data/osmTestData10",t.getPath()});

        OSM osm = new OSM(t.getPath());

        assertEquals(123909190213L, osm.timestamp.get());
        assertEquals(43289094023904L, osm.sequenceNumber.get());

        assertEquals(3, osm.nodes.size());
        assertEquals(new Node(11,11), osm.nodes.get(11L));
        assertEquals(new Node(22,22), osm.nodes.get(22L));
        assertEquals(new Node(33,33), osm.nodes.get(33L));


        assertEquals(2, osm.ways.size());
        assertArrayEquals(new long[]{5,55,555}, osm.ways.get(55L).nodes);
        assertArrayEquals(new long[]{6,66,666}, osm.ways.get(66L).nodes);

        assertEquals(1, osm.relations.size());
        Relation m = osm.relations.get(33L);
        assertEquals(111, m.members.get(0).id);
        assertEquals(OSMEntity.Type.NODE, m.members.get(0).type);
        assertEquals("ROLE", m.members.get(0).role);


        assertEquals(2, osm.index.size());
        assertTrue(osm.index.contains(OSM.indexMake(1,2,3L)));
        assertTrue(osm.index.contains(OSM.indexMake(4,5,6L)));

    }
}