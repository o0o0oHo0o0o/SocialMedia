IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'Auditing')
    EXEC('CREATE SCHEMA Auditing');
GO


CREATE TABLE Auditing.ActionLogs (
    LogID BIGINT PRIMARY KEY IDENTITY(1,1),


    PerformedByUserID INT NULL, 


    ActionType NVARCHAR(50) NOT NULL, 


    TargetEntityType NVARCHAR(50) NULL, 
    TargetEntityID NVARCHAR(100) NULL, 


    PerformedAt DATETIME2(3) NOT NULL DEFAULT GETUTCDATE(), 

	Status NVARCHAR(20) NOT NULL DEFAULT 'SUCCESS'
    CONSTRAINT CK_ActionLogs_Status CHECK (Status IN ('SUCCESS', 'FAILURE')),
    FailureReason NVARCHAR(1000) NULL,

    Details NVARCHAR(MAX) NULL, 
    ClientIP VARCHAR(45) NULL   


    FOREIGN KEY (PerformedByUserID) REFERENCES CoreData.Users(UserID) ON DELETE SET NULL
);
GO


CREATE NONCLUSTERED INDEX IX_ActionLogs_PerformedByUserID ON Auditing.ActionLogs(PerformedByUserID);
GO
CREATE NONCLUSTERED INDEX IX_ActionLogs_TargetEntity ON Auditing.ActionLogs(TargetEntityType, TargetEntityID);
GO