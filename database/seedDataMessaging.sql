USE SocialMedia;

INSERT INTO Messaging.Conversations (ConversationName, IsGroupChat, CreatedAt)
VALUES
    (N'Thảo luận nhóm về công nghệ', 1, GETDATE()),
    (N'Chia sẻ sách yêu thích', 1, GETDATE()),
    (N'Họp nhóm project AI', 1, GETDATE()),
    (N'Chơi game cùng bạn bè', 0, GETDATE()),
    (N'Bàn luận về thể thao', 0, GETDATE()),
    (N'Giới thiệu về các khóa học lập trình', 1, GETDATE()),
    (N'Chia sẻ mẹo vặt trong cuộc sống', 0, GETDATE()),
    (N'Học tiếng Anh cùng nhau', 1, GETDATE()),
    (N'Cập nhật tình hình dịch bệnh', 0, GETDATE()),
    (N'Giới thiệu các quán ăn ngon', 0, GETDATE()),
    (N'Chia sẻ kinh nghiệm du lịch', 1, GETDATE()),
    (N'Đọc sách và thảo luận', 1, GETDATE()),
    (N'Chia sẻ thông tin về công nghệ mới', 1, GETDATE()),
    (N'Tập yoga và sức khỏe', 1, GETDATE()),
    (N'Trò chuyện về phim ảnh', 0, GETDATE()),
    (N'Học kỹ năng mềm', 1, GETDATE()),
    (N'Mẹo vặt cuộc sống', 0, GETDATE()),
    (N'Kinh nghiệm làm việc hiệu quả', 1, GETDATE()),
    (N'Giới thiệu sản phẩm mới', 0, GETDATE()),
    (N'Chia sẻ thông tin tuyển dụng', 0, GETDATE());


INSERT INTO Messaging.ConversationMembers (ConversationID, UserID, JoinedAt)
VALUES
    (1, 1, GETDATE()),
    (1, 2, GETDATE()),
    (1, 3, GETDATE()),
    (1, 4, GETDATE()),
    (1, 5, GETDATE()),
    (2, 6, GETDATE()),
    (2, 7, GETDATE()),
    (2, 8, GETDATE()),
    (2, 9, GETDATE()),
    (2, 10, GETDATE()),
    (3, 11, GETDATE()),
    (3, 12, GETDATE()),
    (3, 13, GETDATE()),
    (3, 14, GETDATE()),
    (3, 15, GETDATE()),
    (4, 16, GETDATE()),
    (4, 17, GETDATE()),
    (4, 18, GETDATE()),
    (4, 19, GETDATE()),
    (4, 20, GETDATE()),
    (5, 1, GETDATE());

INSERT INTO Messaging.Messages (ConversationID, SenderID, Content, SentAt)
VALUES
    (1, 1, N'Chào mọi người, hôm nay chúng ta sẽ thảo luận về AI trong công nghệ!', GETDATE()),
    (1, 2, N'Chào các bạn, tôi vừa đọc một bài viết về AI rất thú vị, ai muốn nghe không?', GETDATE()),
    (1, 3, N'Rất mong được nghe chia sẻ của mọi người, mình cũng rất quan tâm đến AI.', GETDATE()),
    (1, 4, N'Mình vừa làm xong một khóa học AI, rất bổ ích!', GETDATE()),
    (1, 5, N'Mình thấy AI có thể thay đổi mọi lĩnh vực trong cuộc sống.', GETDATE()),
    (2, 6, N'Mọi người đọc sách gì hay gần đây không?', GETDATE()),
    (2, 7, N'Mình mới xong một cuốn sách về phát triển bản thân, rất hay!', GETDATE()),
    (2, 8, N'Mình vừa mới hoàn thành cuốn sách về tư duy phản biện, cực kỳ thú vị!', GETDATE()),
    (2, 9, N'Đề xuất một cuốn sách hay về tự học nhé!', GETDATE()),
    (2, 10, N'Có ai đọc cuốn "Sapiens" chưa? Cuốn sách này cực kỳ hay!', GETDATE()),
    (3, 11, N'Team, hôm nay chúng ta cần quyết định framework nào để sử dụng cho dự án.', GETDATE()),
    (3, 12, N'Mình đề xuất dùng React cho frontend, các bạn thấy sao?', GETDATE()),
    (3, 13, N'Mình nghĩ nên dùng Vue.js, dễ học và triển khai nhanh.', GETDATE()),
    (3, 14, N'Mọi người có kinh nghiệm với Angular không? Mình thấy nó cũng rất mạnh!', GETDATE()),
    (3, 15, N'Chúng ta cần chọn framework dễ mở rộng và cộng đồng lớn.', GETDATE()),
    (4, 16, N'Ai thích chơi game không, hôm nay mình sẽ livestream game đấy!', GETDATE()),
    (4, 17, N'Mình đang chơi trò Among Us, các bạn vào tham gia đi!', GETDATE()),
    (4, 18, N'Các bạn có game nào hay không? Mình đang tìm game mới chơi.', GETDATE()),
    (4, 19, N'Chơi game này rất vui, mình sẽ chơi vào tối nay.', GETDATE()),
    (4, 20, N'Các bạn có thích game sinh tồn không? Mình đang chơi PUBG.', GETDATE());



