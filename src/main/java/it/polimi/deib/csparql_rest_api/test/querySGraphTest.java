package it.polimi.deib.csparql_rest_api.test;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

/**
 * Created by Marco Balduini on 26/04/16 in the rsp-services-csparql project.
 */
public class querySGraphTest {

    private static String queryString = "PREFIX sld: <http://streamreasoning.org/ontologies/SLD4TripleWave#> " +
            "SELECT ?wsurl ?tboxurl ?aboxurl " +
            "WHERE {" +
            "?sGraph sld:streamLocation ?wsurl ; " +
            "sld:tBoxLocation ?tboxurl . " +
            "OPTIONAL { ?sGraph sld::staticaBoxLoxation ?aboxurl . } " +
            "}";

    private static Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);

    public static void main(String[] args) {
        Model sGraph = RDFDataMgr.loadModel("/users/baldo/Desktop/test.rdf", Lang.JSONLD);

        sGraph.write(System.out);

        QueryExecution qexec = QueryExecutionFactory.create(query, sGraph);

        System.out.println(ResultSetFormatter.asText(qexec.execSelect()));
    }

}
