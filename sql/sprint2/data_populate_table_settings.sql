-- populate table SETTINGS with sample data

DELETE FROM STARNUMBER.SETTINGS;


INSERT INTO STARNUMBER.SETTINGS (setting_name, setting_description)
VALUES ( 'SIP URI','');

INSERT INTO STARNUMBER.SETTINGS (setting_name, setting_description)
VALUES ( 'Pattern','');

INSERT INTO STARNUMBER.SETTINGS (setting_name, setting_description)
VALUES ( 'Destination','');

INSERT INTO STARNUMBER.SETTINGS (setting_name, setting_description)
VALUES ( 'Schedule','');

select * from STARNUMBER.SETTINGS;

