USE SocialMedia
INSERT INTO CoreData.Users (Username, Email, PasswordHash, FullName, Bio, ProfilePictureURL)
VALUES
    ('nguyenan', 'nguyenan@example.com', 'hashedpassword123', 'Nguyễn An', 'Mình yêu thích chia sẻ về công nghệ và học hỏi những điều mới!', 'https://example.com/profile/nguyenan.jpg'),
    ('hoangbui', 'hoangbui@example.com', 'hashedpassword456', 'Hoàng Bùi', 'Mọi thứ đều có thể thay đổi, chỉ cần bạn dám thử!', 'https://example.com/profile/hoangbui.jpg'),
    ('lanhien', 'lanhien@example.com', 'hashedpassword789', 'Lân Hiền', 'Chuyên gia thiết kế đồ họa và nhiếp ảnh!', 'https://example.com/profile/lanhien.jpg'),
    ('minhhoang', 'minhhoang@example.com', 'hashedpassword001', 'Minh Hoàng', 'Mình là lập trình viên và đam mê công nghệ!', 'https://example.com/profile/minhhoang.jpg'),
    ('thienthai', 'thienthai@example.com', 'hashedpassword002', 'Thiện Thái', 'Đam mê âm nhạc và chơi guitar!', 'https://example.com/profile/thienthai.jpg'),
    ('quanghieu', 'quanghieu@example.com', 'hashedpassword003', 'Quang Hiếu', 'Chuyên gia phát triển web và ứng dụng di động!', 'https://example.com/profile/quanghieu.jpg'),
    ('thuhoai', 'thuhoai@example.com', 'hashedpassword004', 'Thư Hoài', 'Tôi yêu nghệ thuật và thích sáng tạo!', 'https://example.com/profile/thuhoai.jpg'),
    ('tuananh', 'tuananh@example.com', 'hashedpassword005', 'Tuấn Anh', 'Đam mê viết lách và chia sẻ kinh nghiệm sống!', 'https://example.com/profile/tuananh.jpg'),
    ('hienle', 'hienle@example.com', 'hashedpassword006', 'Hiền Lê', 'Mình thích đi du lịch và khám phá các nền văn hóa!', 'https://example.com/profile/hienle.jpg'),
    ('lananh', 'lananh@example.com', 'hashedpassword007', 'Làn Anh', 'Giới thiệu về các món ăn đặc sắc của Việt Nam!', 'https://example.com/profile/lananh.jpg'),
    ('datluong', 'datluong@example.com', 'hashedpassword008', 'Đạt Lương', 'Mình là người đam mê thể thao và du lịch!', 'https://example.com/profile/datluong.jpg'),
    ('xuonglinh', 'xuonglinh@example.com', 'hashedpassword009', 'Xương Linh', 'Chuyên gia về marketing và quảng cáo trực tuyến!', 'https://example.com/profile/xuonglinh.jpg'),
    ('quyenle', 'quyenle@example.com', 'hashedpassword010', 'Quyền Lê', 'Mình yêu thích công việc sáng tạo và nghệ thuật!', 'https://example.com/profile/quyenle.jpg'),
    ('huuthanh', 'huuthanh@example.com', 'hashedpassword011', 'Hữu Thành', 'Đam mê phát triển phần mềm và nghiên cứu công nghệ!', 'https://example.com/profile/huuthanh.jpg'),
    ('kimlinh', 'kimlinh@example.com', 'hashedpassword012', 'Kim Linh', 'Mình thích đọc sách và học hỏi các kiến thức mới!', 'https://example.com/profile/kimlinh.jpg'),
    ('vannhien', 'vannhien@example.com', 'hashedpassword013', 'Văn Nhiên', 'Tôi yêu thích chia sẻ về các chủ đề khoa học xã hội!', 'https://example.com/profile/vannhien.jpg'),
    ('vuthao', 'vuthao@example.com', 'hashedpassword014', 'Vũ Thảo', 'Mình là người đam mê tự học và phát triển bản thân!', 'https://example.com/profile/vuthao.jpg'),
    ('trongnhan', 'trongnhan@example.com', 'hashedpassword015', 'Trọng Nhân', 'Mình là sinh viên ngành kỹ thuật phần mềm!', 'https://example.com/profile/trongnhan.jpg'),
    ('thucan', 'thucan@example.com', 'hashedpassword016', 'Thục An', 'Mình yêu thích thử nghiệm các món ăn mới!', 'https://example.com/profile/thucan.jpg'),
    ('tuanminh', 'tuanminh@example.com', 'hashedpassword017', 'Tuấn Minh', 'Mình là người đam mê sáng tạo và phát triển phần mềm!', 'https://example.com/profile/tuanminh.jpg');

