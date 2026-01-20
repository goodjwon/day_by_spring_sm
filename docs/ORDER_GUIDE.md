# 주문(Order) 시스템 가이드

## 개요

이 문서는 온라인 서점의 주문(Order) 기능을 화면 흐름 순서로 설명합니다. 각 화면마다 어떤 API가 호출되는지, 어떻게 테스트하는지 실습하면서 배울 수 있습니다.

**대상 독자**: 백엔드 개발자, 프론트엔드 개발자, QA 테스터, 초보 개발자

---

## 기본 정보

- **Base URL**: `http://localhost:8080/api`
- **Data Format**: `application/json`
- **Authentication**: JWT Bearer Token 필요

### 데이터 모델

#### 주문 (Order)

```json
{
  "id": 1,
  "memberId": 4,
  "memberName": "James Wilson",
  "memberEmail": "james.wilson@email.com",
  "status": "PENDING",
  "orderItems": [
    {
      "id": 1,
      "bookId": 15,
      "bookTitle": "Clean Code",
      "bookAuthor": "Robert C. Martin",
      "bookIsbn": "978-0132350884",
      "quantity": 2,
      "price": 45000,
      "totalPrice": 90000
    }
  ],
  "totalAmount": 90000,
  "discountAmount": 5000,
  "finalAmount": 85000,
  "pointsUsed": 1000,
  "pointsEarned": 850,
  "couponCode": "WELCOME10",
  "payment": {
    "id": 1,
    "method": "CREDIT_CARD",
    "status": "COMPLETED",
    "amount": 84000,
    "cardCompany": "신한카드",
    "cardNumber": "1234-****-****-5678",
    "installmentMonths": 3
  },
  "delivery": {
    "id": 1,
    "recipientName": "홍길동",
    "phoneNumber": "010-1234-5678",
    "zipCode": "06234",
    "address": "서울특별시 강남구 테헤란로 123",
    "addressDetail": "101동 1001호",
    "deliveryMemo": "부재 시 경비실에 맡겨주세요",
    "status": "PREPARING",
    "trackingNumber": null,
    "courierCompany": null
  },
  "orderDate": "2025-10-15T10:00:00",
  "confirmedDate": null,
  "shippedDate": null,
  "deliveredDate": null,
  "cancelledDate": null,
  "cancellationReason": null,
  "createdDate": "2025-10-15T10:00:00",
  "updatedDate": "2025-10-15T10:00:00"
}
```

---

## 상태 정의

### 주문 상태 (OrderStatus)

| 상태 | 설명 |
|------|------|
| `PENDING` | 주문 접수됨 (초기 상태) |
| `CONFIRMED` | 주문 확인됨 (결제 완료) |
| `SHIPPED` | 배송 시작됨 |
| `DELIVERED` | 배송 완료됨 |
| `CANCELLED` | 주문 취소됨 |

### 결제 상태 (PaymentStatus)

| 상태 | 설명 |
|------|------|
| `PENDING` | 결제 대기중 |
| `COMPLETED` | 결제 완료 |
| `FAILED` | 결제 실패 |
| `CANCELLED` | 결제 취소 |
| `REFUNDED` | 환불 완료 |
| `PARTIAL_REFUNDED` | 부분 환불 |

### 배송 상태 (DeliveryStatus)

| 상태 | 설명 |
|------|------|
| `PREPARING` | 배송 준비중 |
| `IN_TRANSIT` | 배송중 |
| `OUT_FOR_DELIVERY` | 배송지 도착 |
| `DELIVERED` | 배송 완료 |
| `FAILED` | 배송 실패 |
| `RETURNED` | 반품 |

### 결제 수단 (PaymentMethod)

| 수단 | 설명 |
|------|------|
| `CREDIT_CARD` | 신용카드 |
| `DEBIT_CARD` | 체크카드 |
| `BANK_TRANSFER` | 계좌이체 |
| `VIRTUAL_ACCOUNT` | 가상계좌 |
| `KAKAO_PAY` | 카카오페이 |
| `NAVER_PAY` | 네이버페이 |
| `TOSS_PAY` | 토스페이 |
| `PAYCO` | 페이코 |
| `PHONE_BILL` | 휴대폰 결제 |

