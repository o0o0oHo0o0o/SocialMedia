DROP DATABASE SocialMedia;
CREATE DATABASE SocialMedia;
USE SocialMedia;

-- *******************************************************************
-- Schema CoreData
-- *******************************************************************

-- Schema CoreData 
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'CoreData')
    EXEC('CREATE SCHEMA CoreData');
GO


-- *******************************************************************
-- 1. User and Roles (Schema CoreData)
-- *******************************************************************
--  Users (CoreData)
CREATE TABLE CoreData.Users (
    UserID INT PRIMARY KEY IDENTITY(1,1),
    Username NVARCHAR(50) UNIQUE NOT NULL,
    Email NVARCHAR(100) UNIQUE NOT NULL,
    PasswordHash NVARCHAR(256) NOT NULL, 
    FullName NVARCHAR(100),
    Bio NVARCHAR(500), 
    ProfilePictureURL NVARCHAR(255), 
    CreatedAt DATETIME DEFAULT GETDATE()
);
GO

CREATE TABLE CoreData.Roles (
	RoleID INT PRIMARY KEY IDENTITY(1,1),
	Rolename NVARCHAR(50) UNIQUE NOT NULL	
)
GO

CREATE TABLE CoreData.UserRole (
	RoleID INT NOT NULL,
	UserID INT NOT NULL,
	PRIMARY KEY (RoleID, UserID),
	FOREIGN KEY (RoleID) REFERENCES CoreData.Roles(RoleID),
	FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID)
)
GO
-- *******************************************************************
-- 2. Posts and Interaction (Schema CoreData)
-- *******************************************************************

-- Posts (CoreData)
CREATE TABLE CoreData.Posts (
    PostID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL, 
    Content NVARCHAR(MAX) NOT NULL, 
    PostType NVARCHAR(20) DEFAULT 'TEXT' NOT NULL, -- Education, Travel, Humor, Food, Art, Positive, Technology, DIY, Health
    Location NVARCHAR(100),
    IsArchived BIT DEFAULT 0,
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME,
    
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID) ON DELETE CASCADE
);
GO
-- *******************************************************************
-- PostMedia
-- *******************************************************************
CREATE TABLE CoreData.PostMedia (
    MediaID BIGINT PRIMARY KEY IDENTITY(1,1),
    PostID INT NOT NULL, 
    MediaURL NVARCHAR(255) NOT NULL, 
    MediaType NVARCHAR(10) NOT NULL, 
    SortOrder INT DEFAULT 0, 
    FOREIGN KEY (PostID) REFERENCES CoreData.Posts(PostID) ON DELETE CASCADE,
);
GO

-- Index quan trọng: Lấy tất cả media của một Post theo thứ tự
CREATE NONCLUSTERED INDEX IX_CoreData_PostMedia_PostID_SortOrder 
ON CoreData.PostMedia (PostID, SortOrder); 
GO

-- Comments (CoreData)
CREATE TABLE CoreData.Comments (
    CommentID INT PRIMARY KEY IDENTITY(1,1),
    PostID INT NOT NULL, 
    UserID INT NOT NULL, 
	ParentCommentID INT NULL,
    Content NVARCHAR(500) NOT NULL, 
    CreatedAt DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (PostID) REFERENCES CoreData.Posts(PostID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID),
	FOREIGN KEY (ParentCommentID) REFERENCES CoreData.Comments(CommentID)
)
GO

CREATE TABLE CoreData.Reactions (
    ReactionID INT PRIMARY KEY IDENTITY(1,1),
    PostID INT NOT NULL, 
    UserID INT NOT NULL, 
    ReactionType NVARCHAR(20) NOT NULL,
    ReactedAt DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (PostID) REFERENCES CoreData.Posts(PostID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID),

    UNIQUE (PostID, UserID)
);
GO


