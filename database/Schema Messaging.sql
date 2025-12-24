USE SocialMedia;
GO

IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'Messaging')
    EXEC('CREATE SCHEMA Messaging');
GO

-- ---------------------------------------------------------
-- 1. CONVERSATIONS
-- ---------------------------------------------------------
CREATE TABLE Messaging.Conversations (
    ConversationID INT PRIMARY KEY IDENTITY(1,1),
    ConversationName NVARCHAR(100),
    GroupImageFile NVARCHAR(255) NULL,
    IsGroupChat BIT NOT NULL DEFAULT 0,
    LastMessageID BIGINT NULL,
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    CreatedByUserID INT NOT NULL,
    FOREIGN KEY (CreatedByUserID) REFERENCES CoreData.Users(UserID)
);
GO

CREATE NONCLUSTERED INDEX IX_Conversations_LastMessageID
ON Messaging.Conversations(LastMessageID);
GO

-- ---------------------------------------------------------
-- 2. CONVERSATION MEMBERS
-- ---------------------------------------------------------
CREATE TABLE Messaging.ConversationMembers (
    ConversationID INT NOT NULL,
    UserID INT NOT NULL,
    Nickname NVARCHAR(100) NULL,
    Role NVARCHAR(20) NOT NULL DEFAULT 'MEMBER'
        CONSTRAINT CK_ConversationMembers_Role CHECK (Role IN ('ADMIN','MEMBER')),
    LastReadMessageID BIGINT NULL,
    MutedUntil DATETIME NULL,
    JoinedAt DATETIME NOT NULL DEFAULT GETDATE(),

    PRIMARY KEY (ConversationID, UserID),
    FOREIGN KEY (ConversationID) REFERENCES Messaging.Conversations(ConversationID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE
);
GO

CREATE NONCLUSTERED INDEX IX_ConversationMembers_UserID
ON Messaging.ConversationMembers(UserID);
GO

-- ---------------------------------------------------------
-- 3. MESSAGES
-- ---------------------------------------------------------
CREATE TABLE Messaging.Messages (
    MessageID BIGINT PRIMARY KEY IDENTITY(1,1),
    ConversationID INT NOT NULL,
    SenderID INT NOT NULL,
    InteractableItemID BIGINT NULL UNIQUE,
    ReplyToMessageID BIGINT NULL,
    MessageType NVARCHAR(20) NOT NULL DEFAULT 'TEXT'
        CONSTRAINT CK_Messages_MessageType CHECK (MessageType IN ('TEXT','NOTIFICATION')),
    SentAt DATETIME NOT NULL DEFAULT GETDATE(),
    IsDeleted BIT NOT NULL DEFAULT 0,
    SequenceNumber BIGINT NOT NULL DEFAULT 0,

    FOREIGN KEY (ConversationID) REFERENCES Messaging.Conversations(ConversationID) ON DELETE CASCADE,
    FOREIGN KEY (SenderID) REFERENCES CoreData.Users(UserID),
    FOREIGN KEY (InteractableItemID) REFERENCES CoreData.InteractableItems(InteractableItemID),
    FOREIGN KEY (ReplyToMessageID) REFERENCES Messaging.Messages(MessageID)
);
GO

-- Body
CREATE TABLE Messaging.MessageBodies (
    MessageID BIGINT PRIMARY KEY,
    Content NVARCHAR(MAX),
    FOREIGN KEY (MessageID) REFERENCES Messaging.Messages(MessageID) ON DELETE CASCADE
);
GO

CREATE NONCLUSTERED INDEX IX_Messages_ConversationID_SentAt
ON Messaging.Messages(ConversationID, SentAt DESC)
INCLUDE (SenderID, MessageType, IsDeleted);
GO

-- ---------------------------------------------------------
-- 4. MESSAGE MEDIA
-- ---------------------------------------------------------
CREATE TABLE Messaging.MessageMedia (
    MediaID BIGINT PRIMARY KEY IDENTITY(1,1),
    MessageID BIGINT NOT NULL,
    MediaName NVARCHAR(255) NOT NULL,
    MediaType NVARCHAR(10) NOT NULL CHECK (MediaType IN ('IMAGE','VIDEO','AUDIO','FILE')),
    FileName NVARCHAR(255) NULL,
    FileSize INT NULL,
    ThumbnailName NVARCHAR(255) NULL,

    FOREIGN KEY (MessageID) REFERENCES Messaging.Messages(MessageID) ON DELETE CASCADE
);
GO

CREATE NONCLUSTERED INDEX IX_MessageMedia_MessageID
ON Messaging.MessageMedia(MessageID);
GO

-- ---------------------------------------------------------
-- 5. PINNED MESSAGES
-- ---------------------------------------------------------
CREATE TABLE Messaging.PinnedMessages (
    ConversationID INT NOT NULL,
    MessageID BIGINT NOT NULL,
    PinnedByUserID INT NOT NULL,
    PinnedAt DATETIME NOT NULL DEFAULT GETDATE(),

    PRIMARY KEY (ConversationID, MessageID),
    FOREIGN KEY (ConversationID) REFERENCES Messaging.Conversations(ConversationID) ON DELETE CASCADE,
    FOREIGN KEY (MessageID) REFERENCES Messaging.Messages(MessageID),
    FOREIGN KEY (PinnedByUserID) REFERENCES CoreData.Users(UserID)
);
GO

-- ---------------------------------------------------------
-- 6. MESSAGE STATUS
-- ---------------------------------------------------------
CREATE TABLE Messaging.MessageStatus (
    MessageID BIGINT NOT NULL,
    UserID INT NOT NULL,
    Status NVARCHAR(20) NOT NULL DEFAULT 'SENT',
    DeliveredAt DATETIME2 NULL,
    ReadAt DATETIME2 NULL,

    CONSTRAINT PK_MessageStatus PRIMARY KEY (MessageID, UserID),
    FOREIGN KEY (MessageID) REFERENCES Messaging.Messages(MessageID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE
);
GO