INSERT INTO CoreData.Roles (Rolename)
VALUES
    ('Admin'),
    ('User'),
    ('Moderator');
INSERT INTO CoreData.UserRole (RoleID, UserID)
VALUES
    (1, 1),  -- Admin cho Nguyễn An
    (2, 2),  -- User cho Hoàng Bùi
    (3, 3),  -- Moderator cho Lân Hiền
    (1, 4),  -- Admin cho Minh Hoàng
    (2, 5),  -- User cho Thiện Thái
    (3, 6),  -- Moderator cho Quang Hiếu
    (1, 7),  -- Admin cho Thư Hoài
    (2, 8),  -- User cho Tuấn Anh
    (3, 9),  -- Moderator cho Hiền Lê
    (1, 10), -- Admin cho Làn Anh
    (2, 11), -- User cho Đạt Lương
    (3, 12), -- Moderator cho Xương Linh
    (1, 13), -- Admin cho Quyền Lê
    (2, 14), -- User cho Hữu Thành
    (3, 15), -- Moderator cho Kim Linh
    (1, 16), -- Admin cho Văn Nhiên
    (2, 17), -- User cho Vũ Thảo
    (3, 18), -- Moderator cho Trọng Nhân
    (1, 19), -- Admin cho Thục An
    (2, 20); -- User cho Minh Tân
INSERT INTO CoreData.Posts (UserID, Content, PostType, Location)
VALUES
    (1, N'Hôm nay mình đã hoàn thành khóa học về Machine Learning! Rất thú vị!', 'TEXT', N'Hà Nội'),
    (2, N'Đây là bức ảnh mình chụp khi đi du lịch tại Sapa. Cảnh sắc quá đẹp!', 'PHOTO', 'Sapa'),
    (3, N'Mới xem một bộ phim kinh dị cực kỳ hấp dẫn! Bạn nào thích thể loại này không?', 'TEXT', N'TP. Hồ Chí Minh'),
    (4, N'Chào các bạn, đây là video chia sẻ về cách lập trình ứng dụng web với ReactJS.', 'VIDEO', N'Hà Nội'),
    (5, N'Mình vừa thử món phở gà ở một quán mới, rất ngon!', 'TEXT', N'Đà Nẵng'),
    (6, N'Mới quay xong một video hướng dẫn làm bánh mì. Mọi người vào xem nhé!', 'VIDEO', N'Hải Phòng'),
    (7, N'Tối nay mình sẽ livestream chơi game. Ai muốn tham gia thì nhớ vào nhé!', 'TEXT', N'TP. Hồ Chí Minh'),
    (8, N'Mình vừa học được một kỹ thuật vẽ mới, mọi người xem thử nhé!', 'PHOTO', N'Hà Nội'),
    (9, N'Một buổi sáng đầy năng lượng! Hy vọng ngày mới sẽ thật tuyệt vời.', 'TEXT', N'Quảng Ninh'),
    (10, N'Video hướng dẫn về cách sửa máy tính, các bạn có thể áp dụng để tự sửa tại nhà!', 'VIDEO',
     N'TP. Hồ Chí Minh'),
    (11, N'Mới mua được chiếc laptop mới, rất hài lòng với chất lượng và hiệu suất!', 'TEXT', N'Hà Nội'),
    (12, N'Chuyến đi Đà Lạt tuyệt vời, cảnh đẹp không thể tin được!', 'PHOTO', N'Đà Lạt'),
    (13, N'Tối nay sẽ có buổi chia sẻ về marketing trên Facebook, ai quan tâm thì đừng bỏ lỡ nhé!', 'TEXT',
     N'TP. Hồ Chí Minh'),
    (14, N'Video hướng dẫn cắt tóc tại nhà. Mọi người thử làm theo nhé!', 'VIDEO', N'Hà Nội'),
    (15, N'Tập yoga vào mỗi sáng để tăng cường sức khỏe, bạn đã thử chưa?', 'TEXT', N'Hải Phòng'),
    (16, N'Những khoảnh khắc đáng nhớ khi đi thăm quan cố đô Huế.', 'PHOTO', N'Huế'),
    (17, N'Đọc sách để trau dồi kiến thức mỗi ngày. Bạn đang đọc sách gì?', 'TEXT', N'TP. Hồ Chí Minh'),
    (18, N'Mới làm xong một video hướng dẫn về Photoshop, hi vọng mọi người sẽ thích!', 'VIDEO', N'Hà Nội'),
    (19, N'Mới khám phá được một quán cà phê cực kỳ dễ thương, rất đáng để thử!', 'TEXT', N'Đà Nẵng'),
    (20, N'Điều quan trọng trong cuộc sống là luôn giữ thái độ tích cực, hãy luôn mỉm cười!', 'TEXT',
     N'TP. Hồ Chí Minh');
