-- Fix Earnings timestamps to use current date/time
-- This simulates earnings that happened today at different times

-- Update all earnings to today with staggered times
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-1 hours') WHERE earning_id = (SELECT MIN(earning_id) FROM Earnings);
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-2 hours') WHERE earning_id IN (SELECT earning_id FROM Earnings ORDER BY earning_id LIMIT 1 OFFSET 1);
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-3 hours') WHERE earning_id IN (SELECT earning_id FROM Earnings ORDER BY earning_id LIMIT 1 OFFSET 2);
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-4 hours') WHERE earning_id IN (SELECT earning_id FROM Earnings ORDER BY earning_id LIMIT 1 OFFSET 3);
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-5 hours') WHERE earning_id IN (SELECT earning_id FROM Earnings ORDER BY earning_id LIMIT 1 OFFSET 4);
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-6 hours') WHERE earning_id IN (SELECT earning_id FROM Earnings ORDER BY earning_id LIMIT 1 OFFSET 5);
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-12 hours') WHERE earning_id IN (SELECT earning_id FROM Earnings ORDER BY earning_id LIMIT 1 OFFSET 6);

-- Set remaining to various times within the last week
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-2 days', '-3 hours') WHERE earning_id IN (SELECT earning_id FROM Earnings ORDER BY earning_id LIMIT 1 OFFSET 7);
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-2 days', '-5 hours') WHERE earning_id IN (SELECT earning_id FROM Earnings ORDER BY earning_id LIMIT 1 OFFSET 8);
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-10 days', '-2 hours') WHERE earning_id IN (SELECT earning_id FROM Earnings ORDER BY earning_id LIMIT 1 OFFSET 9);
UPDATE Earnings SET created_at = datetime('now', 'localtime', '-35 days', '-1 hours') WHERE earning_id IN (SELECT earning_id FROM Earnings ORDER BY earning_id LIMIT 1 OFFSET 10);

-- Verify the update
SELECT 'Updated Earnings:' as message;
SELECT earning_id, order_id, amount, datetime(created_at, 'localtime') as created_at
FROM Earnings
ORDER BY created_at DESC;

