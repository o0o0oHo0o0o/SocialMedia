-- *******************************************************************
-- SOCIAL MEDIA DATABASE
-- *******************************************************************
use master
ALTER DATABASE SocialMedia SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
GO
DROP DATABASE IF EXISTS SocialMedia;
GO

CREATE DATABASE SocialMedia;
GO

USE SocialMedia;
GO

-- *******************************************************************
-- Tạo Schema CoreData
-- *******************************************************************
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'CoreData')
    EXEC('CREATE SCHEMA CoreData');
GO

-- *******************************************************************
-- 1. USERS AND ROLES
-- *******************************************************************

-- Users
CREATE TABLE CoreData.Users (
    UserID INT PRIMARY KEY IDENTITY(1,1),
    Username NVARCHAR(50) UNIQUE NOT NULL,
    Email NVARCHAR(100) UNIQUE NOT NULL,
	PhoneNumber NVARCHAR(15),
    PasswordHash NVARCHAR(256),
    FullName NVARCHAR(100) COLLATE Vietnamese_CI_AS,
    Bio NVARCHAR(500) COLLATE Vietnamese_CI_AS,
	ProfilePictureURL NVARCHAR(2048),
    CreatedAt DATETIME DEFAULT GETDATE(),
	LastLogin DateTime,
	AuthProvider NVARCHAR,
	IsVerified BIT NOT NULL DEFAULT 0,
	IsDeleted BIT NOT NULL DEFAULT 0,
    DeletedAt DATETIME2 NULL
);
GO
USE SocialMedia;
DECLARE @maxTokenId INT = (SELECT ISNULL(MAX(TokenID), 0) FROM CoreData.RefreshTokens);
DBCC CHECKIDENT ('CoreData.RefreshTokens', RESEED, @maxTokenId);

-- *******************************************************************
-- 0. REFRESH TOKENS
-- *******************************************************************
CREATE TABLE CoreData.RefreshTokens (

    TokenID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT,
    Token NVARCHAR(36) NOT NULL UNIQUE,
    ExpiryDate DATETIME2,
    CreatedAt DATETIME2,
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID)
);

ALTER TABLE CoreData.Users
ALTER COLUMN AuthProvider NVARCHAR(20);
GO
CREATE NONCLUSTERED INDEX IX_Users_ActiveUsers
ON CoreData.Users(UserID) WHERE DeletedAt IS NULL;
GO
-- Roles
CREATE TABLE CoreData.Roles (
    RoleID INT PRIMARY KEY IDENTITY(1,1),
    RoleName NVARCHAR(50) UNIQUE NOT NULL
);
GO

