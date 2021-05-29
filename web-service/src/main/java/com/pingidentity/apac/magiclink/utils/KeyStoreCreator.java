package com.pingidentity.apac.magiclink.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class KeyStoreCreator {
	
	public static KeyStore getKeyStoreFromFiles(String keystorePass, String pkFileStr, String [] certFiles) throws Exception
	{
		File pkFile = new File(pkFileStr);
		
		byte [] pkFileBytes = getFileBytes(pkFile);
		
		byte [][] certs = new byte[certFiles.length][];
		
		for(int count = 0; count < certFiles.length; count++)
		{
			String certFileStr = certFiles[count];
			
			File certFile = new File(certFileStr);
			
			byte [] certFileBytes = getFileBytes(certFile);

			certs[count] = certFileBytes;
			
		}
		
		return getKeyStore(keystorePass, pkFileBytes, certs);
		
	}
	
	public static KeyStore getKeyStore(String keystorePass, byte [] pkBytes, String [] certs, Charset charset) throws Exception
	{
		byte[][] returnCerts = new byte[certs.length][];
		
		for(int count=0; count < certs.length; count++)
		{
			String cert = certs[count];
			
			byte [] certBytes = cert.getBytes(charset);
			
			returnCerts[count] = certBytes;
		}
		
		return getKeyStore(keystorePass, pkBytes, returnCerts);
	}
	
	public static KeyStore getKeyStore(String keystorePass, byte [] pkBytes, byte [][] certBytes) throws Exception
	{		
		PrivateKey key = generatePrivateKeyFromDER(pkBytes);

		KeyStore keystore = KeyStore.getInstance("JKS");
		keystore.load(null);
		
		Certificate [] certs = new Certificate[certBytes.length];
		
		for(int count = 0; count < certBytes.length; count++)
		{
			byte [] certFileBytes = certBytes[count];

			X509Certificate cert = generateCertificateFromDER(certFileBytes);
			
			keystore.setCertificateEntry("cert-alias-" + count, cert);
			
			certs[count] = cert;
			
		}
		keystore.setKeyEntry("key-alias", key, keystorePass.toCharArray(),
				certs);
		
		return keystore;
		
	}
	
	public static KeyStore getKeyStore(String keystorePass, PrivateKey key, X509Certificate [] certs) throws Exception
	{		

		KeyStore keystore = KeyStore.getInstance("JKS");
		keystore.load(null);
		
		for(int count = 0; count < certs.length; count++)
		{
			X509Certificate cert = certs[count];
			
			keystore.setCertificateEntry("cert-alias-" + count, cert);
			
			certs[count] = cert;
			
		}
		keystore.setKeyEntry("key-alias", key, keystorePass.toCharArray(),
				certs);
		
		return keystore;
		
	}

	public static byte [] getFileBytes(File file) throws Exception {
		byte [] fileBytes = new byte[(int)file.length()];
		FileInputStream fis = new FileInputStream(file);

		try
		{
			fis.read(fileBytes);
		}
		catch(Exception e)
		{
			return null;
		}
		finally
		{
			fis.close();
		}
		
		return fileBytes;
	}

	private static PrivateKey generatePrivateKeyFromDER(byte[] keyBytes)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

		KeyFactory factory = KeyFactory.getInstance("RSA");

		return factory.generatePrivate(spec);
	}

	private static X509Certificate generateCertificateFromDER(byte[] certBytes)
			throws CertificateException {
		CertificateFactory factory = CertificateFactory.getInstance("X.509");

		return (X509Certificate) factory
				.generateCertificate(new ByteArrayInputStream(certBytes));
	}
}