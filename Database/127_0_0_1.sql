-- phpMyAdmin SQL Dump
-- version 4.8.2
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th12 04, 2018 lúc 04:29 AM
-- Phiên bản máy phục vụ: 10.1.34-MariaDB
-- Phiên bản PHP: 7.2.8

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `humanmanager`
--
CREATE DATABASE IF NOT EXISTS `humanmanager` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `humanmanager`;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `account`
--

CREATE TABLE `account` (
  `username` char(50) NOT NULL,
  `password` char(50) NOT NULL,
  `positionID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Đang đổ dữ liệu cho bảng `account`
--

INSERT INTO `account` (`username`, `password`, `positionID`) VALUES
('goodbye', 'a', 3),
('sale', 'sale', 2);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `dayoff`
--

CREATE TABLE `dayoff` (
  `ID` int(11) NOT NULL,
  `DayOff` datetime NOT NULL,
  `EmployeeID` int(11) NOT NULL,
  `status` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Đang đổ dữ liệu cho bảng `dayoff`
--

INSERT INTO `dayoff` (`ID`, `DayOff`, `EmployeeID`, `status`) VALUES
(5, '2018-11-27 00:00:00', 1, 2),
(6, '2018-11-28 00:00:00', 1, 3),
(12, '2018-12-02 00:00:00', 2, 2),
(13, '2018-12-02 00:00:00', 1, 1),
(14, '2018-12-06 00:00:00', 2, 1),
(15, '2018-12-05 00:00:00', 1, 1),
(16, '2018-12-06 00:00:00', 2, 1),
(17, '2018-12-08 00:00:00', 2, 1),
(18, '2018-12-06 00:00:00', 1, 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `emlpoyee`
--

CREATE TABLE `emlpoyee` (
  `ID` int(11) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `Birth` datetime NOT NULL,
  `Address` varchar(100) DEFAULT NULL,
  `Gender` varchar(5) DEFAULT NULL,
  `Country` varchar(100) DEFAULT NULL,
  `PositionID` int(11) NOT NULL,
  `username` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Đang đổ dữ liệu cho bảng `emlpoyee`
--

INSERT INTO `emlpoyee` (`ID`, `Name`, `Birth`, `Address`, `Gender`, `Country`, `PositionID`, `username`) VALUES
(1, 'superman', '1996-11-14 00:00:00', 'adasdasdasdasd', 'Nam', 'Viet nam', 2, 'sale'),
(2, 'goodbye', '1970-01-01 00:00:00', 'asdasd', 'Male', 'asdasdas', 3, 'goodbye');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `file`
--

CREATE TABLE `file` (
  `ID` int(11) NOT NULL,
  `filename` varchar(50) NOT NULL,
  `path` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Đang đổ dữ liệu cho bảng `file`
--

INSERT INTO `file` (`ID`, `filename`, `path`) VALUES
(1, 'AAA.txt', 'src//File//AAA.txt'),
(2, 'tkb.jpg', 'src//File//tkb.jpg'),
(3, 'draw.docx', 'src//File//draw.docx'),
(4, 'mvc.pptx', 'src//File//mvc.pptx');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `positiion`
--

CREATE TABLE `positiion` (
  `ID` int(11) NOT NULL,
  `Name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Đang đổ dữ liệu cho bảng `positiion`
--

INSERT INTO `positiion` (`ID`, `Name`) VALUES
(1, 'admin'),
(2, 'sale'),
(3, 'business'),
(4, 'superman');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `salaryforpositionbyday`
--

CREATE TABLE `salaryforpositionbyday` (
  `ID` int(11) NOT NULL,
  `salary` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Đang đổ dữ liệu cho bảng `salaryforpositionbyday`
--

INSERT INTO `salaryforpositionbyday` (`ID`, `salary`) VALUES
(2, 200000),
(3, 150000),
(4, 100000);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `status`
--

CREATE TABLE `status` (
  `ID` int(11) NOT NULL,
  `Name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Đang đổ dữ liệu cho bảng `status`
--

INSERT INTO `status` (`ID`, `Name`) VALUES
(1, 'wait'),
(2, 'done'),
(3, 'remove');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `timekeeping`
--

CREATE TABLE `timekeeping` (
  `ID` int(11) NOT NULL,
  `TimeIn` datetime NOT NULL,
  `TimeOut` datetime DEFAULT NULL,
  `Late` int(11) NOT NULL DEFAULT '0',
  `Extra` int(11) NOT NULL DEFAULT '0',
  `EmployeeID` int(11) NOT NULL,
  `status` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Đang đổ dữ liệu cho bảng `timekeeping`
--

INSERT INTO `timekeeping` (`ID`, `TimeIn`, `TimeOut`, `Late`, `Extra`, `EmployeeID`, `status`) VALUES
(2, '2018-12-01 07:08:00', '2018-12-01 07:19:00', 0, 0, 1, 2),
(3, '2018-12-01 11:20:00', NULL, 1, 0, 1, 1),
(11, '2018-12-03 09:31:00', '2018-12-03 10:37:00', 1, 0, 1, 2),
(12, '2018-12-04 07:50:00', '2018-12-04 08:37:00', 1, 0, 1, 2),
(13, '2018-12-04 08:29:00', '2018-12-04 08:33:00', 1, 0, 2, 2);

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `account`
--
ALTER TABLE `account`
  ADD PRIMARY KEY (`username`),
  ADD KEY `FK_A_P` (`positionID`);

--
-- Chỉ mục cho bảng `dayoff`
--
ALTER TABLE `dayoff`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `FK_DO_E` (`EmployeeID`),
  ADD KEY `FK_S_DO` (`status`);

--
-- Chỉ mục cho bảng `emlpoyee`
--
ALTER TABLE `emlpoyee`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `FK_E_P` (`PositionID`),
  ADD KEY `FK_E_A` (`username`);

--
-- Chỉ mục cho bảng `file`
--
ALTER TABLE `file`
  ADD PRIMARY KEY (`ID`);

--
-- Chỉ mục cho bảng `positiion`
--
ALTER TABLE `positiion`
  ADD PRIMARY KEY (`ID`);

--
-- Chỉ mục cho bảng `salaryforpositionbyday`
--
ALTER TABLE `salaryforpositionbyday`
  ADD PRIMARY KEY (`ID`);

--
-- Chỉ mục cho bảng `status`
--
ALTER TABLE `status`
  ADD PRIMARY KEY (`ID`);

--
-- Chỉ mục cho bảng `timekeeping`
--
ALTER TABLE `timekeeping`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `FK_TK_E` (`EmployeeID`),
  ADD KEY `FK_TK_S` (`status`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `dayoff`
--
ALTER TABLE `dayoff`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT cho bảng `file`
--
ALTER TABLE `file`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT cho bảng `timekeeping`
--
ALTER TABLE `timekeeping`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `account`
--
ALTER TABLE `account`
  ADD CONSTRAINT `FK_A_P` FOREIGN KEY (`positionID`) REFERENCES `positiion` (`ID`);

--
-- Các ràng buộc cho bảng `dayoff`
--
ALTER TABLE `dayoff`
  ADD CONSTRAINT `FK_DO_E` FOREIGN KEY (`EmployeeID`) REFERENCES `emlpoyee` (`ID`),
  ADD CONSTRAINT `FK_S_DO` FOREIGN KEY (`status`) REFERENCES `status` (`ID`);

--
-- Các ràng buộc cho bảng `emlpoyee`
--
ALTER TABLE `emlpoyee`
  ADD CONSTRAINT `FK_E_A` FOREIGN KEY (`username`) REFERENCES `account` (`username`),
  ADD CONSTRAINT `FK_E_P` FOREIGN KEY (`PositionID`) REFERENCES `positiion` (`ID`);

--
-- Các ràng buộc cho bảng `salaryforpositionbyday`
--
ALTER TABLE `salaryforpositionbyday`
  ADD CONSTRAINT `FK_SFPBD_P` FOREIGN KEY (`ID`) REFERENCES `positiion` (`ID`);

--
-- Các ràng buộc cho bảng `timekeeping`
--
ALTER TABLE `timekeeping`
  ADD CONSTRAINT `FK_TK_E` FOREIGN KEY (`EmployeeID`) REFERENCES `emlpoyee` (`ID`),
  ADD CONSTRAINT `FK_TK_S` FOREIGN KEY (`status`) REFERENCES `status` (`ID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
