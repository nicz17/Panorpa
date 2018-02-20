package controller;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import common.base.Logger;


public class WebProbe {
	
	private static final Logger log = new Logger("WebProbe", true);
	
	protected final String strUrl;

	public WebProbe(String url) {
		strUrl = url;
	}
	
	public boolean sendProbe() {
		int code = getResponseCode();
		log.info("URL " + strUrl + " got response " + code);
		boolean isOkResponse = (code == HttpURLConnection.HTTP_OK);
		
		if (!isOkResponse) {
			log.warn("URL " + strUrl + " got response " + code);
		}
		
		return isOkResponse;
	}
	
	protected int getResponseCode() {
		int code = 0;
		try {
			URL url = new URL(strUrl);
	        HttpURLConnection http = (HttpURLConnection)url.openConnection();
	        http.setRequestMethod("HEAD");
	        http.setInstanceFollowRedirects(true);
	        code = http.getResponseCode();
		} catch (MalformedURLException e) {
			log.error("Malformed URL: " + strUrl, e);
		} catch (IOException e) {
			log.error("Failed to connect to URL: " + strUrl, e);
		}
        return code;
	}

	
	public static void main(String args[]) {
        WebProbe probe = new WebProbe("http://galerie-insecte.org/galerie/Osmia_cornuta.html");
        probe.sendProbe();
        
        probe = new WebProbe("http://galerie-insecte.org/galerie/Osmia_cornutatarata.html");
        probe.sendProbe();
        
        probe = new WebProbe("galerie-insecte.org/galerie/Osmia_cornuta.html");
        probe.sendProbe();
        
        probe = new WebProbe("http://www.google.ch/erfkjdfnc.html");
        probe.sendProbe();
        
        probe = new WebProbe("bleu");
        probe.sendProbe();
        
        probe = new WebProbe(null);
        probe.sendProbe();
    }
	
}



