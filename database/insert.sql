USE SocialMedia;
GO

BEGIN TRANSACTION;

BEGIN TRY
    -- 1. KHAI BÁO BIẾN
    DECLARE @User1ID INT, @User2ID INT;
    DECLARE @ConversationID INT;
    DECLARE @ItemID BIGINT; -- Dùng chung cho InteractableItems
    DECLARE @MsgID BIGINT;  -- Dùng chung cho Messages

    -- 2. LẤY ID CỦA 2 USER DỰA TRÊN EMAIL
    SELECT @User1ID = UserID FROM CoreData.Users WHERE Email = 'dungdespam001@gmail.com';
    SELECT @User2ID = UserID FROM CoreData.Users WHERE Email = 'dungdespam002@gmail.com';

    -- Kiểm tra nếu user không tồn tại
    IF @User1ID IS NULL OR @User2ID IS NULL
    BEGIN
        PRINT 'Lỗi: Không tìm thấy User với email đã cung cấp.';
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- 3. TẠO CUỘC HỘI THOẠI (Chat 1-1)
    INSERT INTO Messaging.Conversations (ConversationName, IsGroupChat, CreatedByUserID, CreatedAt)
    VALUES (NULL, 0, @User1ID, GETDATE());
    
    SET @ConversationID = SCOPE_IDENTITY();

    -- 4. THÊM THÀNH VIÊN VÀO HỘI THOẠI
    -- Thêm User 1
    INSERT INTO Messaging.ConversationMembers (ConversationID, UserID, Role, JoinedAt)
    VALUES (@ConversationID, @User1ID, 'ADMIN', GETDATE());

    -- Thêm User 2
    INSERT INTO Messaging.ConversationMembers (ConversationID, UserID, Role, JoinedAt)
    VALUES (@ConversationID, @User2ID, 'MEMBER', GETDATE());

    -- ==========================================================
    -- 5. BẮT ĐẦU CHAT
    -- ==========================================================

    -- >>> TIN NHẮN 1: User 1 nhắn "Chào bạn, lâu rồi không gặp!" <<<
    -- B1: Tạo Interactable Item
    INSERT INTO CoreData.InteractableItems (ItemType, CreatedAt) VALUES ('MESSAGE', GETDATE());
    SET @ItemID = SCOPE_IDENTITY();

    -- B2: Tạo Message
    INSERT INTO Messaging.Messages (ConversationID, SenderID, InteractableItemID, MessageType, SentAt)
    VALUES (@ConversationID, @User1ID, @ItemID, 'TEXT', GETDATE());
    SET @MsgID = SCOPE_IDENTITY();

    -- B3: Tạo Body
    INSERT INTO Messaging.MessageBodies (MessageID, Content)
    VALUES (@MsgID, N'Chào bạn, lâu rồi không gặp!');

    -- ==========================================================

    -- >>> TIN NHẮN 2: User 2 trả lời "Chào CaMap, mình vẫn khỏe. Bạn thế nào?" (Sau 5 giây) <<<
    -- B1: Tạo Interactable Item
    INSERT INTO CoreData.InteractableItems (ItemType, CreatedAt) VALUES ('MESSAGE', DATEADD(SECOND, 5, GETDATE()));
    SET @ItemID = SCOPE_IDENTITY();

    -- B2: Tạo Message
    INSERT INTO Messaging.Messages (ConversationID, SenderID, InteractableItemID, MessageType, SentAt)
    VALUES (@ConversationID, @User2ID, @ItemID, 'TEXT', DATEADD(SECOND, 5, GETDATE()));
    SET @MsgID = SCOPE_IDENTITY();

    -- B3: Tạo Body
    INSERT INTO Messaging.MessageBodies (MessageID, Content)
    VALUES (@MsgID, N'Chào CaMap, mình vẫn khỏe. Bạn thế nào?');

    -- ==========================================================

    -- >>> TIN NHẮN 3: User 1 nhắn "Mình đang test database chút thôi kkk" (Sau 10 giây) <<<
    -- B1: Tạo Interactable Item
    INSERT INTO CoreData.InteractableItems (ItemType, CreatedAt) VALUES ('MESSAGE', DATEADD(SECOND, 10, GETDATE()));
    SET @ItemID = SCOPE_IDENTITY();

    -- B2: Tạo Message
    INSERT INTO Messaging.Messages (ConversationID, SenderID, InteractableItemID, MessageType, SentAt)
    VALUES (@ConversationID, @User1ID, @ItemID, 'TEXT', DATEADD(SECOND, 10, GETDATE()));
    SET @MsgID = SCOPE_IDENTITY(); -- Đây là tin nhắn cuối cùng

    -- B3: Tạo Body
    INSERT INTO Messaging.MessageBodies (MessageID, Content)
    VALUES (@MsgID, N'Mình đang test database chút thôi kkk');

    -- ==========================================================
    -- 6. CẬP NHẬT TRẠNG THÁI CUỐI CÙNG
    -- ==========================================================

    -- Cập nhật LastMessageID cho Conversation
    UPDATE Messaging.Conversations
    SET LastMessageID = @MsgID
    WHERE ConversationID = @ConversationID;

    -- Cập nhật trạng thái đã xem (Giả sử cả 2 đều đã đọc tin nhắn cuối cùng)
    UPDATE Messaging.ConversationMembers
    SET LastReadMessageID = @MsgID
    WHERE ConversationID = @ConversationID;

    COMMIT TRANSACTION;
    PRINT 'Đã tạo hội thoại và chèn tin nhắn thành công!';
    
    -- Hiển thị kết quả kiểm tra
    SELECT * FROM Messaging.Conversations WHERE ConversationID = @ConversationID;
    SELECT m.MessageID, u.Username, mb.Content, m.SentAt 
    FROM Messaging.Messages m
    JOIN Messaging.MessageBodies mb ON m.MessageID = mb.MessageID
    JOIN CoreData.Users u ON m.SenderID = u.UserID
    WHERE m.ConversationID = @ConversationID
    ORDER BY m.SentAt ASC;

