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

SELECT * FROM CoreData.Posts
