/**
 * @typedef {Object} CampaignDto
 * @property {number} id
 * @property {string} name
 * @property {string} description
 * @property {string} createdAt
 * @property {number} userId
 *
 * @typedef {Object} CampaignStatsDto
 * @property {number} total
 * @property {number} sent
 * @property {number} failed
 * @property {number} pending
 *
 * @typedef {Object} CreateCampaignRequest
 * @property {string} name
 * @property {string} description
 *
 * @typedef {Object} EmailTemplateDto
 * @property {number} id
 * @property {string} name
 * @property {string} subject
 * @property {string} message
 * @property {number} campaignId
 * @property {number} userId
 *
 * @typedef {Object} CreateTemplateRequest
 * @property {string} name
 * @property {string} subject
 * @property {string} message
 * @property {string} createdAt
 * @property {number} campaignId
 *
 * @typedef {Object} EmailMessageDto
 * @property {number} id
 * @property {string} recipientEmail
 * @property {string} recipientName
 * @property {string} sentAt
 * @property {string} sentMessage
 * @property {string} status
 * @property {string} scheduledAt
 * @property {number} userId
 *
 * @typedef {Object} EmailMessageRequest
 * @property {string} recipientEmail
 * @property {string} recipientName
 * @property {string} messageText
 * @property {string} scheduledAt
 * @property {number} userId
 * @property {number} templateId
 * @property {number} smtpId
 * @property {number} campaignId
 *
 * @typedef {Object} EmailRecipientDto
 * @property {string} email
 * @property {string} name
 *
 * @typedef {Object} EmailRequest
 * @property {number} templateId
 * @property {number} smtpId
 * @property {string} recipientEmail
 *
 * @typedef {Object} SendEmailRequest
 * @property {string} recipientEmail
 * @property {string} recipientName
 * @property {number} templateId
 * @property {number} smtpId
 * @property {number} campaignId
 *
 * @typedef {Object} SendBatchEmailRequest
 * @property {File} file
 * @property {string} scheduledAt
 * @property {number} templateId
 * @property {number[]} smtpIds
 * @property {number} campaignId
 *
 * @typedef {Object} LoginRequest
 * @property {string} email
 * @property {string} password
 *
 * @typedef {Object} RegisterUserRequest
 * @property {string} username
 * @property {string} password
 * @property {string} email
 *
 * @typedef {Object} JwtResponse
 * @property {string} token
 *
 * @typedef {Object} SmtpDto
 * @property {number} id
 * @property {string} email
 * @property {string} smtpType
 * @property {number} userId
 *
 * @typedef {Object} RegisterEmailRequest
 * @property {string} email
 * @property {string} smtpHost
 * @property {number} smtpPort
 * @property {string} smtpUsername
 * @property {string} smtpPassword
 *
 * @typedef {Object} UserDto
 * @property {number} id
 * @property {string} username
 * @property {string} email
 *
 * @typedef {Object} GmailConnectRequest
 * @property {string} code
 * @property {string} senderEmail
 *
 * @typedef {Object} OAuthTokens
 * @property {string} accessToken
 * @property {string} refreshToken
 * @property {number} expiresIn
 * @property {string} tokenType
 * @property {string} scope
 *
 * @typedef {Object} ResetPasswordRequest
 * @property {string} email
 *
 * @typedef {Object} ResetPasswordConfirmRequest
 * @property {string} token
 * @property {string} newPassword
 *
 * @typedef {Object} ConfirmationResponse
 * @property {boolean} success
 * @property {string} message
 *
 * @typedef {Object} GenericResponse
 * @property {string} message
 *
 * @typedef {Object} ErrorDto
 * @property {string} error
 *
 * @typedef {Object} EmailReplyDto
 * @property {number} id
 * @property {string} originalMessageId
 * @property {string} repliedMessageId
 * @property {string} senderEmail
 * @property {string} receivedAt
 * @property {string} subject
 * @property {string} content
 * @property {number} emailMessageId
 *
 * @typedef {Object} EmailReplyResponseDto
 * @property {string} message
 * @property {number} smtpId
 * @property {number} templateId
 * @property {number} campaignId
 */
