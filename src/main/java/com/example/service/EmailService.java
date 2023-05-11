package com.example.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	public boolean sendEmail(String subject,String message,String toAddress)
	{
		boolean flag=false;
		String from="brijeshsavaliya678@gmail.com";
		String host="smtp.gmail.com";
		Properties properties = System.getProperties();
		System.out.println("PROPERTICES" + properties);
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		Session session = Session.getInstance(properties,new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				// TODO Auto-generated method stub
				return new PasswordAuthentication("brijeshsavaliya678@gmail.com", "xdimsegjpxsqbepz"
						+ "");
			}
		
		});
		session.setDebug(true);
		MimeMessage mimeMessage = new MimeMessage(session);
		try {
			mimeMessage.setFrom(from);
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
			mimeMessage.setSubject(subject);
			mimeMessage.setContent(message,"text/html");
			Transport.send(mimeMessage);
			System.out.println("sent successfully");
			flag=true;
		} catch (Exception exception) {
			// TODO: handle exception
			exception.printStackTrace();
		}
		return flag;
	}
}
