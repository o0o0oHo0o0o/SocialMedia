-- Lấy bảng + cột
SELECT t.name AS TableName, c.name AS ColumnName, ty.name AS DataType
FROM sys.columns c
JOIN sys.tables t ON c.object_id = t.object_id
JOIN sys.types ty ON c.user_type_id = ty.user_type_id
ORDER BY t.name, c.column_id;

-- Lấy quan hệ (foreign key)
SELECT fk.name AS FKName, tp.name AS ParentTable, tr.name AS ReferencedTable
FROM sys.foreign_keys fk
JOIN sys.tables tp ON fk.parent_object_id = tp.object_id
JOIN sys.tables tr ON fk.referenced_object_id = tr.object_id;

WITH Last5 AS (
    SELECT TOP 4 UserID
    FROM CoreData.Users
    ORDER BY UserID DESC
)
DELETE FROM CoreData.Users
WHERE UserID IN (SELECT UserID FROM Last5);


USE master;
GO

DECLARE @kill NVARCHAR(MAX) = N'';

-- Tìm toàn bộ session đang kết nối đến SocialMedia
SELECT @kill += N'KILL ' + CAST(session_id AS NVARCHAR(10)) + N';'
FROM sys.dm_exec_sessions
WHERE database_id = DB_ID('SocialMedia');

-- In ra danh sách session bị kill (để kiểm tra)
PRINT @kill;

-- Kill toàn bộ session
EXEC sp_executesql @kill;
GO

-- Giờ drop an toàn
DROP DATABASE IF EXISTS SocialMedia;
GO


-- ================================================
-- CODE XÓA TOÀN BỘ DỮ LIỆU
-- ================================================
USE SocialMedia;
GO

-- BẮT BUỘC: BẬT CÁC THIẾT LẬP CẦN THIẾT
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
GO

-- Tắt toàn bộ ràng buộc
EXEC sp_MSforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL';
GO

-- XÓA DỮ LIỆU THEO THỨ TỰ NGƯỢC (an toàn 100%, không dùng ?)
DECLARE @Tables TABLE (ID INT IDENTITY(1,1), TableName NVARCHAR(256));
INSERT INTO @Tables (TableName)
SELECT QUOTENAME(SCHEMA_NAME(schema_id)) + '.' + QUOTENAME(name)
FROM sys.tables
WHERE SCHEMA_NAME(schema_id) = 'CoreData'
ORDER BY 
    CASE 
        WHEN name = 'Users' THEN 999
        WHEN name = 'Posts' THEN 998
        WHEN name = 'Shares' THEN 997
        ELSE 1 
    END DESC;

DECLARE @i INT = 1, @max INT, @sql NVARCHAR(MAX), @tbl NVARCHAR(256);

SELECT @max = MAX(ID) FROM @Tables;

WHILE @i <= @max
BEGIN
    SELECT @tbl = TableName FROM @Tables WHERE ID = @i;
    
    SET @sql = 'DELETE FROM ' + @tbl + ';';
    
    PRINT 'Đang xóa: ' + @tbl;
    EXEC sp_executesql @sql;
    
    SET @i = @i + 1;
END
GO

-- Bật lại ràng buộc
EXEC sp_MSforeachtable 'ALTER TABLE ? WITH CHECK CHECK CONSTRAINT ALL';
GO

