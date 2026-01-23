# 대출(Loan) 시스템 가이드

## 개요

이 문서는 도서관 관리 시스템의 대출(Loan) 기능을 화면 흐름 순서로 설명합니다. 각 화면마다 어떤 API가 호출되는지, 어떻게 테스트하는지 실습하면서 배울 수 있습니다.

**대상 독자**: 백엔드 개발자, 프론트엔드 개발자, QA 테스터, 초보 개발자

---

## 기본 정보

- **Base URL**: `http://localhost:8080/api`
- **Data Format**: `application/json`
- **Authentication**: JWT Bearer Token 필요
- **관리자 경로**: `/admin/*`
- **사용자 경로**: `/client/*`

### 데이터 모델

```json
{
  "id": 1,
  "bookId": 15,
  "bookTitle": "The Silent Patient",
  "bookAuthor": "Alex Michaelides",
  "bookIsbn": "978-1234567890",
  "memberId": 4,
  "memberName": "James Wilson",
  "memberEmail": "james.wilson@email.com",
  "loanDate": "2025-10-02T11:45:00",
  "dueDate": "2025-10-16T11:45:00",
  "returnDate": null,
  "status": "ACTIVE",
  "extensionCount": 0,
  "overdueFee": 0,
  "isOverdue": false,
  "overdueDays": 0,
  "daysUntilDue": 14,
  "canExtendNow": false,
  "createdDate": "2025-10-02T11:45:00",
  "updatedDate": "2025-10-02T11:45:00"
}
```

**상태 (status)**:
- `ACTIVE`: 대출 중
- `OVERDUE`: 연체 중
- `RETURNED`: 반납 완료
- `CANCELLED`: 취소됨

---

## 비즈니스 규칙

### 대출 규칙

| 항목 | 규칙 |
|------|------|
| **최대 대출 권수** | 5권 |
| **대출 기간** | 7일 / 14일 / 21일 / 30일 중 선택 (기본 14일) |
| **최대 연장 횟수** | 3회 |
| **연장 시기** | 반납 예정일 3일 전부터 가능 |
| **연체료** | 1일당 1,000원 |

### 대출 가능 조건
- 도서가 대출 가능한 상태 (`available: true`)
- 회원의 현재 대출 중인 책 수 < 최대 대출 권수 (5권)
- 회원에게 연체 중인 책이 없음

### 연장 가능 조건
- 대출 상태가 `ACTIVE`인 경우
- 연장 횟수가 최대 연장 횟수(3회) 미만
- 반납 예정일 3일 전부터 (너무 이르면 불가)
- 연체 상태가 아닌 경우

---

## 관리자 기능

### 1. 대출 목록 조회하기

관리자가 가장 먼저 보게 되는 화면입니다. 모든 회원의 대출 기록을 한눈에 볼 수 있습니다.

#### 화면 흐름

Admin > Loans > 목록 (페이지 로드 시 자동 API 호출)

**페이지**: `/admin/loans`

#### API 명세

**엔드포인트**: `GET /api/admin/loans`

**Query Parameters**:

| 파라미터 | 타입 | 설명 | 기본값 |
|----------|------|------|--------|
| `page` | number | 페이지 번호 (0부터 시작) | 0 |
| `size` | number | 페이지 당 항목 수 | 10 |
| `searchQuery` | string | 도서명, 회원명, 이메일 검색 | - |
| `statusFilter` | string | ACTIVE, OVERDUE, RETURNED, ALL | ALL |
| `sortKey` | string | loanDate, dueDate, bookTitle | loanDate |
| `sortOrder` | string | asc, desc | desc |

#### curl 예제

