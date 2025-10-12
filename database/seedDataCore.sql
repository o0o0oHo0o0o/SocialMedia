USE SocialMedia
-- 15 active users
INSERT INTO CoreData.Users (Username, Email, PasswordHash, FullName, Bio, ProfilePictureURL, CreatedAt, IsDeleted, DeletedAt)
VALUES
    ('nguyenan', 'nguyenan@example.com', 'passwordhash1', 'Nguyễn An', 'Lập trình viên yêu thích công nghệ.', 'https://profilepic1.com', '2020-01-15 08:30:00', 0, NULL),
    ('hoangbui', 'hoangbui@example.com', 'passwordhash2', 'Hoàng Bùi', 'Đam mê ẩm thực và nhiếp ảnh.', 'https://profilepic2.com', '2020-03-22 14:45:20', 0, NULL),
    ('lanhien', 'lanhien@example.com', 'passwordhash3', 'Lân Hiền', 'Du lịch và âm nhạc là đam mê của tôi.', 'https://profilepic3.com', '2020-05-10 16:20:50', 0, NULL),
    ('minhhoang', 'minhhoang@example.com', 'passwordhash4', 'Minh Hoàng', 'Nghiên cứu viên và giảng viên thể dục.', 'https://profilepic4.com', '2021-06-05 11:00:00', 0, NULL),
    ('thienthai', 'thienthai@example.com', 'passwordhash5', 'Thiện Thái', 'Yêu thích thiên nhiên và du lịch.', 'https://profilepic5.com', '2021-08-18 13:10:10', 0, NULL),
    ('quanghieu', 'quanghieu@example.com', 'passwordhash6', 'Quang Hiếu', 'Thiết kế đồ họa và sáng tạo nội dung.', 'https://profilepic6.com', '2021-10-25 09:30:05', 0, NULL),
    ('thuhoi', 'thuhoi@example.com', 'passwordhash7', 'Thư Hoài', 'Chuyên gia marketing và cố vấn khởi nghiệp.', 'https://profilepic7.com', '2022-01-12 14:05:15', 0, NULL),
    ('tuananh', 'tuananh@example.com', 'passwordhash8', 'Tuấn Anh', 'Sách và mèo là hai điều tôi yêu thích.', 'https://profilepic8.com', '2022-03-17 17:40:30', 0, NULL),
    ('hienle', 'hienle@example.com', 'passwordhash9', 'Hiền Lê', 'Gamer và yêu thích công nghệ.', 'https://profilepic9.com', '2022-05-21 11:55:00', 0, NULL),
    ('lananh', 'lananh@example.com', 'passwordhash10', 'Làn Anh', 'Nhà văn và người kể chuyện chuyên nghiệp.', 'https://profilepic10.com', '2022-07-25 08:25:50', 0, NULL),
    ('datluong', 'datluong@example.com', 'passwordhash11', 'Đạt Lương', 'Đầu bếp và người phê bình ẩm thực.', 'https://profilepic11.com', '2022-09-12 13:00:00', 0, NULL),
    ('xuanlinh', 'xuanlinh@example.com', 'passwordhash12', 'Xương Linh', 'Nhiếp ảnh gia và nhà thám hiểm.', 'https://profilepic12.com', '2023-01-02 15:30:45', 0, NULL),
    ('quyenle', 'quyenle@example.com', 'passwordhash13', 'Quyền Lê', 'Kỹ sư phần mềm và doanh nhân.', 'https://profilepic13.com', '2023-03-14 18:15:10', 0, NULL),
    ('huuthanh', 'huuthanh@example.com', 'passwordhash14', 'Hữu Thành', 'Họa sĩ và nhạc sĩ.', 'https://profilepic14.com', '2023-05-25 12:40:20', 0, NULL),
    ('kimlinh', 'kimlinh@example.com', 'passwordhash15', 'Kim Linh', 'Nhà văn du lịch và tác giả sách.', 'https://profilepic15.com', '2023-07-30 09:25:00', 0, NULL);


-- 5 deleted users
INSERT INTO CoreData.Users (Username, Email, PasswordHash, FullName, Bio, ProfilePictureURL, CreatedAt, IsDeleted, DeletedAt)
VALUES
    ('khanhhoa', 'khanhhoa@example.com', 'passwordhash16', 'Khánh Hòa', 'Chuyên gia SEO và truyền thông.', 'https://profilepic16.com', '2021-02-20 10:30:00', 1, '2023-08-15 11:30:00'),
    ('thiennguyen', 'thiennguyen@example.com', 'passwordhash17', 'Thiên Nguyễn', 'Đầu bếp và người yêu thích khám phá văn hóa.', 'https://profilepic17.com', '2021-05-25 14:20:00', 1, '2023-08-20 16:45:00'),
    ('phuongtam', 'phuongtam@example.com', 'passwordhash18', 'Phương Tam', 'Nhiếp ảnh gia chuyên nghiệp, yêu thích nghệ thuật.', 'https://profilepic18.com', '2021-07-10 13:15:00', 1, '2023-08-22 09:00:00'),
    ('tuananhx', 'tuananhx@example.com', 'passwordhash19', 'Tuấn Anh X', 'Kỹ sư phần mềm và người đam mê công nghệ.', 'https://profilepic19.com', '2022-01-18 15:40:00', 1, '2023-08-25 18:25:00'),
    ('minhcuong', 'minhcuong@example.com', 'passwordhash20', 'Minh Cường', 'Blogger và reviewer sách.', 'https://profilepic20.com', '2022-03-12 10:50:00', 1, '2023-09-10 17:00:00');


INSERT INTO CoreData.Roles (Rolename)
VALUES
    ('Admin'),
    ('User'),
    ('Moderator');
