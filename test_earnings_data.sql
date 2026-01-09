-- Test earnings data and verify queries

-- Show all earnings with formatted dates
.headers on
.mode column

SELECT '=== ALL EARNINGS ===' as info;
SELECT earning_id, delivery_person_id, order_id, amount,
       datetime(created_at, 'localtime') as local_time,
       DATE(created_at, 'localtime') as local_date
FROM Earnings
ORDER BY created_at DESC;

SELECT '';
SELECT '=== TODAY CALCULATION ===' as info;
SELECT DATE('now', 'localtime') as today_date;
SELECT COUNT(*) as today_count, COALESCE(SUM(amount), 0) as today_total
FROM Earnings
WHERE DATE(created_at, 'localtime') = DATE('now', 'localtime');

SELECT '';
SELECT '=== WEEKLY CALCULATION (Last 7 Days) ===' as info;
SELECT DATE('now', 'localtime', '-6 days') as week_start,
       DATE('now', 'localtime') as week_end;
SELECT COUNT(*) as week_count, COALESCE(SUM(amount), 0) as week_total
FROM Earnings
WHERE DATE(created_at, 'localtime') >= DATE('now', 'localtime', '-6 days');

SELECT '';
SELECT '=== MONTHLY CALCULATION (Last 30 Days) ===' as info;
SELECT DATE('now', 'localtime', '-29 days') as month_start,
       DATE('now', 'localtime') as month_end;
SELECT COUNT(*) as month_count, COALESCE(SUM(amount), 0) as month_total
FROM Earnings
WHERE DATE(created_at, 'localtime') >= DATE('now', 'localtime', '-29 days');

SELECT '';
SELECT '=== DAILY BREAKDOWN (Last 7 Days) ===' as info;
SELECT DATE(created_at, 'localtime') as date,
       COUNT(*) as count,
       COALESCE(SUM(amount), 0) as total
FROM Earnings
WHERE DATE(created_at, 'localtime') >= DATE('now', 'localtime', '-6 days')
  AND DATE(created_at, 'localtime') <= DATE('now', 'localtime')
GROUP BY DATE(created_at, 'localtime')
ORDER BY date ASC;

