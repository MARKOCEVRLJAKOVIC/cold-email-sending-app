# Cold Email Automation Platform

A full-stack application for cold email automation campaigns. The platform allows user registration, SMTP account management, campaign and template creation, batch email sending with CSV imports, and automatic follow-up scheduling. It also supports Gmail OAuth2 integration for connecting personal or business inboxes.

---

## Tech Stack

- **Tools**: Built in Intellij using Java
- **Backend**: Spring Boot (Java 24), Spring Security, Jakarta Mail (Angus Mail), OAuth2
- **Database**: SQL
- **Frontend**: HTML, CSS, JavaScript (vanilla)
- **Deployment**: runnable locally or on a server/cloud

---

## Core Features

- User registration, login, email verification and password reset
- Management of multiple SMTP accounts (regular + Gmail OAuth2)
- Campaign and email template creation
- Batch email sending with CSV import
- Message status tracking: **PENDING, SENT, FAILED, REPLIED**
- Campaign analytics with statistics per campaign
- Automatic follow-ups based on defined delays
- Gmail inbox replies synced back into the system and linked to original campaigns

---

## API Overview

All endpoints follow a RESTful structure. Authentication is handled with JWT, except for login/register endpoints.

### Auth API
- `POST /auth/register` – Register new user
- `GET /auth/confirm` – Confirm email using token
- `POST /auth/login` – User login
- `POST /auth/refresh` – Refresh JWT token
- `POST /auth/me` – Get authenticated user

### User API
- `GET /users/{id}` – Get user by ID
- `POST /users/registerAdmin` – Register admin
- `PUT /users/{id}` – Update user
- `DELETE /users/{id}` – Delete user

### Campaign API
- `GET /campaigns` – Get all campaigns
- `GET /campaigns/{id}` – Get campaign by ID
- `GET /campaigns/{id}/stats` – Campaign statistics
- `GET /campaigns/{id}/replied` – Replied emails
- `POST /campaigns` – Create campaign
- `PUT /campaigns/{id}` – Update campaign
- `DELETE /campaigns/{id}` – Delete campaign

### Template API
- `GET /templates` – Get all templates
- `GET /templates/{id}` – Get template by ID
- `POST /templates` – Create template
- `PUT /templates/{id}` – Update template
- `DELETE /templates/{id}` – Delete template

### SMTP API
- `GET /smtp` – Get all SMTP accounts
- `GET /smtp/{id}` – Get one SMTP account
- `POST /smtp` – Add new SMTP account
- `PUT /smtp/{id}` – Update SMTP account
- `DELETE /smtp/{id}` – Delete SMTP account

### Gmail OAuth API
- `GET /oauth-url` – Generate Gmail OAuth2 URL
- `GET /callback` – OAuth callback
- `GET /gmail-smtp` – Get connected Gmail accounts
- `POST /gmail-smtp` – Connect Gmail account
- `DELETE /gmail-smtp/{id}` – Disconnect Gmail account

### Email Message API
- `GET /email-messages` – Get all messages
- `GET /email-messages/campaign/{campaignId}` – Get campaign messages
- `GET /email-messages/{id}` – Get one message
- `POST /email-messages/send-batch` – Send emails in batch
- `PUT /email-messages/{id}` – Update message
- `DELETE /email-messages/{id}` – Delete message

### Email Reply API
- `GET /reply` – Get all replies
- `GET /reply/{id}` – Get one reply
- `POST /reply/respond/{replyId}` – Reply to a reply
- `DELETE /reply/{id}` – Delete reply

### Follow-up API
- `GET /follow-ups` – Get all follow-ups
- `GET /follow-ups/{id}` – Get one follow-up
- `POST /follow-ups/campaign/{campaignId}` – Add follow-up to campaign
- `PUT /follow-ups/{id}` – Update follow-up
- `DELETE /follow-ups/{id}` – Delete follow-up

### Password Reset API
- `POST /password/forgot` – Request password reset
- `POST /password/reset` – Reset password

## SOLID Implementation

### Open/Closed Principle

This allows adding new senders (e.g., Mailgun, SendGrid, Outlook)  
without modifying existing business logic, following the Open/Closed Principle.

```java
public interface EmailSender {
    void sendEmails(EmailMessage emailMessage) throws MessagingException;
}
```
```java
public interface EmailConnectionService {
    void connect(OAuthTokens tokens, String senderEmail);
}
```
```java
public interface TokenService {
    OAuthTokens refreshAccessToken(String refreshToken);
}
```

### Liskov Substitution Principle and Interface Segregation Principle

Each email sender (e.g., Gmail, SendGrid) can be used interchangeably since they respect the same `EmailSender` contract,  
and the interfaces are small and focused (no unused methods).

```java
public class GmailSmtpSender implements EmailSender {
    /*...*/
    @Override
    public void sendEmails(EmailMessage email) throws MessagingException {
        /*...*/
    }
}
```

### Dependency Inversion Principle

Modules like `SendBatchEmailsService` depend on the `EmailSender` abstraction,  
not on concrete implementations. This makes the system flexible and easy to test.

```java
@Service
@RequiredArgsConstructor
public class SendBatchEmailsService {

    private final EmailSender emailSender; // depends on abstraction

    public void send(EmailMessage emailMessage) throws MessagingException {
        emailSender.sendEmails(emailMessage);
    }
}
```
```java
public interface EmailSender {
    void sendEmails(EmailMessage emailMessage) throws MessagingException;
}
```
```java
@Service
public class GmailSmtpSender implements EmailSender {
    @Override
    public void sendEmails(EmailMessage email) throws MessagingException {
        /*...*/
    }
}
```
