-- SQL that deletes all users and repopulates table users to a known state


DELETE FROM STARNUMBER.USERS WHERE 1=1;


-- Create user testuser

INSERT INTO STARNUMBER.USERS (USERNAME, PASSWORD, EMAIL)
VALUES (
'testuser',
'password',
'testuser@example.com'
);


INSERT INTO STARNUMBER.USERS (USERNAME, PASSWORD, EMAIL)
VALUES (
'testuser1',
'password1',
'testuser1@example.com'
);

INSERT INTO STARNUMBER.USERS (USERNAME, PASSWORD, EMAIL)
VALUES (
'testuser2',
'password2',
'testuser2@example.com'
);



commit;

SELECT * FROM STARNUMBER.USERS;

