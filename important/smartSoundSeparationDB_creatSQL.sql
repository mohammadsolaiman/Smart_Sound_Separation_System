-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema SmartSoundSeparationDB
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema SmartSoundSeparationDB
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `SmartSoundSeparationDB` DEFAULT CHARACTER SET utf8 ;
USE `SmartSoundSeparationDB` ;

-- -----------------------------------------------------
-- Table `SmartSoundSeparationDB`.`sound`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `SmartSoundSeparationDB`.`sound` ;

CREATE TABLE IF NOT EXISTS `SmartSoundSeparationDB`.`sound` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `dir` NVARCHAR(500) NOT NULL,
  `name` NVARCHAR(100) NOT NULL,
  `type` NVARCHAR(45) NOT NULL,
  `description` NVARCHAR(500) NULL,
  `is_learned` TINYINT(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `SmartSoundSeparationDB`.`stft_wlen`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `SmartSoundSeparationDB`.`stft_wlen` ;

CREATE TABLE IF NOT EXISTS `SmartSoundSeparationDB`.`stft_wlen` (
  `stft_wlen` INT NOT NULL,
  PRIMARY KEY (`stft_wlen`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `SmartSoundSeparationDB`.`basis`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `SmartSoundSeparationDB`.`basis` ;

CREATE TABLE IF NOT EXISTS `SmartSoundSeparationDB`.`basis` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `k` INT NOT NULL,
  `s_id` INT NOT NULL,
  `data` MEDIUMTEXT NOT NULL,
  `stft_win_len` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `sound_id_idx` (`s_id` ASC),
  INDEX `stft_wlen_idx` (`stft_win_len` ASC),
  CONSTRAINT `sound_id`
    FOREIGN KEY (`s_id`)
    REFERENCES `SmartSoundSeparationDB`.`sound` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `stft_wlen`
    FOREIGN KEY (`stft_win_len`)
    REFERENCES `SmartSoundSeparationDB`.`stft_wlen` (`stft_wlen`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `SmartSoundSeparationDB`.`separated`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `SmartSoundSeparationDB`.`separated` ;

CREATE TABLE IF NOT EXISTS `SmartSoundSeparationDB`.`separated` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `sound_id` INT NOT NULL,
  `stft_wlen` INT NOT NULL,
  `mixed_id` INT NOT NULL,
  `spectrogram_path` NVARCHAR(250) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `sound_id_idx` (`sound_id` ASC),
  INDEX `mixed_id_idx` (`mixed_id` ASC),
  INDEX `stft_wlen_idx` (`stft_wlen` ASC),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  CONSTRAINT `sep_sound_id`
    FOREIGN KEY (`sound_id`)
    REFERENCES `SmartSoundSeparationDB`.`sound` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `mixed_source_id`
    FOREIGN KEY (`mixed_id`)
    REFERENCES `SmartSoundSeparationDB`.`sound` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sep_stft_wlen`
    FOREIGN KEY (`stft_wlen`)
    REFERENCES `SmartSoundSeparationDB`.`stft_wlen` (`stft_wlen`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
