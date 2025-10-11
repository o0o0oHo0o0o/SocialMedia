USE SocialMedia
DROP PROCEDURE IF EXISTS CleanAndReseedTable;
EXEC CleanAndReseedTable @TableName = 'CoreData.Users';
DELETE FROM CoreData.Posts WHERE PostID = 10
CREATE PROCEDURE CleanAndReseedTable
    @TableName NVARCHAR(128)
AS
BEGIN
    DECLARE @SqlDelete NVARCHAR(MAX)
    DECLARE @SqlReseed NVARCHAR(MAX)

    -- Dynamically create the DELETE query
    SET @SqlDelete = 'DELETE FROM ' + @TableName + ' WHERE CreatedAt is not null'

    -- Execute the DELETE query
    EXEC sp_executesql @SqlDelete

    -- Dynamically create the DBCC CHECKIDENT query
    SET @SqlReseed = 'DBCC CHECKIDENT (''' + @TableName + ''', RESEED, 0)'

    -- Execute the DBCC CHECKIDENT query
    EXEC sp_executesql @SqlReseed
END
--- Drop trigger
DROP TRIGGER CoreData.trg_DeletePostCascade
DROP TRIGGER CoreData.trg_DeleteCommentCascade
---Post
CREATE TRIGGER trg_DeletePostCascade
ON CoreData.Posts
AFTER DELETE
AS
BEGIN
    -- Delete FeedItems when a Share is deleted
    DELETE FROM CoreData.FeedItems WHERE FeedItemID IN (SELECT ShareID FROM deleted) AND ActivityType = 'CREATED';
    -- Delete InteractableItems when a Post is deleted
    DELETE FROM CoreData.InteractableItems WHERE InteractableItemID IN (SELECT PostID FROM deleted);
END;
GO

---Comment
CREATE TRIGGER trg_DeleteCommentCascade
ON CoreData.Comments
AFTER DELETE
AS
BEGIN

    -- Delete InteractableItems when a Comment is deleted (both OwnInteractableItemID and TargetInteractableItemID are involved)
    DELETE FROM CoreData.InteractableItems WHERE InteractableItemID IN (SELECT OwnInteractableItemID FROM deleted)
END;
GO
---SHARE
CREATE TRIGGER trg_DeleteShareCascade
ON CoreData.Shares
AFTER DELETE
AS
BEGIN
    -- Delete FeedItems when a Share is deleted
    DELETE FROM CoreData.FeedItems WHERE FeedItemID IN (SELECT ShareID FROM deleted) AND ActivityType = 'SHARED';
    -- Delete InteractableItems when a Share is deleted
    DELETE FROM CoreData.InteractableItems WHERE InteractableItemID IN (SELECT InteractableItemID FROM deleted);
END;
GO

---Message
CREATE TRIGGER trg_DeleteMessageCascade
ON Messaging.Messages
AFTER DELETE
AS
BEGIN
    -- Delete InteractableItems when a Message is deleted
    DELETE FROM CoreData.InteractableItems WHERE InteractableItemID IN (SELECT InteractableItemID FROM deleted);
END;
GO
