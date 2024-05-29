CREATE DATABASE IF NOT EXISTS `roomescape`;
CREATE DATABASE IF NOT EXISTS `roomescape_test`;

CREATE USER 'test'@'%' IDENTIFIED BY 'test';
GRANT ALL PRIVILEGES ON `roomescape_test`.* TO 'test'@'%';

CREATE USER 'dev'@'%' IDENTIFIED BY 'dev';
GRANT ALL PRIVILEGES ON `roomescape`.* TO 'dev'@'%';

FLUSH PRIVILEGES;