END TRY
BEGIN CATCH
    ROLLBACK TRANSACTION;
    PRINT 'Đã xảy ra lỗi: ' + ERROR_MESSAGE();
END CATCH;
GO




USE SocialMedia;
GO

-- 1. KHAI BÁO BIẾN ĐỂ LƯU ID VỪA TẠO
DECLARE @ConversationID INT;
DECLARE @InteractableID BIGINT;
DECLARE @MessageID BIGINT;
DECLARE @Now DATETIME = GETDATE();

-- ====================================================
-- BƯỚC 1: TẠO CUỘC TRÒ CHUYỆN (GROUP CHAT)
-- ====================================================
-- Sửa: Bỏ UpdatedAt, LastMessageSentAt. Thêm CreatedByUserID (Người tạo là User 1)
INSERT INTO Messaging.Conversations (ConversationName, IsGroupChat, CreatedAt, CreatedByUserID)
VALUES (N'Anh Em Dev Social Media', 1, @Now, 1);

-- Lấy ID của nhóm vừa tạo
SET @ConversationID = SCOPE_IDENTITY();
PRINT 'Created Conversation ID: ' + CAST(@ConversationID AS NVARCHAR(20));

-- ====================================================
-- BƯỚC 2: THÊM 3 THÀNH VIÊN VÀO NHÓM
-- ====================================================
-- User 1 (Admin nhóm)
INSERT INTO Messaging.ConversationMembers (ConversationID, UserID, Role, JoinedAt, Nickname)
VALUES (@ConversationID, 1, 'ADMIN', @Now, N'Cá Mập Lãnh Đạo');

-- User 2 (Member)
INSERT INTO Messaging.ConversationMembers (ConversationID, UserID, Role, JoinedAt, Nickname)
VALUES (@ConversationID, 2, 'MEMBER', @Now, N'Đệ Nhịp Nhàng');

-- User 1002 (Member)
INSERT INTO Messaging.ConversationMembers (ConversationID, UserID, Role, JoinedAt, Nickname)
VALUES (@ConversationID, 1002, 'MEMBER', @Now, N'Sao Sáng Tạo');

-- ====================================================
-- BƯỚC 3: TẠO TIN NHẮN 1 (User 1 gửi "Alo alo")
-- ====================================================
-- A. Tạo InteractableItem
INSERT INTO CoreData.InteractableItems (ItemType, CreatedAt) VALUES ('MESSAGE', @Now); -- Lưu ý: Schema là CoreData
SET @InteractableID = SCOPE_IDENTITY();

-- B. Tạo Message
INSERT INTO Messaging.Messages (ConversationID, SenderID, InteractableItemID, MessageType, SentAt, SequenceNumber, IsDeleted)
VALUES (@ConversationID, 1, @InteractableID, 'TEXT', @Now, 1, 0);
SET @MessageID = SCOPE_IDENTITY();

-- C. Tạo Body
INSERT INTO Messaging.MessageBodies (MessageID, Content) VALUES (@MessageID, N'Alo alo, chào mừng 2 anh em vào dự án!');

-- ====================================================
-- BƯỚC 4: TẠO TIN NHẮN 2 (User 2 trả lời)
-- ====================================================
-- A. Tạo InteractableItem
INSERT INTO CoreData.InteractableItems (ItemType, CreatedAt) VALUES ('MESSAGE', DATEADD(SECOND, 5, @Now));
SET @InteractableID = SCOPE_IDENTITY();

-- B. Tạo Message
INSERT INTO Messaging.Messages (ConversationID, SenderID, InteractableItemID, MessageType, SentAt, SequenceNumber, IsDeleted)
VALUES (@ConversationID, 2, @InteractableID, 'TEXT', DATEADD(SECOND, 5, @Now), 2, 0);
SET @MessageID = SCOPE_IDENTITY();

-- C. Tạo Body
INSERT INTO Messaging.MessageBodies (MessageID, Content) VALUES (@MessageID, N'Em nghe đây sếp ơi, code backend xong chưa?');

-- ====================================================
-- BƯỚC 5: TẠO TIN NHẮN 3 (User 1002 trả lời)
-- ====================================================
-- A. Tạo InteractableItem
INSERT INTO CoreData.InteractableItems (ItemType, CreatedAt) VALUES ('MESSAGE', DATEADD(SECOND, 10, @Now));
SET @InteractableID = SCOPE_IDENTITY();

-- B. Tạo Message
INSERT INTO Messaging.Messages (ConversationID, SenderID, InteractableItemID, MessageType, SentAt, SequenceNumber, IsDeleted)
VALUES (@ConversationID, 1002, @InteractableID, 'TEXT', DATEADD(SECOND, 10, @Now), 3, 0);
SET @MessageID = SCOPE_IDENTITY();

-- C. Tạo Body
INSERT INTO Messaging.MessageBodies (MessageID, Content) VALUES (@MessageID, N'Em mới join, xin chào mọi người nha ^^');

-- ====================================================
-- BƯỚC CUỐI: CẬP NHẬT METADATA CHO GROUP
-- ====================================================
-- Chỉ update LastMessageID
UPDATE Messaging.Conversations
SET LastMessageID = @MessageID
WHERE ConversationID = @ConversationID;

PRINT 'DONE! Group created successfully.';
GO

select * from CoreData.Users;
SELECT * FROM Messaging.Conversations;