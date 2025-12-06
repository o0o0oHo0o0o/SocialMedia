-- *******************************************************************
-- MESSAGING SCHEMA ENHANCEMENTS
-- SQL Server 2022 - SocialMedia Database
-- *******************************************************************
USE SocialMedia;
GO

-- *******************************************************************
-- 1. ADD SEQUENCE NUMBER TO MESSAGES
-- *******************************************************************
-- Add SequenceNumber column for message ordering
ALTER TABLE Messaging.Messages 
ADD SequenceNumber BIGINT NOT NULL DEFAULT 0;
GO

-- Create sequence per conversation
CREATE SEQUENCE Messaging.MessageSequence
    START WITH 1
    INCREMENT BY 1
    NO CACHE;
GO

-- *******************************************************************
-- 2. MESSAGE STATUS TABLE (Read Receipts)
-- *******************************************************************
CREATE TABLE Messaging.MessageStatus (
    MessageID BIGINT NOT NULL,
    UserID INT NOT NULL,
    Status NVARCHAR(20) NOT NULL DEFAULT 'SENT'
        CONSTRAINT CK_MessageStatus_Status CHECK (Status IN ('SENT', 'DELIVERED', 'READ')),
    DeliveredAt DATETIME NULL,
    ReadAt DATETIME NULL,
    
    PRIMARY KEY (MessageID, UserID),
    FOREIGN KEY (MessageID) REFERENCES Messaging.Messages(MessageID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE
);
GO

CREATE NONCLUSTERED INDEX IX_MessageStatus_UserID_Status 
ON Messaging.MessageStatus(UserID, Status) 
INCLUDE (MessageID, ReadAt);
GO

-- *******************************************************************
-- 3. USER DEVICE TOKENS (FCM Push Notifications)
-- *******************************************************************
CREATE TABLE Messaging.UserDeviceTokens (
    TokenID BIGINT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    DeviceToken NVARCHAR(255) NOT NULL UNIQUE,
    DeviceType NVARCHAR(20) NOT NULL 
        CONSTRAINT CK_UserDeviceTokens_DeviceType CHECK (DeviceType IN ('ANDROID', 'IOS', 'WEB')),
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

-- *******************************************************************
-- 4. WEBSOCKET SESSIONS (Temporary tokens)
-- *******************************************************************
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

-- Cleanup expired sessions (scheduled job)
CREATE OR ALTER PROCEDURE Messaging.SP_CleanupExpiredWebSocketSessions
AS
BEGIN
    SET NOCOUNT ON;
    
    UPDATE Messaging.WebSocketSessions
    SET IsActive = 0
    WHERE ExpiresAt < GETDATE() AND IsActive = 1;
END;
GO

-- *******************************************************************
-- 5. ADDITIONAL INDEXES FOR PERFORMANCE
-- *******************************************************************
DROP INDEX IF EXISTS IX_Messages_SenderID_SentAt
    ON Messaging.Messages;
GO
-- Index for fetching conversation messages with pagination
CREATE NONCLUSTERED INDEX IX_Messages_ConversationID_SequenceNumber
ON Messaging.Messages(ConversationID, SequenceNumber DESC)
INCLUDE (SenderID, MessageType, SentAt, IsDeleted); -- Bỏ Content ra

-- Index for unread message count
CREATE NONCLUSTERED INDEX IX_Messages_SenderID_SentAt
ON Messaging.Messages(SenderID, SentAt DESC)
INCLUDE (ConversationID, MessageType, IsDeleted);
GO

-- Index for conversation members lookup
CREATE NONCLUSTERED INDEX IX_ConversationMembers_UserID_JoinedAt
ON Messaging.ConversationMembers(UserID, JoinedAt DESC)
INCLUDE (ConversationID, LastReadMessageID, MutedUntil);
GO

-- Index for media lookup
CREATE NONCLUSTERED INDEX IX_MessageMedia_MessageID_MediaType
ON Messaging.MessageMedia(MessageID, MediaType)
INCLUDE (MediaURL, ThumbnailURL);
GO

-- *******************************************************************
-- 6. STORED PROCEDURES FOR COMMON OPERATIONS
-- *******************************************************************

-- Get unread message count per conversation
CREATE OR ALTER PROCEDURE Messaging.SP_GetUnreadMessageCount
    @UserID INT,
    @ConversationID INT
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT COUNT(*) as UnreadCount
    FROM Messaging.Messages M
    INNER JOIN Messaging.ConversationMembers CM 
        ON M.ConversationID = CM.ConversationID
    WHERE CM.UserID = @UserID
        AND M.ConversationID = @ConversationID
        AND M.SequenceNumber > ISNULL(
            (SELECT TOP 1 M2.SequenceNumber 
             FROM Messaging.Messages M2 
             WHERE M2.MessageID = CM.LastReadMessageID), 0
        )
        AND M.SenderID != @UserID
        AND M.IsDeleted = 0;
END;
GO

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

-- Get conversation list with last message
CREATE OR ALTER PROCEDURE Messaging.SP_GetUserConversations
    @UserID INT,
    @PageSize INT = 20,
    @PageNumber INT = 1
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        C.ConversationID,
        
        -- 1. LOGIC TÊN HỘI THOẠI
        -- Nếu là Group: Lấy ConversationName
        -- Nếu là Private: Lấy FullName của người kia (OtherUser)
        ConversationName = CASE 
            WHEN C.IsGroupChat = 1 THEN C.ConversationName COLLATE Vietnamese_CI_AS
            ELSE ISNULL(OtherU.FullName, OtherU.Username) COLLATE Vietnamese_CI_AS
        END,

        -- 2. LOGIC AVATAR
        -- Nếu là Group: Lấy GroupImageURL
        -- Nếu là Private: Lấy ProfilePictureURL của người kia
        -- (Lưu ý: Đây là FileName MinIO, về Java mới convert thành Link)
        GroupImageURL = CASE 
            WHEN C.IsGroupChat = 1 THEN C.GroupImageURL COLLATE Vietnamese_CI_AS
            ELSE OtherU.ProfilePictureURL COLLATE Vietnamese_CI_AS
        END,

        C.IsGroupChat,
        C.CreatedAt,
        
        -- 3. LOGIC TIN NHẮN CUỐI CÙNG
        LM.MessageID as LastMessageID,
        
        -- Nếu Content null (do gửi ảnh/file), hiển thị text thay thế dựa vào MediaType
        LastMessageContent = CASE 
            WHEN LMB.Content IS NOT NULL AND LEN(LMB.Content) > 0 THEN LMB.Content
            WHEN EXISTS (SELECT 1 FROM Messaging.MessageMedia MM WHERE MM.MessageID = LM.MessageID AND MM.MediaType = 'IMAGE') THEN N'[Hình ảnh]'
            WHEN EXISTS (SELECT 1 FROM Messaging.MessageMedia MM WHERE MM.MessageID = LM.MessageID AND MM.MediaType = 'VIDEO') THEN N'[Video]'
            WHEN EXISTS (SELECT 1 FROM Messaging.MessageMedia MM WHERE MM.MessageID = LM.MessageID AND MM.MediaType = 'FILE') THEN N'[Tập tin]'
            WHEN EXISTS (SELECT 1 FROM Messaging.MessageMedia MM WHERE MM.MessageID = LM.MessageID AND MM.MediaType = 'AUDIO') THEN N'[Tin nhắn thoại]'
            ELSE N'' 
        END,
        
        LM.SentAt as LastMessageSentAt,
        SenderU.Username as LastMessageSender, -- Người gửi tin cuối
        
        -- 4. TRẠNG THÁI
        CM.LastReadMessageID,
        CM.MutedUntil,
        
        -- Đếm số tin chưa đọc (So sánh SequenceNumber tin cuối với tin đã đọc của User)
        (
            SELECT COUNT(*) 
            FROM Messaging.Messages M
            WHERE M.ConversationID = C.ConversationID
                  AND M.SequenceNumber > ISNULL(
                      (SELECT M2.SequenceNumber 
                       FROM Messaging.Messages M2 
                       WHERE M2.MessageID = CM.LastReadMessageID), 0
                  )
                  AND M.SenderID != @UserID -- Không đếm tin mình gửi
                  AND M.IsDeleted = 0
        ) as UnreadCount

    FROM Messaging.Conversations C
    
    -- Join để lấy thông tin của User hiện tại trong hội thoại
    INNER JOIN Messaging.ConversationMembers CM 
        ON C.ConversationID = CM.ConversationID
    
    -- Join để tìm "Người kia" (Other User) CHỈ KHI LÀ CHAT 1-1 (IsGroupChat = 0)
    -- Điều kiện AND C.IsGroupChat = 0 rất quan trọng để tránh duplicate dòng khi query Group
    LEFT JOIN Messaging.ConversationMembers OtherCM 
        ON C.ConversationID = OtherCM.ConversationID 
        AND OtherCM.UserID != @UserID 
        AND C.IsGroupChat = 0 
        
    LEFT JOIN CoreData.Users OtherU 
        ON OtherCM.UserID = OtherU.UserID

    -- Join lấy tin nhắn cuối
    LEFT JOIN Messaging.Messages LM 
        ON C.LastMessageID = LM.MessageID
        
    LEFT JOIN Messaging.MessageBodies LMB 
        ON LM.MessageID = LMB.MessageID
        
    LEFT JOIN CoreData.Users SenderU 
        ON LM.SenderID = SenderU.UserID

    WHERE CM.UserID = @UserID
    
    ORDER BY LM.SentAt DESC
    
    OFFSET (@PageNumber - 1) * @PageSize ROWS
    FETCH NEXT @PageSize ROWS ONLY;
END;
GO

-- *******************************************************************
-- 7. VIEWS FOR COMMON QUERIES
-- *******************************************************************

-- View for message details with sender info
ALTER VIEW Messaging.VW_MessageDetails
AS
SELECT 
    M.MessageID,
    M.ConversationID,
    M.SenderID,
    U.Username as SenderUsername,
    U.ProfilePictureURL as SenderAvatar,
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
    ) as MediaCount
FROM Messaging.Messages M
INNER JOIN CoreData.Users U ON M.SenderID = U.UserID
LEFT JOIN Messaging.MessageBodies MB ON M.MessageID = MB.MessageID;
GO