INSERT INTO CoreData.Posts (UserID, Content, PostType, Location)
VALUES
    (1, N'Hôm nay mình đã hoàn thành khóa học về Machine Learning! Rất thú vị!', 'Education', N'Hà Nội'),
    (2, N'Đây là bức ảnh mình chụp khi đi du lịch tại Sapa. Cảnh sắc quá đẹp!', 'Travel', 'Sapa'),
    (3, N'Mới xem một bộ phim kinh dị cực kỳ hấp dẫn! Bạn nào thích thể loại này không?', 'Humor', N'TP. Hồ Chí Minh'),
    (4, N'Chào các bạn, đây là video chia sẻ về cách lập trình ứng dụng web với ReactJS.', 'Education', N'Hà Nội'),
    (5, N'Mình vừa thử món phở gà ở một quán mới, rất ngon!', 'Food', N'Đà Nẵng'),
    (6, N'Mới quay xong một video hướng dẫn làm bánh mì. Mọi người vào xem nhé!', 'Food', N'Hải Phòng'),
    (7, N'Tối nay mình sẽ livestream chơi game. Ai muốn tham gia thì nhớ vào nhé!', 'Humor', N'TP. Hồ Chí Minh'),
    (8, N'Mình vừa học được một kỹ thuật vẽ mới, mọi người xem thử nhé!', 'Art', N'Hà Nội'),
    (9, N'Một buổi sáng đầy năng lượng! Hy vọng ngày mới sẽ thật tuyệt vời.', 'Positive', N'Quảng Ninh'),
    (10, N'Video hướng dẫn về cách sửa máy tính, các bạn có thể áp dụng để tự sửa tại nhà!', 'Education',
     N'TP. Hồ Chí Minh'),
    (11, N'Mới mua được chiếc laptop mới, rất hài lòng với chất lượng và hiệu suất!', 'Technology', N'Hà Nội'),
    (12, N'Chuyến đi Đà Lạt tuyệt vời, cảnh đẹp không thể tin được!', 'Travel', N'Đà Lạt'),
    (13, N'Tối nay sẽ có buổi chia sẻ về marketing trên Facebook, ai quan tâm thì đừng bỏ lỡ nhé!', 'Education',
     N'TP. Hồ Chí Minh'),
    (14, N'Video hướng dẫn cắt tóc tại nhà. Mọi người thử làm theo nhé!', 'DIY', N'Hà Nội'),
    (15, N'Tập yoga vào mỗi sáng để tăng cường sức khỏe, bạn đã thử chưa?', 'Health', N'Hải Phòng'),
    (16, N'Những khoảnh khắc đáng nhớ khi đi thăm quan cố đô Huế.', 'Travel', N'Huế'),
    (17, N'Đọc sách để trau dồi kiến thức mỗi ngày. Bạn đang đọc sách gì?', 'Education', N'TP. Hồ Chí Minh'),
    (18, N'Mới làm xong một video hướng dẫn về Photoshop, hi vọng mọi người sẽ thích!', 'Education', N'Hà Nội'),
    (19, N'Mới khám phá được một quán cà phê cực kỳ dễ thương, rất đáng để thử!', 'Food', N'Đà Nẵng'),
    (20, N'Điều quan trọng trong cuộc sống là luôn giữ thái độ tích cực, hãy luôn mỉm cười!', 'Positive',
     N'TP. Hồ Chí Minh');