---

## 비즈니스 규칙

### 주문 규칙

| 항목 | 규칙 |
|------|------|
| **최소 주문 항목** | 1개 이상 |
| **주문 취소 가능 상태** | `PENDING`, `CONFIRMED` |
| **배송 시작 가능 상태** | `CONFIRMED` |
| **배송 완료 가능 상태** | `SHIPPED` |

### 주문 상태 전이

```
PENDING → CONFIRMED → SHIPPED → DELIVERED
    ↓         ↓
CANCELLED  CANCELLED
```

- `PENDING` → `CONFIRMED`: 주문 확인 (결제 완료)
- `CONFIRMED` → `SHIPPED`: 배송 시작
- `SHIPPED` → `DELIVERED`: 배송 완료
- `PENDING` / `CONFIRMED` → `CANCELLED`: 주문 취소 (배송 시작 후에는 취소 불가)

---

## 주문 생성하기

새로운 주문을 생성합니다.

### 화면 흐름

Cart > Checkout > 배송정보 입력 > 결제정보 입력 > Place Order (성공 시 주문 완료 페이지로 이동)

**페이지**: `/checkout`

### API 명세

**엔드포인트**: `POST /api/orders`

**Request Body**:

```json
{
  "memberId": 4,
  "items": [
    {
      "bookId": 15,
      "quantity": 2
    },
    {
      "bookId": 23,
      "quantity": 1
    }
  ],
  "discountAmount": 5000,
  "pointsUsed": 1000,
  "couponCode": "WELCOME10",
  "payment": {
    "method": "CREDIT_CARD",
    "amount": 84000,
    "pgProvider": "토스페이먼츠",
    "cardCompany": "신한카드",
    "cardNumber": "1234-****-****-5678",
    "installmentMonths": 3
  },
  "delivery": {
    "recipientName": "홍길동",
    "phoneNumber": "010-1234-5678",
    "zipCode": "06234",
    "address": "서울특별시 강남구 테헤란로 123",
    "addressDetail": "101동 1001호",
    "deliveryMemo": "부재 시 경비실에 맡겨주세요"
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `memberId` | Long | O | 회원 ID |
| `items` | List | O | 주문 항목 목록 (최소 1개) |
| `items[].bookId` | Long | O | 도서 ID |
| `items[].quantity` | Integer | O | 수량 (최소 1) |
| `discountAmount` | BigDecimal | X | 할인 금액 (기본 0) |
| `pointsUsed` | Integer | X | 사용 포인트 (기본 0) |
| `couponCode` | String | X | 쿠폰 코드 |
| `payment` | Object | O | 결제 정보 |
| `payment.method` | String | O | 결제 수단 |
| `payment.amount` | BigDecimal | O | 결제 금액 |
| `delivery` | Object | O | 배송 정보 |
| `delivery.recipientName` | String | O | 수령인 이름 |
| `delivery.phoneNumber` | String | O | 전화번호 (형식: 010-1234-5678) |
| `delivery.address` | String | O | 배송 주소 |

**비즈니스 로직**:
- 회원 ID로 회원 정보 조회
- 각 도서 ID로 도서 정보 조회 및 금액 계산
- 삭제된 도서(`isDeleted: true`)는 주문 불가
- 총 금액(`totalAmount`) = 각 (도서 가격 × 수량)의 합
- 최종 금액(`finalAmount`) = 총 금액 - 할인 금액
- 결제/배송 정보 함께 생성
- 주문 확인 이메일 발송

### curl 예제

```bash
curl -X POST "http://localhost:8080/api/orders" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 4,
    "items": [
      { "bookId": 15, "quantity": 2 },
      { "bookId": 23, "quantity": 1 }
    ],
    "discountAmount": 5000,
    "pointsUsed": 1000,
    "couponCode": "WELCOME10",
    "payment": {
      "method": "CREDIT_CARD",
      "amount": 84000,
      "pgProvider": "토스페이먼츠",
      "cardCompany": "신한카드",
      "cardNumber": "1234-****-****-5678",
      "installmentMonths": 3
    },
    "delivery": {
      "recipientName": "홍길동",
      "phoneNumber": "010-1234-5678",
      "zipCode": "06234",
      "address": "서울특별시 강남구 테헤란로 123",
      "addressDetail": "101동 1001호",
      "deliveryMemo": "부재 시 경비실에 맡겨주세요"
    }
  }'
