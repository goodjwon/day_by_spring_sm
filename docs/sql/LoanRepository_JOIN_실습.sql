-- ========================================
-- LoanRepository JOIN 실습 SQL 스크립트
-- ========================================
-- 작성일: 2025-11-22
-- 용도: LoanRepository의 JOIN 쿼리를 실제 SQL로 실습
-- 사용법:
--   1. 테이블 생성 (DDL)
--   2. 샘플 데이터 삽입 (INSERT)
--   3. 각 쿼리 실행 및 결과 확인
-- ========================================

-- ========================================
-- 1. 테이블 생성 (DDL)
-- ========================================

-- Member 테이블
CREATE TABLE IF NOT EXISTS member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    membership_type VARCHAR(20) DEFAULT 'REGULAR',
    join_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Book 테이블
CREATE TABLE IF NOT EXISTS book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(17) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL,
    available BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_date TIMESTAMP NULL
);

-- Book 인덱스
CREATE INDEX idx_book_isbn ON book(isbn);
CREATE INDEX idx_book_title ON book(title);
CREATE INDEX idx_book_author ON book(author);

-- Loan 테이블
CREATE TABLE IF NOT EXISTS loan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    loan_date TIMESTAMP NOT NULL,
    due_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP NULL,
    overdue_fee DECIMAL(10, 2) DEFAULT 0.00,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NULL,
    FOREIGN KEY (member_id) REFERENCES member(id),
    FOREIGN KEY (book_id) REFERENCES book(id)
);

-- ========================================
-- 2. 샘플 데이터 삽입
-- ========================================

-- 기존 데이터 삭제 (실습용)
DELETE FROM loan;
DELETE FROM book;
DELETE FROM member;

-- Member 데이터
INSERT INTO member (id, name, email, membership_type, join_date) VALUES
(1, '김철수', 'kim@test.com', 'REGULAR', '2024-01-01 10:00:00'),
(2, '이영희', 'lee@test.com', 'PREMIUM', '2024-01-15 11:00:00'),
(3, '박민수', 'park@test.com', 'REGULAR', '2024-02-01 09:00:00'),
(4, '홍길동', 'hong@test.com', 'REGULAR', '2024-02-10 14:00:00'),
(5, '강감찬', 'kang@test.com', 'PREMIUM', '2024-03-01 16:00:00'),
(6, '독서왕', 'reader@test.com', 'PREMIUM', '2024-03-15 10:30:00'),
(7, '연체자1', 'overdue1@test.com', 'REGULAR', '2024-04-01 09:00:00'),
(8, '연체자2', 'overdue2@test.com', 'REGULAR', '2024-04-05 10:00:00');

-- Book 데이터
INSERT INTO book (id, title, author, isbn, price, available, created_date) VALUES
(1, '자바의 정석', '남궁성', '9788994492032', 30000.00, TRUE, '2024-01-01 00:00:00'),
(2, '스프링 부트 핵심 가이드', '장정우', '9791158392703', 28000.00, TRUE, '2024-01-01 00:00:00'),
(3, '클린 코드', '로버트 마틴', '9788966260959', 33000.00, TRUE, '2024-01-01 00:00:00'),
(4, '이펙티브 자바', '조슈아 블로크', '9788966262281', 36000.00, TRUE, '2024-01-01 00:00:00'),
(5, '토비의 스프링', '이일민', '9788960773431', 40000.00, TRUE, '2024-01-01 00:00:00'),
(6, '인기도서', '저자', '9780000000001', 25000.00, TRUE, '2024-01-01 00:00:00'),
(7, '대여중 도서1', '저자1', '9780000000002', 20000.00, FALSE, '2024-01-01 00:00:00'),
(8, '대여중 도서2', '저자2', '9780000000003', 22000.00, FALSE, '2024-01-01 00:00:00'),
(9, '반납완료 도서', '저자3', '9780000000004', 18000.00, TRUE, '2024-01-01 00:00:00'),
(10, '연체도서1', '저자', '9780000000005', 21000.00, FALSE, '2024-01-01 00:00:00'),
(11, '연체도서2', '저자', '9780000000006', 23000.00, FALSE, '2024-01-01 00:00:00');