-- Reset Identity về 0
DECLARE @ResetSQL NVARCHAR(MAX) = '';
SELECT @ResetSQL += 
    'IF OBJECTPROPERTY(OBJECT_ID(''' + QUOTENAME(SCHEMA_NAME(schema_id)) + '.' + QUOTENAME(name) + '''), ''TableHasIdentity'') = 1 ' +
    'DBCC CHECKIDENT (''' + QUOTENAME(SCHEMA_NAME(schema_id)) + '.' + QUOTENAME(name) + ''', RESEED, 0); '
FROM sys.tables
WHERE SCHEMA_NAME(schema_id) = 'CoreData';

EXEC sp_executesql @ResetSQL;
GO

PRINT '=== XÓA HOÀN TẤT! TẤT CẢ DỮ LIỆU TRONG CoreData ĐÃ BỊ XÓA SẠCH ===';
PRINT 'Không còn lỗi QUOTED_IDENTIFIER. Identity đã được reset.';






























USE SocialMedia;
GO
SET NOCOUNT ON;

BEGIN TRAN;

-- Upsert me (dùng Email làm key)
DECLARE @Me INT = (SELECT UserID FROM CoreData.Users WHERE Email = 'dungdespam001@gmail.com');
IF @Me IS NULL
BEGIN
    INSERT INTO CoreData.Users (Username, Email, FullName, CreatedAt, IsVerified)
    VALUES ('dungdespam001', 'dungdespam001@gmail.com', N'Dung Despam', GETDATE(), 1);
    SET @Me = SCOPE_IDENTITY();
END
ELSE
BEGIN
    UPDATE CoreData.Users
    SET Username = 'dungdespam001',
        FullName = N'Dung Despam',
        IsVerified = 1
    WHERE UserID = @Me;
END

-- Upsert friend
DECLARE @Friend INT = (SELECT UserID FROM CoreData.Users WHERE Email = 'friend@example.com');
IF @Friend IS NULL
BEGIN
    INSERT INTO CoreData.Users (Username, Email, FullName, CreatedAt, IsVerified)
    VALUES ('frienddemo', 'friend@example.com', N'Bạn Demo', GETDATE(), 1);
    SET @Friend = SCOPE_IDENTITY();
END
ELSE
BEGIN
    UPDATE CoreData.Users
    SET Username = 'frienddemo',
        FullName = N'Bạn Demo',
        IsVerified = 1
    WHERE UserID = @Friend;
END

IF @Me IS NULL OR @Friend IS NULL
BEGIN
    RAISERROR(N'Không lấy được UserID cho me hoặc friend', 16, 1);
    ROLLBACK TRAN;
    RETURN;
END

-- Tạo/kiểm tra conversation 1-1
DECLARE @ConvId INT;
SELECT TOP 1 @ConvId = c.ConversationID
FROM Messaging.Conversations c
JOIN Messaging.ConversationMembers m1 ON m1.ConversationID = c.ConversationID AND m1.UserID = @Me
JOIN Messaging.ConversationMembers m2 ON m2.ConversationID = c.ConversationID AND m2.UserID = @Friend
WHERE c.IsGroupChat = 0;

IF @ConvId IS NULL
BEGIN
    INSERT INTO Messaging.Conversations (ConversationName, GroupImageURL, IsGroupChat, CreatedAt, CreatedByUserID)
    VALUES (NULL, NULL, 0, GETDATE(), @Me);
    SET @ConvId = SCOPE_IDENTITY();
END

-- Đảm bảo thành viên tồn tại
IF NOT EXISTS (SELECT 1 FROM Messaging.ConversationMembers WHERE ConversationID = @ConvId AND UserID = @Me)
BEGIN
    INSERT INTO Messaging.ConversationMembers (ConversationID, UserID, Role, JoinedAt)
    VALUES (@ConvId, @Me, 'MEMBER', GETDATE());
END
IF NOT EXISTS (SELECT 1 FROM Messaging.ConversationMembers WHERE ConversationID = @ConvId AND UserID = @Friend)
BEGIN
    INSERT INTO Messaging.ConversationMembers (ConversationID, UserID, Role, JoinedAt)
    VALUES (@ConvId, @Friend, 'MEMBER', GETDATE());
END

-- Chèn tin nhắn mẫu nếu chưa có
DECLARE @Msg1 BIGINT = NULL, @Msg2 BIGINT = NULL, @Msg3 BIGINT = NULL;

IF NOT EXISTS (
    SELECT 1 FROM Messaging.Messages ms
    JOIN Messaging.MessageBodies mb ON mb.MessageID = ms.MessageID
    WHERE ms.ConversationID = @ConvId AND ms.SenderID = @Friend
      AND mb.Content = N'Chào Dung! Dạo này thế nào?'
)
BEGIN
    INSERT INTO Messaging.Messages (ConversationID, SenderID, MessageType, SentAt)
    VALUES (@ConvId, @Friend, 'TEXT', DATEADD(MINUTE,-30,GETDATE()));
    SET @Msg1 = SCOPE_IDENTITY();

    INSERT INTO Messaging.MessageBodies (MessageID, Content)
    VALUES (@Msg1, N'Chào Dung! Dạo này thế nào?');

    INSERT INTO Messaging.MessageStatus (MessageID, UserID, Status, DeliveredAt, ReadAt)
    VALUES (@Msg1, @Me, 'DELIVERED', DATEADD(MINUTE,-29,GETDATE()), NULL);
    INSERT INTO Messaging.MessageStatus (MessageID, UserID, Status, DeliveredAt, ReadAt)
    VALUES (@Msg1, @Friend, 'READ', DATEADD(MINUTE,-28,GETDATE()), DATEADD(MINUTE,-28,GETDATE()));
END
ELSE
BEGIN
    SELECT TOP 1 @Msg1 = ms.MessageID
    FROM Messaging.Messages ms
    JOIN Messaging.MessageBodies mb ON mb.MessageID = ms.MessageID
    WHERE ms.ConversationID = @ConvId AND ms.SenderID = @Friend
      AND mb.Content = N'Chào Dung! Dạo này thế nào?'
    ORDER BY ms.SentAt DESC;
END

IF NOT EXISTS (
    SELECT 1 FROM Messaging.Messages ms
    JOIN Messaging.MessageBodies mb ON mb.MessageID = ms.MessageID
    WHERE ms.ConversationID = @ConvId AND ms.SenderID = @Me
      AND mb.Content = N'Mình ổn, cảm ơn! Bạn sao rồi?'
)
BEGIN
    INSERT INTO Messaging.Messages (ConversationID, SenderID, MessageType, SentAt)
    VALUES (@ConvId, @Me, 'TEXT', DATEADD(MINUTE,-25,GETDATE()));
    SET @Msg2 = SCOPE_IDENTITY();

    INSERT INTO Messaging.MessageBodies (MessageID, Content)
    VALUES (@Msg2, N'Mình ổn, cảm ơn! Bạn sao rồi?');

    INSERT INTO Messaging.MessageStatus (MessageID, UserID, Status, DeliveredAt, ReadAt)
    VALUES (@Msg2, @Me, 'READ', DATEADD(MINUTE,-24,GETDATE()), DATEADD(MINUTE,-24,GETDATE()));
    INSERT INTO Messaging.MessageStatus (MessageID, UserID, Status, DeliveredAt, ReadAt)
    VALUES (@Msg2, @Friend, 'DELIVERED', DATEADD(MINUTE,-23,GETDATE()), NULL);
END
ELSE
BEGIN
    SELECT TOP 1 @Msg2 = ms.MessageID
    FROM Messaging.Messages ms
    JOIN Messaging.MessageBodies mb ON mb.MessageID = ms.MessageID
    WHERE ms.ConversationID = @ConvId AND ms.SenderID = @Me
      AND mb.Content = N'Mình ổn, cảm ơn! Bạn sao rồi?'
    ORDER BY ms.SentAt DESC;
END

IF NOT EXISTS (
    SELECT 1 FROM Messaging.Messages ms
    JOIN Messaging.MessageBodies mb ON mb.MessageID = ms.MessageID
    WHERE ms.ConversationID = @ConvId AND ms.SenderID = @Friend
      AND mb.Content = N'Xem tấm ảnh này nhé!'
)
BEGIN
    INSERT INTO Messaging.Messages (ConversationID, SenderID, MessageType, SentAt)
    VALUES (@ConvId, @Friend, 'TEXT', DATEADD(MINUTE,-10,GETDATE()));
    SET @Msg3 = SCOPE_IDENTITY();

    INSERT INTO Messaging.MessageBodies (MessageID, Content)
    VALUES (@Msg3, N'Xem tấm ảnh này nhé!');

    IF NOT EXISTS (SELECT 1 FROM Messaging.MessageMedia WHERE MessageID = @Msg3)
    BEGIN
        INSERT INTO Messaging.MessageMedia (MessageID, MediaName, MediaType, FileName, FileSize, ThumbnailName)
        VALUES (@Msg3, N'https://example.com/demo.jpg', 'IMAGE', N'demo.jpg', 153421, NULL);
    END

    INSERT INTO Messaging.MessageStatus (MessageID, UserID, Status, DeliveredAt, ReadAt)
    VALUES (@Msg3, @Me, 'DELIVERED', DATEADD(MINUTE,-9,GETDATE()), NULL);
    INSERT INTO Messaging.MessageStatus (MessageID, UserID, Status, DeliveredAt, ReadAt)
    VALUES (@Msg3, @Friend, 'READ', DATEADD(MINUTE,-8,GETDATE()), DATEADD(MINUTE,-8,GETDATE()));
END
ELSE
BEGIN
    SELECT TOP 1 @Msg3 = ms.MessageID
    FROM Messaging.Messages ms
    JOIN Messaging.MessageBodies mb ON mb.MessageID = ms.MessageID
    WHERE ms.ConversationID = @ConvId AND ms.SenderID = @Friend
      AND mb.Content = N'Xem tấm ảnh này nhé!'
    ORDER BY ms.SentAt DESC;
END

-- Cập nhật LastMessageID
DECLARE @Last BIGINT = ISNULL(@Msg3,
    (SELECT TOP 1 MessageID FROM Messaging.Messages WHERE ConversationID = @ConvId ORDER BY SentAt DESC));
UPDATE Messaging.Conversations SET LastMessageID = @Last WHERE ConversationID = @ConvId;

-- LastRead: me đọc đến @Msg2, friend đọc đến @Msg3 (nếu có)
IF @Msg2 IS NOT NULL
    UPDATE Messaging.ConversationMembers SET LastReadMessageID = @Msg2 WHERE ConversationID = @ConvId AND UserID = @Me;
IF @Msg3 IS NOT NULL
    UPDATE Messaging.ConversationMembers SET LastReadMessageID = @Msg3 WHERE ConversationID = @ConvId AND UserID = @Friend;

COMMIT TRAN;