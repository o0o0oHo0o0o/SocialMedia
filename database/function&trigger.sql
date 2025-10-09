USE SocialMedia

DROP PROCEDURE IF EXISTS CleanAndReseedTable;
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

---User
CREATE TRIGGER trg_DeleteUserCascade
ON CoreData.Users
AFTER DELETE
AS
BEGIN
    -- Delete UserRole when a User is deleted
    DELETE FROM CoreData.UserRole WHERE UserID IN (SELECT UserID FROM deleted);
    
    -- Delete Posts when a User is deleted
    DELETE FROM CoreData.Posts WHERE UserID IN (SELECT UserID FROM deleted);
    
    -- Delete Stories when a User is deleted
    DELETE FROM CoreData.Stories WHERE UserID IN (SELECT UserID FROM deleted);
    
    -- Delete PostMedia when a Post is deleted
    DELETE FROM CoreData.PostMedia WHERE PostID IN (SELECT PostID FROM deleted);
    
    -- Delete Shares when a User is deleted
    DELETE FROM CoreData.Shares WHERE UserID IN (SELECT UserID FROM deleted);
    
    -- Delete FeedItems when a User is deleted
    DELETE FROM CoreData.FeedItems WHERE UserID IN (SELECT UserID FROM deleted);
    DELETE FROM CoreData.FeedItems WHERE ActorUserID IN (SELECT UserID FROM deleted);
    
    -- Delete Comments when a User is deleted
    DELETE FROM CoreData.Comments WHERE UserID IN (SELECT UserID FROM deleted);
    
    -- Delete Reactions when a User is deleted
    DELETE FROM CoreData.Reactions WHERE UserID IN (SELECT UserID FROM deleted);
    
    -- Delete PostTags when a Post is deleted
    DELETE FROM CoreData.PostTags WHERE PostID IN (SELECT PostID FROM deleted);
    
    -- Delete PostHashtags when a Post is deleted
    DELETE FROM CoreData.PostHashtags WHERE PostID IN (SELECT PostID FROM deleted);
    
    -- Delete Follows when a User is deleted
    DELETE FROM CoreData.Follows WHERE FollowerID IN (SELECT UserID FROM deleted) OR FollowingID IN (SELECT UserID FROM deleted);
    
    -- Delete Blocks when a User is deleted
    DELETE FROM CoreData.Blocks WHERE BlockerID IN (SELECT UserID FROM deleted) OR BlockedUserID IN (SELECT UserID FROM deleted);
    
    -- Delete Reports when a User is deleted
    DELETE FROM CoreData.Reports WHERE ReporterID IN (SELECT UserID FROM deleted);
    DELETE FROM CoreData.Reports WHERE ReportedUserID IN (SELECT UserID FROM deleted);
    
    -- Delete Notifications when a User is deleted
    DELETE FROM CoreData.Notifications WHERE RecipientUserID IN (SELECT UserID FROM deleted);
    DELETE FROM CoreData.Notifications WHERE ActorUserID IN (SELECT UserID FROM deleted);
    
    -- Delete any associated comments on interactable items like shares, posts, or reactions
    DELETE FROM CoreData.Comments WHERE TargetInteractableItemID IN (SELECT InteractableItemID FROM CoreData.InteractableItems WHERE CreatedBy IN (SELECT UserID FROM deleted));
    DELETE FROM CoreData.Reactions WHERE InteractableItemID IN (SELECT InteractableItemID FROM CoreData.InteractableItems WHERE CreatedBy IN (SELECT UserID FROM deleted));
END;
GO
---Role
CREATE TRIGGER trg_DeleteRoleCascade
ON CoreData.Roles
AFTER DELETE
AS
BEGIN
    -- Delete UserRole when a Role is deleted
    DELETE FROM CoreData.UserRole WHERE RoleID IN (SELECT RoleID FROM deleted);

    -- No need for cascading delete on `Posts`, `Shares`, etc., because RoleID is not directly referenced there.