-- Dữ liệu mẫu cho bảng UserRole
INSERT INTO CoreData.UserRole (RoleID, UserID, AssignedAt)
VALUES
    (1, 1, '2020-01-15 08:30:00'),  -- Admin cho Nguyễn An
    (2, 2, '2020-03-22 14:45:20'),  -- User cho Hoàng Bùi
    (3, 3, '2020-05-10 16:20:50'),  -- Moderator cho Lân Hiền
    (1, 4, '2021-06-05 11:00:00'),  -- Admin cho Minh Hoàng
    (2, 5, '2021-08-18 13:10:10'),  -- User cho Thiện Thái
    (3, 6, '2021-10-25 09:30:05'),  -- Moderator cho Quang Hiếu
    (1, 7, '2022-01-12 14:05:15'),  -- Admin cho Thư Hoài
    (2, 8, '2022-03-17 17:40:30'),  -- User cho Tuấn Anh
    (3, 9, '2022-05-21 11:55:00'),  -- Moderator cho Hiền Lê
    (1, 10, '2022-07-25 08:25:50'), -- Admin cho Làn Anh
    (2, 11, '2022-09-12 13:00:00'), -- User cho Đạt Lương
    (3, 12, '2023-01-02 15:30:45'), -- Moderator cho Xương Linh
    (1, 13, '2023-03-14 18:15:10'), -- Admin cho Quyền Lê
    (2, 14, '2023-05-25 12:40:20'), -- User cho Hữu Thành
    (3, 15, '2023-07-30 09:25:00'), -- Moderator cho Kim Linh
    -- 5 người dùng đã bị xóa
    (1, 16, '2021-02-20 10:30:00'),  -- Admin cho Khánh Hòa
    (2, 17, '2021-05-25 14:20:00'),  -- User cho Thiên Nguyễn
    (3, 18, '2021-07-10 13:15:00'),  -- Moderator cho Phương Tam
    (1, 19, '2022-01-18 15:40:00'),  -- Admin cho Tuấn Anh X
    (2, 20, '2022-03-12 10:50:00');  -- User cho Minh Cường

-- Dữ liệu mẫu cho bảng Posts với trạng thái đa dạng và UpdatedAt
INSERT INTO CoreData.InteractableItems (ItemType, CreatedAt)
VALUES
    -- Mục liên quan đến bài viết
    ('POST', '2021-02-01 08:30:00'),
    ('POST', '2021-03-05 12:45:00'),
    ('POST', '2021-04-10 14:20:00'),
    ('POST', '2021-05-15 16:00:00'),
    ('POST', '2021-06-20 09:10:00'),
    ('POST', '2021-07-25 11:30:00'),
    ('POST', '2021-08-30 18:15:00'),
    ('POST', '2021-09-10 13:45:00'),
    ('POST', '2021-10-18 17:05:00'),
    ('POST', '2021-11-25 15:25:00'),
    ('POST', '2021-12-05 10:40:00'),
    ('POST', '2022-01-10 09:55:00'),
    ('POST', '2022-02-20 14:00:00'),
    ('POST', '2022-03-05 16:30:00'),
    ('POST', '2022-04-12 12:20:00'),
    ('POST', '2022-05-15 08:40:00'),
    ('POST', '2022-06-25 19:00:00'),
    ('POST', '2022-07-18 11:10:00'),
    ('POST', '2022-08-22 10:00:00'),
    ('POST', '2022-09-30 14:50:00');