```

### 성공 응답 (201 Created)

```json
{
  "id": 1,
  "memberId": 4,
  "memberName": "James Wilson",
  "memberEmail": "james.wilson@email.com",
  "status": "PENDING",
  "orderItems": [
    {
      "id": 1,
      "bookId": 15,
      "bookTitle": "Clean Code",
      "bookAuthor": "Robert C. Martin",
      "bookIsbn": "978-0132350884",
      "quantity": 2,
      "price": 45000,
      "totalPrice": 90000
    },
    {
      "id": 2,
      "bookId": 23,
      "bookTitle": "Effective Java",
      "bookAuthor": "Joshua Bloch",
      "bookIsbn": "978-0134685991",
      "quantity": 1,
      "price": 38000,
      "totalPrice": 38000
    }
  ],
  "totalAmount": 128000,
  "discountAmount": 5000,
  "finalAmount": 123000,
  "pointsUsed": 1000,
  "pointsEarned": 1230,
  "couponCode": "WELCOME10",
  "orderDate": "2025-10-15T10:00:00"
}
```

### 실패 응답

**1) 회원을 찾을 수 없는 경우** (404 Not Found)

```json
{
  "status": 404,
  "code": "MEMBER_NOT_FOUND",
  "message": "회원을 찾을 수 없습니다. ID: 999"
}
```

**2) 도서를 찾을 수 없는 경우** (404 Not Found)

```json
{
  "status": 404,
  "code": "BOOK_NOT_FOUND",
  "message": "도서를 찾을 수 없습니다. ID: 999"
}
```

**3) 삭제된 도서를 주문하려는 경우** (409 Conflict)

```json
{
  "status": 409,
  "code": "DELETED_BOOK_ACCESS",
  "message": "삭제된 도서는 주문할 수 없습니다: Clean Code"
}
```

**4) 주문 항목이 비어있는 경우** (400 Bad Request)

```json
{
  "status": 400,
  "code": "EMPTY_ORDER_ITEMS",
  "message": "주문 항목이 비어있습니다."
}
```

---

## 주문 목록 조회하기

전체 주문 목록을 조회합니다.

### 화면 흐름

Admin > Orders > 목록 (페이지 로드 시 자동 API 호출)

**페이지**: `/admin/orders`

### API 명세

**엔드포인트**: `GET /api/orders`

### curl 예제

```bash
curl -X GET "http://localhost:8080/api/orders" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### 성공 응답 (200 OK)

```json
[
  {
    "id": 1,
    "memberId": 4,
    "memberName": "James Wilson",
    "memberEmail": "james.wilson@email.com",
    "status": "PENDING",
    "totalAmount": 128000,
    "discountAmount": 5000,
    "finalAmount": 123000,
    "orderDate": "2025-10-15T10:00:00"
  },
  {
    "id": 2,
    "memberId": 5,
    "memberName": "홍길동",
    "memberEmail": "hong@example.com",
    "status": "DELIVERED",
    "totalAmount": 45000,
    "discountAmount": 0,
    "finalAmount": 45000,
    "orderDate": "2025-10-14T15:30:00"
  }
]
```

---

## 주문 상세 조회하기

특정 주문의 모든 정보를 확인합니다.

### 화면 흐름

Admin > Orders > 목록 > 주문 항목 클릭 > 주문 상세