-- Loan 데이터
-- 1. 김철수의 대여 중인 도서
INSERT INTO loan (id, member_id, book_id, status, loan_date, due_date, return_date, overdue_fee, created_date) VALUES
(1, 1, 1, 'ACTIVE', '2024-11-08 10:00:00', '2024-11-22 10:00:00', NULL, 0.00, '2024-11-08 10:00:00');

-- 2. 이영희의 대여 중인 도서
INSERT INTO loan (id, member_id, book_id, status, loan_date, due_date, return_date, overdue_fee, created_date) VALUES
(2, 2, 2, 'ACTIVE', '2024-11-10 11:00:00', '2024-11-24 11:00:00', NULL, 0.00, '2024-11-10 11:00:00');

-- 3. 박민수의 연체 중인 도서
INSERT INTO loan (id, member_id, book_id, status, loan_date, due_date, return_date, overdue_fee, created_date) VALUES
(3, 3, 3, 'OVERDUE', '2024-11-02 09:00:00', '2024-11-17 09:00:00', NULL, 5000.00, '2024-11-02 09:00:00');

-- 4. 홍길동의 대여 중인 도서
INSERT INTO loan (id, member_id, book_id, status, loan_date, due_date, return_date, overdue_fee, created_date) VALUES
(4, 4, 4, 'ACTIVE', '2024-11-12 14:00:00', '2024-11-26 14:00:00', NULL, 0.00, '2024-11-12 14:00:00');

-- 5. 강감찬의 대여 중인 도서
INSERT INTO loan (id, member_id, book_id, status, loan_date, due_date, return_date, overdue_fee, created_date) VALUES
(5, 5, 5, 'ACTIVE', '2024-11-13 16:00:00', '2024-11-27 16:00:00', NULL, 0.00, '2024-11-13 16:00:00');

-- 6. 인기도서 대여 내역 (반납 완료)
INSERT INTO loan (id, member_id, book_id, status, loan_date, due_date, return_date, overdue_fee, created_date) VALUES
(6, 1, 6, 'RETURNED', '2024-10-15 10:00:00', '2024-10-29 10:00:00', '2024-10-28 15:00:00', 0.00, '2024-10-15 10:00:00');

-- 7. 인기도서 대여 내역 (현재 대여 중)
INSERT INTO loan (id, member_id, book_id, status, loan_date, due_date, return_date, overdue_fee, created_date) VALUES
(7, 2, 6, 'ACTIVE', '2024-11-05 11:00:00', '2024-11-19 11:00:00', NULL, 0.00, '2024-11-05 11:00:00');

-- 8. 독서왕의 대여 중인 도서들
INSERT INTO loan (id, member_id, book_id, status, loan_date, due_date, return_date, overdue_fee, created_date) VALUES
(8, 6, 7, 'ACTIVE', '2024-11-17 10:30:00', '2024-12-01 10:30:00', NULL, 0.00, '2024-11-17 10:30:00'),
(9, 6, 8, 'ACTIVE', '2024-11-19 10:30:00', '2024-12-03 10:30:00', NULL, 0.00, '2024-11-19 10:30:00');

-- 9. 독서왕의 반납 완료 도서
INSERT INTO loan (id, member_id, book_id, status, loan_date, due_date, return_date, overdue_fee, created_date) VALUES
(10, 6, 9, 'RETURNED', '2024-11-02 10:30:00', '2024-11-16 10:30:00', '2024-11-17 09:00:00', 1000.00, '2024-11-02 10:30:00');

-- 10. 연체자들의 연체 도서
INSERT INTO loan (id, member_id, book_id, status, loan_date, due_date, return_date, overdue_fee, created_date) VALUES
(11, 7, 10, 'OVERDUE', '2024-10-28 09:00:00', '2024-11-11 09:00:00', NULL, 11000.00, '2024-10-28 09:00:00'),
(12, 8, 11, 'OVERDUE', '2024-11-03 10:00:00', '2024-11-16 10:00:00', NULL, 6000.00, '2024-11-03 10:00:00');