INSERT INTO CoreData.Posts (UserID, InteractableItemID, Content, PostTopic, Location, IsArchived, CreatedAt, UpdatedAt, IsDeleted, DeletedAt)
VALUES
    -- Bài viết hoạt động
    (1, 1, 'Chào các bạn, mình là Nguyễn An!', 'Giới thiệu', 'Hà Nội', 0, '2021-02-01 08:30:00', '2021-02-02 09:00:00', 0, NULL),
    (2, 2, 'Mình đang có một chuyến du lịch ở Sapa.', 'Du lịch', 'Sapa', 0, '2021-03-05 12:45:00', '2021-03-06 14:30:00', 0, NULL),
    (3, 3, 'Bài viết về cuộc sống của mình!', 'Cuộc sống', 'Hồ Chí Minh', 0, '2021-04-10 14:20:00', '2021-04-11 15:00:00', 0, NULL),

    -- Bài viết đã lưu trữ (IsArchived = 1)
    (4, 4, 'Những suy nghĩ về công việc và cuộc sống.', 'Suy ngẫm', 'Đà Nẵng', 1, '2021-05-15 16:00:00', '2021-05-16 17:20:00', 0, NULL),
    (5, 5, 'Mới làm một món ăn ngon tuyệt!', 'Ẩm thực', 'Hà Nội', 1, '2021-06-20 09:10:00', '2021-06-21 10:15:00', 0, NULL),
    (6, 6, 'Sự kiện thú vị trong ngành công nghệ!', 'Công nghệ', 'Cần Thơ', 1, '2021-07-25 11:30:00', '2021-07-26 12:45:00', 0, NULL),

    -- Bài viết đã xóa (IsDeleted = 1, DeletedAt không NULL)
    (7, 7, 'Mới nhận được một giải thưởng từ cuộc thi!', 'Thành tựu', 'Hà Nội', 0, '2021-08-30 18:15:00', '2021-08-31 19:00:00', 1, '2021-09-01 09:00:00'),
    (8, 8, 'Cảm nhận về một cuốn sách mới đọc.', 'Sách', 'Hồ Chí Minh', 0, '2021-09-10 13:45:00', '2021-09-11 14:10:00', 1, '2021-09-12 10:00:00'),

    -- Bài viết đã xóa (IsDeleted = 1, DeletedAt không NULL)
    (9, 9, 'Mình yêu thích việc học các ngôn ngữ mới!', 'Học tập', 'Hà Nội', 0, '2021-10-18 17:05:00', '2021-10-19 18:30:00', 1, '2021-10-20 14:30:00'),
    (10, 10, 'Chia sẻ một số kinh nghiệm du lịch!', 'Du lịch', 'Sapa', 0, '2021-11-25 15:25:00', '2021-11-26 16:00:00', 1, '2021-11-28 13:00:00'),

    -- Bài viết đã xóa và hết hạn (DeletedAt và IsDeleted = 1)
    (11, 11, 'Thử nghiệm một công thức mới từ nhà bếp.', 'Ẩm thực', 'Hồ Chí Minh', 0, '2021-12-05 10:40:00', '2021-12-06 11:15:00', 1, '2021-12-10 16:00:00'),

    -- Bài viết bình thường (IsArchived = 0, IsDeleted = 0)
    (12, 12, 'Làm thế nào để nâng cao kỹ năng lập trình?', 'Lập trình', 'Đà Nẵng', 0, '2022-01-10 09:55:00', '2022-01-11 10:30:00', 0, NULL),
    (13, 13, 'Chia sẻ về những bài học từ thất bại.', 'Học hỏi', 'Cần Thơ', 0, '2022-02-20 14:00:00', '2022-02-21 15:30:00', 0, NULL),

    -- Bài viết đã xóa (IsDeleted = 1, DeletedAt không NULL)
    (14, 14, 'Tham gia một dự án phát triển cộng đồng.', 'Cộng đồng', 'Hà Nội', 0, '2022-03-05 16:30:00', '2022-03-06 17:10:00', 1, '2022-03-07 12:00:00'),
    (15, 15, 'Hướng dẫn cách tạo blog cá nhân với WordPress.', 'Hướng dẫn', 'Hồ Chí Minh', 0, '2022-04-12 12:20:00', '2022-04-13 13:00:00', 1, '2022-04-15 18:10:00'),

    -- Bài viết đã lưu trữ (IsArchived = 1)
    (16, 16, 'Mới mua chiếc máy tính mới, quá đã!', 'Công nghệ', 'Đà Nẵng', 1, '2022-05-15 08:40:00', '2022-05-16 09:30:00', 0, NULL),
    (17, 17, 'Review về chuyến du lịch tại Phú Quốc.', 'Du lịch', 'Phú Quốc', 1, '2022-06-25 19:00:00', '2022-06-26 20:10:00', 0, NULL),

    -- Bài viết bình thường (IsArchived = 0, IsDeleted = 0)
    (18, 18, 'Sự kiện công nghệ sắp tới ở TP.HCM.', 'Công nghệ', 'Hồ Chí Minh', 0, '2022-07-18 11:10:00', '2022-07-19 12:25:00', 0, NULL),
    (19, 19, 'Lập kế hoạch học tập cho năm mới.', 'Học tập', 'Hà Nội', 0, '2022-08-22 10:00:00', '2022-08-23 11:00:00', 0, NULL),

    -- Bài viết đã xóa và hết hạn (IsDeleted = 1, DeletedAt không NULL)
    (20, 20, 'Cảm nhận về bộ phim yêu thích gần đây.', 'Phim', 'Đà Nẵng', 0, '2022-09-30 14:50:00', '2022-10-01 15:30:00', 1, '2022-10-02 08:30:00');

-- Dữ liệu mẫu cho bảng Stories
INSERT INTO CoreData.InteractableItems (ItemType, CreatedAt)
VALUES
    ('STORY', '2022-01-01 10:00:00'),
    ('STORY', '2022-02-10 14:30:00'),
    ('STORY', '2022-03-20 08:15:00'),
    ('STORY', '2022-04-25 18:45:00'),
    ('STORY', '2022-05-05 11:00:00'),
    ('STORY', '2022-06-15 16:25:00'),
    ('STORY', '2022-07-20 07:50:00'),
    ('STORY', '2022-08-10 12:40:00'),
    ('STORY', '2022-09-05 09:30:00'),
    ('STORY', '2022-10-12 17:15:00'),
    ('STORY', '2022-11-01 13:00:00'),
    ('STORY', '2022-12-25 15:00:00'),
    ('STORY', '2023-01-05 10:05:00'),
    ('STORY', '2023-02-10 14:10:00'),
    ('STORY', '2023-03-03 19:20:00'),
    ('STORY', '2023-04-18 11:45:00'),
    ('STORY', '2023-05-12 08:30:00'),
    ('STORY', '2023-06-25 13:00:00'),
    ('STORY', '2023-07-30 10:10:00'),
    ('STORY', '2023-08-10 15:55:00');
INSERT INTO CoreData.Stories (UserID, InteractableItemID, MediaURL, MediaType, CreatedAt)
VALUES
    (1, 21, 'https://example.com/images/story1.jpg', 'IMAGE', '2022-01-01 10:00:00'),
    (2, 22, 'https://example.com/videos/story2.mp4', 'VIDEO', '2022-02-10 14:30:00'),
    (3, 23, 'https://example.com/images/story3.jpg', 'IMAGE', '2022-03-20 08:15:00'),
    (4, 24, 'https://example.com/videos/story4.mp4', 'VIDEO', '2022-04-25 18:45:00'),
    (5, 25, 'https://example.com/images/story5.jpg', 'IMAGE', '2022-05-05 11:00:00'),
    (6, 26, 'https://example.com/videos/story6.mp4', 'VIDEO', '2022-06-15 16:25:00'),
    (7, 27, 'https://example.com/images/story7.jpg', 'IMAGE', '2022-07-20 07:50:00'),
    (8, 28, 'https://example.com/videos/story8.mp4', 'VIDEO', '2022-08-10 12:40:00'),
    (9, 29, 'https://example.com/images/story9.jpg', 'IMAGE', '2022-09-05 09:30:00'),
    (10, 30, 'https://example.com/videos/story10.mp4', 'VIDEO', '2022-10-12 17:15:00'),
    (11, 31, 'https://example.com/images/story11.jpg', 'IMAGE', '2022-11-01 13:00:00'),
    (12, 32, 'https://example.com/videos/story12.mp4', 'VIDEO', '2022-12-25 15:00:00'),
    (13, 33, 'https://example.com/images/story13.jpg', 'IMAGE', '2023-01-05 10:05:00'),
    (14, 34, 'https://example.com/videos/story14.mp4', 'VIDEO', '2023-02-10 14:10:00'),
    (15, 35, 'https://example.com/images/story15.jpg', 'IMAGE', '2023-03-03 19:20:00'),
    (16, 36, 'https://example.com/videos/story16.mp4', 'VIDEO', '2023-04-18 11:45:00'),
    (17, 37, 'https://example.com/images/story17.jpg', 'IMAGE', '2023-05-12 08:30:00'),
    (18, 38, 'https://example.com/videos/story18.mp4', 'VIDEO', '2023-06-25 13:00:00'),
    (19, 39, 'https://example.com/images/story19.jpg', 'IMAGE', '2023-07-30 10:10:00'),
    (20, 40, 'https://example.com/videos/story20.mp4', 'VIDEO', '2023-08-10 15:55:00');