**페이지**: `/admin/orders/:id`

### API 명세

**엔드포인트**: `GET /api/orders/{id}`

**Path Parameters**:
- `id`: 주문 ID (number)

### curl 예제

```bash
curl -X GET "http://localhost:8080/api/orders/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### 성공 응답 (200 OK)

```json
{
  "id": 1,
  "memberId": 4,
  "memberName": "James Wilson",
  "memberEmail": "james.wilson@email.com",
  "status": "PENDING",
  "orderItems": [...],
  "totalAmount": 128000,
  "discountAmount": 5000,
  "finalAmount": 123000,
  "pointsUsed": 1000,
  "pointsEarned": 1230,
  "couponCode": "WELCOME10",
  "payment": {...},
  "delivery": {...},
  "orderDate": "2025-10-15T10:00:00",
  "confirmedDate": null,
  "shippedDate": null,
  "deliveredDate": null,
  "cancelledDate": null,
  "cancellationReason": null,
  "createdDate": "2025-10-15T10:00:00",
  "updatedDate": "2025-10-15T10:00:00"
}
```

### 실패 응답

**주문을 찾을 수 없는 경우** (404 Not Found)

```json
{
  "status": 404,
  "code": "ORDER_NOT_FOUND",
  "message": "주문을 찾을 수 없습니다. ID: 999"
}
```

---

## 주문 확인하기

접수된 주문을 확인 처리합니다.

### 화면 흐름

Admin > Orders > 주문 상세 > Confirm Order 버튼

**페이지**: `/admin/orders/:id`

### API 명세

**엔드포인트**: `PATCH /api/orders/{id}/confirm`

**Path Parameters**:
- `id`: 주문 ID (number)

**비즈니스 로직**:
- 현재 상태가 `PENDING`인 경우에만 확인 가능
- `status`를 `CONFIRMED`로 변경
- `confirmedDate`를 현재 시간으로 설정

### curl 예제

```bash
curl -X PATCH "http://localhost:8080/api/orders/1/confirm" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### 성공 응답 (200 OK)

```json
{
  "id": 1,
  "status": "CONFIRMED",
  "confirmedDate": "2025-10-15T14:30:00",
  "orderDate": "2025-10-15T10:00:00"
}
```

### 실패 응답

**이미 확인된 주문인 경우** (409 Conflict)

```json
{
  "status": 409,
  "code": "INVALID_ORDER_STATE",
  "message": "확인할 수 없는 주문입니다. 현재 상태: CONFIRMED"
}
```

---

## 배송 시작하기

주문의 배송을 시작합니다.

### 화면 흐름

Admin > Orders > 주문 상세 > Ship Order 버튼 > 운송장 번호 입력

**페이지**: `/admin/orders/:id`

### API 명세

**엔드포인트**: `PATCH /api/orders/{id}/ship`

**Query Parameters**:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `trackingNumber` | string | O | 운송장 번호 |
| `courierCompany` | string | O | 택배사명 |

**비즈니스 로직**:
- 현재 상태가 `CONFIRMED`인 경우에만 배송 시작 가능
- `status`를 `SHIPPED`로 변경
- `shippedDate`를 현재 시간으로 설정
- 배송 정보에 운송장 번호 및 택배사 정보 저장

### curl 예제

