package com.pingidentity.apac.magiclink.utils;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailSender {
	private static final Logger log = LoggerFactory.getLogger(EmailSender.class);
	
	private final Session emailSession;
	private final String emailFrom;
	
	private final ExecutorService executor;
	
	public EmailSender(Session emailSession, String emailFrom, int emailThreads)
	{
		this.emailSession = emailSession;
		this.emailFrom = emailFrom;
		executor = Executors.newFixedThreadPool(emailThreads);
	}

	public boolean send(String toAddress, String subject, String htmlText)
	{	
		Callable<Boolean> sendEmailCall = new SendEmailCall(toAddress, subject, htmlText);
		FutureTask<Boolean> sendEmailTask = new FutureTask<Boolean>(sendEmailCall);
		
		executor.execute(sendEmailTask);
		
		return true;
	}
	
	class SendEmailCall implements Callable<Boolean> {
		
		private final String htmlText, toAddress, subject;
		
		public SendEmailCall(String toAddress, String subject, String htmlText){
			this.toAddress = toAddress;
			this.htmlText = htmlText;
			this.subject = subject;
		}
		
		@Override
		public Boolean call() throws Exception {
			sendEmail(toAddress, subject, htmlText);
			
			return true;
		}
	}
	
	private void sendEmail(String toEmail, String subject, String htmlText) throws IOException {

		try {

			if(log.isDebugEnabled())
				log.debug("Sending email to: " + toEmail);
			// Create a default MimeMessage object.
			Message message = new MimeMessage(emailSession);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(emailFrom));

			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));

			// Set Subject: header field
			message.setSubject(subject);

			// This mail has 2 part, the BODY and the embedded image
			MimeMultipart multipart = new MimeMultipart("related");

			// first part (the html)
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(htmlText, "text/html");
			// add it
			multipart.addBodyPart(messageBodyPart);

			// put everything together
			message.setContent(multipart);
			// Send message
			Transport.send(message);
			
			if(log.isDebugEnabled())
				log.info("Sending email to succeeded: " + toEmail);

		} catch (MessagingException e) {

			log.error("Sending email to failed: " + toEmail);
			throw new RuntimeException(e);
		}
	}
}
