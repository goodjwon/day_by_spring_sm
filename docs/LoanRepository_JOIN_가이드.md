# LoanRepository JOIN 가이드

## 개요

Loan 엔티티는 Member(회원)와 Book(도서)와 관계를 맺고 있습니다.
이런 경우 **JOIN**을 사용하여 연관된 데이터를 효율적으로 조회할 수 있습니다.

## Loan 엔티티의 관계

```java
@Entity
public class Loan {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  // 대여한 회원

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;      // 대여한 도서
}
```

---

## JOIN이 필요한 경우

### 1. 회원 이름으로 대여 조회
**상황**: "김철수" 회원의 대여 내역을 조회하고 싶다

**문제**: Loan 테이블에는 회원 이름이 없고, `member_id`만 있다
**해결**: Member 테이블과 JOIN 필요

```java
// ❌ 불가능 - Loan 테이블에 name이 없음
@Query("SELECT l FROM Loan l WHERE l.name = :name")
List<Loan> findByMemberName(@Param("name") String name);

// ✅ JOIN 사용
@Query("SELECT l FROM Loan l JOIN l.member m WHERE m.name = :name")
List<Loan> findByMemberName(@Param("name") String name);
```

### 2. 도서 제목으로 대여 조회
**상황**: "자바의 정석" 도서의 대여 내역을 조회하고 싶다

```java
// ✅ Book과 JOIN
@Query("SELECT l FROM Loan l JOIN l.book b WHERE b.title = :title")
List<Loan> findByBookTitle(@Param("title") String title);
```

### 3. 특정 회원의 연체된 대여 조회
**상황**: 특정 이메일을 가진 회원의 연체 대여 조회

```java
// ✅ Member와 JOIN + 조건 추가
@Query("SELECT l FROM Loan l JOIN l.member m " +
       "WHERE m.email = :email AND l.status = 'OVERDUE'")
List<Loan> findOverdueLoansByMemberEmail(@Param("email") String email);
```

### 4. N+1 문제 해결 (Fetch Join)
**상황**: 대여 목록과 함께 회원/도서 정보도 필요하다

```java
// ❌ N+1 문제 발생
@Query("SELECT l FROM Loan l")
List<Loan> findAll();
// 1번 쿼리: Loan 조회
// N번 쿼리: 각 Loan마다 Member, Book 조회 (LAZY 로딩)

// ✅ Fetch Join으로 한 번에 조회
@Query("SELECT l FROM Loan l " +
       "JOIN FETCH l.member " +
       "JOIN FETCH l.book")
List<Loan> findAllWithMemberAndBook();
// 1번 쿼리로 모든 데이터 조회!
```

---

## JOIN 종류

### 1. INNER JOIN (기본)
양쪽 테이블에 모두 데이터가 있는 경우만 조회

```java
// JPQL에서는 JOIN만 쓰면 INNER JOIN
@Query("SELECT l FROM Loan l JOIN l.member m WHERE m.name = :name")
List<Loan> findByMemberName(@Param("name") String name);
```

**SQL 변환 결과:**
```sql
SELECT l.* FROM loan l
INNER JOIN member m ON l.member_id = m.id
WHERE m.name = '김철수'
```

### 2. LEFT JOIN
왼쪽 테이블(Loan) 데이터는 모두 가져오고, 오른쪽은 있으면 가져옴

```java
@Query("SELECT l FROM Loan l LEFT JOIN l.member m WHERE m.name = :name")
List<Loan> findByMemberNameIncludingDeleted(@Param("name") String name);
```

### 3. FETCH JOIN
연관된 엔티티를 즉시 로딩 (N+1 문제 해결)

```java
@Query("SELECT l FROM Loan l JOIN FETCH l.member JOIN FETCH l.book")
List<Loan> findAllWithDetails();
```

---

## 실전 예제

### 예제 1: 특정 도서를 대여한 회원 목록

```java
/**
 * 특정 도서를 대여한 회원 목록 조회
 * @param bookTitle 도서 제목
 * @return 회원 목록
 */
@Query("SELECT DISTINCT l.member FROM Loan l " +
       "JOIN l.book b " +
       "WHERE b.title = :bookTitle")
List<Member> findMembersByBookTitle(@Param("bookTitle") String bookTitle);
```

**포인트:**
- `SELECT DISTINCT l.member` - Loan이 아닌 Member를 조회
- `DISTINCT` - 중복 회원 제거 (같은 회원이 여러 번 대여한 경우)

