/*******************************************************************************
 * Copyright 2013 DEIB - Politecnico di Milano
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
 ******************************************************************************/
package polimi.deib.csparql_rest_api;

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

import polimi.deib.csparql_rest_api.exception.ServerErrorException;

import com.google.gson.Gson;
import com.hp.hpl.jena.rdf.model.Model;
/**
 * 
 * @author Marco Balduini
 *
 */
public class Csparql_Remote_API {

	private String serverAddress;
	private URI uri;

	private DefaultHttpClient client;
	private HttpResponse httpResponse;
	private HttpEntity httpEntity;
	private HttpParams httpParams;

	private ArrayList<BasicNameValuePair> formparams;
	private UrlEncodedFormEntity requestParamsEntity;
	private PoolingClientConnectionManager cm;

	private Logger logger = LoggerFactory.getLogger(Csparql_Remote_API.class.getName());
	private Gson gson;

	public Csparql_Remote_API(String serverAddress) {
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
	 */
	public String registerStream(String inputStreamName) throws ServerErrorException{
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
				throw new ServerErrorException("Error while registering stream " + inputStreamName);
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
			logger.error("error during IO operation", e);
			method.abort();
		}

		return "Error";
	}

	/**
	 * Unregister specified RDF Stream from engine
	 * @param inputStreamName name of the stream to unregister.
	 * @return json response from server. 
	 * @throws ServerErrorException 
	 */
	public String unregisterStream(String inputStreamName) throws ServerErrorException{
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
				throw new ServerErrorException("Error while unregistering stream " + inputStreamName);
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
			logger.error("error during IO operation", e);
			method.abort();
		} 

