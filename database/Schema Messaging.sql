-- *******************************************************************
-- (Schema Messaging)
-- *******************************************************************
USE SocialMedia;
-- Schema Messaging 
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'Messaging')
    EXEC('CREATE SCHEMA Messaging');
GO

-- (Conversations) (Messaging)
CREATE TABLE Messaging.Conversations (
    ConversationID INT PRIMARY KEY IDENTITY(1,1),
    ConversationName NVARCHAR(100), 
    IsGroupChat BIT DEFAULT 0, 
    CreatedAt DATETIME DEFAULT GETDATE()
);
GO

-- (ConversationMembers) (Messaging)
CREATE TABLE Messaging.ConversationMembers (
    ConversationMemberID INT PRIMARY KEY IDENTITY(1,1),
    ConversationID INT NOT NULL,
    UserID INT NOT NULL,
    JoinedAt DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (ConversationID) REFERENCES Messaging.Conversations(ConversationID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID), -- Liên k?t d?n Users ? Schema CoreData
    
    UNIQUE (ConversationID, UserID) 
);
GO

-- (Messages) (Messaging)
CREATE TABLE Messaging.Messages (
    MessageID BIGINT PRIMARY KEY IDENTITY(1,1),
    ConversationID INT NOT NULL, 
    SenderID INT NOT NULL, 
    Content NVARCHAR(MAX) NOT NULL, 
    SentAt DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (ConversationID) REFERENCES Messaging.Conversations(ConversationID) ON DELETE CASCADE,
    FOREIGN KEY (SenderID) REFERENCES CoreData.Users(UserID) -- Liên k?t d?n Users ? Schema CoreData
);
GO
-- *******************************************************************
--  (INDEXES) 
-- *******************************************************************

-- Index cho Posts:
CREATE NONCLUSTERED INDEX IX_CoreData_Posts_UserID_CreatedAt 
ON CoreData.Posts (UserID, CreatedAt DESC); 
GO

-- Index cho Follows: 
CREATE NONCLUSTERED INDEX IX_CoreData_Follows_FollowerID 
ON CoreData.Follows (FollowerID); 
GO

-- Index cho Comments: 
CREATE NONCLUSTERED INDEX IX_CoreData_Comments_PostID_CreatedAt 
ON CoreData.Comments (PostID, CreatedAt);
GO

-- Index cho Messages: 
CREATE NONCLUSTERED INDEX IX_Messaging_Messages_ConversationID_SentAt 
ON Messaging.Messages (ConversationID, SentAt DESC);
GO

-- Index cho Likes: 
CREATE NONCLUSTERED INDEX IX_Reactions_PostID
ON CoreData.Reactions (PostID);

CREATE NONCLUSTERED INDEX IX_Reactions_PostID_ReactionType
ON CoreData.Reactions (PostID, ReactionType);

CREATE NONCLUSTERED INDEX IX_Reactions_PostID_UserID
ON CoreData.Reactions (PostID, UserID);