-- Dữ liệu cho bảng PostMedia với InteractableItemID bắt đầu từ 21
INSERT INTO CoreData.InteractableItems (ItemType, CreatedAt)
VALUES
    -- Mục liên quan đến ảnh/video của bài viết 1
    ('MEDIA', '2021-02-01 08:30:00'),
    ('MEDIA', '2021-02-01 08:40:00'),
    ('MEDIA', '2021-03-05 12:45:00'),
    ('MEDIA', '2021-03-05 12:55:00'),

    -- Mục liên quan đến ảnh/video của bài viết 2
    ('MEDIA', '2021-04-10 14:20:00'),
    ('MEDIA', '2021-04-10 14:30:00'),

    -- Mục liên quan đến ảnh/video của bài viết 3
    ('MEDIA', '2021-05-15 16:00:00'),
    ('MEDIA', '2021-05-15 16:10:00'),

    -- Mục liên quan đến ảnh/video của bài viết 4
    ('MEDIA', '2021-06-20 09:10:00'),
    ('MEDIA', '2021-06-20 09:20:00'),

    -- Mục liên quan đến ảnh/video của bài viết 5
    ('MEDIA', '2021-07-25 11:30:00'),
    ('MEDIA', '2021-07-25 11:40:00'),

    -- Mục liên quan đến ảnh/video của bài viết 6
    ('MEDIA', '2021-08-30 18:15:00'),
    ('MEDIA', '2021-08-30 18:25:00'),

    -- Mục liên quan đến ảnh/video của bài viết 7
    ('MEDIA', '2021-09-10 13:45:00'),
    ('MEDIA', '2021-09-10 13:55:00'),

    -- Mục liên quan đến ảnh/video của bài viết 8
    ('MEDIA', '2021-10-18 17:05:00'),
    ('MEDIA', '2021-10-18 17:15:00'),

    -- Mục liên quan đến ảnh/video của bài viết 9
    ('MEDIA', '2021-11-25 15:25:00'),
    ('MEDIA', '2021-11-25 15:35:00'),

    -- Mục liên quan đến ảnh/video của bài viết 10
    ('MEDIA', '2021-12-05 10:40:00'),
    ('MEDIA', '2021-12-05 10:50:00'),

    -- Mục liên quan đến ảnh/video của bài viết 11
    ('MEDIA', '2022-01-10 09:55:00'),
    ('MEDIA', '2022-01-10 10:05:00');
INSERT INTO CoreData.PostMedia (PostID, InteractableItemID, MediaURL, MediaType, Caption, SortOrder)
VALUES
    (1, 41, 'https://example.com/media1.jpg', 'IMAGE', 'Ảnh đẹp từ chuyến đi biển', 0),
    (1, 42, 'https://example.com/media2.jpg', 'IMAGE', 'Chuyến đi tuyệt vời', 1),
    (2, 43, 'https://example.com/video1.mp4', 'VIDEO', 'Video giới thiệu sản phẩm mới', 0),
    (2, 44, 'https://example.com/media3.jpg', 'IMAGE', 'Đừng bỏ lỡ sự kiện này!', 0),
    (3, 45, 'https://example.com/media4.jpg', 'IMAGE', 'Cảnh hoàng hôn tuyệt vời', 0),
    (3, 46, 'https://example.com/video2.mp4', 'VIDEO', 'Video vui nhộn từ buổi tiệc', 1),
    (4, 47, 'https://example.com/media5.jpg', 'IMAGE', 'Cảm giác tuyệt vời khi leo núi', 0),
    (4, 48, 'https://example.com/video3.mp4', 'VIDEO', 'Sự kiện thể thao cuối tuần', 0),
    (5, 49, 'https://example.com/media6.jpg', 'IMAGE', 'Sản phẩm mới ra mắt', 0),
    (6, 50, 'https://example.com/media7.jpg', 'IMAGE', 'Khám phá vẻ đẹp của thiên nhiên', 0),
    (6, 51, 'https://example.com/video4.mp4', 'VIDEO', 'Buổi hòa nhạc mùa hè', 0),
    (7, 52, 'https://example.com/media8.jpg', 'IMAGE', 'Bữa tiệc sinh nhật vui nhộn', 0),
    (7, 53, 'https://example.com/video5.mp4', 'VIDEO', 'Phỏng vấn đặc biệt với diễn viên nổi tiếng', 1),
    (8, 54, 'https://example.com/media9.jpg', 'IMAGE', 'Kỳ nghỉ hè tại Đà Lạt', 0),
    (8, 55, 'https://example.com/video6.mp4', 'VIDEO', 'Chuyến du lịch sang trọng tại Phú Quốc', 0),
    (9, 56, 'https://example.com/media10.jpg', 'IMAGE', 'Ảnh chụp từ sự kiện âm nhạc lớn', 0),
    (9, 57, 'https://example.com/video7.mp4', 'VIDEO', 'Trải nghiệm thử xe mới', 1),
    (10, 58, 'https://example.com/media11.jpg', 'IMAGE', 'Phong cảnh tuyệt đẹp ở Đà Nẵng', 0),
    (10, 59, 'https://example.com/video8.mp4', 'VIDEO', 'Hướng dẫn sử dụng phần mềm mới', 0),
    (11, 60, 'https://example.com/media12.jpg', 'IMAGE', 'Thưởng thức cà phê sáng tại Hà Nội', 0);