```bash
# 기본 조회 (첫 페이지, 10개)
curl -X GET "http://localhost:8080/api/admin/loans" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"

# 검색: "James"라는 이름이 포함된 대출
curl -X GET "http://localhost:8080/api/admin/loans?searchQuery=James" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 연체 대출만 조회
curl -X GET "http://localhost:8080/api/admin/loans?statusFilter=OVERDUE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 반납일 기준 오름차순 정렬
curl -X GET "http://localhost:8080/api/admin/loans?sortKey=dueDate&sortOrder=asc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 성공 응답 (200 OK)

```json
{
  "content": [
    {
      "id": 1,
      "bookId": 15,
      "bookTitle": "The Silent Patient",
      "bookAuthor": "Alex Michaelides",
      "bookIsbn": "978-1234567890",
      "memberId": 4,
      "memberName": "James Wilson",
      "memberEmail": "james.wilson@email.com",
      "loanDate": "2025-10-02T11:45:00",
      "dueDate": "2025-10-16T11:45:00",
      "returnDate": null,
      "status": "ACTIVE",
      "extensionCount": 0,
      "overdueFee": 0,
      "isOverdue": false,
      "overdueDays": 0,
      "daysUntilDue": 14,
      "canExtendNow": false
    }
  ],
  "totalPages": 5,
  "totalElements": 50,
  "number": 0,
  "size": 10
}
```

---

### 2. 대출 생성하기

관리자가 오프라인 요청을 받아 새로운 대출을 등록합니다.

#### 화면 흐름

Admin > Loans > 목록 > New Loan 버튼 > 회원/도서 선택 > Create Loan (성공 시 목록으로 이동)

**페이지**: `/admin/loans/add`

#### API 명세

**엔드포인트**: `POST /api/admin/loans`

**Request Body**:

```json
{
  "memberId": 4,
  "bookId": 15,
  "loanDays": 14
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `memberId` | Long | O | 회원 ID |
| `bookId` | Long | O | 도서 ID |
| `loanDays` | Integer | X | 대출 기간 (7, 14, 21, 30 중 선택, 기본 14) |

**비즈니스 로직**:
- 서버는 `bookId`로 도서 정보 조회 후 `bookTitle`, `bookAuthor` 자동 설정
- 서버는 `memberId`로 회원 정보 조회 후 `memberName`, `memberEmail` 자동 설정
- 도서의 `available` 상태를 `false`로 변경
- 초기 상태는 `ACTIVE`, `extensionCount`는 0

#### curl 예제

```bash
curl -X POST "http://localhost:8080/api/admin/loans" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 4,
    "bookId": 15,
    "loanDays": 14
  }'
```

#### 성공 응답 (201 Created)

```json
{
  "id": 21,
  "bookId": 15,
  "bookTitle": "The Silent Patient",
  "bookAuthor": "Alex Michaelides",
  "bookIsbn": "978-1234567890",
  "memberId": 4,
  "memberName": "James Wilson",
  "memberEmail": "james.wilson@email.com",
  "loanDate": "2025-10-02T11:45:00",
  "dueDate": "2025-10-16T11:45:00",
  "returnDate": null,
  "status": "ACTIVE",
  "extensionCount": 0,
  "overdueFee": 0,
  "isOverdue": false,
  "daysUntilDue": 14,
  "canExtendNow": false
}
```

#### 실패 응답

**1) 도서가 이미 대출 중인 경우** (409 Conflict)

```json
{
  "timestamp": "2025-10-15T14:30:00",
  "status": 409,
  "error": "Conflict",
  "code": "BOOK_ALREADY_LOANED",
  "message": "이미 대여 중인 도서입니다: 15"
}
```

**2) 회원이 대출 권수 초과** (409 Conflict)

```json
{
  "status": 409,
  "code": "LOAN_LIMIT_EXCEEDED",
  "message": "대여 가능 권수를 초과했습니다. (현재: 5/5)"
}
```

**3) 회원에게 연체 중인 대출이 있는 경우** (409 Conflict)

```json
{
  "status": 409,
  "code": "OVERDUE_LOANS_EXIST",
  "message": "연체 중인 도서가 있습니다. 먼저 반납해주세요."
}
```

---

### 3. 대출 상세 조회하기

특정 대출의 모든 정보를 확인하고 관리합니다.

#### 화면 흐름

Admin > Loans > 목록 > 대출 항목 클릭 > 대출 상세

**페이지**: `/admin/loans/:id`

#### API 명세

**엔드포인트**: `GET /api/admin/loans/{id}`

**Path Parameters**:
- `id`: 대출 ID (number)

#### curl 예제