INSERT INTO CoreData.PostMedia (PostID, MediaURL, MediaType, SortOrder)
VALUES
    (1, 'https://example.com/media/photo1.jpg', 'PHOTO', 0),
    (1, 'https://example.com/media/photo2.jpg', 'PHOTO', 1),
    (2, 'https://example.com/media/photo3.jpg', 'PHOTO', 0),
    (3, 'https://example.com/media/video1.mp4', 'VIDEO', 0),
    (4, 'https://example.com/media/video2.mp4', 'VIDEO', 0),
    (5, 'https://example.com/media/photo4.jpg', 'PHOTO', 0),
    (6, 'https://example.com/media/photo5.jpg', 'PHOTO', 0),
    (7, 'https://example.com/media/video3.mp4', 'VIDEO', 0),
    (8, 'https://example.com/media/photo6.jpg', 'PHOTO', 0),
    (9, 'https://example.com/media/photo7.jpg', 'PHOTO', 0),
    (10, 'https://example.com/media/video4.mp4', 'VIDEO', 0),
    (11, 'https://example.com/media/photo8.jpg', 'PHOTO', 0),
    (12, 'https://example.com/media/photo9.jpg', 'PHOTO', 0),
    (13, 'https://example.com/media/video5.mp4', 'VIDEO', 0),
    (14, 'https://example.com/media/photo10.jpg', 'PHOTO', 0),
    (15, 'https://example.com/media/photo11.jpg', 'PHOTO', 0),
    (16, 'https://example.com/media/photo12.jpg', 'PHOTO', 0),
    (17, 'https://example.com/media/video6.mp4', 'VIDEO', 0),
    (18, 'https://example.com/media/photo13.jpg', 'PHOTO', 0),
    (19, 'https://example.com/media/photo14.jpg', 'PHOTO', 0),
    (20, 'https://example.com/media/video7.mp4', 'VIDEO', 0);
INSERT INTO CoreData.Comments (PostID, UserID, ParentCommentID, Content)
VALUES
    (1, 2, NULL, N'Chúc mừng bạn đã hoàn thành khóa học!'),
    (1, 3, NULL, N'Wow, nghe hấp dẫn quá! Có thể chia sẻ tài liệu được không?'),
    (2, 4, NULL, N'Cảnh đẹp quá, mình cũng muốn đi Sapa!'),
    (3, 5, NULL, N'Phim gì vậy bạn? Mình đang tìm phim mới để xem!'),
    (4, 6, NULL, N'Video rất hay, mình đã thử theo cách của bạn và thành công!'),
    (5, 7, NULL, N'Phở gà nghe hấp dẫn thật, mình sẽ ghé thử!'),
    (6, 8, NULL, N'Mình cũng yêu thích làm bánh mì, chờ video hướng dẫn của bạn!'),
    (7, 9, NULL, N'Mình sẽ tham gia livestream, bạn chơi game gì vậy?'),
    (8, 10, NULL, N'Mình cũng đang học vẽ, video của bạn rất hữu ích!'),
    (9, 11, NULL, N'Ngày mới tốt đẹp nhé bạn, luôn đầy năng lượng!'),
    (10, 12, NULL, N'Video về sửa máy tính rất hay, cảm ơn bạn chia sẻ!'),
    (11, 13, NULL, N'Chúc mừng bạn đã mua được laptop mới, nhìn xịn quá!'),
    (12, 14, NULL, N'Đà Lạt đẹp quá, mình cũng muốn đi vào mùa này!'),
    (13, 15, NULL, N'Chắc chắn sẽ tham gia buổi chia sẻ của bạn!'),
    (14, 16, NULL, N'Mình sẽ thử cắt tóc theo hướng dẫn của bạn!'),
    (15, 17, NULL, N'Tập yoga giúp sức khỏe rất tốt, mình cũng đang duy trì mỗi sáng!'),
    (16, 18, NULL, N'Huế thật tuyệt vời, bạn có gợi ý gì cho chuyến đi không?'),
    (17, 19, NULL, N'Mình cũng đang đọc một cuốn sách về phát triển bản thân!'),
    (18, 20, NULL, N'Video Photoshop của bạn rất chi tiết, cảm ơn vì đã chia sẻ!'),
    (19, 1, NULL, N'Quán cà phê đó mình cũng mới thử, quả thật rất đáng giá!'),
    (20, 2, NULL, N'Đúng vậy, thái độ tích cực là chìa khóa để sống vui vẻ!');
