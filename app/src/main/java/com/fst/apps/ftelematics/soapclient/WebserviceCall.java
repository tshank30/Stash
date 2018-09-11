package com.fst.apps.ftelematics.soapclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class WebserviceCall implements ISocketOperator {

	String namespace = "http://tempuri.org/";
	private String url = "http://23.94.21.22:2122/vtsServices.asmx";
	private String newServiceUrl ="http://205.147.110.119:2122/vtsServices.asmx";
	String SOAP_ACTION;
	
	public WebserviceCall(IAppManager appManager) 
	{

	}
		
	private Object CallSOAPWebService(String method, Hashtable<String, Object> param, String resultNode) throws Exception 
	{
		OutputStream out = null;
		int respCode = -1;
		Object result = null;
		String wsdlURL=  newServiceUrl;
		HttpURLConnection httpURLConnection = null;
		URL	url;
		try 
		{
			SOAP_ACTION = namespace + method;
			url = new URL(wsdlURL);
			httpURLConnection = (HttpURLConnection) url.openConnection();

			do 
			{
				byte[] createBodyEnvelop=createBody(method,param);
				httpURLConnection.setRequestMethod("POST");
				httpURLConnection.setRequestProperty("Connection", "keep-alive");
				httpURLConnection.setRequestProperty("Content-Type", "text/xml");
				httpURLConnection.setRequestProperty("UseCookieContainer","True");
				httpURLConnection.setRequestProperty("SOAPAction",SOAP_ACTION);
				HttpURLConnection.setFollowRedirects(false);
				httpURLConnection.setDoOutput(true);
				httpURLConnection.setDoInput(true);
				httpURLConnection.setRequestProperty("Content-length",createBodyEnvelop.length + "");
				httpURLConnection.setReadTimeout(60 * 1000);
				try{
				httpURLConnection.connect();
				}catch(ConnectException e)
				{
					Log.e("Connection Exception", "Could not connect to service, please check!!");
				}
				out = httpURLConnection.getOutputStream();

				if (out != null) 
				{
					out.write(createBodyEnvelop);
					out.flush();
				}

				if (httpURLConnection != null) {
					respCode = httpURLConnection.getResponseCode();

				}
			}while (respCode == -1);

			// If it works fine
			if (respCode == 200)
			{
				try
				{
					InputStream responce = httpURLConnection.getInputStream();	
					result = convertStreamToString(responce, resultNode);
				} 
				catch (Exception e1)
				{
					throw e1;
				}
			} 
			else
			{
				try
				{
					InputStream responce = httpURLConnection.getErrorStream();
					String	resultfaultstring = (String) convertStreamToString(responce, "faultstring");
					throw new Exception(resultfaultstring);
				} 
				catch (Exception e1)
				{					
					e1.printStackTrace();
					throw e1;
				}
			}
			return result;
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		/*finally
		{
			if (out != null) 
			{
				out.close();
				out.flush();
				out = null;
			}
			if (httpURLConnection != null) 
			{
				if(httpURLConnection.getDoInput())
					httpURLConnection.getInputStream().close();

				httpURLConnection.disconnect();
				httpURLConnection =null;
				url =null;
			}
		}*/
		return result;
	}


	public static class _FakeX509TrustManager implements javax.net.ssl.X509TrustManager 
	{
		private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[] {};

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}
		
		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public boolean isClientTrusted(X509Certificate[] chain) {
			return (true);
		}

		public boolean isServerTrusted(X509Certificate[] chain) {
			return (true);
		}

		public X509Certificate[] getAcceptedIssuers() {
			return (_AcceptedIssuers);
		}
	}

	public static String createSoapHeader() 
	{
		String soapHeader = null;

		soapHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<soap:Envelope "
				+ "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\""
				+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
				+ " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"" + ">";
		return soapHeader;
	}

	public  byte[] createBody(String method, Hashtable<String, Object> params) 
	{
		StringBuilder requestData = new StringBuilder();

		requestData.append(createSoapHeader());
		requestData.append("<soap:Body>"
				+ "<"+method+" xmlns=\""+namespace+"\">");

		Set set = params.entrySet();
		Iterator it = set.iterator();
		while (it.hasNext()) 
		{
			Map.Entry entry = (Map.Entry) it.next();
			requestData.append("<"+entry.getKey()+">");
			requestData.append(entry.getValue());
			requestData.append("</"+entry.getKey()+">");
		}

		requestData.append("</"+method+">"
				+ "</soap:Body> </soap:Envelope>");

		return requestData.toString().trim().getBytes();
	}

	private Object convertStreamToString(InputStream is, String resultNode) throws Exception		
	{
		Object result=null;
		Object resultInternal=null;
		ArrayList<String> _results=new ArrayList<String>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom=db.parse(is);
		NodeList nl = dom.getElementsByTagName(resultNode);

		if(nl != null && nl.getLength() > 0) 
		{
			if(nl.getLength()>1)
			{
				for (int i = 0; i < nl.getLength(); i++) 
				{
					Node _node = nl.item(i);
					_node.normalize();
					if(_node!=null&&_node.getFirstChild()!=null)
					{
						resultInternal=_node.getFirstChild().getNodeValue();
						_results.add((String) resultInternal);
					}											
				}
				result=_results;
			}
			else
			{				
				if(nl.item(0)!=null&&nl.item(0).getFirstChild()!=null)
				{
					result= nl.item(0).getFirstChild().getNodeValue();

					if(resultNode.equals("getContainerListReturn")&&result.toString().contains("401 Unauthorized"))
					{
						throw new Exception((String) result);
					}
				}
			}
		}
		return result;
	}

	@Override
	public String sendHttpRequest(String method,Hashtable<String, Object> param, String resultNode) 
	{
		try 
		{
			return CallSOAPWebService(method, param, resultNode).toString();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int startListening(int port) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void stopListening() {
		// TODO Auto-generated method stub
	}

	@Override
	public void exit() {
		// TODO Auto-generated method stub
	}

	@Override
	public int getListeningPort() {
		// TODO Auto-generated method stub
		return 0;
	}
}
