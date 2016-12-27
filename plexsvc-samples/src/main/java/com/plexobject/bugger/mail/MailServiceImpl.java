package com.plexobject.bugger.mail;

import javax.jws.WebService;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;

@WebService
@Path("/mail")
public class MailServiceImpl implements MailService {

	@Override
	public void sendEmail(MailRequest request) {
		System.out.println("sendMail:\n\tTo: " + request.getTo() + "\n\tCC: " + request.getCc() + "\n\tFrom: "
				+ request.getFrom() + "\n\tSubject: " + request.getSubject() + "\n\tText: " + request.getText() + "\n");
	}

	@Override
	public void sendEmailx(@FormParam("to") String to, @FormParam("cc") String cc, @FormParam("from") String from,
			@FormParam("subject") String subject, @FormParam("text") String text) {
		System.out.println("sendMailx:\n\tTo: " + to + "\n\tCC: " + cc + "\n\tFrom: " + from + "\n\tSubject: " + subject
				+ "\n\tText: " + text + "\n");

	}
}
