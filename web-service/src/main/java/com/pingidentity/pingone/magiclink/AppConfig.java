package com.pingidentity.pingone.magiclink;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.pingidentity.pingone.magiclink.otp.IRequestStorage;
import com.pingidentity.pingone.magiclink.otp.impl.LocalRequestStorageImpl;
import com.pingidentity.pingone.magiclink.utils.ClassLoaderUtil;

@Configuration
@ComponentScan("com.pingidentity.pingone.magiclink")
public class AppConfig extends WebMvcConfigurerAdapter {
	private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

	private final Properties configProps;
	
	public AppConfig()
	{
		InputStream configPropsIS = ClassLoaderUtil.getResourceAsStream("application.properties",
				this.getClass());
		
		configProps = new Properties();
		try {
			configProps.load(configPropsIS);
			
		} catch (IOException e) {
		}
	}
	
	@Bean
	public Integer emailSenderThreads()
	{
		String config = getConfig("mail.threads");
		
		return Integer.parseInt(config);
	}
	
	@Bean
	public String emailFrom()
	{
		String config = getConfig("mail.smtp.from");
		
		return config;
	}
	
	@Bean
	public String emailTemplateBody()
	{
		String config = getConfig("mail.template.body");
		
		return config;
	}
	
	@Bean
	public String emailTemplateSubject()
	{
		String config = getConfig("mail.template.subject");
		
		return config;
	}
	
	@Bean 
	public Session emailSession()
	{
		// Sender's email ID needs to be mentioned
		final String username = getConfig("mail.smtp.username");// change accordingly
		final String password = getConfig("mail.smtp.password");// change accordingly

		Properties props = new Properties();
		props.put("mail.smtp.auth", getConfig("mail.smtp.auth"));
		props.put("mail.smtp.starttls.enable", getConfig("mail.smtp.starttls.enable"));
		props.put("mail.smtp.host", getConfig("mail.smtp.host"));
		props.put("mail.smtp.port", getConfig("mail.smtp.port"));

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		
		return session;
	}
	
	@Bean
	public Long otlExpiresMilliseconds()
	{
		String config = getConfig("otl.expire.ms");
		
		return Long.parseLong(config);
	}
	
	@Bean
	public String pingoneBaseUrl()
	{
		String config = getConfig("pingone.base.url");
		
		return config;
	}
	
	@Bean
	public String pingoneClientId()
	{
		String config = getConfig("pingone.client.id");
		
		return config;
	}
	
	@Bean
	public String pingoneClientSecret()
	{
		String config = getConfig("pingone.client.secret");
		
		return config;
	}
		
	@Bean
	public String baseUrl()
	{
		String config = getConfig("base.url");
		
		return config;
	}
	
	@Bean
	public String ipAddressHeader()
	{
		String config = getConfig("header.ipaddress");
		
		return config;
	}
	
	@Bean
	public Boolean isDevMode()
	{
		String config = getConfig("devmode");
		
		if(config == null)
			config = "false";
		
		return Boolean.valueOf(config);
	}
	
	@Bean
	public IRequestStorage requestStorage()
	{
		return new LocalRequestStorageImpl();
	}

	private String getConfig(String configName) {
		String envName = "MAGICLINK-" + configName.replaceAll("\\.", "-");

		if (System.getenv(envName) != null && !System.getenv(envName).isEmpty())
		{
			log.debug(String.format("Config %s=%s", envName, System.getenv(envName)));
			return System.getenv(envName);
		}

		envName = "MAGICLINK_" + configName.replaceAll("\\.", "_");

		if (System.getenv(envName) != null && !System.getenv(envName).isEmpty())
		{
			log.debug(String.format("Config %s=%s", envName, System.getenv(envName)));
			return System.getenv(envName);
		}

		return configProps.getProperty(configName);
	}
}