		return "Error";
	}

	/**
	 * Put new data into specified RDF Stream
	 * @param inputStreamName name of the stream
	 * @param RDF_Json_Serialization RDF/Json serialization of data to put into stream
	 * @return json response from server. 
	 * @throws ServerErrorException 
	 */
	public String feedStream(String inputStreamName, String RDF_Json_Serialization) throws ServerErrorException{
		HttpPost method = null;
		String httpEntityContent;

		try{
			String encodedName = URLEncoder.encode(inputStreamName, "UTF-8");
			uri = new URI(serverAddress + "/streams/" + encodedName);

			method = new HttpPost(uri);

			method.setHeader("Cache-Control","no-cache");

			method.setEntity(new StringEntity(RDF_Json_Serialization));

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
				throw new ServerErrorException("Error while feeding stream " + inputStreamName);
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
			logger.error("error during IO operation", e);
			method.abort();
		}

		return "Error";
	}

	/**
	 * Put new data into specified RDF Stream
	 * @param inputStreamName name of the stream
	 * @param model Jena Model containing data to put into stream
	 * @return json response from server. 
	 */
	public String feedStream(String inputStreamName, Model model){
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
			method.setEntity(new StringEntity(w.toString()));

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
				throw new ServerErrorException("Error while feeding stream " + inputStreamName);
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
			logger.error("error during IO operation", e);
			method.abort();
		} catch(Exception e){
			logger.error("general Exception", e);
			method.abort();
		} 

		return "Error";
	}

	/**
	 * Get information about specific stream
	 * @param inputStreamName name of the stream
	 * @return json serialization of stream informations
	 * @throws ServerErrorException 
	 */
	public String getStreamInfo(String inputStreamName) throws ServerErrorException{
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
				throw new ServerErrorException("Error while getting information about stream " + inputStreamName);
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
			logger.error("error during IO operation", e);
			method.abort();
		}

		return "Error";
	}

	/**
	 * Get information about all the streams registered on the engine
	 * @return json serialization of streams informations
	 * @throws ServerErrorException 
	 */
	public String getStreamsInfo() throws ServerErrorException{
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
				throw new ServerErrorException("Error while getting information about streams");
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
			logger.error("error during IO operation", e);
			method.abort();
		} 

		return "Error";
	}


	//Queries

	/**
	 * Method to register a new query into the engine.
	 * @param queryName name of the new query (this name must match to the name specified in the body)
	 * @param queryBody string representing the query in C-SPAQRL language . 
	 * @return json representation of query ID
	 * @throws ServerErrorException 
	 */
	public String registerQuery(String queryName, String queryBody) throws ServerErrorException{
		HttpPut method = null;
		String httpEntityContent;

		try{
			uri = new URI(serverAddress + "/queries/" + queryName);

			method = new HttpPut(uri);

			method.setHeader("Cache-Control","no-cache");

//			formparams = new ArrayList<BasicNameValuePair>();
//			formparams.add(new BasicNameValuePair("querybody", queryBody));
//			requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");

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
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new ServerErrorException("Error while registering stream " + queryName);
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
			logger.error("error during IO operation", e);
			method.abort();
		}

		return "Error";
	}

	/**
	 * Method to unregister a query from the engine.
	 * @param queryURI unique uri of the query
	 * @return json response from server. 
	 * @throws ServerErrorException 
	 */
	public String unregisterQuery(String queryURI) throws ServerErrorException{
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
				throw new ServerErrorException("Error while unregistering query " + queryURI);
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
			logger.error("error during IO operation", e);
			method.abort();
		}

		return "Error";
	}

	/**
	 * Method to get information about specified query.
	 * @param queryURI unique uri of the query
	 * @return json serialization of query informations. 
	 * @throws ServerErrorException 
	 */
	public String getQueryInfo(String queryURI) throws ServerErrorException{
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
				throw new ServerErrorException("Error while getting information about query " + queryURI);
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
			logger.error("error during IO operation", e);
			method.abort();
		}

		return "Error";
	}

	/**
	 * Method to get information about queries.
	 * @return json serialization of queries informations. 
	 * @throws ServerErrorException 
	 */
	public String getQueriesInfo() throws ServerErrorException{
		HttpGet method = null;
		String httpEntityContent;

		try{
			uri = new URI(serverAddress + "/queries");

			method = new HttpGet(uri);

			method.setHeader("Cache-Control","no-cache");

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			System.out.println("HTTPResponse code for URI " + uri.toString() + " : " + httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);

			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return httpEntityContent;
			} else {
				throw new ServerErrorException("Error while getting information about queries");
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
			logger.error("error during IO operation", e);
			method.abort();
		}

		return "Error";
	}

	/**
	 * Method to pause specific query
	 * @param queryURI unique uri of the query to pause
	 * @return json response from server
	 * @throws ServerErrorException 
	 */
	public String pauseQuery(String queryURI) throws ServerErrorException{
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
				throw new ServerErrorException("Error while pausing query " + queryURI); 
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
			logger.error("error during IO operation", e);
			method.abort();
		} 

		return "Error";
	} 

	/**
	 * Method to restart specific query
	 * @param queryURI unique uri of the query to restart
	 * @return json response from server
	 * @throws ServerErrorException 
	 */
	public String restartQuery(String queryURI) throws ServerErrorException{
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
				throw new ServerErrorException("Error while restarting query " + queryURI); 
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
			logger.error("error during IO operation", e);
			method.abort();
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
	 */
	public String addObserver(String queryURI, String callbackUrl) throws ServerErrorException{

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
				throw new ServerErrorException("Error while adding observer to query " + queryURI); 
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
			logger.error("error during IO operation", e);
			method.abort();
		} 

		return "Error";

	}

	/**
	 * Method to delete observer
	 * @param observerURI unique uri of the observer to delete
	 * @return json resonse from server
	 * @throws ServerErrorException 
	 */
	public String deleteObserver(String observerURI) throws ServerErrorException{

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
				throw new ServerErrorException("Error while deleting observer " + observerURI); 
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
			logger.error("error during IO operation", e);
			method.abort();
		}

		return "Error";

	}

	/**
	 * Method to get informations about specific observer
	 * @param observerURI unique uri of the observer to delete
	 * @return json resonse from server
	 * @throws ServerErrorException 
	 */
	public String getObserverInformations(String observerURI) throws ServerErrorException{

		HttpGet method = null;
		String httpEntityContent;

		try{
			uri = new URI(observerURI);

			method = new HttpGet(uri);

			method.setHeader("Cache-Control","no-cache");

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			System.out.println("HTTPResponse code for URI " + uri.toString() + " : " + httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new ServerErrorException("Error while getting information about observer " + observerURI); 
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
			logger.error("error during IO operation", e);
			method.abort();
		}

		return "Error";

	}

	/**
	 * Method to get informations about observers
	 * @param queryURI unique uri of the query observed by the observer to delete
	 * @return json resonse from server
	 * @throws ServerErrorException 
	 */
	public String getObserversInformations(String queryURI) throws ServerErrorException{

		HttpGet method = null;
		String httpEntityContent;

		try{
			uri = new URI(queryURI);

			method = new HttpGet(uri);

			method.setHeader("Cache-Control","no-cache");

			httpResponse = client.execute(method);
			httpEntity = httpResponse.getEntity();
			System.out.println("HTTPResponse code for URI " + uri.toString() + " : " + httpResponse.getStatusLine().getStatusCode());

			httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

			InputStream istream = httpEntity.getContent();
			httpEntityContent = streamToString(istream);
			if(istream.available() != 0)
				EntityUtils.consume(httpEntity);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				return gson.fromJson(httpEntityContent, String.class);
			} else {
				throw new ServerErrorException("Error while getting information about observers attached to query " + queryURI); 
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
			logger.error("error during IO operation", e);
			method.abort();
		} 

		return "Error";

	}

	public String launchUpdateQuery(String querybody) throws ServerErrorException{

		HttpPost method = null;
		String httpEntityContent;

		try{
			uri = new URI(serverAddress + "/updatekb");

			method = new HttpPost(uri);

			method.setHeader("Cache-Control","no-cache");

//			formparams = new ArrayList<BasicNameValuePair>();
//			formparams.add(new BasicNameValuePair("queryBody", querybody));
//			requestParamsEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			
			method.setEntity(new StringEntity(querybody));

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
				throw new ServerErrorException("Error while launching update query"); 
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
			logger.error("error during IO operation", e);
			method.abort();
		} 

		return "Error";

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
