package com.prabandhx.prabandhx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    /**
     * Send invitation email to collaborator
     */
    public void sendInvitationEmail(String toEmail, String projectName, String inviteUrl, String permissionLevel, int expiryDays) {
        System.out.println("📧 Preparing to send invitation email to: " + toEmail);
        
        try {
            // Create a mime message for HTML content
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("You're invited to collaborate on project: " + projectName);
            
            // Build HTML email content
            String emailContent = buildInviteEmailContent(projectName, inviteUrl, permissionLevel, expiryDays);
            helper.setText(emailContent, true);
            
            // Send the email
            mailSender.send(message);
            
            System.out.println("✅ Invitation email sent successfully to: " + toEmail);
            
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send HTML email: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to simple text email
            sendSimpleInvitationEmail(toEmail, projectName, inviteUrl, permissionLevel, expiryDays);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error sending email: " + e.getMessage());
            e.printStackTrace();
            
            // Ultimate fallback - just log the link
            logInviteLink(toEmail, projectName, inviteUrl, permissionLevel, expiryDays);
        }
    }

    /**
     * Fallback method - send plain text email
     */
    private void sendSimpleInvitationEmail(String toEmail, String projectName, String inviteUrl, String permissionLevel, int expiryDays) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("You're invited to collaborate on project: " + projectName);
            
            String text = String.format(
                "You have been invited to collaborate on project: %s\n\n" +
                "Permission Level: %s\n" +
                "Invite Link: %s\n" +
                "This invitation will expire in %d days.\n\n" +
                "Click the link to accept the invitation and access the project.",
                projectName, permissionLevel, inviteUrl, expiryDays
            );
            
            message.setText(text);
            mailSender.send(message);
            
            System.out.println("✅ Simple invitation email sent successfully to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send simple email: " + e.getMessage());
            logInviteLink(toEmail, projectName, inviteUrl, permissionLevel, expiryDays);
        }
    }

    /**
     * Ultimate fallback - just log the invite link to console
     */
    private void logInviteLink(String toEmail, String projectName, String inviteUrl, String permissionLevel, int expiryDays) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📧 INVITATION EMAIL (CONSOLE FALLBACK)");
        System.out.println("=".repeat(80));
        System.out.println("To: " + toEmail);
        System.out.println("Project: " + projectName);
        System.out.println("Permission: " + permissionLevel);
        System.out.println("Expires in: " + expiryDays + " days");
        System.out.println("\n🔗 INVITE LINK: " + inviteUrl);
        System.out.println("=".repeat(80) + "\n");
    }

    /**
     * Build HTML email content for invitation
     */
    private String buildInviteEmailContent(String projectName, String inviteUrl, String permissionLevel, int expiryDays) {
        String permissionDescription = getPermissionDescription(permissionLevel);
        
        return String.format(
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset='UTF-8'>" +
            "<style>" +
            "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }" +
            ".container { max-width: 600px; margin: 20px auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }" +
            ".header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; }" +
            ".header h2 { margin: 0; font-size: 28px; font-weight: 600; }" +
            ".content { padding: 30px; }" +
            ".project-name { font-size: 24px; color: #667eea; font-weight: 600; margin: 10px 0; }" +
            ".permission-box { background: #f8f9fa; border-left: 4px solid #667eea; padding: 15px; margin: 20px 0; border-radius: 8px; }" +
            ".permission-box p { margin: 5px 0; }" +
            ".permission-level { font-size: 18px; color: #667eea; font-weight: 600; }" +
            ".button { display: inline-block; padding: 14px 32px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-decoration: none; border-radius: 30px; font-weight: 600; margin: 20px 0; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4); }" +
            ".button:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5); }" +
            ".footer { background: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #999; border-top: 1px solid #eee; }" +
            ".expiry-note { background: #fff3cd; color: #856404; padding: 10px; border-radius: 8px; margin: 20px 0; font-size: 14px; }" +
            ".link-box { background: #f1f5f9; padding: 15px; border-radius: 8px; word-break: break-all; font-family: monospace; font-size: 12px; margin: 20px 0; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h2>✨ You're Invited!</h2>" +
            "</div>" +
            "<div class='content'>" +
            "<p>Hello,</p>" +
            "<p>You have been invited to collaborate on the project:</p>" +
            "<div class='project-name'>📁 %s</div>" +
            "<div class='permission-box'>" +
            "<p><strong>Permission Level:</strong> <span class='permission-level'>%s</span></p>" +
            "<p><strong>What you can do:</strong> %s</p>" +
            "</div>" +
            "<div class='expiry-note'>" +
            "⏰ This invitation will expire in <strong>%d days</strong>." +
            "</div>" +
            "<p style='text-align: center;'>" +
            "<a href='%s' class='button'>Accept Invitation</a>" +
            "</p>" +
            "<p style='color: #666; font-size: 14px;'>If the button doesn't work, copy and paste this link into your browser:</p>" +
            "<div class='link-box'>%s</div>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>This is an automated message from PrabandhX. Please do not reply to this email.</p>" +
            "<p>© 2026 PrabandhX. All rights reserved.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>",
            projectName,
            permissionLevel,
            permissionDescription,
            expiryDays,
            inviteUrl,
            inviteUrl
        );
    }

    /**
     * Get permission description
     */
    private String getPermissionDescription(String permissionLevel) {
        switch (permissionLevel) {
            case "VIEWER":
                return "You can view project contents and tasks";
            case "EDITOR":
                return "You can view and edit tasks";
            case "UPLOADER":
                return "You can view and upload files";
            case "ADMIN":
                return "You have full control over the project";
            default:
                return "Access to project";
        }
    }

    /**
     * Send invitation accepted notification
     */
    public void sendInvitationAcceptedEmail(String toEmail, String projectName, String collaboratorEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Invitation accepted for project: " + projectName);
            
            String text = String.format(
                "User %s has accepted the invitation to collaborate on project: %s.\n\n" +
                "They now have access to the project with their assigned permissions.\n\n" +
                "You can view the project at: %s/projects",
                collaboratorEmail, projectName, baseUrl
            );
            
            message.setText(text);
            mailSender.send(message);
            
            System.out.println("✅ Acceptance notification sent to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send acceptance email: " + e.getMessage());
        }
    }

    /**
     * Send access expired notification
     */
    public void sendAccessExpiredEmail(String toEmail, String projectName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Access expired for project: " + projectName);
            
            String text = String.format(
                "Your access to project '%s' has expired.\n\n" +
                "If you need continued access, please request a new invitation.\n\n" +
                "Thank you for collaborating!",
                projectName
            );
            
            message.setText(text);
            mailSender.send(message);
            
            System.out.println("✅ Expiry notification sent to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send expiry email: " + e.getMessage());
        }
    }
}