-- ========================================
-- 3. 데이터 확인
-- ========================================

-- 전체 데이터 확인
SELECT '=== Members ===' AS '';
SELECT * FROM member;

SELECT '=== Books ===' AS '';
SELECT * FROM book;

SELECT '=== Loans ===' AS '';
SELECT * FROM loan;

-- ========================================
-- 4. JOIN 쿼리 실습
-- ========================================

-- ==========================================
-- 4.1 기본 조회 쿼리 (JOIN 없음)
-- ==========================================

-- Query 1: findByMemberId - 특정 회원의 모든 대여 내역
-- JPQL: SELECT l FROM Loan l WHERE l.member.id = :memberId
SELECT '=== Query 1: findByMemberId ===' AS '';
SELECT * FROM loan
WHERE member_id = 1;
-- 결과: 김철수(ID=1)의 대여 내역 2건


-- Query 2: findByBookId - 특정 도서의 모든 대여 내역
-- JPQL: SELECT l FROM Loan l WHERE l.book.id = :bookId
SELECT '=== Query 2: findByBookId ===' AS '';
SELECT * FROM loan
WHERE book_id = 6;
-- 결과: 인기도서(ID=6)의 대여 내역 2건


-- Query 3: findByMemberIdAndReturnDateIsNull - 특정 회원의 미반납 도서
-- JPQL: SELECT l FROM Loan l WHERE l.member.id = :memberId AND l.returnDate IS NULL
SELECT '=== Query 3: findByMemberIdAndReturnDateIsNull ===' AS '';
SELECT * FROM loan
WHERE member_id = 1
  AND return_date IS NULL;
-- 결과: 김철수의 대여 중인 도서 1건


-- Query 4: findByBookIdAndReturnDateIsNull - 특정 도서가 대여 중인지 확인
-- JPQL: SELECT l FROM Loan l WHERE l.book.id = :bookId AND l.returnDate IS NULL
SELECT '=== Query 4: findByBookIdAndReturnDateIsNull ===' AS '';
SELECT * FROM loan
WHERE book_id = 7
  AND return_date IS NULL;
-- 결과: 대여중 도서1(ID=7)의 현재 대여 내역


-- Query 5: findOverdueLoans - 연체된 대여 목록
-- JPQL: SELECT l FROM Loan l WHERE l.returnDate IS NULL AND l.dueDate < :currentDate
SELECT '=== Query 5: findOverdueLoans ===' AS '';
SELECT * FROM loan
WHERE return_date IS NULL
  AND due_date < CURRENT_TIMESTAMP;
-- 결과: 현재 시점 기준 연체 중인 대여 내역


-- Query 6: findByReturnDateIsNull - 전체 미반납 도서
-- JPQL: List<Loan> findByReturnDateIsNull()
SELECT '=== Query 6: findByReturnDateIsNull ===' AS '';
SELECT * FROM loan
WHERE return_date IS NULL;
-- 결과: 모든 대여 중인 도서


-- Query 7: findByLoanDateBetween - 특정 기간의 대여 내역
-- JPQL: SELECT l FROM Loan l WHERE l.loanDate BETWEEN :startDate AND :endDate
SELECT '=== Query 7: findByLoanDateBetween ===' AS '';
SELECT * FROM loan
WHERE loan_date BETWEEN '2024-11-01 00:00:00' AND '2024-11-15 23:59:59';
-- 결과: 11월 1일~15일 사이의 대여 내역


-- Query 8: existsByBookIdAndReturnDateIsNull - 도서 대여 가능 여부
-- JPQL: SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Loan l
--       WHERE l.book.id = :bookId AND l.returnDate IS NULL
SELECT '=== Query 8: existsByBookIdAndReturnDateIsNull ===' AS '';
SELECT CASE
    WHEN COUNT(*) > 0 THEN 'TRUE - 대여 중'
    ELSE 'FALSE - 대여 가능'