-- UserRole (Many-to-Many)
CREATE TABLE CoreData.UserRole (
    RoleID INT NOT NULL,
    UserID INT NOT NULL,
    AssignedAt DATETIME DEFAULT GETDATE(),
    
    PRIMARY KEY (RoleID, UserID),
    FOREIGN KEY (RoleID) REFERENCES CoreData.Roles(RoleID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE
);
GO

-- *******************************************************************
-- 2. INTERACTABLE ITEMS (TRUNG TÂM)
-- *******************************************************************

-- InteractableItems
CREATE TABLE CoreData.InteractableItems (
    InteractableItemID BIGINT PRIMARY KEY IDENTITY(1,1),
    ItemType NVARCHAR(20) NOT NULL 
        CHECK (ItemType IN ('POST', 'MEDIA', 'COMMENT', 'SHARE', 'MESSAGE', 'STORY')),
    CreatedAt DATETIME DEFAULT GETDATE()
);
GO

CREATE INDEX IX_InteractableItems_ItemType 
ON CoreData.InteractableItems(ItemType);
GO

-- *******************************************************************
-- 3. POSTS, STORIES
-- *******************************************************************

CREATE TABLE CoreData.Posts (
    PostID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL, 
    InteractableItemID BIGINT UNIQUE NOT NULL,
    Content NVARCHAR(MAX), 
    PostTopic NVARCHAR(50),
    Location NVARCHAR(100),
    IsArchived BIT DEFAULT 0,
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME,
    IsDeleted BIT NOT NULL DEFAULT 0,
    DeletedAt DATETIME2 NULL,
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (InteractableItemID) REFERENCES CoreData.InteractableItems(InteractableItemID) ON DELETE CASCADE
);
GO
CREATE NONCLUSTERED INDEX IX_Posts_ActivePosts
ON CoreData.Posts(PostID, UserID, CreatedAt) WHERE DeletedAt IS NULL;
GO

CREATE INDEX IX_Posts_UserID_CreatedAt 
ON CoreData.Posts(UserID, CreatedAt DESC);
GO

CREATE INDEX IX_Posts_InteractableItemID 
ON CoreData.Posts(InteractableItemID);
GO

CREATE TABLE CoreData.Stories (
    StoryID BIGINT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
	InteractableItemID BIGINT UNIQUE NOT NULL,
    MediaURL NVARCHAR(255) NOT NULL,
    MediaType NVARCHAR(10) NOT NULL CHECK (MediaType IN ('IMAGE', 'VIDEO')),
    CreatedAt DATETIME DEFAULT GETDATE(),
    ExpiresAt AS DATEADD(hour, 24, CreatedAt) PERSISTED, -- Cột tính toán tự động
	
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE,
	FOREIGN KEY (InteractableItemID) REFERENCES CoreData.InteractableItems 
);
GO

-- Index để lấy story của user và để dọn dẹp story đã hết hạn
CREATE INDEX IX_Stories_UserID_CreatedAt ON CoreData.Stories(UserID, CreatedAt DESC);
CREATE INDEX IX_Stories_ExpiresAt ON CoreData.Stories(ExpiresAt);
GO

-- *******************************************************************
-- 4. POST MEDIA
-- *******************************************************************

CREATE TABLE CoreData.PostMedia (
    MediaID BIGINT PRIMARY KEY IDENTITY(1,1),
    PostID INT NOT NULL, 
    InteractableItemID BIGINT UNIQUE NOT NULL,
    MediaURL NVARCHAR(255) NOT NULL, 
    MediaType NVARCHAR(10) NOT NULL 
        CHECK (MediaType IN ('IMAGE', 'VIDEO')),
    Caption NVARCHAR(500),
    SortOrder INT DEFAULT 0,
    
    FOREIGN KEY (PostID) REFERENCES CoreData.Posts(PostID) ON DELETE CASCADE,
    FOREIGN KEY (InteractableItemID) REFERENCES CoreData.InteractableItems(InteractableItemID) 
);
GO

CREATE INDEX IX_PostMedia_PostID_SortOrder 
ON CoreData.PostMedia(PostID, SortOrder);
GO

CREATE INDEX IX_PostMedia_InteractableItemID 
ON CoreData.PostMedia(InteractableItemID);
GO

-- *******************************************************************
-- 5. SHARES (Đặt trước FeedItems để tránh lỗi dependency)
-- *******************************************************************

CREATE TABLE CoreData.Shares (
    ShareID BIGINT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    OriginalPostID INT NOT NULL,
    InteractableItemID BIGINT UNIQUE NOT NULL,
    ShareCaption NVARCHAR(500),
    CreatedAt DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (OriginalPostID) REFERENCES CoreData.Posts(PostID) ON DELETE NO ACTION,
    FOREIGN KEY (InteractableItemID) REFERENCES CoreData.InteractableItems(InteractableItemID) ON DELETE CASCADE,

    UNIQUE (UserID, OriginalPostID)
);
GO

CREATE INDEX IX_Shares_UserID_CreatedAt 
ON CoreData.Shares(UserID, CreatedAt DESC);
GO

CREATE INDEX IX_Shares_OriginalPostID 
ON CoreData.Shares(OriginalPostID);
GO

CREATE INDEX IX_Shares_InteractableItemID 
ON CoreData.Shares(InteractableItemID);
GO

-- *******************************************************************
-- 6. FEED ITEMS
-- *******************************************************************

CREATE TABLE CoreData.FeedItems (
    FeedItemID BIGINT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    PostID INT NOT NULL,
    ActivityType NVARCHAR(20) NOT NULL
        CHECK (ActivityType IN ('CREATED', 'SHARED')),
    ActorUserID INT NOT NULL,
    ItemID BIGINT NULL,
    CreatedAt DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (PostID) REFERENCES CoreData.Posts(PostID),
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (ActorUserID) REFERENCES CoreData.Users(UserID) ON DELETE NO ACTION,
);
GO

CREATE INDEX IX_FeedItems_UserID_CreatedAt 
ON CoreData.FeedItems(UserID, CreatedAt DESC);
GO

CREATE INDEX IX_FeedItems_PostID
ON CoreData.FeedItems(PostID);
GO

-- *******************************************************************
-- 7. COMMENTS
-- *******************************************************************

CREATE TABLE CoreData.Comments (
    CommentID BIGINT PRIMARY KEY IDENTITY(1,1), 
    UserID INT NOT NULL,
    TargetInteractableItemID BIGINT NOT NULL,
    OwnInteractableItemID BIGINT UNIQUE NOT NULL,
    ParentCommentID BIGINT NULL,
    Content NVARCHAR(1000) NOT NULL, 
    CreatedAt DATETIME DEFAULT GETDATE(),
    IsDeleted BIT NOT NULL DEFAULT 0,
    DeletedAt DATETIME2 NULL,
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (TargetInteractableItemID) 
        REFERENCES CoreData.InteractableItems(InteractableItemID),
    FOREIGN KEY (OwnInteractableItemID) 
        REFERENCES CoreData.InteractableItems(InteractableItemID) ON DELETE NO ACTION,
    FOREIGN KEY (ParentCommentID) 
        REFERENCES CoreData.Comments(CommentID)
);
GO

CREATE NONCLUSTERED INDEX IX_Comments_ActiveComments
ON CoreData.Comments(CommentID, TargetInteractableItemID) WHERE DeletedAt IS NULL;
GO

CREATE INDEX IX_Comments_TargetInteractableItemID_CreatedAt 
ON CoreData.Comments(TargetInteractableItemID, CreatedAt DESC);
GO

CREATE INDEX IX_Comments_UserID 
ON CoreData.Comments(UserID);
GO

CREATE INDEX IX_Comments_ParentCommentID 
ON CoreData.Comments(ParentCommentID);
GO

CREATE INDEX IX_Comments_OwnInteractableItemID 
ON CoreData.Comments(OwnInteractableItemID);
GO

-- *******************************************************************
-- 8. REACTIONS
-- *******************************************************************

CREATE TABLE CoreData.Reactions (
    ReactionID BIGINT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    InteractableItemID BIGINT NOT NULL,
    ReactionType NVARCHAR(20) NOT NULL 
        CHECK (ReactionType IN ('LIKE', 'LOVE', 'HAHA', 'WOW', 'SAD', 'ANGRY')),
    ReactedAt DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID),
    FOREIGN KEY (InteractableItemID) 
        REFERENCES CoreData.InteractableItems(InteractableItemID) ON DELETE CASCADE,
    
    UNIQUE (UserID, InteractableItemID)
);
GO