### 예제 2: 회원별 대여 권수 통계

```java
/**
 * 회원별 대여 권수 조회
 * @return Map<회원 이름, 대여 권수>
 */
@Query("SELECT m.name, COUNT(l) FROM Loan l " +
       "JOIN l.member m " +
       "GROUP BY m.id, m.name " +
       "ORDER BY COUNT(l) DESC")
List<Object[]> countLoansByMember();

// 사용법
List<Object[]> results = loanRepository.countLoansByMember();
for (Object[] row : results) {
    String memberName = (String) row[0];
    Long loanCount = (Long) row[1];
    System.out.println(memberName + ": " + loanCount + "권");
}
```

### 예제 3: 특정 기간 대여한 회원과 도서 정보

```java
/**
 * 특정 기간에 대여한 내역 조회 (회원, 도서 정보 포함)
 * @param startDate 시작일
 * @param endDate 종료일
 * @return 대여 목록
 */
@Query("SELECT l FROM Loan l " +
       "JOIN FETCH l.member m " +
       "JOIN FETCH l.book b " +
       "WHERE l.loanDate BETWEEN :startDate AND :endDate " +
       "ORDER BY l.loanDate DESC")
List<Loan> findLoansInPeriodWithDetails(
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate
);
```

**포인트:**
- `JOIN FETCH` 사용 - Member, Book 정보를 한 번에 로딩
- N+1 문제 예방
- 날짜 범위 조건

### 예제 4: 연체 중인 대여와 회원 정보

```java
/**
 * 연체 중인 대여 조회 (회원 정보 포함)
 * @return 연체 대여 목록
 */
@Query("SELECT l FROM Loan l " +
       "JOIN FETCH l.member m " +
       "WHERE l.status = 'OVERDUE' " +
       "ORDER BY l.dueDate ASC")
List<Loan> findOverdueLoansWithMember();
```

### 예제 5: 특정 회원의 현재 대여 중인 도서 목록

```java
/**
 * 특정 회원의 대여 중인 도서 조회
 * @param memberId 회원 ID
 * @return 도서 목록
 */
@Query("SELECT l.book FROM Loan l " +
       "WHERE l.member.id = :memberId " +
       "AND l.returnDate IS NULL")
List<Book> findCurrentlyBorrowedBooks(@Param("memberId") Long memberId);
```

**포인트:**
- `l.member.id` - JOIN 없이 ID로 직접 접근 가능
- `SELECT l.book` - Loan이 아닌 Book 엔티티 반환

### 예제 6: 복잡한 조건의 조회

```java
/**
 * 프리미엄 회원의 미반납 대여 조회
 * @param membershipType 회원 등급
 * @return 대여 목록
 */
@Query("SELECT l FROM Loan l " +
       "JOIN FETCH l.member m " +
       "JOIN FETCH l.book b " +
       "WHERE m.membershipType = :membershipType " +
       "AND l.returnDate IS NULL " +
       "ORDER BY l.loanDate DESC")
List<Loan> findUnreturnedLoansByMembershipType(
    @Param("membershipType") MembershipType membershipType
);
```

---

## JOIN vs 중첩 프로퍼티 접근

### 중첩 프로퍼티로 충분한 경우

ID만 비교하는 경우 JOIN 없이도 가능:

```java
// ✅ JOIN 불필요 - ID 비교만
@Query("SELECT l FROM Loan l WHERE l.member.id = :memberId")
List<Loan> findByMemberId(@Param("memberId") Long memberId);

// ❌ 불필요한 JOIN
@Query("SELECT l FROM Loan l JOIN l.member m WHERE m.id = :memberId")
List<Loan> findByMemberId(@Param("memberId") Long memberId);
```

### JOIN이 필요한 경우

ID가 아닌 다른 필드로 검색하는 경우:

```java
// ✅ JOIN 필요 - 회원 이름으로 검색
@Query("SELECT l FROM Loan l JOIN l.member m WHERE m.name LIKE %:name%")
List<Loan> searchByMemberName(@Param("name") String name);
```

---

## N+1 문제 예방

### 문제 상황

```java
// ❌ N+1 문제 발생
@Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE'")
List<Loan> findActiveLoans();

// 사용
List<Loan> loans = repository.findActiveLoans();
for (Loan loan : loans) {
    System.out.println(loan.getMember().getName());  // 여기서 추가 쿼리 발생!
    System.out.println(loan.getBook().getTitle());   // 여기서 추가 쿼리 발생!
}
```

