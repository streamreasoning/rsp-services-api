/*******************************************************************************
 * Copyright 2014 DEIB - Politecnico di Milano
 *
 * Marco Balduini (marco.balduini@polimi.it)
 * Emanuele Della Valle (emanuele.dellavalle@polimi.it)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This work was partially supported by the European project LarKC (FP7-215535) and by the European project MODAClouds (FP7-318484)
 ******************************************************************************/
package it.polimi.deib.csparql_rest_api;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import it.polimi.deib.csparql_rest_api.exception.ObserverErrorException;
import it.polimi.deib.csparql_rest_api.exception.QueryErrorException;
import it.polimi.deib.csparql_rest_api.exception.ServerErrorException;
import it.polimi.deib.csparql_rest_api.exception.StaticKnowledgeErrorException;
import it.polimi.deib.csparql_rest_api.exception.StreamErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
/**
 *
 * @author Marco Balduini
 *
 */
public class RSP_services_csparql_API {

    private String serverAddress;
    private URI uri;

    private CloseableHttpClient client;
    private HttpResponse httpResponse;
    private HttpEntity httpEntity;
    private RequestConfig requestConfig;

    private ArrayList<BasicNameValuePair> formparams;
    private UrlEncodedFormEntity requestParamsEntity;
    private PoolingHttpClientConnectionManager cm;

    private Logger logger = LoggerFactory.getLogger(RSP_services_csparql_API.class.getName());
    private Gson gson;

    public RSP_services_csparql_API(String serverAddress) {
        super();
        this.serverAddress = serverAddress;
        cm = new PoolingHttpClientConnectionManager();
        client = HttpClientBuilder
                .create()
                .setConnectionManager(cm)
                .build();
        gson = new Gson();

        requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .build();
    }


    //Streams

