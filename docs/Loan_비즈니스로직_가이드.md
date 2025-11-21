# Loan 엔티티 비즈니스 로직 가이드

## 개요

`Loan` 엔티티는 도서 대여 시스템의 핵심 비즈니스 로직을 담당합니다.
이 문서는 학생들이 Loan 엔티티의 메소드들을 이해하고 올바르게 사용할 수 있도록 작성되었습니다.

## 주요 개념

### 1. 자체 객체 변경 메소드란?

Loan 엔티티의 메소드들은 **자기 자신(this)의 상태를 변경하거나 조회하는 메소드**입니다.
따라서 **파라미터 없이** 객체 자신의 필드 값만으로 작동합니다.

```java
// ✅ 좋은 예: 파라미터 없이 자신의 상태만 확인
public boolean isOverdue() {
    if(returnDate != null) {
        return false;
    }
    return LocalDateTime.now().isAfter(dueDate);
}

// ❌ 나쁜 예: 불필요한 파라미터 (학생들이 자주 하는 실수)
public boolean isOverdue(LocalDateTime currentTime) {
    // 외부에서 시간을 받을 필요가 없습니다!
}
```

### 2. 왜 파라미터가 필요 없나요?

**Loan 객체는 이미 필요한 모든 정보를 가지고 있습니다:**
- `loanDate` - 대여일
- `dueDate` - 반납 예정일
- `returnDate` - 실제 반납일
- `overdueFee` - 연체료

현재 시간은 `LocalDateTime.now()`로 얻을 수 있으므로, 굳이 파라미터로 받을 필요가 없습니다.

---

## 비즈니스 로직 메소드 상세 설명

### 1. isOverdue() - 연체 여부 확인

**목적:** 이 대여 건이 연체되었는지 확인합니다.

**동작 방식:**
```java
public boolean isOverdue() {
    if(returnDate != null) {
        return false;   // 이미 반납된 경우.
    }
    return LocalDateTime.now().isAfter(dueDate);
}
```

**흐름도:**
```
시작
  ↓
이미 반납했나요? (returnDate != null)
  ↓ YES → 연체 아님 (false) 반환
  ↓ NO
현재 시간이 반납 예정일을 지났나요?
  ↓ YES → 연체임 (true) 반환
  ↓ NO → 연체 아님 (false) 반환
```

**예제:**
```java
Loan loan = Loan.builder()
    .dueDate(LocalDateTime.of(2025, 11, 10, 23, 59))
    .build();

// 현재 시간이 2025-11-14라면
boolean overdue = loan.isOverdue();  // true (연체됨)
```

**학생 실수 사례:**
```java
// ❌ 잘못된 예
public boolean isOverdue(LocalDateTime currentTime) {
    // 파라미터를 받을 필요가 없습니다!
}

// ❌ 잘못된 예 2
public boolean checkOverdue() {
    // 메소드 이름은 is로 시작하는 것이 Java 관례입니다
}
```

---

### 2. getOverdueDays() - 연체 일수 계산

**목적:** 며칠이나 연체되었는지 계산합니다.

**동작 방식:**
```java
public long getOverdueDays() {
    if (!isOverdue()) {
        return 0;  // 연체가 아니면 0일
    }
    return ChronoUnit.DAYS.between(dueDate, LocalDateTime.now());
}
```

**계산 로직:**
1. 먼저 연체 여부 확인 (`isOverdue()` 재사용)
2. 연체가 아니면 0 반환
3. 연체라면: `현재 시간 - 반납 예정일 = 연체 일수`

**예제:**
```java
Loan loan = Loan.builder()
    .dueDate(LocalDateTime.of(2025, 11, 10, 0, 0))  // 11월 10일
    .build();

// 현재 시간이 2025-11-14라면
long days = loan.getOverdueDays();  // 4일 연체
```

**왜 ChronoUnit.DAYS.between을 사용하나요?**
- Java 8+ 에서 날짜 차이를 계산하는 표준 방법입니다
- 시간대(timezone) 처리가 정확합니다
- 일(day) 단위로 명확하게 계산됩니다

---

### 3. calculateOverdueFee() - 연체료 계산

**목적:** 연체 일수에 따른 연체료를 계산합니다.