-- Dữ liệu cho bảng Shares với InteractableItemID bắt đầu từ 41
INSERT INTO CoreData.InteractableItems (ItemType, CreatedAt)
VALUES
    ('SHARE', '2023-10-01 12:00:00'),
    ('SHARE', '2023-10-01 14:05:00'),
    ('SHARE', '2023-10-01 15:30:00'),
    ('SHARE', '2023-10-01 16:00:00'),
    ('SHARE', '2023-10-02 09:10:00'),
    ('SHARE', '2023-10-02 11:20:00'),
    ('SHARE', '2023-10-02 12:30:00'),
    ('SHARE', '2023-10-02 13:40:00'),
    ('SHARE', '2023-10-02 14:50:00'),
    ('SHARE', '2023-10-02 15:05:00'),
    ('SHARE', '2023-10-03 08:00:00'),
    ('SHARE', '2023-10-03 10:20:00'),
    ('SHARE', '2023-10-03 11:30:00'),
    ('SHARE', '2023-10-03 12:45:00'),
    ('SHARE', '2023-10-03 13:50:00'),
    ('SHARE', '2023-10-03 14:15:00'),
    ('SHARE', '2023-10-03 14:40:00'),
    ('SHARE', '2023-10-04 09:00:00'),
    ('SHARE', '2023-10-04 10:30:00'),
    ('SHARE', '2023-10-04 12:00:00');

INSERT INTO CoreData.Shares (UserID, OriginalPostID, InteractableItemID, ShareCaption, CreatedAt)
VALUES
    (1, 1, 61, 'Chia sẻ bài viết tuyệt vời này!', '2023-10-01 12:00:00'),
    (2, 2, 62, 'Mọi người không nên bỏ lỡ video này', '2023-10-01 14:05:00'),
    (3, 3, 63, 'Bài viết rất hữu ích cho những ai đam mê công nghệ', '2023-10-01 15:30:00'),
    (4, 4, 64, 'Chia sẻ những khoảnh khắc tuyệt vời của tôi', '2023-10-01 16:00:00'),
    (5, 5, 65, 'Đừng quên tham gia sự kiện sắp tới nhé!', '2023-10-02 09:10:00'),
    (6, 6, 66, 'Chuyến du lịch này thật đáng nhớ!', '2023-10-02 11:20:00'),
    (7, 7, 67, 'Nhớ theo dõi để xem thêm các video mới', '2023-10-02 12:30:00'),
    (8, 8, 68, 'Bài viết này nói về những điều quan trọng', '2023-10-02 13:40:00'),
    (9, 9, 69, 'Rất vui vì sự kiện đã thành công ngoài mong đợi', '2023-10-02 14:50:00'),
    (10, 10, 70, 'Bài viết này chia sẻ những kiến thức rất hữu ích', '2023-10-02 15:05:00'),
    (11, 11, 71, 'Chuyến đi này khiến tôi cảm thấy thư giãn hơn bao giờ hết', '2023-10-03 08:00:00'),
    (12, 12, 72, 'Đây là video tôi rất thích, mong mọi người xem thử', '2023-10-03 10:20:00'),
    (13, 13, 73, 'Những sản phẩm mới ra mắt thật sự ấn tượng', '2023-10-03 11:30:00'),
    (14, 14, 74, 'Thật tuyệt vời khi có thể cùng bạn bè chia sẻ bài viết này', '2023-10-03 12:45:00'),
    (15, 15, 75, 'Chuyến du lịch này thật sự rất đáng giá', '2023-10-03 13:50:00'),
    (16, 16, 76, 'Video này quá hay, tôi đã học được nhiều điều mới', '2023-10-03 14:15:00'),
    (17, 17, 77, 'Nếu bạn chưa xem bài viết này thì thật tiếc đấy!', '2023-10-03 14:40:00'),
    (18, 18, 78, 'Bài viết này rất nhiều thông tin hay ho về du lịch', '2023-10-04 09:00:00'),
    (19, 19, 79, 'Mình thấy bài viết này rất ý nghĩa, chia sẻ cho mọi người!', '2023-10-04 10:30:00'),
    (20, 20, 80, 'Hãy tham gia cuộc thi này để có cơ hội nhận giải thưởng lớn!', '2023-10-04 12:00:00');



