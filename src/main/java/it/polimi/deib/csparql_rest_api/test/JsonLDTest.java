package it.polimi.deib.csparql_rest_api.test;


import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.utils.JsonUtils;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * Created by Marco Balduini on 21/04/16 in the rsp-services-csparql project.
 */
public class JsonLDTest {

    public static void main(String[] args) throws IOException {
        String s = readFile("/Users/baldo/Desktop/jld.jsonld", Charset.forName("UTF8"));
        Model m = deserializizeAsJsonSerialization(s, new JsonLdOptions());
        m.write(System.out);

    }

    public static Model deserializizeAsJsonSerialization(String asJsonSerialization, JsonLdOptions options){

//		System.out.println(asJsonSerialization);
//		logger.info("Input string {}", asJsonSerialization);

        Model model = ModelFactory.createDefaultModel();
        try {
            Object jsonObject = null;
            RDFDataset rd = null;

            try {
                jsonObject = JsonUtils.fromString(asJsonSerialization);

                if (options != null)
                    rd = (RDFDataset) JsonLdProcessor.toRDF(jsonObject, options);
                else
                    rd = (RDFDataset) JsonLdProcessor.toRDF(jsonObject);

            } catch (Exception e) {
                e.printStackTrace();
            }

            Set<String> graphNames = rd.graphNames();

            for (String graphName : graphNames){

                List<RDFDataset.Quad> l = rd.getQuads(graphName);

                ResourceImpl subject;
                PropertyImpl predicate;
                ResourceImpl object;

                for (com.github.jsonldjava.core.RDFDataset.Quad q : l) {
                    if (q.getSubject().isBlankNode()) {
                        AnonId aid = new AnonId(q.getSubject().getValue());
                        subject = new ResourceImpl(aid);
                    } else {
                        subject = new ResourceImpl(q.getSubject().getValue());
                    }

                    predicate = new PropertyImpl(q.getPredicate().getValue());

                    if (!q.getObject().isLiteral()) {
                        if (q.getObject().isBlankNode()) {
                            AnonId aid = new AnonId(q.getObject().getValue());
                            object = new ResourceImpl(aid);
                        } else {
                            object = new ResourceImpl(q.getObject().getValue());
                        }
                        model.add(subject, predicate, object);
                    } else {
                        model.add(subject, predicate, model.createLiteral(q.getObject().getValue(), q.getObject().getDatatype()));
                    }
                }
            }
            return model;
        } catch (Exception e) {
            e.printStackTrace();
            return ModelFactory.createDefaultModel();
        }
    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