```bash
curl -X PATCH "http://localhost:8080/api/orders/1/ship?trackingNumber=123456789&courierCompany=CJ대한통운" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### 성공 응답 (200 OK)

```json
{
  "id": 1,
  "status": "SHIPPED",
  "shippedDate": "2025-10-16T09:00:00",
  "delivery": {
    "id": 1,
    "status": "IN_TRANSIT",
    "trackingNumber": "123456789",
    "courierCompany": "CJ대한통운",
    "shippedDate": "2025-10-16T09:00:00"
  }
}
```

### 실패 응답

**배송 시작할 수 없는 상태인 경우** (409 Conflict)

```json
{
  "status": 409,
  "code": "INVALID_ORDER_STATE",
  "message": "배송을 시작할 수 없는 주문입니다. 현재 상태: PENDING"
}
```

---

## 배송 완료 처리하기

주문의 배송을 완료 처리합니다.

### 화면 흐름

Admin > Orders > 주문 상세 > Mark as Delivered 버튼

**페이지**: `/admin/orders/:id`

### API 명세

**엔드포인트**: `PATCH /api/orders/{id}/deliver`

**Path Parameters**:
- `id`: 주문 ID (number)

**비즈니스 로직**:
- 현재 상태가 `SHIPPED`인 경우에만 배송 완료 가능
- `status`를 `DELIVERED`로 변경
- `deliveredDate`를 현재 시간으로 설정

### curl 예제

```bash
curl -X PATCH "http://localhost:8080/api/orders/1/deliver" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### 성공 응답 (200 OK)

```json
{
  "id": 1,
  "status": "DELIVERED",
  "deliveredDate": "2025-10-17T14:00:00",
  "delivery": {
    "id": 1,
    "status": "DELIVERED",
    "deliveredDate": "2025-10-17T14:00:00"
  }
}
```

### 실패 응답

**배송 완료 처리할 수 없는 상태인 경우** (409 Conflict)

```json
{
  "status": 409,
  "code": "INVALID_ORDER_STATE",
  "message": "배송완료 처리할 수 없는 주문입니다. 현재 상태: CONFIRMED"
}
```

---

## 주문 취소하기

주문을 취소합니다.

### 화면 흐름

Admin > Orders > 주문 상세 > Cancel Order 버튼 > 취소 사유 입력 > Confirm

**페이지**: `/admin/orders/:id`

### API 명세

**엔드포인트**: `PATCH /api/orders/{id}/cancel`

**Query Parameters**:

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `reason` | string | X | "고객 요청" | 취소 사유 |

**비즈니스 로직**:
- `PENDING` 또는 `CONFIRMED` 상태에서만 취소 가능
- `SHIPPED`, `DELIVERED` 상태에서는 취소 불가
- `status`를 `CANCELLED`로 변경
- `cancelledDate`를 현재 시간으로 설정
- `cancellationReason`에 취소 사유 저장

### curl 예제

```bash
# 기본 취소 (사유: 고객 요청)
curl -X PATCH "http://localhost:8080/api/orders/1/cancel" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"

# 사유 지정 취소
curl -X PATCH "http://localhost:8080/api/orders/1/cancel?reason=품절로%20인한%20취소" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### 성공 응답 (200 OK)

```json
{
  "id": 1,
  "status": "CANCELLED",
  "cancelledDate": "2025-10-15T16:00:00",
  "cancellationReason": "품절로 인한 취소"
}
```

### 실패 응답

**취소할 수 없는 상태인 경우** (409 Conflict)

```json
{
  "status": 409,
  "code": "ORDER_CANCELLATION_NOT_ALLOWED",
  "message": "주문을 취소할 수 없습니다. 주문 ID: 1, 현재 상태: SHIPPED"
}
```

---

## 검색 및 필터 기능

### 상태별 주문 조회

**엔드포인트**: `GET /api/orders/status/{status}`

```bash
# 대기 중인 주문 조회
curl -X GET "http://localhost:8080/api/orders/status/PENDING" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 배송 중인 주문 조회
curl -X GET "http://localhost:8080/api/orders/status/SHIPPED" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 날짜 범위로 주문 조회

**엔드포인트**: `GET /api/orders/date-range`

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `startDate` | ISO DateTime | O | 시작 날짜 |
| `endDate` | ISO DateTime | O | 종료 날짜 |

```bash
curl -X GET "http://localhost:8080/api/orders/date-range?startDate=2025-10-01T00:00:00&endDate=2025-10-31T23:59:59" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 금액 범위로 주문 조회

**엔드포인트**: `GET /api/orders/amount-range`

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `minAmount` | BigDecimal | O | 최소 금액 |
| `maxAmount` | BigDecimal | O | 최대 금액 |

```bash
curl -X GET "http://localhost:8080/api/orders/amount-range?minAmount=50000&maxAmount=200000" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 특정 도서가 포함된 주문 조회