INSERT INTO CoreData.FeedItems (UserID, ItemID, ActivityType, ActorUserID, CreatedAt)
VALUES
    -- Hoạt động "CREATED" (Tạo bài viết mới)
    (1, 1, 'CREATED', 1, '2023-10-01 08:30:00'),
    (2, 2, 'CREATED', 2, '2023-10-01 09:00:00'),
    (3, 3, 'CREATED', 3, '2023-10-01 10:00:00'),
    (4, 4, 'CREATED', 4, '2023-10-01 11:15:00'),
    (5, 5, 'CREATED', 5, '2023-10-02 09:30:00'),
    (6, 6, 'CREATED', 6, '2023-10-02 10:45:00'),
    (7, 7, 'CREATED', 7, '2023-10-02 12:00:00'),
    (8, 8, 'CREATED', 8, '2023-10-02 13:30:00'),
    (9, 9, 'CREATED', 9, '2023-10-02 15:00:00'),
    (10, 10, 'CREATED', 10, '2023-10-02 16:30:00'),

    -- Hoạt động "SHARED" (Chia sẻ bài viết)
    (1, 1, 'SHARED', 1, '2023-10-01 12:00:00'),
    (2, 2, 'SHARED', 2, '2023-10-01 14:05:00'),
    (3, 3, 'SHARED', 3, '2023-10-01 15:30:00'),
    (4, 4, 'SHARED', 4, '2023-10-01 16:00:00'),
    (5, 5, 'SHARED', 5, '2023-10-02 09:10:00'),
    (6, 6, 'SHARED', 6, '2023-10-02 11:20:00'),
    (7, 7, 'SHARED', 7, '2023-10-02 12:30:00'),
    (8, 8, 'SHARED', 8, '2023-10-02 13:40:00'),
    (9, 9, 'SHARED', 9, '2023-10-02 14:50:00'),
    (10, 10, 'SHARED', 10, '2023-10-02 15:05:00');

-- Dữ liệu cho bảng Comments
-- Dữ liệu cho bảng InteractableItems
INSERT INTO CoreData.InteractableItems (ItemType, CreatedAt)
VALUES
    ('COMMENT', '2023-10-01 10:00:00'),
    ('COMMENT', '2023-10-01 10:30:00'),
    ('COMMENT', '2023-10-01 11:00:00'),
    ('COMMENT', '2023-10-01 12:00:00'),
    ('COMMENT', '2023-10-01 13:00:00'),
    ('COMMENT', '2023-10-01 14:00:00'),
    ('COMMENT', '2023-10-01 14:30:00'),
    ('COMMENT', '2023-10-01 15:00:00'),
    ('COMMENT', '2023-10-01 16:00:00'),
    ('COMMENT', '2023-10-01 17:00:00'),
    ('COMMENT', '2023-10-01 18:00:00'),
    ('COMMENT', '2023-10-01 18:30:00'),
    ('COMMENT', '2023-10-01 19:00:00'),
    ('COMMENT', '2023-10-02 08:00:00'),
    ('COMMENT', '2023-10-02 09:00:00'),
    ('COMMENT', '2023-10-02 10:00:00'),
    ('COMMENT', '2023-10-02 11:00:00'),
    ('COMMENT', '2023-10-02 12:00:00'),
    ('COMMENT', '2023-10-02 13:00:00'),
    ('COMMENT', '2023-10-02 14:00:00');

INSERT INTO CoreData.Comments (UserID, TargetInteractableItemID, OwnInteractableItemID, ParentCommentID, Content, CreatedAt, IsDeleted)
VALUES
    (1, 10, 81, NULL, 'Bài viết này thật tuyệt vời!', '2023-10-01 10:00:00', 0),
    (2, 15, 82, NULL, 'Tôi đồng ý với bạn, rất hay!', '2023-10-01 10:30:00', 0),
    (3, 20, 83, NULL, 'Cảm ơn bạn đã chia sẻ!', '2023-10-01 11:00:00', 0),
    (4, 25, 84, NULL, 'Chủ đề này rất thú vị!', '2023-10-01 12:00:00', 0),
    (5, 30, 85, NULL, 'Mình sẽ thử làm theo hướng dẫn của bạn.', '2023-10-01 13:00:00', 0),
    (6, 35, 86, NULL, 'Bài viết này rất bổ ích, cảm ơn bạn!', '2023-10-01 14:00:00', 0),
    (7, 40, 87, NULL, 'Thông tin rất chi tiết, cảm ơn vì đã chia sẻ!', '2023-10-01 14:30:00', 0),
    (8, 45, 88, NULL, 'Mình thấy bài viết này có thể cải thiện thêm về...', '2023-10-01 15:00:00', 0),
    (9, 50, 89, NULL, 'Đồng tình với bạn, rất bổ ích!', '2023-10-01 16:00:00', 0),
    (10, 55, 90, NULL, 'Mình sẽ làm thử, cảm ơn bạn!', '2023-10-01 17:00:00', 0),
    (1, 60, 91, 1, 'Cảm ơn bạn đã đóng góp ý kiến!', '2023-10-01 18:00:00', 0),
    (2, 5, 92, 2, 'Mình rất thích phần chia sẻ của bạn!', '2023-10-01 18:30:00', 0),
    (3, 12, 93, 3, 'Cảm ơn bạn đã tán thành với mình!', '2023-10-01 19:00:00', 0),
    (4, 18, 94, 4, 'Mình thấy bài viết này có thể thêm phần giải thích nữa.', '2023-10-02 08:00:00', 0),
    (5, 23, 95, 5, 'Bài viết hay quá, cảm ơn bạn đã chia sẻ!', '2023-10-02 09:00:00', 0),
    (6, 28, 96, 6, 'Mình thấy bài viết này rất hữu ích!', '2023-10-02 10:00:00', 0),
    (7, 32, 97, 7, 'Cảm ơn vì đã chia sẻ bài viết này, rất tuyệt!', '2023-10-02 11:00:00', 0),
    (8, 38, 98, 8, 'Bài viết này rất hay nhưng có thể bổ sung thêm ví dụ.', '2023-10-02 12:00:00', 0),
    (9, 42, 99, 9, 'Đây là bài viết tuyệt vời, tôi hoàn toàn đồng ý!', '2023-10-02 13:00:00', 0),
    (10, 47, 100, 10, 'Cảm ơn bạn đã cung cấp thông tin chi tiết!', '2023-10-02 14:00:00', 0);


