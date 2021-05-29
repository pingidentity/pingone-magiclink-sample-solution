package com.pingidentity.apac.magiclink.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.util.StringUtils;

public class Tools {

	public static String executeHTTP(String targetURL, String method,
			String authorization, String content, String contentType)
			throws Exception {

		HttpURLConnection connection = null;
		try {
			// Create connection
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setUseCaches(false);
			HttpURLConnection.setFollowRedirects(true);
			connection.setInstanceFollowRedirects(true);
			
			if (!StringUtils.isEmpty(contentType))
			{
				connection.setRequestProperty("Content-Type", contentType);
				connection.setRequestProperty("Accept", contentType);
			}
			
			if (authorization != null && !authorization.equals("")) {
				connection.setRequestProperty("Authorization", authorization);
			}

			if (method.equalsIgnoreCase("GET")) {
				connection.setRequestMethod("GET");
				connection.setDoOutput(false);

				connection.connect();
			} else if (method.equalsIgnoreCase("POST")) {

				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Length",
						Integer.toString(content.length()));
				DataOutputStream wr = new DataOutputStream(
						connection.getOutputStream());
				wr.write(content.getBytes());
				wr.close();
			} else {
				throw new Exception("Invalid method: " + method);
			}

			int responseCode = connection.getResponseCode();

			// Get Response
			InputStream is = null;
			
			boolean hasError = false;

			if (responseCode < 400)
			{
				is = connection.getInputStream();
			}
			else
			{
				is = connection.getErrorStream();
				hasError = true;
			}

			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if
															// not Java 5+
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			
			String responseString = response.toString();
			
			if(hasError)
				throw new Exception(responseString);

			return responseString;

		} catch (Exception e) {
			throw new Exception("Error executing HTTP request", e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

    public static void disableSSLCertificateChecking() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        }};

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
}