END AS is_borrowed
FROM loan
WHERE book_id = 1
  AND return_date IS NULL;
-- 결과: TRUE/FALSE


-- Query 9: countOverdueLoans - 연체 건수
-- JPQL: SELECT COUNT(l) FROM Loan l
--       WHERE l.returnDate IS NULL AND l.dueDate < CURRENT_TIMESTAMP
SELECT '=== Query 9: countOverdueLoans ===' AS '';
SELECT COUNT(*) AS overdue_count
FROM loan
WHERE return_date IS NULL
  AND due_date < CURRENT_TIMESTAMP;
-- 결과: 연체 건수


-- ==========================================
-- 4.2 JOIN 쿼리
-- ==========================================

-- Query 10: findByMemberName - 회원 이름으로 대여 조회 (JOIN)
-- JPQL: SELECT l FROM Loan l JOIN l.member m WHERE m.name = :name
SELECT '=== Query 10: findByMemberName (JOIN) ===' AS '';
SELECT l.*
FROM loan l
INNER JOIN member m ON l.member_id = m.id
WHERE m.name = '김철수';
-- 결과: 김철수의 모든 대여 내역
-- 💡 포인트: Loan 테이블에는 회원 이름이 없어서 Member와 JOIN 필요


-- Query 11: findByBookTitle - 도서 제목으로 대여 조회 (JOIN)
-- JPQL: SELECT l FROM Loan l JOIN l.book b WHERE b.title = :title
SELECT '=== Query 11: findByBookTitle (JOIN) ===' AS '';
SELECT l.*
FROM loan l
INNER JOIN book b ON l.book_id = b.id
WHERE b.title = '자바의 정석';
-- 결과: '자바의 정석' 도서의 모든 대여 내역
-- 💡 포인트: Loan 테이블에는 도서 제목이 없어서 Book과 JOIN 필요


-- Query 12: findOverdueLoansByMemberEmail - 특정 이메일 회원의 연체 대여 (JOIN)
-- JPQL: SELECT l FROM Loan l JOIN l.member m
--       WHERE m.email = :email AND l.status = 'OVERDUE'
SELECT '=== Query 12: findOverdueLoansByMemberEmail (JOIN) ===' AS '';
SELECT l.*
FROM loan l
INNER JOIN member m ON l.member_id = m.id
WHERE m.email = 'park@test.com'
  AND l.status = 'OVERDUE';
-- 결과: park@test.com 회원의 연체 중인 대여
-- 💡 포인트: 이메일은 Member 테이블에만 있어서 JOIN 필요


-- Query 13: findAllWithMemberAndBook - 모든 대여 정보 (회원, 도서 포함)
-- JPQL: SELECT l FROM Loan l JOIN FETCH l.member JOIN FETCH l.book
-- 💡 N+1 문제 해결을 위한 FETCH JOIN
SELECT '=== Query 13: findAllWithMemberAndBook (FETCH JOIN) ===' AS '';
SELECT
    l.id AS loan_id,
    l.status,
    l.loan_date,
    l.due_date,
    m.id AS member_id,
    m.name AS member_name,
    m.email AS member_email,
    b.id AS book_id,
    b.title AS book_title,
    b.author AS book_author
FROM loan l
INNER JOIN member m ON l.member_id = m.id
INNER JOIN book b ON l.book_id = b.id;
-- 결과: 대여 정보와 회원, 도서 정보를 한 번에 조회
-- 💡 포인트: N+1 문제 방지 - 1번의 쿼리로 모든 정보 조회


-- Query 14: findMembersByBookTitle - 특정 도서를 대여한 회원 목록
-- JPQL: SELECT DISTINCT l.member FROM Loan l JOIN l.book b WHERE b.title = :bookTitle
SELECT '=== Query 14: findMembersByBookTitle ===' AS '';
SELECT DISTINCT
    m.id,
    m.name,
    m.email,
    m.membership_type