**엔드포인트**: `GET /api/orders/book/{bookId}`

```bash
curl -X GET "http://localhost:8080/api/orders/book/15" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 통계 및 분석 기능

### 주문 통계 조회

**엔드포인트**: `GET /api/orders/statistics`

```bash
curl -X GET "http://localhost:8080/api/orders/statistics" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 성공 응답 (200 OK)

```json
{
  "totalOrders": 150,
  "pendingOrders": 10,
  "confirmedOrders": 25,
  "shippedOrders": 15,
  "deliveredOrders": 90,
  "cancelledOrders": 10,
  "totalRevenue": 12500000
}
```

### 기간별 매출 조회

**엔드포인트**: `GET /api/orders/revenue`

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `startDate` | ISO DateTime | O | 시작 날짜 |
| `endDate` | ISO DateTime | O | 종료 날짜 |

```bash
curl -X GET "http://localhost:8080/api/orders/revenue?startDate=2025-10-01T00:00:00&endDate=2025-10-31T23:59:59" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 성공 응답 (200 OK)

```json
{
  "startDate": "2025-10-01T00:00:00",
  "endDate": "2025-10-31T23:59:59",
  "revenue": 5250000
}
```

---

## 에러 코드 정리

| 에러 코드 | HTTP 상태 | 설명 |
|-----------|----------|------|
| `ORDER_NOT_FOUND` | 404 | 주문을 찾을 수 없음 |
| `MEMBER_NOT_FOUND` | 404 | 회원을 찾을 수 없음 |
| `BOOK_NOT_FOUND` | 404 | 도서를 찾을 수 없음 |
| `DELETED_BOOK_ACCESS` | 409 | 삭제된 도서 주문 시도 |
| `EMPTY_ORDER_ITEMS` | 400 | 주문 항목이 비어있음 |
| `ORDER_CANCELLATION_NOT_ALLOWED` | 409 | 주문 취소 불가 (배송 시작 후) |
| `INVALID_ORDER_STATE` | 409 | 잘못된 주문 상태 전이 |
| `PAYMENT_AMOUNT_MISMATCH` | 409 | 결제 금액 불일치 |

---

## API 엔드포인트 요약

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| `POST` | `/api/orders` | 주문 생성 |
| `GET` | `/api/orders` | 전체 주문 목록 조회 |
| `GET` | `/api/orders/{id}` | 주문 상세 조회 |
| `PATCH` | `/api/orders/{id}/confirm` | 주문 확인 |
| `PATCH` | `/api/orders/{id}/ship` | 배송 시작 |
| `PATCH` | `/api/orders/{id}/deliver` | 배송 완료 |
| `PATCH` | `/api/orders/{id}/cancel` | 주문 취소 |
| `GET` | `/api/orders/status/{status}` | 상태별 주문 조회 |
| `GET` | `/api/orders/date-range` | 날짜 범위 주문 조회 |
| `GET` | `/api/orders/amount-range` | 금액 범위 주문 조회 |
| `GET` | `/api/orders/book/{bookId}` | 도서별 주문 조회 |
| `GET` | `/api/orders/statistics` | 주문 통계 조회 |
| `GET` | `/api/orders/revenue` | 기간별 매출 조회 |

---

## 코드 참조

### 엔티티
- **Order Entity**: `src/main/java/com/example/spring/entity/Order.java`
- **OrderItem Entity**: `src/main/java/com/example/spring/entity/OrderItem.java`
- **OrderStatus Enum**: `src/main/java/com/example/spring/entity/OrderStatus.java`
- **Payment Entity**: `src/main/java/com/example/spring/entity/Payment.java`
- **PaymentMethod Enum**: `src/main/java/com/example/spring/entity/PaymentMethod.java`
- **PaymentStatus Enum**: `src/main/java/com/example/spring/entity/PaymentStatus.java`
- **Delivery Entity**: `src/main/java/com/example/spring/entity/Delivery.java`
- **DeliveryStatus Enum**: `src/main/java/com/example/spring/entity/DeliveryStatus.java`

### DTO
- **CreateOrderRequest**: `src/main/java/com/example/spring/dto/CreateOrderRequest.java`
- **OrderItemRequest**: `src/main/java/com/example/spring/dto/OrderItemRequest.java`
- **PaymentRequest**: `src/main/java/com/example/spring/dto/PaymentRequest.java`
- **DeliveryRequest**: `src/main/java/com/example/spring/dto/DeliveryRequest.java`
- **OrderResponse**: `src/main/java/com/example/spring/dto/response/OrderResponse.java`
- **OrderItemResponse**: `src/main/java/com/example/spring/dto/response/OrderItemResponse.java`

### 서비스
- **OrderService**: `src/main/java/com/example/spring/service/OrderService.java`
- **OrderServiceImpl**: `src/main/java/com/example/spring/service/impl/OrderServiceImpl.java`

### 컨트롤러
- **OrderController**: `src/main/java/com/example/spring/controller/OrderController.java`

### 예외
- **OrderException**: `src/main/java/com/example/spring/exception/OrderException.java`

### 리포지토리
- **OrderRepository**: `src/main/java/com/example/spring/repository/OrderRepository.java`
- **OrderItemRepository**: `src/main/java/com/example/spring/repository/OrderItemRepository.java`

---

## 학습 가이드

### 1단계: 기본 조회부터 시작

```bash
# 1. 주문 목록 조회 (가장 기본)
curl -X GET "http://localhost:8080/api/orders" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 2. 특정 주문 조회
curl -X GET "http://localhost:8080/api/orders/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 3. 주문 통계 조회
curl -X GET "http://localhost:8080/api/orders/statistics" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2단계: 검색/필터 실습

