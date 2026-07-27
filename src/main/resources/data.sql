-- FIXED: correct table name to match the entity
INSERT INTO coupon (id, code, discount, min_basket_value)
VALUES (NEXT VALUE FOR coupon_seq, 'TEST1', 10.00, 50.00),
       (NEXT VALUE FOR coupon_seq, 'TEST2', 15.00, 100.00),
       (NEXT VALUE FOR coupon_seq, 'TEST3', 20.00, 200.00);