END;
GO
---Interactable Item
CREATE TRIGGER trg_DeleteInteractableItemCascade
ON CoreData.InteractableItems
AFTER DELETE
AS
BEGIN
    -- Delete Posts when an InteractableItem is deleted
    DELETE FROM CoreData.Posts WHERE InteractableItemID IN (SELECT InteractableItemID FROM deleted);
    
    -- Delete PostMedia when an InteractableItem is deleted
    DELETE FROM CoreData.PostMedia WHERE InteractableItemID IN (SELECT InteractableItemID FROM deleted);

    -- Delete Shares when an InteractableItem is deleted
    DELETE FROM CoreData.Shares WHERE InteractableItemID IN (SELECT InteractableItemID FROM deleted);

    -- Delete Comments when an InteractableItem is deleted
    DELETE FROM CoreData.Comments WHERE TargetInteractableItemID IN (SELECT InteractableItemID FROM deleted);
    DELETE FROM CoreData.Comments WHERE OwnInteractableItemID IN (SELECT InteractableItemID FROM deleted);
    
    -- Delete Reactions when an InteractableItem is deleted
    DELETE FROM CoreData.Reactions WHERE InteractableItemID IN (SELECT InteractableItemID FROM deleted);

    -- Delete PostHashtags when an InteractableItem is deleted
    DELETE FROM CoreData.PostHashtags WHERE PostID IN (SELECT PostID FROM CoreData.Posts WHERE InteractableItemID IN (SELECT InteractableItemID FROM deleted));
END;
GO
---Post
CREATE TRIGGER trg_DeletePostCascade
ON CoreData.Posts
AFTER DELETE
AS
BEGIN
    -- Delete PostMedia when a Post is deleted
    DELETE FROM CoreData.PostMedia WHERE PostID IN (SELECT PostID FROM deleted);

    -- Delete Shares when a Post is deleted
    DELETE FROM CoreData.Shares WHERE OriginalPostID IN (SELECT PostID FROM deleted);

    -- Delete Comments when a Post is deleted (comments targeted at the post)
    DELETE FROM CoreData.Comments WHERE TargetInteractableItemID IN (SELECT InteractableItemID FROM CoreData.InteractableItems WHERE CreatedBy IN (SELECT UserID FROM deleted));
    
    -- Delete PostTags when a Post is deleted
    DELETE FROM CoreData.PostTags WHERE PostID IN (SELECT PostID FROM deleted);

    -- Delete PostHashtags when a Post is deleted
    DELETE FROM CoreData.PostHashtags WHERE PostID IN (SELECT PostID FROM deleted);
    
    -- Delete FeedItems when a Post is deleted (if posts were shared or involved in feed)
    DELETE FROM CoreData.FeedItems WHERE ItemID IN (SELECT PostID FROM deleted);
END;
GO
---Comment
CREATE TRIGGER trg_DeleteCommentCascade
ON CoreData.Comments
AFTER DELETE
AS
BEGIN
    -- Delete any comments replying to this comment
    DELETE FROM CoreData.Comments WHERE ParentCommentID IN (SELECT CommentID FROM deleted);
    
    -- Delete Reactions on this comment
    DELETE FROM CoreData.Reactions WHERE InteractableItemID IN (SELECT InteractableItemID FROM CoreData.InteractableItems WHERE CreatedBy IN (SELECT UserID FROM deleted));

    -- Delete FeedItems if comment was part of feed
    DELETE FROM CoreData.FeedItems WHERE ItemID IN (SELECT CommentID FROM deleted);
END;
GO
---Hagstag
CREATE TRIGGER trg_DeleteHashtagCascade
ON CoreData.Hashtags
AFTER DELETE
AS
BEGIN
    -- Delete PostHashtags when a Hashtag is deleted
    DELETE FROM CoreData.PostHashtags WHERE HashtagID IN (SELECT HashtagID FROM deleted);
END;
GO
---Follow
CREATE TRIGGER trg_DeleteFollowCascade
ON CoreData.Follows
AFTER DELETE
AS
BEGIN
    -- Delete FeedItems related to follows (if exists)
    DELETE FROM CoreData.FeedItems WHERE ItemID IN (SELECT FollowingID FROM deleted);
