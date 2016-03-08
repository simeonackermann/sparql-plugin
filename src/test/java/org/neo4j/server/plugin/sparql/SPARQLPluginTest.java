/**
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.plugin.sparql;

import static org.junit.Assert.assertTrue;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.impls.sail.SailGraph;
import com.tinkerpop.blueprints.impls.sail.impls.LinkedDataSailGraph;
import com.tinkerpop.blueprints.oupls.sail.GraphSail;
import info.aduna.iteration.CloseableIteration;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.server.rest.repr.OutputFormat;
import org.neo4j.server.rest.repr.Representation;
import org.neo4j.server.rest.repr.formats.JsonFormat;
import org.neo4j.test.ImpermanentGraphDatabase;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

public class SPARQLPluginTest
{

    private static SPARQLPlugin plugin = null;
    private static OutputFormat json = null;
    private static GraphDatabaseService neo4j;

    @Before
    public void setUpBeforeClass() throws Exception
    {
        json = new OutputFormat( new JsonFormat(),
                new URI( "http://localhost/" ), null );
        neo1 = new Neo4j2Graph( new TestGraphDatabaseFactory().newImpermanentDatabase());
//        neo1.setMaxBufferSize( 20000 );
        // Neo4jBatchGraph neo1 = new Neo4jBatchGraph( "target/db1" );
        plugin = new SPARQLPlugin();
        neo4j = new GraphDatabaseFactory().newEmbeddedDatabase("target/db1");

    }

    public static Sail insertData( Neo4j2Graph neo1 ) throws SailException,
            RepositoryException
    {
        Sail sail = new GraphSail( neo1 );
        sail.initialize();
        SailRepositoryConnection sc = new SailRepository( sail ).getConnection();
        ValueFactory vf = sail.getValueFactory();
        sc.add( vf.createURI( "http://neo4j.org#joe" ),
                vf.createURI( "http://neo4j.org#knows" ),
                vf.createURI( "http://neo4j.org#sara" ),
                vf.createURI( "http://neo4j.org" ) );
        sc.add( vf.createURI( "http://neo4j.org#joe" ),
                vf.createURI( "http://neo4j.org#name" ),
                vf.createLiteral( "joe" ), vf.createURI( "http://neo4j.org" ) );
        sc.commit();
        CloseableIteration<Statement, RepositoryException> results = sc.getStatements(
                null, null, null, false );
        System.out.println( "dump-----------" );
        while ( results.hasNext() )
        {
            System.out.println( results.next() );
        }
        System.out.println( "dump end-----------" );
        sc.close();
        return sail;
    }

    private static Representation executeSelect( final String script, Map params )
    {
        return plugin.executeSPARQL( neo4j, script, params );
    }

    private static String queryString = "" + "SELECT ?x ?y " + "WHERE { "
                                        + "?x <http://neo4j.org#knows> ?y ."
                                        + "}";
    private static Neo4j2Graph neo1;

    @Test
    @Ignore
    public void executeSelect() throws Exception
    {
        insertData( neo1 );
        Representation result = SPARQLPluginTest.executeSelect( queryString,
                new HashMap() );
//        String format = json.format( result );
//        assertTrue( format.contains( "sara" ) );
//        assertTrue( format.contains( "joe" ) );
    }

    @Test
    public void executeInsert() throws Exception
    {
        Representation result = plugin.executeInsert( neo4j,
                "http://neo4j.org#joe", "http://neo4j.org#knows",
                "http://neo4j.org#sara", "http://neo4j.org" );
        result = plugin.executeInsert( neo4j,
                "http://neo4j.org#joe", "http://neo4j.org#name",
                "joe", "http://neo4j.org" );
        result = SPARQLPluginTest.executeSelect( queryString,
                new HashMap() );
//        String format = json.format( result );
//        assertTrue( format.contains( "sara" ) );
//        assertTrue( format.contains( "joe" ) );
    }

    @After
    public void cleanUp()
    {
        neo1.shutdown();
        neo4j.shutdown();
    }
    
    
    @Test
    public void testLinkedData() {
        LinkedDataSailGraph linkedDataSailGraph = new LinkedDataSailGraph(new SailGraph(new GraphSail(neo1)));
        Vertex vertex = linkedDataSailGraph.addVertex("http://dbpedia.org/resource/ABBA");
        Iterable<Vertex> vertices = vertex.getVertices(Direction.OUT);
        assertTrue(vertices.iterator().hasNext());
//        System.out.println(vertices.iterator().next());


    }

}
