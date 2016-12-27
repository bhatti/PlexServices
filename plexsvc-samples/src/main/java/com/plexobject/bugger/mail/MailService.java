package com.plexobject.bugger.mail;

import javax.jws.WebService;

@WebService
public interface MailService {
	void sendEmail(MailRequest request);

	void sendEmailx(String to, String cc, String from, String subject, String text);
}
