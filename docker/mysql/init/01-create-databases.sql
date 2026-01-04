-- Create databases for each microservice
CREATE DATABASE IF NOT EXISTS rxbuddy_user;
CREATE DATABASE IF NOT EXISTS rxbuddy_tenant;
CREATE DATABASE IF NOT EXISTS rxbuddy_card;
CREATE DATABASE IF NOT EXISTS rxbuddy_inventory;
CREATE DATABASE IF NOT EXISTS rxbuddy_billing;
CREATE DATABASE IF NOT EXISTS rxbuddy_customer;
CREATE DATABASE IF NOT EXISTS rxbuddy_doctor;
CREATE DATABASE IF NOT EXISTS rxbuddy_supplier;
CREATE DATABASE IF NOT EXISTS rxbuddy_notification;

-- Grant permissions
GRANT ALL PRIVILEGES ON rxbuddy_user.* TO 'rxbuddy'@'%';
GRANT ALL PRIVILEGES ON rxbuddy_tenant.* TO 'rxbuddy'@'%';
GRANT ALL PRIVILEGES ON rxbuddy_card.* TO 'rxbuddy'@'%';
GRANT ALL PRIVILEGES ON rxbuddy_inventory.* TO 'rxbuddy'@'%';
GRANT ALL PRIVILEGES ON rxbuddy_billing.* TO 'rxbuddy'@'%';
GRANT ALL PRIVILEGES ON rxbuddy_customer.* TO 'rxbuddy'@'%';
GRANT ALL PRIVILEGES ON rxbuddy_doctor.* TO 'rxbuddy'@'%';
GRANT ALL PRIVILEGES ON rxbuddy_supplier.* TO 'rxbuddy'@'%';
GRANT ALL PRIVILEGES ON rxbuddy_notification.* TO 'rxbuddy'@'%';

FLUSH PRIVILEGES;
