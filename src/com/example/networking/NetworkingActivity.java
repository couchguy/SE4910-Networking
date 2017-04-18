package com.example.networking;

import android.os.Bundle;
import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import android.util.Log;

import	android.widget.ImageView;	
import	android.widget.Toast;	
import	android.graphics.Bitmap;	
import	android.graphics.BitmapFactory;	
import	android.os.AsyncTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.view.Menu;

public class NetworkingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		new DownloadImageTask().execute("http://jayurbain.com/images/mel-fetch-mke.jpg");
		//new DownloadImageTask().execute("http://triangle.groups.msoe.edu/Retreat%202012%20Picture.jpg");
		
		new AccessWebServiceTask().execute("apple");
	}
	
	private InputStream openHttpConnection(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;
		
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		
		if(!(conn instanceof HttpURLConnection)) {
			throw new IOException("Not an HTTP connection");
		}
		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();
			response = httpConn.getResponseCode();
			if(response == HttpURLConnection.HTTP_OK){
				in  = httpConn.getInputStream();
			}
			
		} catch (Exception ex) {
			Log.d("Networking", ex.getLocalizedMessage());
			throw new IOException("Error connection");
		}
		
		return in;
	}

	private Bitmap DownloadImage(String URL) {
		Bitmap bitmap = null;
		InputStream in = null;
		try {
			in = openHttpConnection(URL);
			bitmap = BitmapFactory.decodeStream(in);
			in.close();
		} catch (IOException e1){
			Log.d("NetworkingActivity", e1.getLocalizedMessage());
		}
		return bitmap;
	}
	
	private String downloadText(String URL) {
		int BUFFER_SIZE = 2000;
		InputStream in = null;
		try {
			in = openHttpConnection(URL);
		} catch (IOException e) {
			Log.d("NetworkingActivity", e.getLocalizedMessage());
			return "";
		}
		InputStreamReader isr = new InputStreamReader(in);
		int charRead;
		String str = "";
		char[] inputBuffer = new char[BUFFER_SIZE];
		try {
			while((charRead = isr.read(inputBuffer)) > 0) {
				// convert the chars to a string
				String readString = String.copyValueOf(inputBuffer, 0, charRead);
				str += readString;
				inputBuffer = new char[BUFFER_SIZE];
			}
			in.close();
		} catch (IOException e){
			Log.d("NetworkingActivity", e.getLocalizedMessage());
		}
		return str;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private String wordDefinition(String word) {
		InputStream in = null;
		String strDefinition = "";
		try {
			in = openHttpConnection(	"http://services.aonaware.com/DictService/DictService.asmx/Define?word="+	word);	
			Document doc = null;	
			DocumentBuilderFactory dbf	= DocumentBuilderFactory.newInstance();	
			DocumentBuilder db;	

			try {
				db = dbf.newDocumentBuilder();
				doc = db.parse(in);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			doc.getDocumentElement().normalize();
			
			//retrieve all the definition elements
			NodeList definitionElements = doc.getElementsByTagName("Definition");
			
			//iterate through each <definition> elements
			for(int i = 0; i < definitionElements.getLength(); i++) {
				Node itemNode = definitionElements.item(i);
				if(itemNode.getNodeType() == Node.ELEMENT_NODE) {
					
					// convert the definition node into an element
					Element definitionElement = (Element) itemNode;
					
					//get all the <worddefiniton> element under
					// the <definition element
					NodeList wordDefinitionElements = (definitionElement).getElementsByTagName("WordDefinition");
					strDefinition = "";
					//iterate through each <wordDefinition> elements
					for(int j = 0; j < wordDefinitionElements.getLength(); j++) {
						//convert <wordDefinition> node into an element
						Element wordDefinitionElement = (Element) wordDefinitionElements.item(j);
						
						//get all the child nodes under the <wordDefinition element
						NodeList textNodes = ((Node)wordDefinitionElement).getChildNodes();
						strDefinition += ((Node) textNodes.item(0)).getNodeValue() + ". \n";
					}
				}
			}
		
			
		} catch (IOException e1) {
			Log.d("NetworkingActivity", e1.getLocalizedMessage());
		}
		
		return strDefinition;
	}
	private class AccessWebServiceTask extends AsyncTask<String, Void, String> {
		
		protected String doInBackground(String... urls) {
			return wordDefinition(urls[0]);
		}
		
		protected void onPostExecute(String result) {
			Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
		}
	}
	
	private class DownloadImageTask extends AsyncTask<String, Bitmap, Long> {
		//---takes in a list of image URLs in String type---
		protected Long doInBackground(String... urls) {
			long imagesCount = 0;
			for (int i = 0; i < urls.length; i++) {
				// ---download the image ---
				Bitmap imageDownloaded = DownloadImage(urls[i]);
				if(imageDownloaded != null) {
					// ---increment the image count---
					imagesCount++;
					try {
						// -- insert a delay of 3 seconds ---
						Thread.sleep(3000);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
					// --- return the image downloaded---
					publishProgress(imageDownloaded);
				}
			}
			// return the total images downloaded count
			return imagesCount;
		}
		//display the image downloaded
		protected void onProgressUpdate(Bitmap... bitmap) {
			ImageView img = (ImageView) findViewById(R.id.img);
			img.setImageBitmap(bitmap[0]);
		}
		
		// when all the images have been downloaded
		protected void onPostExecute(Long imagesDownloaded) {
			Toast.makeText(getBaseContext(), "Total " + imagesDownloaded + " images downloaded", Toast.LENGTH_LONG).show();
			
		}
	}

}