```bash
curl -X GET "http://localhost:8080/api/admin/loans/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

#### 성공 응답 (200 OK)

```json
{
  "id": 1,
  "bookId": 15,
  "bookTitle": "The Silent Patient",
  "bookAuthor": "Alex Michaelides",
  "bookIsbn": "978-1234567890",
  "memberId": 4,
  "memberName": "James Wilson",
  "memberEmail": "james.wilson@email.com",
  "loanDate": "2025-10-02T11:45:00",
  "dueDate": "2025-10-16T11:45:00",
  "returnDate": null,
  "status": "ACTIVE",
  "extensionCount": 0,
  "overdueFee": 0,
  "isOverdue": false,
  "overdueDays": 0,
  "daysUntilDue": 3,
  "canExtendNow": true
}
```

#### 실패 응답

**대출을 찾을 수 없는 경우** (404 Not Found)

```json
{
  "status": 404,
  "code": "LOAN_NOT_FOUND",
  "message": "대여를 찾을 수 없습니다: 999"
}
```

---

### 4. 반납 처리하기

대출 중인 도서를 반납 완료 상태로 변경합니다.

#### 화면 흐름

Admin > Loans > 대출 상세 > Mark as Returned 버튼 > 확인 다이얼로그 > Yes

**페이지**: `/admin/loans/:id`

#### API 명세

**엔드포인트**: `PATCH /api/admin/loans/{id}`

**Request Body**:

```json
{
  "status": "RETURNED"
}
```

**비즈니스 로직**:
- 서버는 `returnDate`를 현재 시간으로 자동 설정
- 도서의 `available` 상태를 `true`로 변경
- 연체인 경우 연체료 계산 (1일당 1,000원)

#### curl 예제

```bash
curl -X PATCH "http://localhost:8080/api/admin/loans/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "RETURNED"
  }'
```

#### 성공 응답 (200 OK)

**정상 반납 (연체 없음)**

```json
{
  "id": 1,
  "status": "RETURNED",
  "returnDate": "2025-10-15T14:30:00",
  "overdueFee": 0,
  "isOverdue": false,
  "overdueDays": 0
}
```

**연체 반납**

```json
{
  "id": 12,
  "status": "RETURNED",
  "returnDate": "2025-10-15T14:30:00",
  "overdueFee": 7000,
  "isOverdue": true,
  "overdueDays": 7
}
```

#### 실패 응답

**이미 반납된 대출인 경우** (409 Conflict)

```json
{
  "status": 409,
  "code": "ALREADY_RETURNED",
  "message": "이미 반납된 대여입니다: 1"
}
```

---

### 5. 반납일 연장하기

대출 기간을 연장합니다.

#### 화면 흐름

Admin > Loans > 대출 상세 > Extend Due Date 버튼 > 날짜 입력 > Save Extension

**페이지**: `/admin/loans/:id`

#### API 명세

**엔드포인트**: `PATCH /api/admin/loans/{id}`

**Request Body**:

```json
{
  "dueDate": "2025-10-30T23:59:59"
}
```

**비즈니스 로직**:
- 새 반납일이 현재 시간보다 미래인 경우 `status`를 `ACTIVE`로 변경
- `extensionCount` 증가

#### curl 예제

```bash
curl -X PATCH "http://localhost:8080/api/admin/loans/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dueDate": "2025-10-30T23:59:59"
  }'