-- Shares (CoreData)
CREATE TABLE CoreData.Shares (
    ShareID INT PRIMARY KEY IDENTITY(1,1),
    PostID INT NOT NULL, 
    UserID INT NOT NULL, 
    SharedAt DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (PostID) REFERENCES CoreData.Posts(PostID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES CoreData.Users(UserID)
);
GO

-- PostTags (CoreData)
CREATE TABLE CoreData.PostTags (
    PostID INT NOT NULL, 
    TaggedUserID INT NOT NULL, 
    TaggedAt DATETIME DEFAULT GETDATE(),
    
    PRIMARY KEY (PostID, TaggedUserID),
    FOREIGN KEY (PostID) REFERENCES CoreData.Posts(PostID) ON DELETE CASCADE,
    FOREIGN KEY (TaggedUserID) REFERENCES CoreData.Users(UserID)
);
GO

-- *******************************************************************
-- 3. Relation (Schema CoreData)
-- *******************************************************************

-- Follows (CoreData)
CREATE TABLE CoreData.Follows (
    FollowerID INT NOT NULL,
    FollowingID INT NOT NULL,
    FollowedAt DATETIME DEFAULT GETDATE(),
    
    PRIMARY KEY (FollowerID, FollowingID),
    FOREIGN KEY (FollowerID) REFERENCES CoreData.Users(UserID),
    FOREIGN KEY (FollowingID) REFERENCES CoreData.Users(UserID),
    
    CHECK (FollowerID <> FollowingID)
);
GO

-- Blocks (CoreData)
CREATE TABLE CoreData.Blocks (
    BlockerID INT NOT NULL, 
    BlockedUserID INT NOT NULL, 
    BlockedAt DATETIME DEFAULT GETDATE(),
    
    PRIMARY KEY (BlockerID, BlockedUserID),
    FOREIGN KEY (BlockerID) REFERENCES CoreData.Users(UserID),
    FOREIGN KEY (BlockedUserID) REFERENCES CoreData.Users(UserID),
    
    CHECK (BlockerID <> BlockedUserID)
);
GO

-- Reports (CoreData)
CREATE TABLE CoreData.Reports (
    ReportID INT PRIMARY KEY IDENTITY(1,1),
    ReporterID INT NOT NULL, 
    ReportedPostID INT, 
    ReportedCommentID INT,
    ReportedUserID INT,
    
    Reason NVARCHAR(255) NOT NULL, 
    ReportStatus NVARCHAR(20) DEFAULT 'PENDING',
    ReportedAt DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (ReporterID) REFERENCES CoreData.Users(UserID) ON DELETE NO ACTION,
    FOREIGN KEY (ReportedPostID) REFERENCES CoreData.Posts(PostID) ON DELETE NO ACTION,
    FOREIGN KEY (ReportedCommentID) REFERENCES CoreData.Comments(CommentID) ON DELETE NO ACTION,
    FOREIGN KEY (ReportedUserID) REFERENCES CoreData.Users(UserID) ON DELETE NO ACTION
);
GO

-- Handle cascade logic 
CREATE TRIGGER trg_CleanupReports_OnUserDelete
ON CoreData.Users
AFTER DELETE
AS
BEGIN
    -- Delete related reports when user is deleted
    DELETE FROM CoreData.Reports 
    WHERE ReporterID IN (SELECT UserID FROM deleted)
       OR ReportedUserID IN (SELECT UserID FROM deleted);
END;
GO

CREATE TRIGGER trg_CleanupReports_OnPostDelete
ON CoreData.Posts
AFTER DELETE
AS
BEGIN
    -- Set NULL for ReportedPostID when post is deleted
    UPDATE CoreData.Reports 
    SET ReportedPostID = NULL
    WHERE ReportedPostID IN (SELECT PostID FROM deleted);
END;
GO

CREATE TRIGGER trg_CleanupReports_OnCommentDelete
ON CoreData.Comments
AFTER DELETE
AS
BEGIN
    -- Set NULL for ReportedCommentID when comment is deleted 
    UPDATE CoreData.Reports 
    SET ReportedCommentID = NULL
    WHERE ReportedCommentID IN (SELECT CommentID FROM deleted);
END;
GO