FROM loan l
INNER JOIN book b ON l.book_id = b.id
INNER JOIN member m ON l.member_id = m.id
WHERE b.title = '인기도서';
-- 결과: '인기도서'를 대여한 적이 있는 회원 목록 (중복 제거)
-- 💡 포인트: DISTINCT로 같은 회원이 여러 번 대여한 경우 중복 제거


-- Query 15: findCurrentlyBorrowedBooks - 특정 회원의 대여 중인 도서 목록
-- JPQL: SELECT l.book FROM Loan l
--       WHERE l.member.id = :memberId AND l.returnDate IS NULL
SELECT '=== Query 15: findCurrentlyBorrowedBooks ===' AS '';
SELECT
    b.id,
    b.title,
    b.author,
    b.isbn
FROM loan l
INNER JOIN book b ON l.book_id = b.id
WHERE l.member_id = 6
  AND l.return_date IS NULL;
-- 결과: 독서왕(ID=6)이 현재 대여 중인 도서 목록
-- 💡 포인트: Loan이 아닌 Book 정보를 반환


-- Query 16: findOverdueLoansWithMember - 연체 대여와 회원 정보 (정렬)
-- JPQL: SELECT l FROM Loan l JOIN FETCH l.member m
--       WHERE l.status = 'OVERDUE' ORDER BY l.dueDate ASC
SELECT '=== Query 16: findOverdueLoansWithMember (정렬) ===' AS '';
SELECT
    l.id AS loan_id,
    l.status,
    l.loan_date,
    l.due_date,
    l.overdue_fee,
    m.id AS member_id,
    m.name AS member_name,
    m.email AS member_email,
    b.title AS book_title
FROM loan l
INNER JOIN member m ON l.member_id = m.id
INNER JOIN book b ON l.book_id = b.id
WHERE l.status = 'OVERDUE'
ORDER BY l.due_date ASC;
-- 결과: 연체 중인 대여 내역 (연체 기한이 가장 오래된 순)
-- 💡 포인트: FETCH JOIN + WHERE + ORDER BY 조합


-- ==========================================
-- 4.3 통계 및 집계 쿼리
-- ==========================================

-- 회원별 대여 권수
SELECT '=== 회원별 대여 권수 ===' AS '';
SELECT
    m.name AS member_name,
    COUNT(l.id) AS loan_count
FROM member m
LEFT JOIN loan l ON m.id = l.member_id
GROUP BY m.id, m.name
ORDER BY loan_count DESC;
-- 결과: 각 회원이 대여한 총 권수


-- 도서별 대여 횟수
SELECT '=== 도서별 대여 횟수 ===' AS '';
SELECT
    b.title AS book_title,
    b.author,
    COUNT(l.id) AS borrow_count
FROM book b
LEFT JOIN loan l ON b.id = l.book_id
GROUP BY b.id, b.title, b.author
ORDER BY borrow_count DESC;
-- 결과: 가장 많이 대여된 도서 순


-- 현재 대여 중인 도서 목록
SELECT '=== 현재 대여 중인 도서 목록 ===' AS '';
SELECT
    b.title AS book_title,
    m.name AS borrower_name,
    l.loan_date,
    l.due_date,
    CASE
        WHEN l.due_date < CURRENT_TIMESTAMP THEN 'OVERDUE'
        ELSE 'ACTIVE'
    END AS status
FROM loan l
INNER JOIN book b ON l.book_id = b.id
INNER JOIN member m ON l.member_id = m.id
WHERE l.return_date IS NULL
ORDER BY l.due_date ASC;
-- 결과: 현재 대여 중인 모든 도서와 대여자 정보


-- 회원별 연체료 합계
SELECT '=== 회원별 연체료 합계 ===' AS '';
SELECT
    m.name AS member_name,
    m.email,
    SUM(l.overdue_fee) AS total_overdue_fee,
    COUNT(CASE WHEN l.status = 'OVERDUE' THEN 1 END) AS overdue_count
FROM member m
LEFT JOIN loan l ON m.id = l.member_id
GROUP BY m.id, m.name, m.email
HAVING SUM(l.overdue_fee) > 0
ORDER BY total_overdue_fee DESC;
-- 결과: 연체료가 있는 회원 목록


