/* Copyright (c) 2006-2011 Regents of the University of Minnesota.
   For licensing terms, see the file LICENSE.
 */

package com.citizensense.android.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.w3c.dom.Document;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.citizensense.android.Item;
import com.citizensense.android.conf.Constants;

/**
 * HTTP Request
 * @author Phil Brown
 */
public class Request extends AsyncTask<Void, Void, HttpResponse> {
	
	/** The URL that is constructed and sent in the request. */
	private String url;
	/** The data to send in a POST request */
	private Item data;
	/** The type of the request. Must be either {@link #GET} 
	 * or {@link #POST}. */
	private int requestType;
	/** The format of the input data. Must be either {@link #JSON} 
	 * or {@link #XML}*/
	private int inputFormat;
	/** Handler that is called after the task has completed */
	private ResponseHandler<?> handler;
	/** The dialog displayed to the user during a request. */
	private ProgressDialog dialog;
	/** Whether or not the user needs to know about this HTTP request. */
	private boolean showPopup;
	/** Used for creating a dialog. */
	private Context context;
	
	/** Request Type*/
	public static final int GET = 0, POST = 1;
	/** Format Type */
	public static final int XML = 0, JSON = 1;
	/** The root URL of the server. */
	public static final String BASE_URL = "http://ugly.cs.umn.edu:8080";
	
	/**
	 * Create a GET Request
	 * @param context used to access the application UI
	 * @param itemType the type of item to get
	 * @param id the id of the item to get
	 * @param handler processes the data after it is received
	 */
	private Request(Context context, Class<? extends Item> itemType, String id, 
			          ResponseHandler<?> handler, boolean showPopup) {
		this(context, handler, showPopup);
		this.requestType = GET;
		if (!(handler instanceof JSONResponseHandler) 
		   && !(handler instanceof XMLResponseHandler)) {
			throw new IllegalArgumentException("Invalid handler." +
					"Must be JSONResponseHandler or XMLResponseHandler");
		}
		/* This assumes that the URL item string will always be equal to the 
		 * lowercase Item name. */
		String[] pkgs = (itemType.getName()).split("\\.");
		String s = pkgs[pkgs.length - 1].toLowerCase();
		url += (id == null ? s : s + "/" + id);
	}//Request
	
	/**
	 * Create a GET Request
	 * @param context used to access the application UI
	 * @param itemType the type of item to get
	 * @param id the id of the item to get
	 * @param handler processes the data after it is received
	 */
	protected Request(Context context, Class<? extends Item> itemType, String id, 
	          JSONResponseHandler handler, boolean showPopup) {
		this(context, itemType, id, (ResponseHandler<JSONArray>) handler, showPopup);
		url += ".json";
	}//Request
	
	/**
	 * Create a GET Request
	 * @param context used to access the application UI
	 * @param itemType the type of item to get
	 * @param id the id of the item to get
	 * @param handler processes the data after it is received
	 */
	protected Request(Context context, Class<? extends Item> itemType, String id, 
	          XMLResponseHandler handler, boolean showPopup) {
		this(context, itemType, id, (ResponseHandler<Document>) handler, showPopup);
		url += ".xml";
	}//Request
	
	/**
	 * Create a POST Request
	 * @param context used for updating the UI
	 * @param data the item to post. This must be a valid {@link Item}.
	 * @param format the format of the item to post. This must be either
	 * {@link #JSON}, {@link #XML}, or {@code null}. null will be treated 
	 * as {@link #XML}.
	 * @param handler where the return String is handled.
	 * @param showPopup true to display a dialog during HTTP transaction.
	 */
	protected Request(Context context, Item data, int format, 
			          BasicResponseHandler handler, boolean showPopup) {
		this(context, handler, showPopup);
		this.requestType = POST;
		this.data = data;
		this.inputFormat = format;
		url += data.getItemName();
		if (format == JSON) {
			url += ".json";
		}
		else if (format == XML) {
			url += ".xml";
		}
		else {
			throw new IllegalArgumentException("Invalid format: " + format +
			". Must JSON, XML, or null");
		}
	}//Request
	
	/**
	 * Generic Constructor for all GET and POST Requests. 
	 * @param context context used for updating the UI
	 * @param handler where the return String is handled.
	 * @param showPopup true to display a dialog during HTTP transaction.
	 */
	private Request(Context context, ResponseHandler<?> handler, 
			        boolean showPopup) {
		this.showPopup = showPopup;
		this.url = BASE_URL + "/csense/";
		this.context = context;
		this.handler = handler;
	}//Request

	/** Creates and shows the dialog */
	@Override
	protected void onPreExecute() {
		if (Constants.DEBUG) {
			Log.d("Request", "***URL***  " + url);
		}
		if (showPopup) {
			dialog = new ProgressDialog(context);
			if (requestType == GET) {
				dialog.setMessage("Retrieving...");
			}
			else {
				dialog.setMessage("Sending...");
			}
			dialog.setCancelable(false);
			dialog.show();
		}
	}//onPreExecute
	
	/** Handles the HTTP GET or POST in a non-UI Thread. */
	@Override
	protected HttpResponse doInBackground(Void... params) {
		HttpClient client = new DefaultHttpClient();
		if (requestType == GET) {
			HttpGet get = new HttpGet(url);
			try {
				return client.execute(get);
			} catch (ClientProtocolException e) {
				//Error communicating with the server
				e.printStackTrace();
			} catch (IOException e) {
				//Sending/Receiving error
				e.printStackTrace();
			}
		}
		else {//POST
			HttpPost post = new HttpPost(url);
			StringEntity entity;
			try{
				if (this.inputFormat == JSON) {
					entity = new StringEntity(data.buildJSON());
				}
				else {
					entity = new StringEntity(data.buildXML());
				}
				if (Constants.DEBUG) {
					Log.i("Request", entity.toString());
				}
				entity.setContentType("text/xml"); 
				post.setHeader("Content-Type",
						       "application/soap+xml;charset=UTF-8");
				post.setEntity(entity);
				return client.execute(post);
			} catch(UnsupportedEncodingException e) {
				//unable to encode data into a StringEntity.
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				//Error communicating with the server
				e.printStackTrace();
			} catch (IOException e) {
				//Sending/Receiving error
				e.printStackTrace();
			}
			
		}
		return null;
	}//doInBackground
	
	/**
	 * Returns the HttpResponse to the handler and closes the dialog
	 */
	@Override
	protected void onPostExecute(HttpResponse response) {
		if (showPopup) {
			dialog.dismiss();
		}
		try {
			handler.handleResponse(response);
		} catch (NullPointerException e) {
			//response returned null
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}//onPostExecute

}//Request