**동작 방식:**
```java
public BigDecimal calculateOverdueFee() {
    long overdueDays = getOverdueDays();
    if (overdueDays <= 0) {
        return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(overdueDays * 1000);
}
```

**비즈니스 규칙:**
- **연체료 = 연체 일수 × 1,000원**
- 연체가 아니면 0원

**예제:**
```java
Loan loan = Loan.builder()
    .dueDate(LocalDateTime.of(2025, 11, 5, 0, 0))
    .build();

// 현재 시간이 2025-11-14라면 (9일 연체)
BigDecimal fee = loan.calculateOverdueFee();  // 9,000원
```

**왜 BigDecimal을 사용하나요?**
- 돈(금액) 계산에는 `double`이나 `float`을 사용하면 안 됩니다!
- 부동소수점 오류로 정확한 계산이 안 됩니다
- `BigDecimal`은 정확한 10진수 계산을 보장합니다

```java
// ❌ 절대 이렇게 하지 마세요!
public double calculateOverdueFee() {
    return overdueDays * 1000.0;  // 부정확한 계산!
}
```

---

### 4. returnBook() - 도서 반납 처리

**목적:** 도서를 반납하고 연체료를 확정합니다.

**동작 방식:**
```java
public void returnBook(LocalDateTime returnTime) {
    if (this.returnDate != null) {
        throw new IllegalStateException("반납된 도서입니다");
    }

    // ⚠️ 중요: 순서가 매우 중요합니다!
    // 1. 연체료를 먼저 계산 (returnDate가 설정되기 전에)
    this.overdueFee = calculateOverdueFee();

    // 2. 반납 정보 설정
    this.returnDate = returnTime;
    this.status = LoanStatus.RETURNED;
}

public void returnBook() {
    returnBook(LocalDateTime.now());
}
```

**왜 순서가 중요한가요?**

```java
// ❌ 잘못된 순서 (학생들이 자주 하는 실수)
public void returnBook() {
    this.returnDate = LocalDateTime.now();  // 먼저 설정하면
    this.overdueFee = calculateOverdueFee(); // 항상 0원이 됩니다!
}
```

**문제 원인:**
- `isOverdue()` 메소드가 `returnDate != null`이면 무조건 `false`를 반환
- 따라서 `returnDate`를 먼저 설정하면 연체료가 항상 0원이 됩니다

**올바른 순서:**
```
1. calculateOverdueFee() 호출 → 연체료 계산 (returnDate가 null인 상태)
2. returnDate 설정 → 반납 완료 표시
3. status를 RETURNED로 변경
```

**예제:**
```java
Loan loan = Loan.builder()
    .dueDate(LocalDateTime.of(2025, 11, 10, 0, 0))
    .status(LoanStatus.ACTIVE)
    .build();

// 2025-11-15에 반납 (5일 연체)
loan.returnBook(LocalDateTime.of(2025, 11, 15, 14, 30));

System.out.println(loan.getOverdueFee());  // 5000원
System.out.println(loan.getStatus());      // RETURNED
System.out.println(loan.getReturnDate());  // 2025-11-15T14:30
```

**중복 반납 방지:**
```java
loan.returnBook();  // 첫 번째 반납 - 성공
loan.returnBook();  // 두 번째 반납 - IllegalStateException 발생!
```

---

### 5. updateStatus() - 대여 상태 업데이트

**목적:** 대여 상태를 현재 상황에 맞게 자동으로 업데이트합니다.

**동작 방식:**
```java
public void updateStatus() {
    if (this.returnDate != null) {
        this.status = LoanStatus.RETURNED;  // 반납 완료
    } else if (isOverdue()) {
        this.status = LoanStatus.OVERDUE;   // 연체 중
    } else {
        this.status = LoanStatus.ACTIVE;    // 정상 대여 중
    }
}
```

**상태 전이도:**
```
returnDate가 있나요?
  ↓ YES → RETURNED (반납 완료)
  ↓ NO
연체되었나요? (isOverdue)
  ↓ YES → OVERDUE (연체 중)
  ↓ NO → ACTIVE (정상 대여 중)
```

**언제 사용하나요?**
- 배치 작업으로 모든 대여 건의 상태를 업데이트할 때
- 대여 정보를 조회할 때 현재 상태를 반영하기 위해

