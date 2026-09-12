-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sep 12, 2026 at 06:37 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `virtuso`
--

-- --------------------------------------------------------

--
-- Table structure for table `student_login`
--

CREATE TABLE `student_login` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `student_login`
--

INSERT INTO `student_login` (`id`, `username`, `password`, `email`) VALUES
(1, 'alan', '1234', 'alan@gmail.com'),
(2, 'alen', '098', 'alen@gmail.com'),
(3, 'ajay', 'ajayy', 'ajay@ajay'),
(4, 'shaji', '12345678', 'asdfghjkl'),
(5, 'appi', 'appi', 'appiajay');

-- --------------------------------------------------------

--
-- Table structure for table `user_saved_passwords`
--

CREATE TABLE `user_saved_passwords` (
  `id` int(11) NOT NULL,
  `logged_in_username` varchar(100) NOT NULL,
  `app_name` varchar(150) NOT NULL,
  `account_username` varchar(150) NOT NULL,
  `generated_password` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_saved_passwords`
--

INSERT INTO `user_saved_passwords` (`id`, `logged_in_username`, `app_name`, `account_username`, `generated_password`, `created_at`) VALUES
(5, 'alan', 'Google', 'alan.wilson@gmail.com', 't8sqDBiqfZ]9:@', '2026-07-17 15:25:02'),
(8, 'shaji', 'konami', 'alenshaji098@gmail.com', '_aXcV>aU', '2026-07-18 04:05:31'),
(9, 'shaji', 'vivo', 'alenshaji098@gmail.com', 'Xx|r2z;l', '2026-07-18 04:08:35');

-- --------------------------------------------------------

--
-- Table structure for table `user_sort_history`
--

CREATE TABLE `user_sort_history` (
  `id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `algorithm_name` varchar(50) NOT NULL,
  `input_array` text NOT NULL,
  `sorted_array` text NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_sort_history`
--

INSERT INTO `user_sort_history` (`id`, `username`, `algorithm_name`, `input_array`, `sorted_array`, `timestamp`) VALUES
(9, 'alan', 'Selection Sort (Max)', '[45, 12, 89, 23, 7, 67, 34]', '[7, 12, 23, 34, 45, 67, 89]', '2026-07-17 13:20:19'),
(10, 'alan', 'Selection Sort (Min)', '[45, 12, 89, 23, 7, 67, 34]', '[7, 12, 23, 34, 45, 67, 89]', '2026-07-17 13:20:40'),
(11, 'alan', 'Selection Sort (Ascending)', '[45, 12, 89, 23, 7, 67, 34]', '[7, 12, 23, 34, 45, 67, 89]', '2026-07-17 13:24:27'),
(12, 'alan', 'Selection Sort (Descending)', '[45, 12, 89, 23, 7, 67, 34]', '[7, 12, 23, 34, 45, 67, 89]', '2026-07-17 13:24:44'),
(13, 'alan', 'Selection Sort (Descending)', '[45, 12, 89, 23, 7, 67, 34]', '[89, 67, 45, 34, 23, 12, 7]', '2026-07-17 13:27:29'),
(14, 'alan', 'Bubble Sort', '[45, 12, 89, 23, 7, 67, 34]', '[7, 12, 23, 34, 45, 67, 89]', '2026-07-17 15:07:37'),
(15, 'alan', 'Bubble Sort', '[45, 12, 89, 23, 7, 67, 34]', '[7, 12, 23, 34, 45, 67, 89]', '2026-07-17 15:25:31'),
(16, 'alan', 'Bubble Sort', '[45, 12, 89, 23, 7, 67, 34]', '[7, 12, 23, 34, 45, 67, 89]', '2026-07-18 03:43:33'),
(17, 'alan', 'Selection Sort (Descending)', '[3, 4, 6, 8, 9, 0]', '[9, 8, 6, 4, 3, 0]', '2026-07-18 03:52:21'),
(18, 'alan', 'Selection Sort (Ascending)', '[3, 4, 6, 8, 9, 0]', '[0, 3, 4, 6, 8, 9]', '2026-07-18 03:52:49'),
(19, 'alan', 'Bubble Sort', '[45, 12, 89, 23, 7, 67, 34]', '[7, 12, 23, 34, 45, 67, 89]', '2026-09-12 01:21:39');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `student_login`
--
ALTER TABLE `student_login`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `user_saved_passwords`
--
ALTER TABLE `user_saved_passwords`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `user_sort_history`
--
ALTER TABLE `user_sort_history`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `student_login`
--
ALTER TABLE `student_login`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `user_saved_passwords`
--
ALTER TABLE `user_saved_passwords`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `user_sort_history`
--
ALTER TABLE `user_sort_history`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
