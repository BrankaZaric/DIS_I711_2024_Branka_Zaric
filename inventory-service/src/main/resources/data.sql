-- Inventory Service Initial Data
-- Link inventory records to products

INSERT INTO inventory (product_id, product_sku, quantity, reserved_quantity, available_quantity, warehouse_location, last_updated)
VALUES
    (1, 'LAPTOP-001', 50, 0, 50, 'Warehouse A - Shelf 1A', CURRENT_TIMESTAMP),
    (2, 'LAPTOP-002', 75, 0, 75, 'Warehouse A - Shelf 1B', CURRENT_TIMESTAMP),
    (3, 'PHONE-001', 100, 0, 100, 'Warehouse B - Shelf 2A', CURRENT_TIMESTAMP),
    (4, 'PHONE-002', 150, 0, 150, 'Warehouse B - Shelf 2B', CURRENT_TIMESTAMP),
    (5, 'KEYBOARD-001', 200, 0, 200, 'Warehouse C - Shelf 3A', CURRENT_TIMESTAMP),
    (6, 'MOUSE-001', 180, 0, 180, 'Warehouse C - Shelf 3B', CURRENT_TIMESTAMP),
    (7, 'MONITOR-001', 40, 0, 40, 'Warehouse A - Shelf 1C', CURRENT_TIMESTAMP),
    (8, 'HEADSET-001', 120, 0, 120, 'Warehouse C - Shelf 3C', CURRENT_TIMESTAMP),
    (9, 'WEBCAM-001', 90, 0, 90, 'Warehouse C - Shelf 3D', CURRENT_TIMESTAMP),
    (10, 'TABLET-001', 60, 0, 60, 'Warehouse B - Shelf 2C', CURRENT_TIMESTAMP),
    (11, 'SPEAKER-001', 200, 0, 200, 'Warehouse C - Shelf 4A', CURRENT_TIMESTAMP),
    (12, 'CHARGER-001', 300, 0, 300, 'Warehouse C - Shelf 4B', CURRENT_TIMESTAMP),
    (13, 'CABLE-001', 500, 0, 500, 'Warehouse C - Shelf 4C', CURRENT_TIMESTAMP),
    (14, 'CASE-001', 150, 0, 150, 'Warehouse C - Shelf 4D', CURRENT_TIMESTAMP),
    (15, 'STAND-001', 100, 0, 100, 'Warehouse C - Shelf 4E', CURRENT_TIMESTAMP)
ON CONFLICT (product_sku) DO NOTHING;