**예제:**
```java
// 상황 1: 정상 대여 중
Loan loan = Loan.builder()
    .dueDate(LocalDateTime.now().plusDays(7))  // 7일 후 반납
    .build();
loan.updateStatus();
System.out.println(loan.getStatus());  // ACTIVE

// 상황 2: 연체 중
loan.setDueDate(LocalDateTime.now().minusDays(3));  // 3일 전이 반납일
loan.updateStatus();
System.out.println(loan.getStatus());  // OVERDUE

// 상황 3: 반납 완료
loan.returnBook();
loan.updateStatus();
System.out.println(loan.getStatus());  // RETURNED
```

---

## LoanStatus Enum 설명

```java
public enum LoanStatus {
    ACTIVE("대여 중"),      // 정상적으로 대여 중
    RETURNED("반납 완료"),  // 반납 완료
    OVERDUE("연체"),        // 반납 예정일이 지났지만 아직 반납 안 함
    CANCELLED("취소됨");    // 대여 취소 (향후 구현)
}
```

---

## 테스트 케이스 작성 가이드

### 기본 테스트 구조

```java
@Test
@DisplayName("설명문")
void 테스트메소드명() {
    // given: 테스트 데이터 준비
    Loan loan = Loan.builder()
        .member(testMember)
        .book(testBook)
        .loanDate(LocalDateTime.now())
        .dueDate(LocalDateTime.now().plusDays(14))
        .build();

    // when: 테스트할 메소드 실행
    boolean result = loan.isOverdue();

    // then: 결과 검증
    assertThat(result).isFalse();
}
```

### 테스트해야 할 시나리오

#### 1. isOverdue() 테스트
```java
// 1. 연체 아님 - 반납 예정일 전
@Test
void isOverdue_반납예정일전_연체아님() {
    Loan loan = Loan.builder()
        .dueDate(LocalDateTime.now().plusDays(7))  // 7일 후
        .build();

    assertThat(loan.isOverdue()).isFalse();
}

// 2. 연체됨 - 반납 예정일 지남
@Test
void isOverdue_반납예정일지남_연체됨() {
    Loan loan = Loan.builder()
        .dueDate(LocalDateTime.now().minusDays(3))  // 3일 전
        .build();

    assertThat(loan.isOverdue()).isTrue();
}

// 3. 이미 반납됨 - 연체 아님
@Test
void isOverdue_이미반납됨_연체아님() {
    Loan loan = Loan.builder()
        .dueDate(LocalDateTime.now().minusDays(5))
        .returnDate(LocalDateTime.now())  // 반납 완료
        .build();

    assertThat(loan.isOverdue()).isFalse();
}
```

#### 2. getOverdueDays() 테스트
```java
// 1. 연체 아님 - 0일 반환
@Test
void getOverdueDays_연체아님_0일반환() {
    Loan loan = Loan.builder()
        .dueDate(LocalDateTime.now().plusDays(5))
        .build();

    assertThat(loan.getOverdueDays()).isEqualTo(0);
}

// 2. 연체됨 - 정확한 일수 계산
@Test
void getOverdueDays_5일연체_5일반환() {
    Loan loan = Loan.builder()
        .dueDate(LocalDateTime.now().minusDays(5))
        .build();

    assertThat(loan.getOverdueDays()).isEqualTo(5);
}
```

#### 3. calculateOverdueFee() 테스트
```java
// 1. 연체 아님 - 0원
@Test
void calculateOverdueFee_연체아님_0원() {
    Loan loan = Loan.builder()
        .dueDate(LocalDateTime.now().plusDays(3))
        .build();

    assertThat(loan.calculateOverdueFee()).isEqualTo(BigDecimal.ZERO);
}

// 2. 5일 연체 - 5000원
@Test
void calculateOverdueFee_5일연체_5000원() {
    Loan loan = Loan.builder()
        .dueDate(LocalDateTime.now().minusDays(5))
        .build();

    BigDecimal fee = loan.calculateOverdueFee();
    assertThat(fee).isEqualByComparingTo(new BigDecimal("5000"));
}
```

