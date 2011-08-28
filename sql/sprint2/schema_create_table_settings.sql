-- Create table settings, which holds dynamically configured data settings
--  TODO:  this is a very dangerous data approach, consider factoring it out if possible
-- This SQL script creates the table.

DROP TABLE IF EXISTS STARNUMBER.SETTINGS;

CREATE TABLE  STARNUMBER.SETTINGS (
  setting_id serial primary key,
  setting_number int,
  setting_value_type varchar(80),
  setting_description varchar(80)
);

COMMENT ON TABLE STARNUMBER.SETTINGS IS 'This table tracks dynamically generated name, value pairs.  TODO: consider refactor';

