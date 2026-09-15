-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sep 15, 2026 at 03:01 PM
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
(6, 'aleng', '789', 'aleng@gmail.com');

-- --------------------------------------------------------

--
-- Table structure for table `user_quiz_history`
--

CREATE TABLE `user_quiz_history` (
  `id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `actual_algorithm` varchar(100) NOT NULL,
  `user_guess` varchar(100) NOT NULL,
  `is_correct` tinyint(1) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_quiz_history`
--

INSERT INTO `user_quiz_history` (`id`, `username`, `actual_algorithm`, `user_guess`, `is_correct`, `timestamp`) VALUES
(20, 'alan', 'Bucket Sort', 'Bucket Sort', 1, '2026-09-15 06:02:29'),
(21, 'alan', 'Selection Sort', 'Bubble Sort', 0, '2026-09-15 06:03:01'),
(22, 'alan', 'Heap Sort', 'Heap Sort', 1, '2026-09-15 07:39:20');

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
(54, 'alan', 'Bubble Sort', '[45, 12, 89, 23, 7, 67, 34]', '[7, 12, 23, 34, 45, 67, 89]', '2026-09-15 06:01:50'),
(55, 'alan', 'Bubble Sort', '[45, 12, 89, 23, 7, 67, 34]', '[7, 12, 23, 34, 45, 67, 89]', '2026-09-15 07:38:37');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `student_login`
--
ALTER TABLE `student_login`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `user_quiz_history`
--
ALTER TABLE `user_quiz_history`
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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `user_quiz_history`
--
ALTER TABLE `user_quiz_history`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT for table `user_sort_history`
--
ALTER TABLE `user_sort_history`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=56;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
