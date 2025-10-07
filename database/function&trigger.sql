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


