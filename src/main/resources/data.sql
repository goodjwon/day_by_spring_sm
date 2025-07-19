-- 도서 데이터
INSERT INTO book (id, title, author, isbn, price, available, created_date)
VALUES (6, 'Clean Code', 'Robert C. Martin', '978-0132350884', 45000, true, CURRENT_TIMESTAMP),
       (2, 'Spring in Action', 'Craig Walls', '978-1617294945', 52000, true, CURRENT_TIMESTAMP),
       (3, 'Effective Java', 'Joshua Bloch', '978-0134685991', 48000, true, CURRENT_TIMESTAMP),
       (4, 'Design Patterns', 'Gang of Four', '978-0201633612', 55000, true, CURRENT_TIMESTAMP),
       (5, 'Refactoring', 'Martin Fowler', '978-0134757599', 50000, false, CURRENT_TIMESTAMP),
       (7, '삭제용 책', '천사무엘', '999-0134757599', 50000, false, CURRENT_TIMESTAMP);

-- 회원 데이터
INSERT INTO member (id, name, email, membership_type, join_date)
VALUES (1, '김개발', 'kim.dev@example.com', 'REGULAR', CURRENT_TIMESTAMP),
       (2, '박스프링', 'park.spring@example.com', 'PREMIUM', CURRENT_TIMESTAMP),
       (3, '이자바', 'lee.java@example.com', 'REGULAR', CURRENT_TIMESTAMP),
       (4, '정코딩', 'jung.coding@example.com', 'SUSPENDED', CURRENT_TIMESTAMP);

-- 대출 데이터 (진행중)
INSERT INTO loan (id, member_id, book_id, loan_date, due_date)
VALUES (1, 1, 6, DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', 9, CURRENT_TIMESTAMP)),
       (2, 2, 2, DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', 18, CURRENT_TIMESTAMP)),
       (3, 3, 3, DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', 4, CURRENT_TIMESTAMP));

-- 대출 데이터 (반납 완료)
INSERT INTO loan (id, member_id, book_id, loan_date, due_date, return_date)
VALUES (4, 1, 4, DATEADD('DAY', -30, CURRENT_TIMESTAMP), DATEADD('DAY', -16, CURRENT_TIMESTAMP),
        DATEADD('DAY', -15, CURRENT_TIMESTAMP)),
       (5, 2, 5, DATEADD('DAY', -25, CURRENT_TIMESTAMP), DATEADD('DAY', -4, CURRENT_TIMESTAMP),
        DATEADD('DAY', -2, CURRENT_TIMESTAMP));