#### 4. returnBook() 테스트
```java
// 1. 정상 반납 - 연체료 0원
@Test
void returnBook_정상반납_연체료0원() {
    Loan loan = Loan.builder()
        .dueDate(LocalDateTime.now().plusDays(3))
        .build();

    loan.returnBook();

    assertThat(loan.getReturnDate()).isNotNull();
    assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
    assertThat(loan.getOverdueFee()).isEqualByComparingTo(BigDecimal.ZERO);
}

// 2. 연체 후 반납 - 연체료 계산됨
@Test
void returnBook_연체후반납_연체료계산() {
    Loan loan = Loan.builder()
        .dueDate(LocalDateTime.now().minusDays(5))
        .build();

    loan.returnBook();

    assertThat(loan.getOverdueFee()).isGreaterThan(BigDecimal.ZERO);
    assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
}

// 3. 중복 반납 - 예외 발생
@Test
void returnBook_중복반납_예외발생() {
    Loan loan = Loan.builder()
        .dueDate(LocalDateTime.now())
        .build();

    loan.returnBook();  // 첫 번째 반납

    assertThatThrownBy(() -> loan.returnBook())  // 두 번째 반납 시도
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("반납된 도서입니다");
}
```

---

## 학생들이 자주 하는 실수 정리

### 1. 불필요한 파라미터 추가
```java
// ❌ 잘못된 예
public boolean isOverdue(LocalDateTime currentTime) {
    return currentTime.isAfter(dueDate);
}

// ✅ 올바른 예
public boolean isOverdue() {
    return LocalDateTime.now().isAfter(dueDate);
}
```

### 2. returnBook() 순서 실수
```java
// ❌ 잘못된 예 - 연체료가 항상 0원
public void returnBook() {
    this.returnDate = LocalDateTime.now();      // ← 먼저 설정하면 안 됨
    this.overdueFee = calculateOverdueFee();    // 항상 0원!
}

// ✅ 올바른 예
public void returnBook() {
    this.overdueFee = calculateOverdueFee();    // ← 먼저 계산
    this.returnDate = LocalDateTime.now();      // 그 다음 설정
}
```

### 3. 금액 계산에 double 사용
```java
// ❌ 잘못된 예
private double overdueFee;
public double calculateOverdueFee() {
    return getOverdueDays() * 1000.0;  // 부정확!
}

// ✅ 올바른 예
private BigDecimal overdueFee;
public BigDecimal calculateOverdueFee() {
    return BigDecimal.valueOf(getOverdueDays() * 1000);
}
```

### 4. 메소드 중복 정의
```java
// ❌ 잘못된 예 - 같은 기능을 여러 이름으로
public boolean isOverdue() { ... }
public boolean checkOverdue() { ... }  // 불필요한 중복
public boolean isLate() { ... }        // 불필요한 중복

// ✅ 올바른 예 - 하나의 명확한 메소드
public boolean isOverdue() { ... }
```

### 5. 상태 업데이트 누락
```java
// ❌ 잘못된 예
public void returnBook() {
    this.returnDate = LocalDateTime.now();
    // status 업데이트 누락!
}

// ✅ 올바른 예
public void returnBook() {
    this.overdueFee = calculateOverdueFee();
    this.returnDate = LocalDateTime.now();
    this.status = LoanStatus.RETURNED;  // ← 상태 업데이트 필수
}
```

---

## 정리

### 핵심 원칙
1. **자체 객체 메소드는 파라미터가 필요 없습니다** - 객체가 이미 모든 정보를 가지고 있습니다
2. **메소드 순서가 중요합니다** - 특히 `returnBook()`에서 연체료 계산 순서
3. **금액은 반드시 BigDecimal** - double/float 절대 사용 금지
4. **상태 관리는 일관성 있게** - returnBook 시 status도 함께 업데이트
5. **테스트는 모든 경우의 수를 커버** - 정상, 연체, 반납 완료 등

### 다음 학습 단계
1. LoanRepository 구현 (데이터베이스 연동)
2. LoanService 구현 (비즈니스 로직 레이어)
3. LoanController 구현 (REST API)
4. 통합 테스트 작성

---

**작성일:** 2025-11-14
**대상:** Spring Boot 학습 학생
**난이도:** 초급~중급