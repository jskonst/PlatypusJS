CREATE TABLE MTD_USERS
(
    USR_NAME VARCHAR2(200) NOT NULL,
    USR_PASSWD VARCHAR2(200),
    USR_FORM   VARCHAR2(200),
    USR_CONTEXT VARCHAR2(200),
    USR_ROLES VARCHAR2(200),
    USR_PHONE VARCHAR2(200),
    USR_EMAIL VARCHAR2(200),
    CONSTRAINT MTD_USERS_PK PRIMARY KEY(USR_NAME)
)
#GO
CREATE TABLE MTD_GROUPS
(
        USR_NAME VARCHAR2(200) NOT NULL,
        GROUP_NAME VARCHAR2(200) NOT NULL
)
#GO
ALTER TABLE MTD_GROUPS
    ADD CONSTRAINT MTD_GROUPS_USERS_FK FOREIGN KEY(USR_NAME) REFERENCES MTD_USERS(USR_NAME) ON DELETE CASCADE 
#GO
insert into MTD_USERS (USR_NAME, USR_PASSWD)
    VALUES ('admin', 'abe6db4c9f5484fae8d79f2e868a673c')
#GO
insert into MTD_GROUPS (USR_NAME, GROUP_NAME)
    VALUES ('admin', 'admin')
#GO