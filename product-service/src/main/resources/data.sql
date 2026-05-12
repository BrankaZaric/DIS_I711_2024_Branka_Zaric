-- Product Service Initial Data
-- Insert sample products

INSERT INTO products (sku, name, description, price, stock_quantity, category, image_url, active, created_at, updated_at)
VALUES
    ('LAPTOP-001', 'Gaming Laptop Pro', 'High-performance gaming laptop with RTX 4080', 1999.99, 50, 'Electronics', 'https://example.com/images/laptop1.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('LAPTOP-002', 'Business Laptop Ultra', 'Lightweight business laptop for professionals', 1299.99, 75, 'Electronics', 'https://example.com/images/laptop2.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PHONE-001', 'Smartphone Pro Max', 'Latest flagship smartphone with 5G', 1199.99, 100, 'Electronics', 'https://example.com/images/phone1.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PHONE-002', 'Budget Smartphone', 'Affordable smartphone with great features', 399.99, 150, 'Electronics', 'https://example.com/images/phone2.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('KEYBOARD-001', 'Mechanical Keyboard RGB', 'Premium mechanical keyboard with RGB lighting', 149.99, 200, 'Accessories', 'https://example.com/images/keyboard1.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MOUSE-001', 'Gaming Mouse Pro', 'High-precision gaming mouse with 16000 DPI', 79.99, 180, 'Accessories', 'https://example.com/images/mouse1.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MONITOR-001', '4K Gaming Monitor 27"', '4K UHD gaming monitor with 144Hz refresh rate', 599.99, 40, 'Electronics', 'https://example.com/images/monitor1.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HEADSET-001', 'Wireless Gaming Headset', 'Premium wireless headset with surround sound', 199.99, 120, 'Accessories', 'https://example.com/images/headset1.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('WEBCAM-001', 'HD Webcam 1080p', 'Full HD webcam for streaming and video calls', 89.99, 90, 'Accessories', 'https://example.com/images/webcam1.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TABLET-001', 'Pro Tablet 12"', 'Professional tablet with stylus support', 899.99, 60, 'Electronics', 'https://example.com/images/tablet1.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SPEAKER-001', 'Bluetooth Speaker', 'Portable bluetooth speaker with 20h battery', 59.99, 200, 'Accessories', 'https://example.com/images/speaker1.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CHARGER-001', 'Fast Charger 65W', 'Universal fast charger with USB-C', 39.99, 300, 'Accessories', 'https://example.com/images/charger1.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CABLE-001', 'USB-C Cable 2m', 'Durable USB-C cable for charging and data', 19.99, 500, 'Accessories', 'https://example.com/images/cable1.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CASE-001', 'Laptop Sleeve 15"', 'Protective laptop sleeve with extra pockets', 29.99, 150, 'Accessories', 'https://example.com/images/case1.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('STAND-001', 'Laptop Stand Adjustable', 'Ergonomic adjustable laptop stand', 49.99, 100, 'Accessories', 'https://example.com/images/stand1.jpg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (sku) DO NOTHING;
