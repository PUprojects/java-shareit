INSERT INTO users (id, name, email) VALUES (1, 'First user', 'first@mail.ru');
INSERT INTO users (id, name, email) VALUES (2, 'Second user', 'second@mail.ru');

INSERT INTO items (id, name, description, available, owner_id, request_id) VALUES
    (1, 'First item', 'First item desc', TRUE, 2, NULL);

INSERT INTO bookings (id, start_date, end_date, item_id, booker_id, status) VALUES
    (1, {ts '2025-01-17 18:47:52.69'}, {ts '2025-01-27 18:47:52.69'}, 1, 1, 'APPROVED');
INSERT INTO bookings (id, start_date, end_date, item_id, booker_id, status) VALUES
    (2, {ts '2025-09-17 18:47:52.69'}, {ts '2025-09-27 18:47:52.69'}, 1, 1, 'APPROVED');

INSERT INTO comments (id, text, item_id, author_id, created) VALUES
    (1, 'First comment text', 1, 1,{ts '2025-01-17 18:47:52.69'});
INSERT INTO comments (id, text, item_id, author_id, created) VALUES
    (2, 'Second comment text', 1, 1,{ts '2025-01-31 18:47:52.69'});