INSERT INTO CoreData.Reactions (UserID, InteractableItemID, ReactionType, ReactedAt)
VALUES
    (1, 1, 'LIKE', '2023-10-01 10:00:00'),
    (2, 2, 'LOVE', '2023-10-01 10:30:00'),
    (3, 3, 'HAHA', '2023-10-01 11:00:00'),
    (4, 4, 'WOW', '2023-10-01 12:00:00'),
    (5, 5, 'SAD', '2023-10-01 13:00:00'),
    (6, 6, 'ANGRY', '2023-10-01 14:00:00'),
    (7, 7, 'LIKE', '2023-10-01 14:30:00'),
    (8, 8, 'LOVE', '2023-10-01 15:00:00'),
    (9, 9, 'HAHA', '2023-10-01 16:00:00'),
    (10, 10, 'WOW', '2023-10-01 17:00:00'),
    (1, 11, 'SAD', '2023-10-01 18:00:00'),
    (2, 12, 'ANGRY', '2023-10-01 19:00:00'),
    (3, 13, 'LIKE', '2023-10-02 08:00:00'),
    (4, 14, 'LOVE', '2023-10-02 09:00:00'),
    (5, 15, 'HAHA', '2023-10-02 10:00:00'),
    (6, 16, 'WOW', '2023-10-02 11:00:00'),
    (7, 17, 'SAD', '2023-10-02 12:00:00'),
    (8, 18, 'ANGRY', '2023-10-02 13:00:00'),
    (9, 19, 'LIKE', '2023-10-02 14:00:00'),
    (10, 20, 'LOVE', '2023-10-02 15:00:00');

INSERT INTO CoreData.PostTags (PostID, TaggedUserID, TaggedAt)
VALUES
    (1, 2, '2023-10-01 09:00:00'),
    (1, 3, '2023-10-01 09:15:00'),
    (2, 4, '2023-10-01 10:00:00'),
    (3, 5, '2023-10-01 10:30:00'),
    (3, 6, '2023-10-01 11:00:00'),
    (4, 7, '2023-10-01 11:30:00'),
    (5, 8, '2023-10-01 12:00:00'),
    (6, 9, '2023-10-01 12:30:00'),
    (7, 10, '2023-10-01 13:00:00'),
    (8, 11, '2023-10-01 13:30:00'),
    (9, 12, '2023-10-01 14:00:00'),
    (10, 13, '2023-10-01 14:30:00'),
    (11, 14, '2023-10-02 08:00:00'),
    (12, 15, '2023-10-02 08:30:00'),
    (13, 16, '2023-10-02 09:00:00'),
    (14, 17, '2023-10-02 09:30:00'),
    (15, 18, '2023-10-02 10:00:00'),
    (16, 19, '2023-10-02 10:30:00'),
    (17, 20, '2023-10-02 11:00:00'),
    (18, 2, '2023-10-02 11:30:00');

INSERT INTO CoreData.Hashtags (TagName)
VALUES
    ('#love'),
    ('#fun'),
    ('#vacation'),
    ('#workout'),
    ('#foodie'),
    ('#tech'),
    ('#fitness'),
    ('#travel'),
    ('#friends'),
    ('#family'),
    ('#motivation'),
    ('#music'),
    ('#art'),
    ('#fashion'),
    ('#adventure'),
    ('#sports'),
    ('#health'),
    ('#nature'),
    ('#photography'),
    ('#goodvibes');

INSERT INTO CoreData.PostHashtags (PostID, HashtagID)
VALUES
    (1, 1),  -- PostID 1, Hashtag #love
    (1, 2),  -- PostID 1, Hashtag #fun
    (2, 3),  -- PostID 2, Hashtag #vacation
    (3, 4),  -- PostID 3, Hashtag #workout
    (4, 5),  -- PostID 4, Hashtag #foodie
    (5, 6),  -- PostID 5, Hashtag #tech
    (6, 7),  -- PostID 6, Hashtag #fitness
    (7, 8),  -- PostID 7, Hashtag #travel
    (8, 9),  -- PostID 8, Hashtag #friends
    (9, 10), -- PostID 9, Hashtag #family
    (10, 11),-- PostID 10, Hashtag #motivation
    (11, 12),-- PostID 11, Hashtag #music
    (12, 13),-- PostID 12, Hashtag #art
    (13, 14),-- PostID 13, Hashtag #fashion
    (14, 15),-- PostID 14, Hashtag #adventure
    (15, 16),-- PostID 15, Hashtag #sports
    (16, 17),-- PostID 16, Hashtag #health
    (17, 18),-- PostID 17, Hashtag #nature
    (18, 19),-- PostID 18, Hashtag #photography
    (19, 20);-- PostID 19, Hashtag #goodvibes

INSERT INTO CoreData.Follows (FollowerID, FollowingID, FollowedAt)
VALUES
    (1, 2, '2025-10-01 10:30:00'),
    (1, 3, '2025-10-02 09:15:00'),
    (2, 1, '2025-09-30 11:00:00'),
    (3, 4, '2025-10-03 13:30:00'),
    (4, 5, '2025-10-04 14:10:00'),
    (5, 6, '2025-10-05 15:00:00'),
    (6, 7, '2025-10-06 16:20:00'),
    (7, 8, '2025-10-07 17:25:00'),
    (8, 9, '2025-10-08 18:35:00'),
    (9, 10, '2025-10-09 19:45:00'),
    (10, 11, '2025-10-10 20:10:00'),
    (11, 12, '2025-10-11 21:15:00'),
    (12, 13, '2025-10-12 22:00:00'),
    (13, 14, '2025-10-13 23:20:00'),
    (14, 15, '2025-10-14 08:00:00'),
    (15, 16, '2025-10-15 09:45:00'),
    (16, 17, '2025-10-16 10:15:00'),
    (17, 18, '2025-10-17 11:30:00'),
    (18, 19, '2025-10-18 12:00:00'),
    (19, 20, '2025-10-19 14:10:00');