INSERT INTO CoreData.Reactions (PostID, UserID, ReactionType)
VALUES
    (1, 2, 'LIKE'),
    (1, 3, 'LOVE'),
    (2, 4, 'WOW'),
    (3, 5, 'LIKE'),
    (4, 6, 'WOW'),
    (5, 7, 'LOVE'),
    (6, 8, 'LIKE'),
    (7, 9, 'LIKE'),
    (8, 10, 'LOVE'),
    (9, 11, 'LIKE'),
    (10, 12, 'WOW'),
    (11, 13, 'LOVE'),
    (12, 14, 'WOW'),
    (13, 15, 'LIKE'),
    (14, 16, 'LOVE'),
    (15, 17, 'WOW'),
    (16, 18, 'LIKE'),
    (17, 19, 'LOVE'),
    (18, 20, 'WOW'),
    (19, 1, 'LIKE'),
    (20, 2, 'LOVE');
INSERT INTO CoreData.Shares (PostID, UserID, SharedAt)
VALUES
    (1, 2, GETDATE()),
    (2, 3, GETDATE()),
    (3, 4, GETDATE()),
    (4, 5, GETDATE()),
    (5, 6, GETDATE()),
    (6, 7, GETDATE()),
    (7, 8, GETDATE()),
    (8, 9, GETDATE()),
    (9, 10, GETDATE()),
    (10, 11, GETDATE()),
    (11, 12, GETDATE()),
    (12, 13, GETDATE()),
    (13, 14, GETDATE()),
    (14, 15, GETDATE()),
    (15, 16, GETDATE()),
    (16, 17, GETDATE()),
    (17, 18, GETDATE()),
    (18, 19, GETDATE()),
    (19, 20, GETDATE()),
    (20, 1, GETDATE());
INSERT INTO CoreData.PostTags (PostID, TaggedUserID, TaggedAt)
VALUES
    (1, 2, GETDATE()),
    (1, 3, GETDATE()),
    (2, 4, GETDATE()),
    (3, 5, GETDATE()),
    (4, 6, GETDATE()),
    (5, 7, GETDATE()),
    (6, 8, GETDATE()),
    (7, 9, GETDATE()),
    (8, 10, GETDATE()),
    (9, 11, GETDATE()),
    (10, 12, GETDATE()),
    (11, 13, GETDATE()),
    (12, 14, GETDATE()),
    (13, 15, GETDATE()),
    (14, 16, GETDATE()),
    (15, 17, GETDATE()),
    (16, 18, GETDATE()),
    (17, 19, GETDATE()),
    (18, 20, GETDATE()),
    (19, 1, GETDATE()),
    (20, 2, GETDATE());