**실행되는 쿼리:**
```
1. SELECT * FROM loan WHERE status = 'ACTIVE'  (Loan 조회)
2. SELECT * FROM member WHERE id = 1           (첫 번째 Member 조회)
3. SELECT * FROM book WHERE id = 1             (첫 번째 Book 조회)
4. SELECT * FROM member WHERE id = 2           (두 번째 Member 조회)
5. SELECT * FROM book WHERE id = 2             (두 번째 Book 조회)
...
```

### 해결 방법 (FETCH JOIN)

```java
// ✅ Fetch Join으로 해결
@Query("SELECT l FROM Loan l " +
       "JOIN FETCH l.member " +
       "JOIN FETCH l.book " +
       "WHERE l.status = 'ACTIVE'")
List<Loan> findActiveLoansWithDetails();
```

**실행되는 쿼리:**
```sql
SELECT l.*, m.*, b.*
FROM loan l
INNER JOIN member m ON l.member_id = m.id
INNER JOIN book b ON l.book_id = b.id
WHERE l.status = 'ACTIVE'
```

**단 1번의 쿼리로 모든 데이터 조회!**

---

## 실무 팁

### 1. Fetch Join은 항상 좋은가?

**NO!** 상황에 따라 다릅니다.

```java
// ❌ 나쁜 예: 필요 없는데 Fetch Join
@Query("SELECT l FROM Loan l " +
       "JOIN FETCH l.member " +
       "JOIN FETCH l.book")
List<Loan> findAll();

// 사용하는 곳
List<Loan> loans = repository.findAll();
// loan.getMember()나 loan.getBook()을 전혀 사용하지 않음!
// → 불필요한 데이터를 가져와서 메모리 낭비
```

**원칙:**
- Member/Book 정보를 사용할 거면 → Fetch Join 사용
- ID만 필요하거나 사용 안 하면 → Fetch Join 불필요

### 2. 여러 컬렉션 Fetch Join 주의

```java
// ❌ 위험: 카테시안 곱 발생
@Query("SELECT m FROM Member m " +
       "JOIN FETCH m.loans " +
       "JOIN FETCH m.orders")
List<Member> findAllWithLoansAndOrders();
```

**해결책:**
1. 별도 쿼리로 분리
2. @EntityGraph 사용
3. Batch Size 설정

### 3. DTO로 필요한 데이터만 조회

불필요한 데이터를 줄이려면 DTO 프로젝션 사용:

```java
// DTO 클래스
public class LoanSummaryDTO {
    private Long loanId;
    private String memberName;
    private String bookTitle;
    private LocalDateTime loanDate;

    public LoanSummaryDTO(Long loanId, String memberName,
                          String bookTitle, LocalDateTime loanDate) {
        this.loanId = loanId;
        this.memberName = memberName;
        this.bookTitle = bookTitle;
        this.loanDate = loanDate;
    }
}

// Repository
@Query("SELECT new com.example.spring.dto.LoanSummaryDTO(" +
       "l.id, m.name, b.title, l.loanDate) " +
       "FROM Loan l " +
       "JOIN l.member m " +
       "JOIN l.book b " +
       "WHERE l.status = 'ACTIVE'")
List<LoanSummaryDTO> findActiveLoansSummary();
```

**장점:**
- 필요한 컬럼만 SELECT
- 메모리 절약
- 성능 향상

---

## 정리

### JOIN을 사용해야 하는 경우
1. 연관 엔티티의 필드로 검색할 때 (회원 이름, 도서 제목 등)
2. 통계나 집계 쿼리 (COUNT, SUM, GROUP BY)
3. N+1 문제 예방 (FETCH JOIN)
4. 여러 테이블의 데이터를 한 번에 조회

### JOIN을 사용하지 않아도 되는 경우
1. ID로만 검색하는 경우
2. 연관 엔티티 데이터를 사용하지 않는 경우
3. 단순 조회 (Lazy Loading으로 충분)

### 성능 최적화 원칙
1. **필요한 데이터만 조회** - DTO 프로젝션 고려
2. **N+1 문제 예방** - Fetch Join 적절히 사용
3. **과도한 Join 자제** - 정말 필요한 경우만
4. **인덱스 고려** - Join 컬럼에 인덱스 생성

---

**작성일:** 2025-11-22
**대상:** Spring Data JPA 학습 학생
**난이도:** 중급
