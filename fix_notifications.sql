-- Drop existing Notifications table if it has wrong structure
DROP TABLE IF EXISTS Notifications;

-- Create Notifications table with correct structure
CREATE TABLE IF NOT EXISTS Notifications (
    notification_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    type TEXT DEFAULT 'INFO',
    order_id INTEGER,
    is_read INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id)
);

-- Insert test notifications (using user_id 3 from earlier)
INSERT INTO Notifications (user_id, title, message, type, order_id, is_read, created_at)
VALUES
(3, 'Welcome!', 'Welcome to Quick Commerce Delivery System!', 'INFO', NULL, 0, datetime('now')),
(3, 'New Order Available', 'A new order is available for delivery in your area', 'ORDER_UPDATE', NULL, 0, datetime('now')),
(3, 'Delivery Update', 'Your delivery is on the way', 'DELIVERY_UPDATE', NULL, 0, datetime('now')),
(3, 'Earnings Added', 'You earned $5.50 from your last delivery', 'EARNING', NULL, 0, datetime('now')),
(3, 'Order Completed', 'Order delivered successfully! Great job!', 'SUCCESS', NULL, 1, datetime('now'));

-- Verify the notifications were created
SELECT COUNT(*) as total_notifications FROM Notifications;
SELECT * FROM Notifications ORDER BY created_at DESC;