-- ==========================================
-- 4.4 복잡한 JOIN 쿼리
-- ==========================================

-- 대여 이력이 있는 회원과 도서의 상세 정보
SELECT '=== 대여 이력 상세 ===' AS '';
SELECT
    l.id AS loan_id,
    m.name AS member_name,
    m.email,
    m.membership_type,
    b.title AS book_title,
    b.author,
    b.price,
    l.loan_date,
    l.due_date,
    l.return_date,
    l.status,
    l.overdue_fee,
    CASE
        WHEN l.return_date IS NOT NULL THEN '반납완료'
        WHEN l.return_date IS NULL AND l.due_date < CURRENT_TIMESTAMP THEN '연체중'
        ELSE '대여중'
    END AS display_status,
    CASE
        WHEN l.return_date IS NULL AND l.due_date < CURRENT_TIMESTAMP
        THEN DATEDIFF(CURRENT_TIMESTAMP, l.due_date)
        ELSE 0
    END AS overdue_days
FROM loan l
INNER JOIN member m ON l.member_id = m.id
INNER JOIN book b ON l.book_id = b.id
ORDER BY l.loan_date DESC;
-- 결과: 모든 대여 이력의 상세 정보


-- ==========================================
-- 5. 실습 문제
-- ==========================================

SELECT '=== 실습 문제 ===' AS '';
SELECT '
실습 문제를 풀어보세요:

1. PREMIUM 회원들의 모든 대여 내역을 조회하세요.
   (힌트: member 테이블과 JOIN, WHERE membership_type = ''PREMIUM'')

2. 현재 대여 중인 도서 중 가격이 30000원 이상인 도서를 조회하세요.
   (힌트: book 테이블과 JOIN, WHERE return_date IS NULL AND price >= 30000)

3. 연체료가 5000원 이상인 대여 건의 회원 이름과 도서 제목을 조회하세요.
   (힌트: 3개 테이블 JOIN, WHERE overdue_fee >= 5000)

4. 도서를 한 번도 대여하지 않은 회원을 찾으세요.
   (힌트: LEFT JOIN 사용, WHERE loan.id IS NULL)

5. 가장 많이 대여된 도서 TOP 3를 찾으세요.
   (힌트: GROUP BY, COUNT, ORDER BY, LIMIT)
' AS practice_problems;


-- 정답 예시 (주석 해제하여 확인)
/*
-- 1번 정답
SELECT l.*, m.name, m.membership_type
FROM loan l
INNER JOIN member m ON l.member_id = m.id
WHERE m.membership_type = 'PREMIUM';

-- 2번 정답
SELECT b.title, b.price, m.name AS borrower
FROM loan l
INNER JOIN book b ON l.book_id = b.id
INNER JOIN member m ON l.member_id = m.id
WHERE l.return_date IS NULL
  AND b.price >= 30000;

-- 3번 정답
SELECT
    m.name AS member_name,
    b.title AS book_title,
    l.overdue_fee
FROM loan l
INNER JOIN member m ON l.member_id = m.id
INNER JOIN book b ON l.book_id = b.id
WHERE l.overdue_fee >= 5000;

-- 4번 정답
SELECT m.*
FROM member m
LEFT JOIN loan l ON m.id = l.member_id
WHERE l.id IS NULL;

-- 5번 정답
SELECT
    b.title,
    COUNT(l.id) AS borrow_count
FROM book b
INNER JOIN loan l ON b.id = l.book_id
GROUP BY b.id, b.title
ORDER BY borrow_count DESC
LIMIT 3;
*/

-- ========================================
-- 6. 데이터 정리 (실습 종료 시)
-- ========================================

-- 실습 종료 후 데이터 삭제 (필요시)
-- DELETE FROM loan;
-- DELETE FROM book;
-- DELETE FROM member;

-- 테이블 삭제 (필요시)
-- DROP TABLE IF EXISTS loan;
-- DROP TABLE IF EXISTS book;
-- DROP TABLE IF EXISTS member;