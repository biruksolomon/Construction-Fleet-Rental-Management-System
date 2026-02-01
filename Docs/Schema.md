# Fleet Management System - Complete Database Schema

## Table of Contents
1. [Design Principles](#design-principles)
2. [Core Tenant & User Structure](#core-tenant--user-structure)
3. [Configuration & Customization](#configuration--customization)
4. [Vehicle & Asset Management](#vehicle--asset-management)
5. [Driver & Employee Management](#driver--employee-management)
6. [Client & Rental Contract System](#client--rental-contract-system)
7. [Time Tracking & GPS](#time-tracking--gps)
8. [Fuel Management](#fuel-management)
9. [Maintenance System](#maintenance-system)
10. [Billing & Invoicing](#billing--invoicing)
11. [Payroll System](#payroll-system)
12. [Reporting & Audit](#reporting--audit)
13. [Relationships Diagram](#relationships-diagram)

---

## Design Principles

### Multi-Company (Multi-Tenant)
- **Single System**: One application instance
- **Multiple Companies**: Each company operates independently
- **Data Isolation**: No data mixing between companies using `company_id` foreign key

### Configuration-Driven
- No hardcoding of business rules
- Everything is adjustable per company
- Dynamic pricing rules and settings

### Audit-Friendly
- Track money, time, usage, and changes
- Complete audit trails for compliance
- Historical data preservation

### Expandable
- GPS and IoT ready
- Mobile application support
- SaaS-ready architecture

---

## Core Tenant & User Structure

### 1. Companies Table
**Entity Class**: `Company.java`

```sql
CREATE TABLE companies (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  business_type ENUM('CAR_RENTAL', 'CONSTRUCTION', 'MIXED') NOT NULL,
  currency VARCHAR(10) DEFAULT 'USD',
  timezone VARCHAR(50) DEFAULT 'UTC',
  language VARCHAR(10) DEFAULT 'en',
  status ENUM('ACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY unique_company_name (name)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| name | VARCHAR(255) | Company name |
| business_type | ENUM | Type of business (CAR_RENTAL, CONSTRUCTION, MIXED) |
| currency | VARCHAR(10) | Default currency for transactions |
| timezone | VARCHAR(50) | Company timezone for reporting |
| language | VARCHAR(10) | Default language |
| status | ENUM | ACTIVE or SUSPENDED |
| created_at | TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | Last update time |

**Repository Method**:
```java
Optional<Company> findByName(String name);
List<Company> findByStatus(Company.Status status);
```

---

### 2. Users Table
**Entity Class**: `User.java`

```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  phone VARCHAR(20),
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('OWNER', 'ADMIN', 'FLEET_MANAGER', 'ACCOUNTANT', 'DRIVER') NOT NULL,
  status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  UNIQUE KEY unique_user_email (email),
  INDEX idx_company_id (company_id),
  INDEX idx_email (email)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| company_id | BIGINT FK | Company this user belongs to |
| full_name | VARCHAR(255) | User's full name |
| email | VARCHAR(255) | Unique email address |
| phone | VARCHAR(20) | Contact phone number |
| password_hash | VARCHAR(255) | Hashed password |
| role | ENUM | User role (OWNER, ADMIN, FLEET_MANAGER, ACCOUNTANT, DRIVER) |
| status | ENUM | ACTIVE or INACTIVE |
| created_at | TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | Last update time |

**Relationships**:
- Belongs to: `Company` (many-to-one)
- Referenced by: `Driver`, `AuditLog`

**Repository Methods**:
```java
Optional<User> findByEmailAndCompanyId(String email, Long companyId);
List<User> findByCompanyIdAndRole(Long companyId, User.Role role);
List<User> findByCompanyIdAndStatus(Long companyId, User.Status status);
```

---

## Configuration & Customization

### 3. Company Settings Table
**Entity Class**: `CompanySetting.java`

```sql
CREATE TABLE company_settings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  setting_key VARCHAR(100) NOT NULL,
  setting_value VARCHAR(1000),
  data_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
  description TEXT,
  FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  UNIQUE KEY unique_company_setting (company_id, setting_key),
  INDEX idx_company_id (company_id)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| company_id | BIGINT FK | Company this setting belongs to |
| setting_key | VARCHAR(100) | Key name (e.g., enable_gps) |
| setting_value | VARCHAR(1000) | Value of the setting |
| data_type | ENUM | Type of data (STRING, NUMBER, BOOLEAN, JSON) |
| description | TEXT | Description of the setting |

**Common Settings**:
- `enable_gps`: Enable GPS tracking
- `driver_required`: Require driver for vehicles
- `max_driver_hours_per_day`: Maximum hours per day
- `fuel_tracking_enabled`: Enable fuel tracking
- `maintenance_alert_threshold`: Alert when maintenance due

---

### 4. Pricing Rules Table
**Entity Class**: `PricingRule.java`

```sql
CREATE TABLE pricing_rules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  applies_to ENUM('VEHICLE', 'DRIVER') NOT NULL,
  pricing_type ENUM('HOURLY', 'DAILY', 'WEEKLY', 'PROJECT') NOT NULL,
  rate DECIMAL(10, 2) NOT NULL,
  overtime_rate DECIMAL(10, 2),
  currency VARCHAR(10) DEFAULT 'USD',
  active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  INDEX idx_company_id (company_id),
  INDEX idx_applies_to (applies_to)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| company_id | BIGINT FK | Company this rule belongs to |
| applies_to | ENUM | VEHICLE or DRIVER |
| pricing_type | ENUM | HOURLY, DAILY, WEEKLY, PROJECT |
| rate | DECIMAL(10, 2) | Standard rate |
| overtime_rate | DECIMAL(10, 2) | Rate for overtime |
| currency | VARCHAR(10) | Currency of the rate |
| active | BOOLEAN | Whether rule is active |
| created_at | TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | Last update time |

---

## Vehicle & Asset Management

### 5. Vehicles Table
**Entity Class**: `Vehicle.java`

```sql
CREATE TABLE vehicles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  plate_number VARCHAR(20) NOT NULL,
  asset_code VARCHAR(50),
  type ENUM('CAR', 'TRUCK', 'EXCAVATOR', 'BULLDOZER', 'CRANE') NOT NULL,
  fuel_type ENUM('DIESEL', 'PETROL', 'ELECTRIC') NOT NULL,
  hourly_rate DECIMAL(10, 2),
  daily_rate DECIMAL(10, 2),
  status ENUM('AVAILABLE', 'RENTED', 'MAINTENANCE', 'INACTIVE') DEFAULT 'AVAILABLE',
  has_gps BOOLEAN DEFAULT FALSE,
  has_fuel_sensor BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  UNIQUE KEY unique_plate (plate_number),
  INDEX idx_company_id (company_id),
  INDEX idx_status (status)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| company_id | BIGINT FK | Company that owns the vehicle |
| plate_number | VARCHAR(20) | License plate number |
| asset_code | VARCHAR(50) | Asset tracking code |
| type | ENUM | Vehicle type (CAR, TRUCK, EXCAVATOR, etc.) |
| fuel_type | ENUM | DIESEL, PETROL, or ELECTRIC |
| hourly_rate | DECIMAL(10, 2) | Hourly rental rate |
| daily_rate | DECIMAL(10, 2) | Daily rental rate |
| status | ENUM | AVAILABLE, RENTED, MAINTENANCE, INACTIVE |
| has_gps | BOOLEAN | GPS tracking enabled |
| has_fuel_sensor | BOOLEAN | Fuel sensor installed |
| created_at | TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | Last update time |

**Relationships**:
- Belongs to: `Company` (many-to-one)
- Referenced by: `VehicleUsageLimit`, `VehicleTimeLog`, `RentalVehicle`, `GpsLog`, `FuelLog`, `FuelAnalysis`, `MaintenanceRecord`

---

### 6. Vehicle Usage Limits Table
**Entity Class**: `VehicleUsageLimit.java`

```sql
CREATE TABLE vehicle_usage_limits (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vehicle_id BIGINT NOT NULL UNIQUE,
  max_hours_per_day INT,
  max_hours_per_month INT,
  maintenance_interval_hours INT,
  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| vehicle_id | BIGINT FK | Vehicle this limit applies to |
| max_hours_per_day | INT | Maximum hours per day |
| max_hours_per_month | INT | Maximum hours per month |
| maintenance_interval_hours | INT | Hours between maintenance |

**Relationships**:
- Belongs to: `Vehicle` (one-to-one)

---

## Driver & Employee Management

### 7. Drivers Table
**Entity Class**: `Driver.java`

```sql
CREATE TABLE drivers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  license_number VARCHAR(50) NOT NULL,
  license_expiry DATE NOT NULL,
  hourly_wage DECIMAL(10, 2),
  employment_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT') NOT NULL,
  status ENUM('ACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY unique_license (license_number),
  INDEX idx_company_id (company_id),
  INDEX idx_user_id (user_id)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| company_id | BIGINT FK | Company the driver works for |
| user_id | BIGINT FK | Associated user account |
| license_number | VARCHAR(50) | Driver's license number |
| license_expiry | DATE | License expiration date |
| hourly_wage | DECIMAL(10, 2) | Hourly wage rate |
| employment_type | ENUM | FULL_TIME, PART_TIME, CONTRACT |
| status | ENUM | ACTIVE or SUSPENDED |
| created_at | TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | Last update time |

**Relationships**:
- Belongs to: `Company` (many-to-one)
- Belongs to: `User` (many-to-one)
- Referenced by: `DriverWorkLimit`, `RentalVehicle`, `PayrollRecord`

---

### 8. Driver Work Limits Table
**Entity Class**: `DriverWorkLimit.java`

```sql
CREATE TABLE driver_work_limits (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  driver_id BIGINT NOT NULL UNIQUE,
  max_hours_per_day INT,
  max_hours_per_week INT,
  FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| driver_id | BIGINT FK | Driver this limit applies to |
| max_hours_per_day | INT | Maximum hours per day |
| max_hours_per_week | INT | Maximum hours per week |

**Relationships**:
- Belongs to: `Driver` (one-to-one)

---

## Client & Rental Contract System

### 9. Clients Table
**Entity Class**: `Client.java`

```sql
CREATE TABLE clients (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  phone VARCHAR(20),
  email VARCHAR(255),
  address TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  INDEX idx_company_id (company_id),
  INDEX idx_email (email)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| company_id | BIGINT FK | Company the client belongs to |
| name | VARCHAR(255) | Client name |
| phone | VARCHAR(20) | Contact phone |
| email | VARCHAR(255) | Contact email |
| address | TEXT | Address |
| created_at | TIMESTAMP | Record creation time |

**Relationships**:
- Belongs to: `Company` (many-to-one)
- Referenced by: `RentalContract`

---

### 10. Rental Contracts Table
**Entity Class**: `RentalContract.java`

```sql
CREATE TABLE rental_contracts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  client_id BIGINT NOT NULL,
  contract_number VARCHAR(50) NOT NULL UNIQUE,
  start_date TIMESTAMP NOT NULL,
  end_date TIMESTAMP NOT NULL,
  include_driver BOOLEAN DEFAULT FALSE,
  pricing_model ENUM('HOURLY', 'DAILY', 'PROJECT') NOT NULL,
  status ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
  INDEX idx_company_id (company_id),
  INDEX idx_client_id (client_id),
  INDEX idx_status (status)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| company_id | BIGINT FK | Company handling the rental |
| client_id | BIGINT FK | Client renting |
| contract_number | VARCHAR(50) | Unique contract reference |
| start_date | TIMESTAMP | Rental start date/time |
| end_date | TIMESTAMP | Rental end date/time |
| include_driver | BOOLEAN | Whether driver is included |
| pricing_model | ENUM | HOURLY, DAILY, PROJECT |
| status | ENUM | ACTIVE, COMPLETED, CANCELLED |
| created_at | TIMESTAMP | Record creation time |

**Relationships**:
- Belongs to: `Company` (many-to-one)
- Belongs to: `Client` (many-to-one)
- Referenced by: `RentalVehicle`, `VehicleTimeLog`, `Invoice`

---

### 11. Rental Vehicles Table
**Entity Class**: `RentalVehicle.java`

```sql
CREATE TABLE rental_vehicles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rental_contract_id BIGINT NOT NULL,
  vehicle_id BIGINT NOT NULL,
  driver_id BIGINT,
  agreed_rate DECIMAL(10, 2) NOT NULL,
  FOREIGN KEY (rental_contract_id) REFERENCES rental_contracts(id) ON DELETE CASCADE,
  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
  FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE SET NULL,
  INDEX idx_rental_contract_id (rental_contract_id),
  INDEX idx_vehicle_id (vehicle_id)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| rental_contract_id | BIGINT FK | Associated rental contract |
| vehicle_id | BIGINT FK | Vehicle being rented |
| driver_id | BIGINT FK | Driver (optional) |
| agreed_rate | DECIMAL(10, 2) | Agreed rental rate |

**Relationships**:
- Belongs to: `RentalContract` (many-to-one)
- Belongs to: `Vehicle` (many-to-one)
- Belongs to: `Driver` (optional many-to-one)

---

## Time Tracking & GPS

### 12. Vehicle Time Logs Table
**Entity Class**: `VehicleTimeLog.java`

```sql
CREATE TABLE vehicle_time_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vehicle_id BIGINT NOT NULL,
  rental_contract_id BIGINT NOT NULL,
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP,
  total_hours DECIMAL(10, 2),
  source ENUM('GPS', 'MOBILE', 'MANUAL') DEFAULT 'MANUAL',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
  FOREIGN KEY (rental_contract_id) REFERENCES rental_contracts(id) ON DELETE CASCADE,
  INDEX idx_vehicle_id (vehicle_id),
  INDEX idx_rental_contract_id (rental_contract_id),
  INDEX idx_start_time (start_time)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| vehicle_id | BIGINT FK | Vehicle being tracked |
| rental_contract_id | BIGINT FK | Associated rental contract |
| start_time | TIMESTAMP | Time vehicle started |
| end_time | TIMESTAMP | Time vehicle ended |
| total_hours | DECIMAL(10, 2) | Total hours used |
| source | ENUM | GPS, MOBILE, or MANUAL |
| created_at | TIMESTAMP | Record creation time |

**Relationships**:
- Belongs to: `Vehicle` (many-to-one)
- Belongs to: `RentalContract` (many-to-one)

---

### 13. GPS Logs Table
**Entity Class**: `GpsLog.java`

```sql
CREATE TABLE gps_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vehicle_id BIGINT NOT NULL,
  latitude DECIMAL(10, 8) NOT NULL,
  longitude DECIMAL(11, 8) NOT NULL,
  speed DECIMAL(10, 2),
  recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
  INDEX idx_vehicle_id (vehicle_id),
  INDEX idx_recorded_at (recorded_at)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| vehicle_id | BIGINT FK | Vehicle being tracked |
| latitude | DECIMAL(10, 8) | GPS latitude |
| longitude | DECIMAL(11, 8) | GPS longitude |
| speed | DECIMAL(10, 2) | Current speed |
| recorded_at | TIMESTAMP | When GPS was recorded |

**Relationships**:
- Belongs to: `Vehicle` (many-to-one)

---

## Fuel Management

### 14. Fuel Logs Table
**Entity Class**: `FuelLog.java`

```sql
CREATE TABLE fuel_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vehicle_id BIGINT NOT NULL,
  refill_date TIMESTAMP NOT NULL,
  liters DECIMAL(10, 2) NOT NULL,
  cost DECIMAL(10, 2) NOT NULL,
  recorded_by ENUM('SYSTEM', 'MANUAL') DEFAULT 'MANUAL',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
  INDEX idx_vehicle_id (vehicle_id),
  INDEX idx_refill_date (refill_date)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| vehicle_id | BIGINT FK | Vehicle being refueled |
| refill_date | TIMESTAMP | Date of refueling |
| liters | DECIMAL(10, 2) | Amount of fuel in liters |
| cost | DECIMAL(10, 2) | Cost of fuel |
| recorded_by | ENUM | SYSTEM or MANUAL |
| created_at | TIMESTAMP | Record creation time |

**Relationships**:
- Belongs to: `Vehicle` (many-to-one)

---

### 15. Fuel Analysis Table
**Entity Class**: `FuelAnalysis.java`

```sql
CREATE TABLE fuel_analysis (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vehicle_id BIGINT NOT NULL,
  expected_consumption DECIMAL(10, 2),
  actual_consumption DECIMAL(10, 2),
  variance DECIMAL(10, 2),
  alert_generated BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
  INDEX idx_vehicle_id (vehicle_id)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| vehicle_id | BIGINT FK | Vehicle being analyzed |
| expected_consumption | DECIMAL(10, 2) | Expected fuel consumption |
| actual_consumption | DECIMAL(10, 2) | Actual fuel consumption |
| variance | DECIMAL(10, 2) | Difference (actual - expected) |
| alert_generated | BOOLEAN | Whether alert was triggered |
| created_at | TIMESTAMP | Record creation time |

**Relationships**:
- Belongs to: `Vehicle` (many-to-one)

---

## Maintenance System

### 16. Maintenance Records Table
**Entity Class**: `MaintenanceRecord.java`

```sql
CREATE TABLE maintenance_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vehicle_id BIGINT NOT NULL,
  maintenance_type ENUM('SERVICE', 'REPAIR') NOT NULL,
  cost DECIMAL(10, 2),
  maintenance_date TIMESTAMP NOT NULL,
  next_due_hours INT,
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
  INDEX idx_vehicle_id (vehicle_id),
  INDEX idx_maintenance_date (maintenance_date)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| vehicle_id | BIGINT FK | Vehicle undergoing maintenance |
| maintenance_type | ENUM | SERVICE or REPAIR |
| cost | DECIMAL(10, 2) | Cost of maintenance |
| maintenance_date | TIMESTAMP | Date of maintenance |
| next_due_hours | INT | Hours until next maintenance due |
| notes | TEXT | Maintenance notes |
| created_at | TIMESTAMP | Record creation time |

**Relationships**:
- Belongs to: `Vehicle` (many-to-one)

---

## Billing & Invoicing

### 17. Invoices Table
**Entity Class**: `Invoice.java`

```sql
CREATE TABLE invoices (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  rental_contract_id BIGINT NOT NULL,
  invoice_number VARCHAR(50) NOT NULL UNIQUE,
  vehicle_cost DECIMAL(10, 2) DEFAULT 0,
  driver_cost DECIMAL(10, 2) DEFAULT 0,
  fuel_cost DECIMAL(10, 2) DEFAULT 0,
  total_cost DECIMAL(10, 2),
  status ENUM('PENDING', 'PAID') DEFAULT 'PENDING',
  issued_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  FOREIGN KEY (rental_contract_id) REFERENCES rental_contracts(id) ON DELETE CASCADE,
  INDEX idx_company_id (company_id),
  INDEX idx_rental_contract_id (rental_contract_id),
  INDEX idx_status (status)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| company_id | BIGINT FK | Company issuing invoice |
| rental_contract_id | BIGINT FK | Associated rental contract |
| invoice_number | VARCHAR(50) | Unique invoice number |
| vehicle_cost | DECIMAL(10, 2) | Cost for vehicle usage |
| driver_cost | DECIMAL(10, 2) | Cost for driver |
| fuel_cost | DECIMAL(10, 2) | Cost for fuel |
| total_cost | DECIMAL(10, 2) | Total invoice amount |
| status | ENUM | PENDING or PAID |
| issued_date | TIMESTAMP | Date invoice was issued |

**Relationships**:
- Belongs to: `Company` (many-to-one)
- Belongs to: `RentalContract` (many-to-one)

---

## Payroll System

### 18. Payroll Periods Table
**Entity Class**: `PayrollPeriod.java`

```sql
CREATE TABLE payroll_periods (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  start_date TIMESTAMP NOT NULL,
  end_date TIMESTAMP NOT NULL,
  status ENUM('OPEN', 'CLOSED') DEFAULT 'OPEN',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  INDEX idx_company_id (company_id),
  INDEX idx_status (status)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| company_id | BIGINT FK | Company this period belongs to |
| start_date | TIMESTAMP | Period start date |
| end_date | TIMESTAMP | Period end date |
| status | ENUM | OPEN or CLOSED |
| created_at | TIMESTAMP | Record creation time |

**Relationships**:
- Belongs to: `Company` (many-to-one)
- Referenced by: `PayrollRecord`

---

### 19. Payroll Records Table
**Entity Class**: `PayrollRecord.java`

```sql
CREATE TABLE payroll_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  driver_id BIGINT NOT NULL,
  payroll_period_id BIGINT NOT NULL,
  total_hours DECIMAL(10, 2),
  base_pay DECIMAL(10, 2),
  overtime_pay DECIMAL(10, 2) DEFAULT 0,
  deductions DECIMAL(10, 2) DEFAULT 0,
  net_pay DECIMAL(10, 2),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE,
  FOREIGN KEY (payroll_period_id) REFERENCES payroll_periods(id) ON DELETE CASCADE,
  INDEX idx_driver_id (driver_id),
  INDEX idx_payroll_period_id (payroll_period_id)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| driver_id | BIGINT FK | Driver being paid |
| payroll_period_id | BIGINT FK | Associated payroll period |
| total_hours | DECIMAL(10, 2) | Total hours worked |
| base_pay | DECIMAL(10, 2) | Base pay amount |
| overtime_pay | DECIMAL(10, 2) | Overtime pay amount |
| deductions | DECIMAL(10, 2) | Total deductions |
| net_pay | DECIMAL(10, 2) | Final net pay |
| created_at | TIMESTAMP | Record creation time |

**Relationships**:
- Belongs to: `Driver` (many-to-one)
- Belongs to: `PayrollPeriod` (many-to-one)

---

## Reporting & Audit

### 20. Audit Logs Table
**Entity Class**: `AuditLog.java`

```sql
CREATE TABLE audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  action VARCHAR(100) NOT NULL,
  entity VARCHAR(100) NOT NULL,
  entity_id BIGINT,
  changes JSON,
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_company_id (company_id),
  INDEX idx_user_id (user_id),
  INDEX idx_timestamp (timestamp),
  INDEX idx_entity (entity)
);
```

| Attribute | Type | Description |
|-----------|------|-------------|
| id | BIGINT | Unique identifier |
| company_id | BIGINT FK | Company performing action |
| user_id | BIGINT FK | User performing action |
| action | VARCHAR(100) | Action type (CREATE, UPDATE, DELETE) |
| entity | VARCHAR(100) | Entity type being modified |
| entity_id | BIGINT | ID of entity being modified |
| changes | JSON | JSON of what changed |
| timestamp | TIMESTAMP | When action occurred |

**Relationships**:
- Belongs to: `Company` (many-to-one)
- Belongs to: `User` (many-to-one)

**Repository Methods**:
```java
List<AuditLog> findByCompanyIdAndEntityOrderByTimestampDesc(Long companyId, String entity);
List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
List<AuditLog> findByCompanyIdAndTimestampBetween(Long companyId, Timestamp start, Timestamp end);
```

---

## Relationships Diagram

### Entity Relationship Summary

```
┌─────────────┐
│  Companies  │ (Root multi-tenant entity)
└──────┬──────┘
       │
       ├─────────────────┬────────────────┬─────────────┬────────────────┐
       │                 │                │             │                │
       ▼                 ▼                ▼             ▼                ▼
    Users        CompanySettings    PricingRules   Vehicles        Drivers
       │                                                  │              │
       │                                                  │              │
       ├─────────────────┬──────────────┬────────────────┼──────────────┤
       │                 │              │                │              │
       ▼                 ▼              ▼                ▼              ▼
    Clients      RentalContracts  VehicleUsageLimit  RentalVehicles  DriverWorkLimit
                       │                              │
                       │                              │
                       ├──────────────────────────────┤
                       │
                       ▼
                  VehicleTimeLog

    Vehicles →  GpsLog
             →  FuelLog
             →  FuelAnalysis
             →  MaintenanceRecord

    RentalContracts → Invoices

    PayrollPeriods → PayrollRecords ← Drivers

    AuditLogs (tracks all changes in Companies/Users)
```

### Key Relationships

1. **One-to-Many**: Company → Users, Vehicles, Drivers, Clients, etc.
2. **One-to-One**: Vehicle ↔ VehicleUsageLimit, Driver ↔ DriverWorkLimit
3. **Many-to-Many (through RentalVehicles)**: RentalContracts ↔ Vehicles
4. **Multi-level**: Companies → RentalContracts → RentalVehicles → Vehicles

---

## Data Flow Examples

### Rental Process
1. Client requests rental → Create `RentalContract`
2. Add vehicles to contract → Create `RentalVehicle` records
3. Start usage → Create `VehicleTimeLog`
4. Record GPS → Create `GpsLog`
5. End rental → Calculate costs
6. Generate invoice → Create `Invoice`

### Fuel Tracking
1. Vehicle refueled → Create `FuelLog`
2. Calculate consumption → Create `FuelAnalysis`
3. Generate alert if variance exceeds threshold

### Payroll
1. Period created → Create `PayrollPeriod`
2. Calculate driver hours from `VehicleTimeLog`
3. Apply pricing rules → Create `PayrollRecord`
4. Calculate deductions → Update `PayrollRecord`

### Maintenance
1. Vehicle due for maintenance → Alert from `VehicleUsageLimit`
2. Perform maintenance → Create `MaintenanceRecord`
3. Update next_due_hours

---

## Indexing Strategy

### Primary Indexes (for common queries)
- `company_id` (most tables) - Multi-tenant isolation
- `user_id` - User lookups
- `vehicle_id` - Vehicle tracking
- `driver_id` - Driver records
- `rental_contract_id` - Contract lookup

### Secondary Indexes (for filtering)
- `status` - State-based queries
- `start_time`, `refill_date`, `maintenance_date` - Temporal queries
- `email` - User authentication

### Composite Indexes
- `(company_id, status)` - Company-specific status queries
- `(vehicle_id, recorded_at)` - Time-series queries for GPS/fuel

---

## Constraints & Validation

### Foreign Key Constraints
- **ON DELETE CASCADE**: When parent deleted, delete children (settings, time logs, audit logs)
- **ON DELETE SET NULL**: Optional relationships (driver in rental_vehicles)

### Unique Constraints
- Company names
- User emails (per company)
- License numbers
- Plate numbers
- Invoice numbers
- Contract numbers

### Business Rules
- `license_expiry` must be in future
- `end_date` > `start_date` for contracts
- `total_cost` = `vehicle_cost` + `driver_cost` + `fuel_cost`
- `net_pay` = `base_pay` + `overtime_pay` - `deductions`

---

## Performance Optimization Tips

1. **Pagination**: Use LIMIT/OFFSET for large result sets
2. **Batch Operations**: Insert multiple fuel logs in batch
3. **Archive**: Move old GPS logs to archive table
4. **Partitioning**: Partition GPS logs by vehicle_id and date
5. **Materialized Views**: Create summary views for reports

---

## Security Considerations

1. **Row Level Security**: Filter by `company_id` in all queries
2. **Audit Trail**: All changes logged in `AuditLog`
3. **Password Hashing**: `password_hash` using bcrypt
4. **Role-Based Access**: Check user role before operations
5. **Data Validation**: Validate dates, amounts, and enums

---

## Migration Strategy

### Phase 1: Core Tables
- Companies, Users, Vehicles, Drivers

### Phase 2: Rental System
- Clients, RentalContracts, RentalVehicles

### Phase 3: Tracking & Operations
- VehicleTimeLogs, GpsLogs, FuelLogs, MaintenanceRecords

### Phase 4: Financial
- Invoices, PayrollPeriods, PayrollRecords

### Phase 5: Audit & Analytics
- AuditLogs, FuelAnalysis

---

## Backup & Recovery

- **Backup Frequency**: Daily (with transactions)
- **Retention**: 30 days
- **Recovery Goal**: 4 hours RTO, minimal RPO
- **Test Recovery**: Monthly

---

## Future Extensions

1. **GPS Real-time Tracking**: WebSocket connection to GPS stream
2. **Mobile App Integration**: Driver mobile app sync
3. **IoT Sensors**: Temperature, humidity, door sensors
4. **Payment Gateway**: Invoice payment integration
5. **Reporting Dashboard**: Real-time analytics
6. **Machine Learning**: Predictive maintenance, route optimization
7. **Multi-currency**: Support multiple currencies per company
8. **Compliance**: GDPR, tax compliance tracking
