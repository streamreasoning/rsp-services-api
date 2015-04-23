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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
/**
 * 
 * @author Marco Balduini
 *
 */
public class RSP_services_csparql_API {

	private String serverAddress;
	private URI uri;

	private DefaultHttpClient client;
	private HttpResponse httpResponse;
	private HttpEntity httpEntity;
	private HttpParams httpParams;

	private ArrayList<BasicNameValuePair> formparams;
	private UrlEncodedFormEntity requestParamsEntity;
	private PoolingClientConnectionManager cm;

	private Logger logger = LoggerFactory.getLogger(RSP_services_csparql_API.class.getName());
	private Gson gson;

	public RSP_services_csparql_API(String serverAddress) {
		super();
		this.serverAddress = serverAddress;
		cm = new PoolingClientConnectionManager();
		client = new DefaultHttpClient(cm);
		gson = new Gson();
	}


	//Streams

	/**
	 * Register new RDF Stream into engine
	 * @param inputStreamName name of the new stream. The name of the stream needs to be a valid URI.
	 * @return json response from server. 
	 * @throws ServerErrorException 
	 * @throws StreamErrorException 
	 */
	public String registerStream(String inputStreamName) throws ServerErrorException, StreamErrorException{
		HttpPut method = null;
		String httpEntityContent;

		try{

			String encodedName = URLEncoder.encode(inputStreamName, "UTF-8");
			uri = new URI(serverAddress + "/streams/" + encodedName);

			method = new HttpPut(uri);

			method.setHeader("Cache-Control","no-cache");

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());
			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new StreamErrorException("Error while registering stream " + inputStreamName + ". ERROR: " + httpEntityContent);
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
			String encodedName = URLEncoder.encode(inputStreamName, "UTF-8");
			uri = new URI(serverAddress + "/streams/" + encodedName);

			method = new HttpDelete(uri);

			method.setHeader("Cache-Control","no-cache");

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new StreamErrorException("Error while unregistering stream " + inputStreamName + ". ERROR: " + httpEntityContent);
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
			String encodedName = URLEncoder.encode(inputStreamName, "UTF-8");
			uri = new URI(serverAddress + "/streams/" + encodedName);

			method = new HttpPost(uri);

			method.setHeader("Cache-Control","no-cache");

			method.setEntity(new StringEntity(RDF_Data_Serialization));

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new StreamErrorException("Error while feeding stream " + inputStreamName + ". ERROR: " + httpEntityContent);
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
			String encodedName = URLEncoder.encode(inputStreamName, "UTF-8");
			uri = new URI(serverAddress + "/streams/" + encodedName);

			method = new HttpPost(uri);

			method.setHeader("Cache-Control","no-cache");

			StringWriter w = new StringWriter();

			model.write(w,"RDF/JSON");

			method.addHeader("content-type", "application/json");
			String jsonModel = w.toString();
			logger.debug("Feeding stream with model:\n{}", jsonModel);
			method.setEntity(new StringEntity(jsonModel));

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new StreamErrorException("Error while feeding stream " + inputStreamName + ". ERROR: " + httpEntityContent);
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
			String encodedName = URLEncoder.encode(inputStreamName, "UTF-8");
			uri = new URI(serverAddress + "/streams/" + encodedName);

			method = new HttpGet(uri);

			method.setHeader("Cache-Control","no-cache");

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);

			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return httpEntityContent;
			} else {
				throw new StreamErrorException("Error while getting information about stream " + inputStreamName + ". ERROR: " + httpEntityContent);
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

			method.setHeader("Cache-Control","no-cache");

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);

			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return httpEntityContent;
			} else {
				throw new StreamErrorException("Error while getting information about streams" + ". ERROR: " + httpEntityContent);
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

			method.setHeader("Cache-Control","no-cache");

			method.setEntity(new StringEntity(queryBody));

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200 && httpEntityContent != null){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new QueryErrorException("Error while registering query " + queryName + ". ERROR: " + httpEntityContent);
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
	 * Method to unregister a query from the engine.
	 * @param queryURI unique uri of the query
	 * @return json response from server. 
	 * @throws ServerErrorException 
	 * @throws QueryErrorException 
	 */
	public String unregisterQuery(String queryURI) throws ServerErrorException, QueryErrorException{
		HttpDelete method = null;
		String httpEntityContent;

		try{
			uri = new URI(queryURI);

			method = new HttpDelete(uri);

			method.setHeader("Cache-Control","no-cache");

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);

			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new QueryErrorException("Error while unregistering query " + queryURI + ". ERROR: " + httpEntityContent);
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
	 * Method to get information about specified query.
	 * @param queryURI unique uri of the query
	 * @return json serialization of query informations. 
	 * @throws ServerErrorException 
	 * @throws QueryErrorException 
	 */
	public String getQueryInfo(String queryURI) throws ServerErrorException, QueryErrorException{
		HttpGet method = null;
		String httpEntityContent;

		try{
			uri = new URI(queryURI);

			method = new HttpGet(uri);

			method.setHeader("Cache-Control","no-cache");

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);

			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return httpEntityContent;
			} else {
				throw new QueryErrorException("Error while getting information about query " + queryURI + ". ERROR: " + httpEntityContent);
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

			method.setHeader("Cache-Control","no-cache");

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {}: {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);

			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return httpEntityContent;
			} else {
				throw new QueryErrorException("Error while getting information about queries" + ". ERROR: " + httpEntityContent);
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
	 * Method to pause specific query
	 * @param queryURI unique uri of the query to pause
	 * @return json response from server
	 * @throws ServerErrorException 
	 * @throws QueryErrorException 
	 */
	public String pauseQuery(String queryURI) throws ServerErrorException, QueryErrorException{
		HttpPost method = null;
		String httpEntityContent;

		try{
			uri = new URI(queryURI);

			method = new HttpPost(uri);

			method.setHeader("Cache-Control","no-cache");

			formparams = new ArrayList<BasicNameValuePair>();
			formparams.add(new BasicNameValuePair("action", "pause"));
			requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");

			method.setEntity(requestParamsEntity);

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new QueryErrorException("Error while pausing query " + queryURI + ". ERROR: " + httpEntityContent); 
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
	 * Method to restart specific query
	 * @param queryURI unique uri of the query to restart
	 * @return json response from server
	 * @throws ServerErrorException 
	 * @throws QueryErrorException 
	 */
	public String restartQuery(String queryURI) throws ServerErrorException, QueryErrorException{
		HttpPost method = null;
		String httpEntityContent;

		try{
			uri = new URI(queryURI);

			method = new HttpPost(uri);

			method.setHeader("Cache-Control","no-cache");

			formparams = new ArrayList<BasicNameValuePair>();
			formparams.add(new BasicNameValuePair("action", "restart"));
			requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");

			method.setEntity(requestParamsEntity);

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new QueryErrorException("Error while restarting query " + queryURI + ". ERROR: " + httpEntityContent); 
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

	/**
	 * Method to attach new observer to query.
	 * @param queryURI unique uri of the query to attach observer
	 * @param callbackUrl Callback URL needed by the server to send the results
	 * @return Json serialization of query results
	 * @throws ServerErrorException 
	 * @throws ObserverErrorException 
	 */
	public String addObserver(String queryURI, String callbackUrl) throws ServerErrorException, ObserverErrorException{

		HttpPost method = null;
		String httpEntityContent;

		try{
			uri = new URI(queryURI);

			method = new HttpPost(uri);

			method.setHeader("Cache-Control","no-cache");

			method.addHeader("content-type", "text/plain");
			method.setEntity(new StringEntity(callbackUrl));

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new ObserverErrorException("Error while adding observer to query " + queryURI + ". ERROR: " + httpEntityContent); 
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
	 * Method to delete observer
	 * @param observerURI unique uri of the observer to delete
	 * @return json resonse from server
	 * @throws ServerErrorException 
	 * @throws ObserverErrorException 
	 */
	public String deleteObserver(String observerURI) throws ServerErrorException, ObserverErrorException{

		HttpDelete method = null;
		String httpEntityContent;

		try{
			uri = new URI(observerURI);

			method = new HttpDelete(uri);

			method.setHeader("Cache-Control","no-cache");

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI " + uri.toString() + " : " + httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new ObserverErrorException("Error while deleting observer " + observerURI + ". ERROR: " + httpEntityContent); 
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
	 * Method to get informations about specific observer
	 * @param observerURI unique uri of the observer to delete
	 * @return json resonse from server
	 * @throws ServerErrorException 
	 * @throws ObserverErrorException 
	 */
	public String getObserverInformations(String observerURI) throws ServerErrorException, ObserverErrorException{

		HttpGet method = null;
		String httpEntityContent;

		try{
			uri = new URI(observerURI);

			method = new HttpGet(uri);

			method.setHeader("Cache-Control","no-cache");

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {}: {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new ObserverErrorException("Error while getting information about observer " + observerURI + ". ERROR: " + httpEntityContent); 
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
	 * Method to get informations about observers
	 * @param queryURI unique uri of the query observed by the observer to delete
	 * @return json resonse from server
	 * @throws ServerErrorException 
	 * @throws ObserverErrorException 
	 */
	public String getObserversInformations(String queryURI) throws ServerErrorException, ObserverErrorException{

		HttpGet method = null;
		String httpEntityContent;

		try{
			uri = new URI(queryURI+"/observers");

			method = new HttpGet(uri);

			method.setHeader("Cache-Control","no-cache");

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {}: {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return httpEntityContent;
			} else {
				throw new ObserverErrorException("Error while getting information about observers attached to query " + queryURI + ". ERROR: " + httpEntityContent); 
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

	//Static Knowledge

	/**
	 * Method to launch a SPARQL Update query against static knowloedge
	 * @param queryBody string representing the query in SPARQL language . 
	 * @return json representation of server response
	 * @throws ServerErrorException
	 * @throws QueryErrorException
	 */
	public String launchUpdateQuery(String queryBody) throws ServerErrorException, QueryErrorException{

		logger.debug("Launching update query: {}", queryBody);
		
		HttpPost method = null;
		String httpEntityContent;

		try{
			uri = new URI(serverAddress + "/kb");

			method = new HttpPost(uri);

			method.setHeader("Cache-Control","no-cache");

			formparams = new ArrayList<BasicNameValuePair>();
			formparams.add(new BasicNameValuePair("action", "update"));
			formparams.add(new BasicNameValuePair("queryBody", queryBody));
			requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");

			method.setEntity(requestParamsEntity);

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new QueryErrorException("Error while launching update query" + ". ERROR: " + httpEntityContent); 
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
	 * Method to put new named model to the internal static knowledge
	 * @param iri IRI of new named model
	 * @param location location (local or remote) of the data
	 * @return json representation of server response
	 * @throws StaticKnowledgeErrorException 
	 * @throws ServerErrorException
	 * @throws URISyntaxException 
	 * @throws QueryErrorException
	 */
	public String putStaticModel(String iri, String location) throws StaticKnowledgeErrorException, ServerErrorException, URISyntaxException {

		HttpPost method = null;
		String httpEntityContent;

		try{
			uri = new URI(serverAddress + "/kb");

			method = new HttpPost(uri);

			method.setHeader("Cache-Control","no-cache");
			
			if(System.getProperty("os.name").contains("Windows")){
				if(!location.startsWith("http://") && !location.startsWith("file:/"))
					location = "file:/" + location;
			}else{
				if(!location.startsWith("http://") && !location.startsWith("file://"))
					location = "file://" + location;
			}
			
			StringWriter sw = new StringWriter();
			ModelFactory.createDefaultModel().read(location).write(sw);
			
			formparams = new ArrayList<BasicNameValuePair>();
			formparams.add(new BasicNameValuePair("action", "put"));
			formparams.add(new BasicNameValuePair("iri", iri));
			formparams.add(new BasicNameValuePair("serialization", sw.toString()));
			requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");

			method.setEntity(requestParamsEntity);

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new StaticKnowledgeErrorException("Eception occurred while putting new model into the internal static dataset"); 
			}

		} catch (IOException e) {
			method.abort();
			throw new ServerErrorException("unreachable host");
		}
	}
	
	/**
	 * Method to remove named model from the internal static knowledge
	 * @param iri IRI of the named model to remove
	 * @return json representation of server response
	 * @throws ServerErrorException 
	 * @throws StaticKnowledgeErrorException 
	 * @throws URISyntaxException 
	 */
	public String removeStaticModel(String iri) throws ServerErrorException, StaticKnowledgeErrorException, URISyntaxException{

		HttpPost method = null;
		String httpEntityContent;

		try{
			uri = new URI(serverAddress + "/kb");

			method = new HttpPost(uri);

			method.setHeader("Cache-Control","no-cache");

			formparams = new ArrayList<BasicNameValuePair>();
			formparams.add(new BasicNameValuePair("action", "delete"));
			formparams.add(new BasicNameValuePair("iri", iri));
			requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");

			method.setEntity(requestParamsEntity);

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			logger.debug("HTTPResponse code for URI {} : {}",uri.toString(),httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new StaticKnowledgeErrorException("Eception occurred while deleting model from the internal static dataset"); 
			}

		} catch (IOException e) {
			method.abort();
			throw new ServerErrorException("unreachable host");
		} 
	}

	private String streamToString(InputStream is){
		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(is, writer, "UTF-8");
			return writer.toString();
		} catch (IOException e) {
			//			logger.error("IO Exception",e);
			return "";
		}
	}
}