```

#### 성공 응답 (200 OK)

```json
{
  "id": 1,
  "dueDate": "2025-10-30T23:59:59",
  "status": "ACTIVE",
  "extensionCount": 1,
  "daysUntilDue": 15,
  "canExtendNow": false
}
```

---

## 사용자 기능

### 1. 도서 대출 신청하기

사용자가 직접 도서를 대출합니다.

#### 화면 흐름

Client > Books > 도서 카드 클릭 > 도서 상세 > 대출 기간 선택 > Borrow Book 버튼

**페이지**: `/client/books/:id`

#### API 명세

**엔드포인트**: `POST /api/client/loans/request`

**Request Body**:

```json
{
  "bookId": 15,
  "loanPeriod": 14
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `bookId` | Long | O | 도서 ID |
| `loanPeriod` | Integer | X | 대출 기간 (7, 14, 21, 30 중 선택, 기본 14) |

**비즈니스 로직**:
- JWT 토큰에서 회원 ID 자동 추출
- `loanDate`: 현재 시간
- `dueDate`: 현재 시간 + loanPeriod일
- 도서의 `available` 상태 확인
- 회원의 대출 가능 권수 확인
- 회원의 연체 여부 확인

#### curl 예제

```bash
curl -X POST "http://localhost:8080/api/client/loans/request" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 15,
    "loanPeriod": 14
  }'
```

#### 성공 응답 (201 Created)

```json
{
  "id": 25,
  "bookId": 15,
  "bookTitle": "The Silent Patient",
  "bookAuthor": "Alex Michaelides",
  "bookIsbn": "978-1234567890",
  "memberId": 4,
  "memberName": "James Wilson",
  "memberEmail": "james.wilson@email.com",
  "loanDate": "2025-10-15T10:00:00",
  "dueDate": "2025-10-29T10:00:00",
  "status": "ACTIVE",
  "extensionCount": 0,
  "overdueFee": 0,
  "isOverdue": false,
  "daysUntilDue": 14,
  "canExtendNow": false
}
```

#### 실패 응답

**도서가 대출 불가능한 경우** (409 Conflict)

```json
{
  "status": 409,
  "code": "BOOK_NOT_AVAILABLE",
  "message": "대여할 수 없는 도서입니다: 15"
}
```

**대출 권수 초과** (409 Conflict)

```json
{
  "status": 409,
  "code": "LOAN_LIMIT_EXCEEDED",
  "message": "대여 가능 권수를 초과했습니다. (현재: 5/5)"
}
```

**연체 중인 도서가 있는 경우** (409 Conflict)

```json
{
  "status": 409,
  "code": "OVERDUE_LOANS_EXIST",
  "message": "연체 중인 도서가 있습니다. 먼저 반납해주세요."
}
```

---

### 2. 내 대출 조회하기

사용자가 자신의 대출 기록을 확인합니다.

#### 화면 흐름

Client > My Loans (헤더 메뉴에서 클릭, 페이지 로드 시 자동 API 호출)

**페이지**: `/client/my-loans`

#### API 명세

**엔드포인트**: `GET /api/client/loans`

**Query Parameters**:

| 파라미터 | 타입 | 설명 | 기본값 |
|----------|------|------|--------|
| `statusFilter` | string | ACTIVE, OVERDUE, RETURNED, ALL | ALL |

**인증**: JWT 토큰에서 회원 ID 자동 추출

#### curl 예제

```bash
# 내 모든 대출 조회
curl -X GET "http://localhost:8080/api/client/loans" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 대출 중인 도서만 조회
curl -X GET "http://localhost:8080/api/client/loans?statusFilter=ACTIVE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 연체 중인 도서만 조회
curl -X GET "http://localhost:8080/api/client/loans?statusFilter=OVERDUE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 성공 응답 (200 OK)

```json
[
  {
    "id": 20,
    "bookId": 1,
    "bookTitle": "Atomic Habits",
    "bookAuthor": "James Clear",
    "bookIsbn": "978-0735211292",
    "memberId": 4,
    "memberName": "James Wilson",
    "memberEmail": "james.wilson@email.com",
    "loanDate": "2025-10-05T10:00:00",
    "dueDate": "2025-10-19T10:00:00",
    "returnDate": null,
    "status": "ACTIVE",
    "extensionCount": 0,
    "overdueFee": 0,
    "isOverdue": false,
    "daysUntilDue": 4,
    "canExtendNow": true
  },
  {
    "id": 12,
    "bookId": 7,
    "bookTitle": "Circe",
    "bookAuthor": "Madeline Miller",
    "bookIsbn": "978-0316556347",
    "memberId": 4,
    "memberName": "James Wilson",
    "memberEmail": "james.wilson@email.com",
    "loanDate": "2025-09-18T11:20:00",
    "dueDate": "2025-10-02T11:20:00",
    "returnDate": null,
    "status": "OVERDUE",
    "extensionCount": 2,
    "overdueFee": 13000,
    "isOverdue": true,
    "overdueDays": 13,
    "daysUntilDue": 0,
    "canExtendNow": false
  }
]
```

---

### 3. 반납 신청하기

사용자가 직접 도서 반납을 신청합니다.

#### 화면 흐름

Client > My Loans > 대출 카드 > Return Book 버튼 > 확인 다이얼로그 > Yes

**페이지**: `/client/my-loans`

#### API 명세

**엔드포인트**: `POST /api/client/loans/{id}/return`

**Path Parameters**:
- `id`: 대출 ID (number)

**Request Body**: 없음

**비즈니스 로직**:
- 서버는 JWT 토큰으로 회원 ID 확인
- 해당 대출이 요청자의 것인지 검증
- `status`를 `RETURNED`로 변경
- `returnDate`를 현재 시간으로 설정
- 연체인 경우 연체료 계산

#### curl 예제

```bash
curl -X POST "http://localhost:8080/api/client/loans/20/return" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

#### 성공 응답 (200 OK)

**정상 반납**

```json
{
  "id": 20,
  "status": "RETURNED",
  "returnDate": "2025-10-15T14:30:00",
  "overdueFee": 0,
  "isOverdue": false,
  "overdueDays": 0
}
```

**연체 반납**

```json
{
  "id": 12,
  "status": "RETURNED",
  "returnDate": "2025-10-15T14:30:00",
  "overdueFee": 7000,
  "isOverdue": true,
  "overdueDays": 7
}
```

#### 실패 응답

**권한 없음 (다른 사람의 대출)** (403 Forbidden)

```json
{
  "status": 403,
  "code": "UNAUTHORIZED_ACCESS",
  "message": "해당 대출 기록에 접근할 권한이 없습니다. 대출 ID: 20"
}
```

---

### 4. 대출 이력 삭제하기

반납 완료된 대출 기록을 삭제합니다.

#### 화면 흐름

Client > My Loans > RETURNED 필터 > 대출 카드 > Delete 버튼 > 확인 다이얼로그 > Yes

**페이지**: `/client/my-loans`

#### API 명세

**엔드포인트**: `DELETE /api/client/loans/{id}`

**Path Parameters**:
- `id`: 대출 ID (number)

**제약 조건**:
- `RETURNED` 상태인 대출만 삭제 가능

#### curl 예제

```bash
curl -X DELETE "http://localhost:8080/api/client/loans/13" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 성공 응답 (204 No Content)

본문 없음

#### 실패 응답

**반납되지 않은 대출 삭제 시도** (409 Conflict)

```json
{
  "status": 409,
  "code": "CANNOT_DELETE_ACTIVE_LOAN",
  "message": "반납 완료된 대출만 삭제할 수 있습니다. 대출 ID: 20, 현재 상태: ACTIVE"
}
```

---

## 에러 코드 정리

| 에러 코드 | HTTP 상태 | 설명 |
|-----------|----------|------|
| `LOAN_NOT_FOUND` | 404 | 대출을 찾을 수 없음 |
| `BOOK_ALREADY_LOANED` | 409 | 도서가 이미 대출 중 |
| `BOOK_NOT_AVAILABLE` | 409 | 도서가 대출 불가능 |
| `LOAN_LIMIT_EXCEEDED` | 409 | 대출 권수 초과 (5권) |
| `OVERDUE_LOANS_EXIST` | 409 | 연체 중인 도서 있음 |
| `ALREADY_RETURNED` | 409 | 이미 반납됨 |
| `EXTENSION_LIMIT_EXCEEDED` | 409 | 연장 횟수 초과 (3회) |
| `EXTENSION_TOO_EARLY` | 409 | 연장 시기가 너무 이름 (3일 전부터 가능) |
| `CANNOT_DELETE_ACTIVE_LOAN` | 409 | 반납되지 않은 대출 삭제 시도 |
| `UNAUTHORIZED_ACCESS` | 403 | 권한 없음 (다른 사람의 대출) |

---

## API 엔드포인트 요약

### 관리자 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| `GET` | `/api/admin/loans` | 전체 대출 목록 조회 (페이징) |
| `GET` | `/api/admin/loans/{id}` | 대출 상세 조회 |
| `POST` | `/api/admin/loans` | 신규 대출 생성 |
| `PATCH` | `/api/admin/loans/{id}` | 대출 정보 수정 (반납, 연장) |
| `DELETE` | `/api/admin/loans/{id}` | 대출 기록 삭제 |
| `GET` | `/api/admin/loans/overdue` | 연체 대출 목록 |
| `GET` | `/api/admin/loans/active` | 현재 대출 중인 목록 |
| `GET` | `/api/admin/loans/member/{memberId}` | 회원별 대출 이력 |
| `GET` | `/api/admin/loans/book/{bookId}` | 도서별 대출 이력 |

### 사용자 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| `GET` | `/api/client/loans` | 내 대출 목록 조회 |
| `POST` | `/api/client/loans/request` | 도서 대출 신청 |
| `POST` | `/api/client/loans/{id}/return` | 도서 반납 신청 |
| `DELETE` | `/api/client/loans/{id}` | 대출 이력 삭제 |

---

## 코드 참조

### 엔티티
- **Loan Entity**: `src/main/java/com/example/spring/entity/Loan.java`
- **LoanStatus Enum**: `src/main/java/com/example/spring/entity/LoanStatus.java`

### DTO
- **CreateLoanRequest**: `src/main/java/com/example/spring/dto/request/CreateLoanRequest.java`
- **ClientLoanRequest**: `src/main/java/com/example/spring/dto/request/ClientLoanRequest.java`
- **UpdateLoanRequest**: `src/main/java/com/example/spring/dto/request/UpdateLoanRequest.java`
- **LoanResponse**: `src/main/java/com/example/spring/dto/response/LoanResponse.java`

### 서비스
- **LoanService**: `src/main/java/com/example/spring/service/LoanService.java`
- **LoanServiceImpl**: `src/main/java/com/example/spring/service/impl/LoanServiceImpl.java`

### 컨트롤러
- **LoanController**: `src/main/java/com/example/spring/controller/LoanController.java`

### 예외
- **LoanException**: `src/main/java/com/example/spring/exception/LoanException.java`

---

## 학습 가이드

### 1단계: 기본 조회부터 시작

```bash
# 1. 대출 목록 조회 (가장 기본)
curl -X GET "http://localhost:8080/api/admin/loans" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 2. 특정 대출 조회
curl -X GET "http://localhost:8080/api/admin/loans/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 3. 내 대출 조회 (사용자)
curl -X GET "http://localhost:8080/api/client/loans" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2단계: 검색/필터 실습

```bash
# 검색
curl -X GET "http://localhost:8080/api/admin/loans?searchQuery=James" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 필터
curl -X GET "http://localhost:8080/api/admin/loans?statusFilter=OVERDUE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 정렬
curl -X GET "http://localhost:8080/api/admin/loans?sortKey=dueDate&sortOrder=asc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3단계: 생성/수정 실습

```bash
# 대출 생성 (관리자)
curl -X POST "http://localhost:8080/api/admin/loans" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "memberId": 4, "bookId": 15, "loanDays": 14 }'

# 대출 신청 (사용자)
curl -X POST "http://localhost:8080/api/client/loans/request" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "bookId": 15, "loanPeriod": 14 }'

# 반납 처리
curl -X PATCH "http://localhost:8080/api/admin/loans/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "status": "RETURNED" }'

# 연장
curl -X PATCH "http://localhost:8080/api/admin/loans/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "dueDate": "2025-10-30T23:59:59" }'
```

### 4단계: 에러 케이스 테스트

```bash
# 이미 대출 중인 도서 대출 시도 (409 에러 발생)
curl -X POST "http://localhost:8080/api/admin/loans" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "memberId": 4, "bookId": 1 }'

# 이미 반납된 대출 재반납 시도 (409 에러 발생)
curl -X PATCH "http://localhost:8080/api/admin/loans/14" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "status": "RETURNED" }'
```

---

**이 문서를 화면 순서대로 따라가면서 실습하면 대출 시스템 전체를 이해할 수 있습니다!**