CREATE INDEX IX_Reactions_InteractableItemID 
ON CoreData.Reactions(InteractableItemID);
GO

CREATE INDEX IX_Reactions_UserID 
ON CoreData.Reactions(UserID);
GO

-- *******************************************************************
-- 9. POST TAGS, HASHTAG
-- *******************************************************************

CREATE TABLE CoreData.PostTags (
    PostID INT NOT NULL, 
    TaggedUserID INT NOT NULL, 
    TaggedAt DATETIME DEFAULT GETDATE(),
    
    PRIMARY KEY (PostID, TaggedUserID),
    FOREIGN KEY (PostID) REFERENCES CoreData.Posts(PostID) ON DELETE NO ACTION,
    FOREIGN KEY (TaggedUserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE
);
GO

CREATE INDEX IX_PostTags_TaggedUserID 
ON CoreData.PostTags(TaggedUserID);
GO

CREATE TABLE CoreData.Hashtags (
    HashtagID INT PRIMARY KEY IDENTITY(1,1),
    TagName NVARCHAR(100) UNIQUE NOT NULL
);
GO

CREATE INDEX IX_Hashtags_TagName ON CoreData.Hashtags(TagName);
GO

CREATE TABLE CoreData.PostHashtags (
    PostID INT NOT NULL,
    HashtagID INT NOT NULL,

    PRIMARY KEY (PostID, HashtagID),
    FOREIGN KEY (PostID) REFERENCES CoreData.Posts(PostID) ON DELETE CASCADE,
    FOREIGN KEY (HashtagID) REFERENCES CoreData.Hashtags(HashtagID) ON DELETE CASCADE
);
GO

CREATE INDEX IX_PostHashtags_HashtagID ON CoreData.PostHashtags(HashtagID);
GO
-- *******************************************************************
-- 10. RELATIONSHIPS
-- *******************************************************************

-- Follows
CREATE TABLE CoreData.Follows (
    FollowerID INT NOT NULL,
    FollowingID INT NOT NULL,
    FollowedAt DATETIME DEFAULT GETDATE(),
    
    PRIMARY KEY (FollowerID, FollowingID),
    FOREIGN KEY (FollowerID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (FollowingID) REFERENCES CoreData.Users(UserID) ON DELETE NO ACTION,

    CHECK (FollowerID <> FollowingID)
);
GO

CREATE INDEX IX_Follows_FollowingID 
ON CoreData.Follows(FollowingID);
GO

-- Blocks
CREATE TABLE CoreData.Blocks (
    BlockerID INT NOT NULL, 
    BlockedUserID INT NOT NULL, 
    BlockedAt DATETIME DEFAULT GETDATE(),
    
    PRIMARY KEY (BlockerID, BlockedUserID),
    FOREIGN KEY (BlockerID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (BlockedUserID) REFERENCES CoreData.Users(UserID) ON DELETE NO ACTION,

    CHECK (BlockerID <> BlockedUserID)
);
GO

CREATE INDEX IX_Blocks_BlockedUserID 
ON CoreData.Blocks(BlockedUserID);
GO

-- *******************************************************************
-- 11. REPORTS
-- *******************************************************************

CREATE TABLE CoreData.Reports (
    ReportID INT PRIMARY KEY IDENTITY(1,1),
    ReporterID INT NOT NULL, 
    ReportedPostID INT NULL, 
    ReportedCommentID BIGINT NULL,
    ReportedUserID INT NULL,
    Reason NVARCHAR(255) NOT NULL, 
    ReportStatus NVARCHAR(20) DEFAULT 'PENDING'
        CHECK (ReportStatus IN ('PENDING', 'REVIEWED', 'RESOLVED', 'REJECTED')),
    ReportedAt DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (ReporterID) REFERENCES CoreData.Users(UserID) ON DELETE NO ACTION,
    FOREIGN KEY (ReportedPostID) REFERENCES CoreData.Posts(PostID) ON DELETE NO ACTION,
    FOREIGN KEY (ReportedCommentID) REFERENCES CoreData.Comments(CommentID) ON DELETE NO ACTION,
    FOREIGN KEY (ReportedUserID) REFERENCES CoreData.Users(UserID) ON DELETE NO ACTION,
    
    -- Phải report ít nhất 1 thứ
    CHECK (ReportedPostID IS NOT NULL OR ReportedCommentID IS NOT NULL OR ReportedUserID IS NOT NULL)
);
GO

CREATE INDEX IX_Reports_ReporterID 
ON CoreData.Reports(ReporterID);
GO

CREATE INDEX IX_Reports_ReportStatus 
ON CoreData.Reports(ReportStatus);
GO

-- *******************************************************************
-- 12. NOFITICATION
-- *******************************************************************
CREATE TABLE CoreData.Notifications (
    NotificationID BIGINT PRIMARY KEY IDENTITY(1,1),
    RecipientUserID INT NOT NULL,  
    ActorUserID INT NOT NULL,      
    NotificationType NVARCHAR(50) NOT NULL
        CHECK (NotificationType IN ('NEW_COMMENT', 'NEW_REACTION', 'NEW_FOLLOWER', 'POST_TAG', 'COMMENT_MENTION')),
    TargetItemID BIGINT NULL,      
    IsRead BIT DEFAULT 0,
    CreatedAt DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (RecipientUserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (ActorUserID) REFERENCES CoreData.Users(UserID)
);
GO

CREATE INDEX IX_Notifications_RecipientUserID_IsRead_CreatedAt
ON CoreData.Notifications(RecipientUserID, IsRead, CreatedAt DESC);
GO


-- *******************************************************************
-- 13. UserDeviceToken
-- *******************************************************************

CREATE TABLE CoreData.UserDeviceToken (

    TokenID BIGINT IDENTITY(1,1) NOT NULL,
    UserID INT NOT NULL,

    -- Chuỗi device token (từ FCM/APNs).
    -- Phải là UNIQUE để tránh lưu trùng lặp.
    DeviceToken NVARCHAR(255) NOT NULL,

    -- Trạng thái: 1 = đang hoạt động (dùng để gửi), 0 = đã vô hiệu (ví dụ: logout)
    IsActive BIT NOT NULL DEFAULT 1,

    -- Lưu loại thiết bị: 'ANDROID', 'IOS', 'WEB'
    DeviceType NVARCHAR(20) NULL,

    LastUsedAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_UserDeviceToken PRIMARY KEY (TokenID),

    CONSTRAINT FK_UserDeviceToken_Users FOREIGN KEY (UserID)
        REFERENCES CoreData.Users (UserID)
        ON DELETE CASCADE,

    CONSTRAINT UQ_UserDeviceToken_DeviceToken UNIQUE (DeviceToken)
);
GO
-- Tạo index cho hàm "findByUser_UserIdAndIsActive"
-- Đây là index quan trọng nhất để tăng tốc độ lấy token khi gửi thông báo
CREATE INDEX IX_UserDeviceToken_UserID_IsActive
ON CoreData.UserDeviceToken (UserID, IsActive)
WHERE IsActive = 1;
GO

-- *******************************************************************
-- 14. WebSocketSessions
-- *******************************************************************
CREATE TABLE CoreData.WebSocketSessions (
    ID BIGINT PRIMARY KEY IDENTITY(1,1),
    SessionToken NVARCHAR(255) NOT NULL,
    UserID INT NOT NULL,
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    ExpiresAt DATETIME NOT NULL,
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE,
	CONSTRAINT CK_WebSocketSessions_ExpiresAfterCreated
CHECK (ExpiresAt > CreatedAt),
);
CREATE UNIQUE INDEX UQ_WebSocketSessions_SessionToken
ON CoreData.WebSocketSessions(SessionToken);

CREATE INDEX IX_WebSocketSessions_User_Active
ON CoreData.WebSocketSessions(UserID, IsActive)
WHERE (IsActive = 1);

CREATE INDEX IX_WebSocketSessions_Expires_Active
ON CoreData.WebSocketSessions(ExpiresAt, IsActive)
WHERE (IsActive = 1);