INSERT INTO CoreData.Blocks (BlockerID, BlockedUserID, BlockedAt)
VALUES
    (1, 3, '2025-10-01 10:30:00'),
    (2, 4, '2025-10-02 09:15:00'),
    (3, 5, '2025-10-03 11:00:00'),
    (4, 6, '2025-10-04 12:30:00'),
    (5, 7, '2025-10-05 13:40:00'),
    (6, 8, '2025-10-06 14:50:00'),
    (7, 9, '2025-10-07 15:25:00'),
    (8, 10, '2025-10-08 16:00:00'),
    (9, 11, '2025-10-09 17:30:00'),
    (10, 12, '2025-10-10 18:10:00'),
    (11, 13, '2025-10-11 19:00:00'),
    (12, 14, '2025-10-12 20:20:00'),
    (13, 15, '2025-10-13 21:00:00'),
    (14, 16, '2025-10-14 22:15:00'),
    (15, 17, '2025-10-15 08:45:00'),
    (16, 18, '2025-10-16 09:10:00'),
    (17, 19, '2025-10-17 10:30:00'),
    (18, 20, '2025-10-18 11:40:00'),
    (19, 2, '2025-10-19 12:00:00'),
    (20, 1, '2025-10-20 14:00:00');

INSERT INTO CoreData.Reports (ReporterID, ReportedPostID, ReportedCommentID, ReportedUserID, Reason, ReportStatus, ReportedAt)
VALUES
    (1, 1, NULL, NULL, 'Spam content', 'PENDING', '2025-10-01 10:30:00'),
    (2, 2, NULL, NULL, 'Offensive language', 'REVIEWED', '2025-10-02 12:00:00'),
    (3, NULL, 3, NULL, 'Harassment', 'RESOLVED', '2025-10-03 13:15:00'),
    (4, 4, NULL, NULL, 'Misinformation', 'PENDING', '2025-10-04 14:45:00'),
    (5, 5, NULL, NULL, 'Hate speech', 'REJECTED', '2025-10-05 15:20:00'),
    (6, NULL, 6, NULL, 'Threatening behavior', 'REVIEWED', '2025-10-06 16:30:00'),
    (7, 7, NULL, NULL, 'Inappropriate content', 'PENDING', '2025-10-07 17:45:00'),
    (8, NULL, 8, NULL, 'Harassment', 'PENDING', '2025-10-08 18:50:00'),
    (9, 9, NULL, NULL, 'Violence', 'REVIEWED', '2025-10-09 19:10:00'),
    (10, NULL, 10, NULL, 'Fake news', 'RESOLVED', '2025-10-10 20:00:00'),
    (11, 11, NULL, NULL, 'Bullying', 'PENDING', '2025-10-11 21:30:00'),
    (12, 12, NULL, NULL, 'Discriminatory', 'PENDING', '2025-10-12 22:00:00'),
    (13, NULL, 13, NULL, 'Inappropriate comment', 'REJECTED', '2025-10-13 23:40:00'),
    (14, 14, NULL, NULL, 'Explicit content', 'REVIEWED', '2025-10-14 08:20:00'),
    (15, NULL, 15, NULL, 'Defamation', 'PENDING', '2025-10-15 09:00:00'),
    (16, 16, NULL, NULL, 'Unsolicited advertisement', 'PENDING', '2025-10-16 10:15:00'),
    (17, 17, NULL, NULL, 'Sexual content', 'REVIEWED', '2025-10-17 11:30:00'),
    (18, NULL, 18, NULL, 'Threats of violence', 'REJECTED', '2025-10-18 12:45:00'),
    (19, 19, NULL, NULL, 'Plagiarism', 'PENDING', '2025-10-19 13:25:00'),
    (20, NULL, 20, NULL, 'Terroristic content', 'RESOLVED', '2025-10-20 14:35:00');

INSERT INTO CoreData.Notifications (RecipientUserID, ActorUserID, NotificationType, TargetItemID, IsRead, CreatedAt)
VALUES
    (1, 2, 'NEW_COMMENT', 1, 0, '2025-10-01 10:30:00'),
    (2, 3, 'NEW_REACTION', 2, 1, '2025-10-02 11:00:00'),
    (3, 4, 'NEW_FOLLOWER', NULL, 0, '2025-10-03 12:30:00'),
    (4, 5, 'POST_TAG', 3, 0, '2025-10-04 13:45:00'),
    (5, 6, 'COMMENT_MENTION', 4, 1, '2025-10-05 14:20:00'),
    (6, 7, 'NEW_COMMENT', 5, 0, '2025-10-06 15:30:00'),
    (7, 8, 'NEW_REACTION', 6, 1, '2025-10-07 16:40:00'),
    (8, 9, 'NEW_FOLLOWER', NULL, 0, '2025-10-08 17:50:00'),
    (9, 10, 'POST_TAG', 7, 1, '2025-10-09 19:00:00'),
    (10, 11, 'COMMENT_MENTION', 8, 0, '2025-10-10 20:00:00'),
    (11, 12, 'NEW_COMMENT', 9, 1, '2025-10-11 21:15:00'),
    (12, 13, 'NEW_REACTION', 10, 0, '2025-10-12 22:30:00'),
    (13, 14, 'NEW_FOLLOWER', NULL, 1, '2025-10-13 23:45:00'),
    (14, 15, 'POST_TAG', 11, 0, '2025-10-14 08:25:00'),
    (15, 16, 'COMMENT_MENTION', 12, 1, '2025-10-15 09:10:00'),
    (16, 17, 'NEW_COMMENT', 13, 0, '2025-10-16 10:20:00'),
    (17, 18, 'NEW_REACTION', 14, 1, '2025-10-17 11:30:00'),
    (18, 19, 'NEW_FOLLOWER', NULL, 0, '2025-10-18 12:00:00'),
    (19, 20, 'POST_TAG', 15, 1, '2025-10-19 13:30:00'),
    (20, 1, 'COMMENT_MENTION', 16, 0, '2025-10-20 14:15:00');