INSERT INTO CoreData.Follows (FollowerID, FollowingID, FollowedAt)
VALUES
    (1, 2, GETDATE()),
    (1, 3, GETDATE()),
    (2, 4, GETDATE()),
    (3, 5, GETDATE()),
    (4, 6, GETDATE()),
    (5, 7, GETDATE()),
    (6, 8, GETDATE()),
    (7, 9, GETDATE()),
    (8, 10, GETDATE()),
    (9, 11, GETDATE()),
    (10, 12, GETDATE()),
    (11, 13, GETDATE()),
    (12, 14, GETDATE()),
    (13, 15, GETDATE()),
    (14, 16, GETDATE()),
    (15, 17, GETDATE()),
    (16, 18, GETDATE()),
    (17, 19, GETDATE()),
    (18, 20, GETDATE()),
    (19, 1, GETDATE()),
    (20, 2, GETDATE());
INSERT INTO CoreData.Blocks (BlockerID, BlockedUserID, BlockedAt)
VALUES
    (1, 2, GETDATE()),
    (2, 3, GETDATE()),
    (3, 4, GETDATE()),
    (4, 5, GETDATE()),
    (5, 6, GETDATE()),
    (6, 7, GETDATE()),
    (7, 8, GETDATE()),
    (8, 9, GETDATE()),
    (9, 10, GETDATE()),
    (10, 11, GETDATE()),
    (11, 12, GETDATE()),
    (12, 13, GETDATE()),
    (13, 14, GETDATE()),
    (14, 15, GETDATE()),
    (15, 16, GETDATE()),
    (16, 17, GETDATE()),
    (17, 18, GETDATE()),
    (18, 19, GETDATE()),
    (19, 20, GETDATE()),
    (20, 1, GETDATE());
INSERT INTO CoreData.Reports (ReporterID, ReportedPostID, ReportedCommentID, ReportedUserID, Reason, ReportStatus, ReportedAt)
VALUES
    (1, 2, NULL, NULL, N'Bài viết vi phạm chính sách', 'PENDING', GETDATE()),
    (2, 3, NULL, NULL, N'Bài viết có nội dung khiêu dâm', 'PENDING', GETDATE()),
    (3, 4, NULL, NULL, N'Lý do không rõ', 'PENDING', GETDATE()),
    (4, NULL, 5, NULL, N'Bình luận chứa ngôn từ thô tục', 'PENDING', GETDATE()),
    (5, 6, NULL, NULL, N'Spam, quảng cáo', 'PENDING', GETDATE()),
    (6, 7, NULL, NULL, N'Bài viết sai sự thật', 'PENDING', GETDATE()),
    (7, NULL, 8, NULL, N'Bình luận xúc phạm cá nhân', 'PENDING', GETDATE()),
    (8, 9, NULL, NULL, N'Bài viết mang tính chính trị', 'PENDING', GETDATE()),
    (9, NULL, 10, NULL, N'Bình luận gây hấn', 'PENDING', GETDATE()),
    (10, 11, NULL, NULL, N'Lý do không rõ', 'PENDING', GETDATE()),
    (11, 12, NULL, NULL, N'Bài viết gây hiểu lầm', 'PENDING', GETDATE()),
    (12, 13, NULL, NULL, N'Bài viết kích động bạo lực', 'PENDING', GETDATE()),
    (13, NULL, 14, NULL, N'Bình luận vi phạm quy định cộng đồng', 'PENDING', GETDATE()),
    (14, 15, NULL, NULL, N'Bài viết có hình ảnh không phù hợp', 'PENDING', GETDATE()),
    (15, 16, NULL, NULL, N'Bài viết mang nội dung xuyên tạc', 'PENDING', GETDATE()),
    (16, NULL, 17, NULL, N'Bình luận vi phạm quyền riêng tư', 'PENDING', GETDATE()),
    (17, 18, NULL, NULL, N'Bài viết xúc phạm tổ chức', 'PENDING', GETDATE()),
    (18, NULL, 19, NULL, N'Bình luận mang tính phân biệt chủng tộc', 'PENDING', GETDATE()),
    (19, 20, NULL, NULL, N'Bài viết có thông tin sai lệch', 'PENDING', GETDATE()),
    (20, 1, NULL, NULL, N'Bài viết khích lệ hành vi phạm pháp', 'PENDING', GETDATE());
