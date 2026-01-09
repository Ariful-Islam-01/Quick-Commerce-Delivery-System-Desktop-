-- Complete fix for Earnings timestamps
-- This will update ALL earnings to have proper current timestamps

.headers on
.mode column

-- Show current state
SELECT 'BEFORE UPDATE:' as status;
SELECT earning_id, delivery_person_id, amount, created_at FROM Earnings LIMIT 5;

-- Update all earnings to current timestamp with different offsets
-- This simulates earnings from today, yesterday, and past days

-- Get all earning IDs
-- Update them with staggered timestamps

-- Today's earnings (various hours ago)
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-1 hours') WHERE earning_id = 1;
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-2 hours') WHERE earning_id = 2;
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-4 hours') WHERE earning_id = 3;
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-6 hours') WHERE earning_id = 4;

-- This week (within last 7 days)
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-1 days') WHERE earning_id = 5;
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-3 days') WHERE earning_id = 6;
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-5 days') WHERE earning_id = 7;

-- This month (within last 30 days)
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-10 days') WHERE earning_id = 8;
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-15 days') WHERE earning_id = 9;
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-20 days') WHERE earning_id = 10;

-- Older (beyond 30 days)
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-35 days') WHERE earning_id = 11;
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-40 days') WHERE earning_id = 12;
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-50 days') WHERE earning_id = 13;

-- Any remaining ones to today
UPDATE Earnings SET created_at = datetime('now', 'localtime') WHERE earning_id > 13;

-- Show after update
SELECT '';
SELECT 'AFTER UPDATE:' as status;
SELECT earning_id, delivery_person_id, amount,
       datetime(created_at, 'localtime') as local_time,
       DATE(created_at, 'localtime') as local_date
FROM Earnings
ORDER BY created_at DESC;

-- Test the queries
SELECT '';
SELECT 'TODAY TEST:' as test;
SELECT DATE('now', 'localtime') as today_date;
SELECT COUNT(*) as count, SUM(amount) as total
FROM Earnings
WHERE delivery_person_id = 4
  AND DATE(created_at, 'localtime') = DATE('now', 'localtime');

SELECT '';
SELECT 'WEEK TEST (Last 7 Days):' as test;
SELECT COUNT(*) as count, SUM(amount) as total
FROM Earnings
WHERE delivery_person_id = 4
  AND DATE(created_at, 'localtime') >= DATE('now', 'localtime', '-6 days');

SELECT '';
SELECT 'MONTH TEST (Last 30 Days):' as test;
SELECT COUNT(*) as count, SUM(amount) as total
FROM Earnings
WHERE delivery_person_id = 4
  AND DATE(created_at, 'localtime') >= DATE('now', 'localtime', '-29 days');

