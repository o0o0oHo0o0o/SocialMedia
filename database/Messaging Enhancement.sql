USE SocialMedia;
GO

-- ---------------------------------------------------------
-- 1. MESSAGE SEQUENCE GENERATOR
-- ---------------------------------------------------------
CREATE SEQUENCE Messaging.MessageSequence
    START WITH 1
    INCREMENT BY 1
    NO CACHE;
GO

-- ---------------------------------------------------------
-- 2. MESSAGE STATUS INDEXES
-- ---------------------------------------------------------
CREATE NONCLUSTERED INDEX IX_MessageStatus_UserID_Status
ON Messaging.MessageStatus(UserID, Status)
INCLUDE (MessageID, ReadAt);
GO

-- ---------------------------------------------------------
-- 3. USER DEVICE TOKENS
-- ---------------------------------------------------------
CREATE TABLE Messaging.UserDeviceTokens (
    TokenID BIGINT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    DeviceToken NVARCHAR(255) NOT NULL UNIQUE,
    DeviceType NVARCHAR(20) NOT NULL 
        CONSTRAINT CK_UserDeviceTokens_DeviceType CHECK (DeviceType IN ('ANDROID','IOS','WEB')),
    DeviceName NVARCHAR(100) NULL,
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    LastUsedAt DATETIME NOT NULL DEFAULT GETDATE(),

    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE
);
GO

CREATE NONCLUSTERED INDEX IX_UserDeviceTokens_UserID_IsActive
ON Messaging.UserDeviceTokens(UserID, IsActive)
WHERE IsActive = 1;
GO

-- ---------------------------------------------------------
-- 4. WEBSOCKET SESSIONS
-- ---------------------------------------------------------
CREATE TABLE Messaging.WebSocketSessions (
    SessionID BIGINT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    SessionToken NVARCHAR(255) NOT NULL UNIQUE,
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    ExpiresAt DATETIME NOT NULL,
    IsActive BIT NOT NULL DEFAULT 1,

    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE
);
GO

CREATE NONCLUSTERED INDEX IX_WebSocketSessions_SessionToken
ON Messaging.WebSocketSessions(SessionToken, IsActive)
WHERE IsActive = 1;
GO

-- Cleanup expired sessions
CREATE OR ALTER PROCEDURE Messaging.SP_CleanupExpiredWebSocketSessions
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE Messaging.WebSocketSessions
    SET IsActive = 0
    WHERE ExpiresAt < GETDATE() AND IsActive = 1;
END;
GO

-- ---------------------------------------------------------
-- 5. PERFORMANCE INDEXES
-- ---------------------------------------------------------
CREATE NONCLUSTERED INDEX IX_Messages_ConversationID_SequenceNumber
ON Messaging.Messages(ConversationID, SequenceNumber DESC)
INCLUDE (SenderID, MessageType, SentAt, IsDeleted);
GO

CREATE NONCLUSTERED INDEX IX_Messages_SenderID_SentAt
ON Messaging.Messages(SenderID, SentAt DESC)
INCLUDE (ConversationID, MessageType, IsDeleted);
GO

CREATE NONCLUSTERED INDEX IX_ConversationMembers_UserID_JoinedAt
ON Messaging.ConversationMembers(UserID, JoinedAt DESC)
INCLUDE (ConversationID, LastReadMessageID, MutedUntil);
GO

CREATE NONCLUSTERED INDEX IX_MessageMedia_MessageID_MediaType
ON Messaging.MessageMedia(MessageID, MediaType)
INCLUDE (MediaName, ThumbnailName);
GO

-- ---------------------------------------------------------
-- 6. STORED PROCEDURES
-- ---------------------------------------------------------

-- Get unread message count
CREATE OR ALTER PROCEDURE Messaging.SP_GetUnreadMessageCount
    @UserID INT,
    @ConversationID INT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT COUNT(*) AS UnreadCount
    FROM Messaging.Messages M
    INNER JOIN Messaging.ConversationMembers CM 
        ON M.ConversationID = CM.ConversationID
    WHERE CM.UserID = @UserID
        AND M.ConversationID = @ConversationID
        AND M.SequenceNumber > ISNULL(
            (SELECT M2.SequenceNumber 
             FROM Messaging.Messages M2 
             WHERE M2.MessageID = CM.LastReadMessageID), 0
        )
        AND M.SenderID <> @UserID
        AND M.IsDeleted = 0;
END;
GO

-- Mark messages as read
CREATE OR ALTER PROCEDURE Messaging.SP_MarkMessagesAsRead
    @UserID INT,
    @ConversationID INT,
    @LastMessageID BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;

    UPDATE Messaging.ConversationMembers WITH (ROWLOCK)
    SET LastReadMessageID = @LastMessageID
    WHERE UserID = @UserID
      AND ConversationID = @ConversationID
      AND (LastReadMessageID IS NULL OR LastReadMessageID < @LastMessageID);

    UPDATE ms WITH (ROWLOCK)
    SET ms.Status = 'READ',
        ms.ReadAt = SYSUTCDATETIME()
    FROM Messaging.MessageStatus ms
    INNER JOIN Messaging.Messages m 
        ON m.MessageID = ms.MessageID
    WHERE ms.UserID = @UserID
      AND m.ConversationID = @ConversationID
      AND m.MessageID <= @LastMessageID
      AND m.SenderID <> @UserID
      AND ms.Status <> 'READ';

    COMMIT TRANSACTION;
END;
GO

-- ---------------------------------------------------------
-- 7. VIEWS
-- ---------------------------------------------------------
CREATE OR ALTER VIEW Messaging.VW_MessageDetails
AS
SELECT 
    M.MessageID,
    M.ConversationID,
    M.SenderID,
    U.Username AS SenderUsername,
    U.ProfilePictureURL AS SenderAvatar,
    MB.Content,
    M.MessageType,
    M.SentAt,
    M.SequenceNumber,
    M.ReplyToMessageID,
    M.IsDeleted,
    (
        SELECT COUNT(*) 
        FROM Messaging.MessageMedia MM 
        WHERE MM.MessageID = M.MessageID
    ) AS MediaCount
FROM Messaging.Messages M
INNER JOIN CoreData.Users U ON M.SenderID = U.UserID
LEFT JOIN Messaging.MessageBodies MB ON M.MessageID = MB.MessageID;
GO
