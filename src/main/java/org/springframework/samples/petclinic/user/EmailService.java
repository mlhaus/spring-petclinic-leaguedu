package org.springframework.samples.petclinic.user;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.SyncPoller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private final EmailClient emailClient;
	private final String senderEmail;

	public EmailService(
		@Value("${azure.communication.connection-string}") String connectionString,
		@Value("${azure.communication.sender-email}") String senderEmail) {

		// Initialize the client once when the application starts
		this.emailClient = new EmailClientBuilder()
			.connectionString(connectionString)
			.buildClient();
		this.senderEmail = senderEmail;
	}

	public void sendPasswordResetEmail(String toAddress, String resetLink) {
		String subject = "Password Reset Request";

		String htmlContent = "<html><body>"
			+ "<h2>Password Reset</h2>"
			+ "<p>We received a request to reset your password. Click the link below to set a new password:</p>"
			+ "<p><a href=\"" + resetLink + "\">Reset Password</a></p>"
			+ "<p>If you did not request this, please ignore this email.</p>"
			+ "</body></html>";

		String plainTextContent = "Please reset your password using this link: " + resetLink
			+ "\n\nIf you did not request this, please ignore this email.";

		EmailMessage message = new EmailMessage()
			.setSenderAddress(this.senderEmail)
			.setToRecipients(toAddress)
			.setSubject(subject)
			.setBodyHtml(htmlContent)
			.setBodyPlainText(plainTextContent);

		try {
			// Send the email and wait for the operation to complete
			SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message, null);
			poller.waitForCompletion();
		} catch (Exception e) {
			// Log the error. In a production environment, use a logger like SLF4J.
			System.err.println("Failed to send email to " + toAddress + ": " + e.getMessage());
		}
	}
}
