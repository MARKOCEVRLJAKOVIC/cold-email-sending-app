# Cold Email Automation Platform

A full-stack application for managing cold email campaigns. The platform allows user registration, SMTP account management, campaign and template creation, batch email sending with CSV imports, and automatic follow-up scheduling. It also supports Gmail OAuth2 integration for connecting personal or business inboxes.

---

## Tech Stack

- **Backend**: Spring Boot (Java 17), Spring Security, Jakarta Mail (Angus Mail), OAuth2
- **Database**: MySQL
- **Frontend**: HTML, CSS, JavaScript (vanilla)
- **Authentication**: JWT with email verification
- **Scheduling**: In-memory scheduling + database persistence
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

---

## Example DTOs

```json
// CampaignDto
{
  "id": 1,
  "name": "Outreach Campaign",
  "description": "First wave of cold emails",
  "createdAt": "2025-08-27T12:00:00Z",
  "userId": 5
}
json
Copy code
// SendBatchEmailRequest
{
  "file": "leads.csv",
  "scheduledAt": "2025-08-30T10:00:00Z",
  "templateId": 2,
  "smtpIds": [1, 3],
  "campaignId": 5
}
json
Copy code
// EmailReplyDto
{
  "id": 10,
  "originalMessageId": "msg-12345",
  "repliedMessageId": "msg-12345-reply",
  "senderEmail": "lead@example.com",
  "receivedAt": "2025-08-28T14:30:00Z",
  "subject": "Interested in your offer",
  "content": "Hi, let's talk further",
  "emailMessageId": 120
}
Database Schema
The platform uses MySQL with relational tables.
Key entities:

users – user accounts with roles and email verification

smtp_credentials – SMTP or Gmail OAuth accounts

campaigns – campaign definitions

email_templates – email templates

email_messages – stored and scheduled emails with statuses

email_replies – inbound replies from leads

follow_up_templates – automatic follow-up configurations

verification_token / password_reset_token – authentication helpers