    /**
     * Register new RDF Stream into engine
     * @param inputStreamName name of the new stream. The name of the stream needs to be a valid URI.
     * @return json response from server.
     * @throws ServerErrorException
     * @throws StreamErrorException
     */
    public String registerStream(String inputStreamName, String inputStreamIRI) throws ServerErrorException, StreamErrorException{
        HttpPut method = null;
        String httpEntityContent;

        try{

            uri = new URI(serverAddress + "/streams/" + inputStreamName);

            method = new HttpPut(uri);
            method.setConfig(requestConfig);

            method.setHeader("Cache-Control","no-cache");

            formparams = new ArrayList<BasicNameValuePair>();
            formparams.add(new BasicNameValuePair("streamIri",inputStreamIRI));
            requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");

            method.setEntity(requestParamsEntity);

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new StreamErrorException("Error while registering stream " + inputStreamName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        } catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";
    }

    /**
     * Unregister specified RDF Stream from engine
     * @param inputStreamName name of the stream to unregister.
     * @return json response from server.
     * @throws ServerErrorException
     * @throws StreamErrorException
     */
    public String unregisterStream(String inputStreamName) throws ServerErrorException, StreamErrorException{
        HttpDelete method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/streams/" + inputStreamName);

            method = new HttpDelete(uri);
            method.setConfig(requestConfig);

            method.setHeader("Cache-Control","no-cache");

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new StreamErrorException("Error while unregistering stream " + inputStreamName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        }  catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";
    }

    /**
     * Put new data into specified RDF Stream
     * @param inputStreamName name of the stream
     * @param RDF_Data_Serialization RDF/Json serialization of data to put into stream
     * @return json response from server.
     * @throws ServerErrorException
     * @throws StreamErrorException
     */
    public String feedStream(String inputStreamName, String RDF_Data_Serialization) throws ServerErrorException, StreamErrorException{
        HttpPost method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/streams/" + inputStreamName);

            method = new HttpPost(uri);
            method.setConfig(requestConfig);

            method.setHeader("Cache-Control","no-cache");

            formparams = new ArrayList<BasicNameValuePair>();
            formparams.add(new BasicNameValuePair("payload",RDF_Data_Serialization));
            requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            method.setEntity(requestParamsEntity);

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new StreamErrorException("Error while feeding stream " + inputStreamName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        } catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";
    }

    /**
     * Put new data into specified RDF Stream
     * @param inputStreamName name of the stream
     * @param model Jena Model containing data to put into stream
     * @return json response from server.
     * @throws StreamErrorException
     * @throws ServerErrorException
     */
    public String feedStream(String inputStreamName, Model model) throws StreamErrorException, ServerErrorException{
        HttpPost method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/streams/" + inputStreamName);

            method = new HttpPost(uri);
            method.setConfig(requestConfig);
            method.setHeader("Cache-Control","no-cache");

            StringWriter w = new StringWriter();

            model.write(w,"RDF/JSON");

            method.addHeader("content-type", "application/json");
            String jsonModel = w.toString();
            logger.debug("Feeding stream with model:\n{}", jsonModel);

            formparams = new ArrayList<BasicNameValuePair>();
            formparams.add(new BasicNameValuePair("payload",jsonModel));
            requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            method.setEntity(requestParamsEntity);

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new StreamErrorException("Error while feeding stream " + inputStreamName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        }  catch (IOException e) {
            method.abort();
            throw new ServerErrorException("Unreachable Host");
        }

        return "Error";
    }

    /**
     * Get information about specific stream
     * @param inputStreamName name of the stream
     * @return json serialization of stream informations
     * @throws ServerErrorException
     * @throws StreamErrorException
     */
    public String getStreamInfo(String inputStreamName) throws ServerErrorException, StreamErrorException{
        HttpGet method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/streams/" + inputStreamName);

            method = new HttpGet(uri);
            method.setConfig(requestConfig);
            method.setHeader("Cache-Control","no-cache");

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new StreamErrorException("Error while getting information about stream " + inputStreamName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        }  catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";
    }

    /**
     * Get information about all the streams registered on the engine
     * @return json serialization of streams informations
     * @throws ServerErrorException
     * @throws StreamErrorException
     */
    public String getStreamsInfo() throws ServerErrorException, StreamErrorException{
        HttpGet method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/streams");

            method = new HttpGet(uri);
            method.setConfig(requestConfig);
            method.setHeader("Cache-Control","no-cache");

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new StreamErrorException("Error while getting information about streams\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        } catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";
    }


    //Queries

    /**
     * Method to register a new query into the engine.
     * @param queryName name of the new query (this name must match the name specified in the body)
     * @param queryBody string representing the query in C-SPARQL language .
     * @return json representation of query ID
     * @throws ServerErrorException
     * @throws QueryErrorException
     */
    public String registerQuery(String queryName, String queryBody) throws ServerErrorException, QueryErrorException{

        logger.debug("Registering query: {}", queryBody);

        HttpPut method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/queries/" + queryName);

            method = new HttpPut(uri);
            method.setConfig(requestConfig);
            method.setHeader("Cache-Control","no-cache");

            formparams = new ArrayList<BasicNameValuePair>();
            formparams.add(new BasicNameValuePair("queryBody",queryBody));
            requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            method.setEntity(requestParamsEntity);

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new QueryErrorException("Error while registering query " + queryName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        } catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";
    }

    public String unregisterQuery(String queryName) throws ServerErrorException, QueryErrorException{
        HttpDelete method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/queries/" + queryName);

            method = new HttpDelete(uri);
            method.setConfig(requestConfig);
            method.setHeader("Cache-Control","no-cache");

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new QueryErrorException("Error while deleting query " + queryName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        } catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";
    }

    public String getQueryInfo(String queryName) throws ServerErrorException, QueryErrorException{
        HttpGet method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/queries/" + queryName);

            method = new HttpGet(uri);
            method.setConfig(requestConfig);
            method.setHeader("Cache-Control","no-cache");

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new QueryErrorException("Error while getting information about query " + queryName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        } catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";
    }

    /**
     * Method to get information about queries.
     * @return json serialization of queries informations.
     * @throws ServerErrorException
     * @throws QueryErrorException
     */
    public String getQueriesInfo() throws ServerErrorException, QueryErrorException{
        HttpGet method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/queries");

            method = new HttpGet(uri);
            method.setConfig(requestConfig);
            method.setHeader("Cache-Control","no-cache");

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {}: {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new QueryErrorException("Error while getting information about queries\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        } catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";
    }

    public String pauseQuery(String queryName) throws ServerErrorException, QueryErrorException{
        HttpPost method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/queries/" + queryName);

            method = new HttpPost(uri);

            method.setHeader("Cache-Control","no-cache");

            formparams = new ArrayList<BasicNameValuePair>();
            formparams.add(new BasicNameValuePair("action", "pause"));
            requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");

            method.setEntity(requestParamsEntity);

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new QueryErrorException("Error while pausing query " + queryName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        } catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";
    }

    public String restartQuery(String queryName) throws ServerErrorException, QueryErrorException{
        HttpPost method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/queries/" + queryName);

            method = new HttpPost(uri);

            method.setHeader("Cache-Control","no-cache");

            formparams = new ArrayList<BasicNameValuePair>();
            formparams.add(new BasicNameValuePair("action", "restart"));
            requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");

            method.setEntity(requestParamsEntity);

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new QueryErrorException("Error while restarting query " + queryName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        } catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";
    }


    //Observer

    public String addObserver(String queryName, String host, int port) throws ServerErrorException, ObserverErrorException{

        HttpPost method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/queries/" + queryName);

            method = new HttpPost(uri);

            method.setHeader("Cache-Control","no-cache");

            method.addHeader("content-type", "text/plain");

            formparams = new ArrayList<BasicNameValuePair>();
            formparams.add(new BasicNameValuePair("action", "addobserver"));
            formparams.add(new BasicNameValuePair("host", host));
            formparams.add(new BasicNameValuePair("port", String.valueOf(port)));
            requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");

            method.setEntity(requestParamsEntity);

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new ServerErrorException("Error while adding observer to query " + queryName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        } catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";

    }

    public String deleteObserver(String queryName, String observerID) throws ServerErrorException, ObserverErrorException{

        HttpDelete method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/queries/" + queryName + "/observers/" + observerID);

            method = new HttpDelete(uri);
            method.setConfig(requestConfig);
            method.setHeader("Cache-Control","no-cache");

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI " + uri.toString() + " : " + httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new ServerErrorException("Error while deleting observer " + observerID + " of query " + queryName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        } catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";

    }

    public String getObserverInformations(String queryName, String observerID) throws ServerErrorException, ObserverErrorException{

        HttpGet method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/queries/" + queryName + "/observers/" + observerID);

            method = new HttpGet(uri);
            method.setConfig(requestConfig);
            method.setHeader("Cache-Control","no-cache");

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {}: {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI " + uri.toString() + " : " + httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new ServerErrorException("Error while getting information about observer " + observerID + " of query " + queryName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        } catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";

    }

    public String getObserversInformations(String queryName) throws ServerErrorException, ObserverErrorException{

        HttpGet method = null;
        String httpEntityContent;

        try{
            uri = new URI(serverAddress + "/queries/" + queryName + "/observers");

            method = new HttpGet(uri);
            method.setConfig(requestConfig);
            method.setHeader("Cache-Control","no-cache");

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI {}: {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

            httpResponse = client.execute(method);
            httpEntity = httpResponse.getEntity();
            logger.debug("HTTPResponse code for URI " + uri.toString() + " : " + httpResponse.getStatusLine().getStatusCode());

            if(httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300){
                return gson.fromJson(IOUtils.toString(httpEntity.getContent(), "UTF-8"), String.class);
            } else {
                throw new ServerErrorException("Error while getting information about observers of query " + queryName + "\n" +
                        "ERROR: " + IOUtils.toString(httpEntity.getContent(), "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("error while encoding", e);
            method.abort();
        } catch (URISyntaxException e) {
            logger.error("error while creating URI", e);
        } catch (ClientProtocolException e) {
            logger.error("error while calling rest service", e);
            method.abort();
        } catch (IOException e) {
            method.abort();
            throw new ServerErrorException("unreachable host");
        }

        return "Error";

    }

//    //Static Knowledge
//
//    /**
//     * Method to launch a SPARQL Update query against static knowloedge
//     * @param queryBody string representing the query in SPARQL language .
//     * @return json representation of server response
//     * @throws ServerErrorException
//     * @throws QueryErrorException
//     */
//    public String launchUpdateQuery(String queryBody) throws ServerErrorException, QueryErrorException{
//
//        logger.debug("Launching update query: {}", queryBody);
//
//        HttpPost method = null;
//        String httpEntityContent;
//
//        try{
//            uri = new URI(serverAddress + "/kb");
//
//            method = new HttpPost(uri);
//
//            method.setHeader("Cache-Control","no-cache");
//
//            formparams = new ArrayList<BasicNameValuePair>();
//            formparams.add(new BasicNameValuePair("action", "update"));
//            formparams.add(new BasicNameValuePair("queryBody", queryBody));
//            requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
//
//            method.setEntity(requestParamsEntity);
//
//            httpResponse = client.execute(method);
//            httpEntity = httpResponse.getEntity();
//            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());
//
//            httpParams = client.getParams();
//            HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
//            InputStream istream = httpEntity.getContent();
//            httpEntityContent = streamToString(istream);
//            if(istream.available() != 0)
//                EntityUtils.consume(httpEntity);
//            if(httpResponse.getStatusLine().getStatusCode() == 200){
//                return gson.fromJson(httpEntityContent, String.class);
//            } else {
//                throw new QueryErrorException("Error while launching update query" + ". ERROR: " + httpEntityContent);
//            }
//
//        } catch (UnsupportedEncodingException e) {
//            logger.error("error while encoding", e);
//            method.abort();
//        } catch (URISyntaxException e) {
//            logger.error("error while creating URI", e);
//        } catch (ClientProtocolException e) {
//            logger.error("error while calling rest service", e);
//            method.abort();
//        } catch (IOException e) {
//            method.abort();
//            throw new ServerErrorException("unreachable host");
//        }
//
//        return "Error";
//
//    }
//
//    /**
//     * Method to put new named model to the internal static knowledge
//     * @param iri IRI of new named model
//     * @param location location (local or remote) of the data
//     * @return json representation of server response
//     * @throws StaticKnowledgeErrorException
//     * @throws ServerErrorException
//     * @throws URISyntaxException
//     * @throws QueryErrorException
//     */
//    public String putStaticModel(String iri, String location) throws StaticKnowledgeErrorException, ServerErrorException, URISyntaxException {
//
//        HttpPost method = null;
//        String httpEntityContent;
//
//        try{
//            uri = new URI(serverAddress + "/kb");
//
//            method = new HttpPost(uri);
//
//            method.setHeader("Cache-Control","no-cache");
//
//            if(System.getProperty("os.name").contains("Windows")){
//                if(!location.startsWith("http://") && !location.startsWith("file:/"))
//                    location = "file:/" + location;
//            }else{
//                if(!location.startsWith("http://") && !location.startsWith("file://"))
//                    location = "file://" + location;
//            }
//
//            StringWriter sw = new StringWriter();
//            ModelFactory.createDefaultModel().read(location).write(sw);
//
//            formparams = new ArrayList<BasicNameValuePair>();
//            formparams.add(new BasicNameValuePair("action", "put"));
//            formparams.add(new BasicNameValuePair("iri", iri));
//            formparams.add(new BasicNameValuePair("serialization", sw.toString()));
//            requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
//
//            method.setEntity(requestParamsEntity);
//
//            httpResponse = client.execute(method);
//            httpEntity = httpResponse.getEntity();
//            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());
//
//            httpParams = client.getParams();
//            HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
//            InputStream istream = httpEntity.getContent();
//            httpEntityContent = streamToString(istream);
//            if(istream.available() != 0)
//                EntityUtils.consume(httpEntity);
//            if(httpResponse.getStatusLine().getStatusCode() == 200){
//                return gson.fromJson(httpEntityContent, String.class);
//            } else {
//                throw new StaticKnowledgeErrorException("Eception occurred while putting new model into the internal static dataset");
//            }
//
//        } catch (IOException e) {
//            method.abort();
//            throw new ServerErrorException("unreachable host");
//        }
//    }
//
//    /**
//     * Method to remove named model from the internal static knowledge
//     * @param iri IRI of the named model to remove
//     * @return json representation of server response
//     * @throws ServerErrorException
//     * @throws StaticKnowledgeErrorException
//     * @throws URISyntaxException
//     */
//    public String removeStaticModel(String iri) throws ServerErrorException, StaticKnowledgeErrorException, URISyntaxException{
//
//        HttpPost method = null;
//        String httpEntityContent;
//
//        try{
//            uri = new URI(serverAddress + "/kb");
//
//            method = new HttpPost(uri);
//
//            method.setHeader("Cache-Control","no-cache");
//
//            formparams = new ArrayList<BasicNameValuePair>();
//            formparams.add(new BasicNameValuePair("action", "delete"));
//            formparams.add(new BasicNameValuePair("iri", iri));
//            requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
//
//            method.setEntity(requestParamsEntity);
//
//            httpResponse = client.execute(method);
//            httpEntity = httpResponse.getEntity();
//            logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());
//
//            httpParams = client.getParams();
//            HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
//            InputStream istream = httpEntity.getContent();
//            httpEntityContent = streamToString(istream);
//            if(istream.available() != 0)
//                EntityUtils.consume(httpEntity);
//            if(httpResponse.getStatusLine().getStatusCode() == 200){
//                return gson.fromJson(httpEntityContent, String.class);
//            } else {
//                throw new StaticKnowledgeErrorException("Eception occurred while deleting model from the internal static dataset");
//            }
//
//        } catch (IOException e) {
//            method.abort();
//            throw new ServerErrorException("unreachable host");
//        }
//    }
}
