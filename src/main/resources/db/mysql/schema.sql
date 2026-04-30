CREATE TABLE IF NOT EXISTS vets (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(30),
  last_name VARCHAR(30),
  INDEX(last_name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS specialties (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80),
  INDEX(name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS vet_specialties (
  vet_id INT(4) UNSIGNED NOT NULL,
  specialty_id INT(4) UNSIGNED NOT NULL,
  FOREIGN KEY (vet_id) REFERENCES vets(id),
  FOREIGN KEY (specialty_id) REFERENCES specialties(id),
  UNIQUE (vet_id,specialty_id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS types (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80),
  INDEX(name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS owners (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(30),
  last_name VARCHAR(30),
  address VARCHAR(255),
  city VARCHAR(80),
  telephone VARCHAR(20),
  INDEX(last_name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS pets (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(30),
  birth_date DATE,
  type_id INT(4) UNSIGNED NOT NULL,
  owner_id INT(4) UNSIGNED,
  INDEX(name),
  FOREIGN KEY (owner_id) REFERENCES owners(id),
  FOREIGN KEY (type_id) REFERENCES types(id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS visits (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  pet_id INT(4) UNSIGNED,
  visit_date DATE,
  description VARCHAR(255),
  FOREIGN KEY (pet_id) REFERENCES pets(id)
) engine=InnoDB;


CREATE TABLE IF NOT EXISTS users (
   id INT AUTO_INCREMENT PRIMARY KEY,
   first_name VARCHAR(50),
   last_name VARCHAR(50),
   nickname VARCHAR(50),
   nickname_is_flagged TINYINT DEFAULT 0,
   email VARCHAR(255) NOT NULL,
   public_email TINYINT DEFAULT 0,
   phone VARCHAR(255),
   public_phone TINYINT DEFAULT 0,
   preferred_language varchar(50) null,
   password_hash VARCHAR(255),
   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
   updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   deleted_at DATETIME,
   UNIQUE INDEX idx_users_email (email),
   INDEX idx_users_name (last_name, first_name)
);

CREATE TABLE IF NOT EXISTS roles (
   id INT AUTO_INCREMENT PRIMARY KEY,
   name VARCHAR(50) NOT NULL UNIQUE,
   description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS permissions (
         id INT AUTO_INCREMENT PRIMARY KEY,
         name VARCHAR(100) NOT NULL UNIQUE,
         description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_roles (
        user_id INT NOT NULL,
        role_id INT NOT NULL,
        PRIMARY KEY (user_id, role_id),
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS permission_role (
             permission_id INT NOT NULL,
             role_id INT NOT NULL,
             PRIMARY KEY (permission_id, role_id),
             FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
             FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS schools (
     id INT AUTO_INCREMENT PRIMARY KEY,
     name VARCHAR(255) NOT NULL,
     domain VARCHAR(255) NOT NULL,
     status_id ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     deleted_at DATETIME DEFAULT NULL,
     UNIQUE INDEX idx_schools_domain (domain)
);

CREATE TABLE IF NOT EXISTS locations (
       id INT AUTO_INCREMENT PRIMARY KEY,
       school_id INT NOT NULL,
       parent_location_id INT NULL,
       name VARCHAR(255) NOT NULL,
       description TEXT,
       address VARCHAR(255),
       latitude DECIMAL(8,4),
       longitude DECIMAL(8,4),
       status_id ENUM('DRAFT', 'ACTIVE', 'CLOSED', 'COMING_SOON') DEFAULT 'ACTIVE',
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       deleted_at DATETIME DEFAULT NULL,
       CONSTRAINT fk_locations_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
       CONSTRAINT fk_locations_parent FOREIGN KEY (parent_location_id) REFERENCES locations(id) ON DELETE SET NULL,
       UNIQUE KEY uk_school_location (school_id, name)
);


CREATE TABLE IF NOT EXISTS recipes
(
  id bigint unsigned auto_increment primary key,
  recipe_ingredients varchar(255) null,
  instructions       varchar(255) not null,
  type               varchar(50)  null,
  category           varchar(50)  null,
  dietary_preference varchar(50)  null,
  internal_notes     varchar(255) not null,
  constraint id unique (id),
  constraint internal_notes unique (internal_notes)
);


CREATE TABLE IF NOT EXISTS leagues (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     school_id INT NOT NULL,
                                     location_id INT,
                                     user_id INT,
                                     name VARCHAR(255) NOT NULL,
                                     description TEXT,
                                     registration_start DATETIME,
                                     registration_end DATETIME,
                                     league_start DATETIME,
                                     league_end DATETIME,
                                     is_public TINYINT DEFAULT 1,
                                     type ENUM('MALE', 'FEMALE', 'COED') NOT NULL,
                                     capacity INT,
                                     capacity_type ENUM('TEAM', 'INDIVIDUAL') NOT NULL,
                                     fee DECIMAL(6,2),
                                     status_id ENUM('DRAFT', 'ACTIVE', 'INACTIVE', 'POSTPONED', 'CANCELLED', 'PAST') DEFAULT 'DRAFT',
                                     copied_from_id INT NULL,
                                     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     deleted_at DATETIME,

                                     CONSTRAINT fk_leagues_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
                                     CONSTRAINT fk_leagues_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE SET NULL,
                                     CONSTRAINT fk_leagues_manager FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                                     CONSTRAINT fk_leagues_copied_from FOREIGN KEY (copied_from_id) REFERENCES leagues(id) ON DELETE SET NULL,

                                     INDEX idx_leagues_school_status (school_id, status_id),

  -- ✅ Prevent duplicate leagues per school
                                     UNIQUE KEY uq_leagues_school_name (school_id, name)
);