END;
GO
---Block
CREATE TRIGGER trg_DeleteBlockCascade
ON CoreData.Blocks
AFTER DELETE
AS
BEGIN
    -- Optionally, delete related FeedItems (this might depend on how you use blocks in feeds)
    DELETE FROM CoreData.FeedItems WHERE ItemID IN (SELECT BlockedUserID FROM deleted);
END;
GO
---Report
CREATE TRIGGER trg_DeleteReportCascade
ON CoreData.Reports
AFTER DELETE
AS
BEGIN
    -- Optionally, delete related FeedItems if reports are used in feeds (e.g., reporting a post)
    DELETE FROM CoreData.FeedItems WHERE ItemID IN (SELECT ReportedPostID FROM deleted WHERE ReportedPostID IS NOT NULL);
    DELETE FROM CoreData.FeedItems WHERE ItemID IN (SELECT ReportedCommentID FROM deleted WHERE ReportedCommentID IS NOT NULL);
    
    -- Optionally, delete Reactions related to the reported posts/comments
    DELETE FROM CoreData.Reactions WHERE InteractableItemID IN (SELECT InteractableItemID FROM CoreData.InteractableItems WHERE CreatedBy IN (SELECT ReportedPostID FROM deleted));
END;
GO
---Notification
CREATE TRIGGER trg_DeleteNotificationCascade
ON CoreData.Notifications
AFTER DELETE
AS
BEGIN
    -- Delete any associated FeedItems if necessary
    DELETE FROM CoreData.FeedItems WHERE ItemID IN (SELECT NotificationID FROM deleted);
END;
GO
---Conversation
CREATE TRIGGER Messaging.TRG_DeleteConversationChildren
ON Messaging.Conversations
AFTER DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Delete Conversation Members
    DELETE FROM Messaging.ConversationMembers
    WHERE ConversationID IN (SELECT ConversationID FROM deleted);

    -- Delete Messages
    DELETE FROM Messaging.Messages
    WHERE ConversationID IN (SELECT ConversationID FROM deleted);

    -- Delete Message Media
    DELETE FROM Messaging.MessageMedia
    WHERE MessageID IN (SELECT MessageID FROM Messaging.Messages WHERE ConversationID IN (SELECT ConversationID FROM deleted));

    -- Delete Pinned Messages
    DELETE FROM Messaging.PinnedMessages
    WHERE ConversationID IN (SELECT ConversationID FROM deleted);
END;
GO
---Member
CREATE TRIGGER Messaging.TRG_DeleteConversationMember
ON Messaging.ConversationMembers
AFTER DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Delete Pinned Messages for the deleted member's conversation
    DELETE FROM Messaging.PinnedMessages
    WHERE ConversationID IN (SELECT ConversationID FROM deleted);
    
    -- Optionally delete all messages by this member (if required)
    -- DELETE FROM Messaging.Messages
    -- WHERE SenderID IN (SELECT UserID FROM deleted) AND ConversationID IN (SELECT ConversationID FROM deleted);
END;
GO
---Messagee
CREATE TRIGGER Messaging.TRG_DeleteMessageChildren
ON Messaging.Messages
AFTER DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Delete related Media
    DELETE FROM Messaging.MessageMedia
    WHERE MessageID IN (SELECT MessageID FROM deleted);

    -- Delete Pinned Messages for the deleted messages
    DELETE FROM Messaging.PinnedMessages
    WHERE MessageID IN (SELECT MessageID FROM deleted);
END;
GO
---Media
CREATE TRIGGER Messaging.TRG_DeleteMessageMedia
ON Messaging.MessageMedia
AFTER DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Nothing else needs to be deleted for MessageMedia specifically, but if you wanted to track other dependencies (e.g., notifications related to media), you could do it here.
    -- Example: DELETE FROM Messaging.PinnedMessages WHERE MediaID IN (SELECT MediaID FROM deleted);
END;
GO
---Pinned 
CREATE TRIGGER Messaging.TRG_DeletePinnedMessages
ON Messaging.PinnedMessages
AFTER DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Nothing else to delete specifically for PinnedMessages.
    -- This trigger is just to ensure the message is removed from the pinned list when deleted.
END;
GO