```bash
# 상태별 조회
curl -X GET "http://localhost:8080/api/orders/status/PENDING" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 날짜 범위 조회
curl -X GET "http://localhost:8080/api/orders/date-range?startDate=2025-10-01T00:00:00&endDate=2025-10-31T23:59:59" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 금액 범위 조회
curl -X GET "http://localhost:8080/api/orders/amount-range?minAmount=50000&maxAmount=200000" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3단계: 주문 생성 및 상태 변경 실습

```bash
# 주문 생성
curl -X POST "http://localhost:8080/api/orders" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 4,
    "items": [{ "bookId": 15, "quantity": 1 }],
    "payment": { "method": "CREDIT_CARD", "amount": 45000 },
    "delivery": { "recipientName": "홍길동", "phoneNumber": "010-1234-5678", "address": "서울시 강남구" }
  }'

# 주문 확인
curl -X PATCH "http://localhost:8080/api/orders/1/confirm" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 배송 시작
curl -X PATCH "http://localhost:8080/api/orders/1/ship?trackingNumber=123456789&courierCompany=CJ대한통운" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 배송 완료
curl -X PATCH "http://localhost:8080/api/orders/1/deliver" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4단계: 에러 케이스 테스트

```bash
# 이미 확인된 주문 재확인 시도 (409 에러)
curl -X PATCH "http://localhost:8080/api/orders/1/confirm" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 배송 시작 후 취소 시도 (409 에러)
curl -X PATCH "http://localhost:8080/api/orders/1/cancel" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 삭제된 도서 주문 시도 (409 에러)
curl -X POST "http://localhost:8080/api/orders" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 4,
    "items": [{ "bookId": 999, "quantity": 1 }],
    "payment": { "method": "CREDIT_CARD", "amount": 45000 },
    "delivery": { "recipientName": "홍길동", "phoneNumber": "010-1234-5678", "address": "서울시 강남구" }
  }'
```

---

**이 문서를 화면 순서대로 따라가면서 실습하면 주문 시스템 전체를 이해할 수 있습니다!**