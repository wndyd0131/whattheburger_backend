# Whattheburger (Fast Food Order System)

<!-- <p align="center">
  <b><a href="#korean">🇰🇷 한국어 (KOR)</a></b> | <b><a href="#english">🇺🇸 English (ENG)</a></b>
</p> -->

# 1. 프로젝트 소개

### 패스트푸드 주문 시스템: Whattheburger (왓더버거)

실제 패스트푸드 주문 서비스를 모델링한 풀스택 프로젝트입니다. ERD와
시퀀스 다이어그램을 기반으로 시스템을 설계하고 Spring Boot와 React
환경에서 구현했습니다. 해당 시스템을 개발하면서 재고 차감 흐름, 장바구니
저장소, 프랜차이즈 매장 데이터 관리 구조 등 서비스 특성에 따른 설계
의사결정을 통해 성능, 데이터 정합성, 확장성을 함께 고려했습니다.

### Repository

- GitHub
    - 백엔드 저장소
        - [https://github.com/wndyd0131/whattheburger_backend](https://github.com/wndyd0131/whattheburger_backend)
    - 프론트엔드 저장소
        - [https://github.com/wndyd0131/whattheburger_frontend](https://github.com/wndyd0131/whattheburger_frontend)

### 주요 기능

- 프랜차이즈 매장 검색 및 선택
- 상품 커스터마이징 (옵션, 수량 등)
- Redis 기반 장바구니
- Stripe Checkout 결제
- Webhook 기반 주문 생성
- WebSocket 실시간 주문 상태 조회
- 주문 내역 조회

### **기술 스택**

#### **백엔드**

- Spring Boot
- Spring Security
- JPA / Hibernate
- Redis
- MySQL

#### **프론트엔드**

- React

#### **인프라**

- AWS EC2
- Nginx
- Docker

#### **외부 서비스**

- Stripe
- Mapbox

#### **테스트**

- JUnit5
- AssertJ
- Testcontainers
- MockMvc
- Mockito

#### **성능 측정 및 모니터링**

- K6
- Spring Actuator

#### **기타**

- WebSocket
- Python

### 스크린샷

![image.png](docs/image.png)

![image.png](docs/image%201.png)

---

# 2. 시스템 아키텍처

## 시스템 아키텍처

![System Architecture.drawio.png](docs/System_Architecture.drawio.png)

## 서비스 구조
```mermaid
flowchart LR
    USER[사용자]

    subgraph CLIENT[Client]
        WEB[React Web]
    end

    subgraph AWS[AWS]
        NGINX[Nginx<br/>Reverse Proxy]

        subgraph BACKEND[Spring Boot Backend]
            API[REST API]
            AUTH[Authentication<br/>JWT / Guest]
            PRODUCT[Product]
            ORDER[Order & Payment]
            INVENTORY[Inventory]
            STORE[Store]
            WEBSOCKET[WebSocket]
            ORDER_TRACKING[Order Tracking]
            CART[Cart]
        end

        MYSQL[(MySQL)]
        REDIS[(Redis)]
        S3[(AWS S3)]
    end

    STRIPE[Stripe]
    MAPBOX[Mapbox API]

    USER --> WEB
    WEB -->|HTTPS / REST| NGINX
    WEB <-->|WebSocket| NGINX

    NGINX --> API
    NGINX --> WEBSOCKET

    API --> AUTH
    API --> ORDER
    API --> PRODUCT
    API --> INVENTORY
    API --> STORE
    API --> CART

    WEBSOCKET --> ORDER_TRACKING

    AUTH --> MYSQL
    PRODUCT --> MYSQL
    ORDER --> MYSQL
    STORE --> MYSQL
    INVENTORY --> MYSQL

    API --> REDIS
    ORDER --> REDIS
    CART --> REDIS

    API --> S3
    API --> MAPBOX

    ORDER -->|Checkout 생성| STRIPE
    STRIPE -->|Webhook| NGINX
```

## 서비스 저장소
```mermaid
flowchart TB
    APP[Spring Boot]

    MYSQL[(MySQL<br/>회원 · 매장 · 상품 · 주문 · 재고)]
    REDIS[(Redis<br/>장바구니 · 주문 세션 · 결제 세션)]
    S3[(S3<br/>상품 이미지)]

    APP --> MYSQL
    APP --> REDIS
    APP --> S3
```


# 시퀀스 다이어그램

## 매장 불러오기
```mermaid
sequenceDiagram
    autonumber

    actor User
    participant React
    participant StoreProductService
    participant StoreService
    participant MapBoxAPI

    User->>React: Click "Menu"

    alt Store cookie exists
        React->>StoreProductService: GET /store/{storeId}/category/product
        StoreProductService-->>React: Product list
        React-->>User: Display menu page

    else Store cookie does not exist
        React-->>User: Display store selection page

        User->>React: Input location

        React->>StoreService: GET /store/nearby
        StoreService-->>React: Nearby stores

        React->>MapBoxAPI: Request map (latitude, longitude)
        MapBoxAPI-->>React: Map component

        React-->>User: Display nearby stores

        User->>React: Click "Select"

        React->>StoreProductService: GET /store/{storeId}/category/product
        StoreProductService-->>React: Product list

        React-->>User: Display menu page
    end
```
## 장바구니 담기
```mermaid
sequenceDiagram

    actor User
    participant React
    participant StoreProductService
    participant CartService
    participant CartSessionStorage
    participant CartValidator
    participant CartCalculator
    autonumber

    User->>React: Select category
    React->>StoreProductService: GET /store/{storeId}/category/product
    StoreProductService-->>React: StoreProduct list
    React-->>User: Display menu by category

    User->>React: Select item
    React->>StoreProductService: GET /store/{storeId}/product/{storeProductId}
    StoreProductService-->>React: StoreProduct
    React-->>User: Display product details

    opt Modify menu
        User->>React: Modify options
        React->>React: dispatchRoot()

        User->>React: Modify option traits
        React->>React: dispatchRoot()
    end

    User->>React: Click "Add to Bag"

    React->>CartService: POST /store/{storeId}/cart

    CartService->>CartSessionStorage: load()
    CartSessionStorage-->>CartService: Cart




    CartService->>CartValidator: validate()
    CartValidator-->>CartService: ValidatedCart

    CartService->>CartCalculator: calculateTotalPrice()
    CartCalculator-->>CartService: CalculatedCart

    CartService->>CartSessionStorage: save()
    CartService-->>React: Cart
    React-->>User: Product added to bag
```

## 주문 생성 및 결제 요청
```mermaid
sequenceDiagram

    actor User
    participant React
    participant OrderService
    participant CartService
    participant CheckoutService
    participant Redis
    participant Stripe
    autonumber

    User->>React: Click "Checkout"


    React->>OrderService: POST /order-session/store/{storeId}
    OrderService->>CartService: Load cart
    CartService-->>OrderService: Cart
    OrderService->>OrderService: Build order session
    OrderService->>Redis: Save order session
    Redis-->>OrderService: Saved
    OrderService-->>React: Order session created

    User->>React: Enter delivery information<br/>Confirm checkout
    React->>CheckoutService: POST /checkout/{orderSessionId}

    CheckoutService->>Redis: Load order session
    Redis-->>CheckoutService: Order Session

    CheckoutService->>Redis: Save Order Session
    Redis-->>CheckoutService: Saved
    CheckoutService->>Stripe: Create checkout session
    Stripe-->>CheckoutService: Checkout URL

    CheckoutService->>Redis: Save checkout session
    Redis-->>CheckoutService: Saved

    CheckoutService-->>React: Stripe redirect url

    React->>Stripe: Redirect to checkout page
    User->>Stripe: Complete payment

    Stripe-->>React: Success URL
    React-->>User: Loading Page
    Stripe->>CheckoutService: Webhook (checkout.session.completed)
```

## 결제 처리
```mermaid
sequenceDiagram

    actor Stripe
    participant CheckoutService
    participant Redis
    participant StockValidator
    participant OrderService
    participant InventoryService
    participant OrderTrackingService
    participant CartService
    participant OrderSessionStorage
    autonumber

    Stripe->>CheckoutService: Webhook (checkout.session.completed)

    CheckoutService->>Redis: Load Checkout Session
    Redis-->>CheckoutService: Checkout Session

    CheckoutService->>Redis: Load Order Session
    Redis-->>CheckoutService: Order Session
    CheckoutService->>Redis: Update Order Session Status
    CheckoutService->>OrderService: Build Order
    OrderService->>InventoryService: Deduct Stock
    InventoryService-->>OrderService: Stock Deducted
    OrderService-->>CheckoutService: Order

    CheckoutService->>OrderTrackingService: Schedule Order Status Updates
    CheckoutService->>CartService: Clean up Cart
    CheckoutService->>OrderService: Add Order to Order Session
    OrderService->>OrderSessionStorage: Update order id
```
## 실시간 주문 상태 조회
```mermaid
sequenceDiagram
    autonumber

    participant CheckoutService
    actor User
    participant React
    participant OrderTrackingWebSocketHandler
    participant OrderTrackingService
    participant OrderService
    participant ApplicationEventPublisher
    participant WebSocketListener

    CheckoutService->>OrderTrackingService: Schedule Order Status Updates

    User->>React: Open order tracking page
    React->>OrderTrackingWebSocketHandler: Connect (/ws/track)

    loop Until order is completed
        OrderTrackingService->>OrderService: Update Order Status
        OrderService->>ApplicationEventPublisher: Publish Order Status Changed Event
        ApplicationEventPublisher->>WebSocketListener: Deliver Event
        WebSocketListener->>OrderTrackingWebSocketHandler: Send Status Update
        OrderTrackingWebSocketHandler-->>React: Order Status Update
        React->>React: Update UI
    end
```
---

# 3. 도메인 설계

# 카탈로그

## ERD

```mermaid
erDiagram
    category ||--o{ category : ""
    category ||--o{ category_product : ""
    product ||--o{ category_product : ""

    product ||--o{ product_option : ""
    options ||--o{ product_option : ""
    custom_rule ||--o{ product_option : ""

    product_option ||--o{ product_option_trait : ""
    option_trait ||--o{ product_option_trait : ""

    product_option ||--o{ product_option_option_quantity : ""
    options ||--o{ option_quantity : ""
    quantity ||--o{ option_quantity : ""
    option_quantity ||--o{ product_option_option_quantity : ""

    category {
        bigint category_id PK
        bigint PARENT_ID FK
        varchar name
        int order_index
    }
    category_product {
        bigint category_product_id PK
        bigint category_id FK
        bigint product_id FK
    }
    product {
        bigint product_id PK
        varchar name
        decimal price
        varchar brief_info
        varchar product_type
        double calories
    }
    options {
        bigint option_id PK
        varchar name
        varchar image_source
        double calories
    }
    custom_rule {
        bigint custom_rule_id PK
        varchar name
        varchar custom_rule_type
        int order_index
        int min_selection
        int max_selection
    }
    option_trait {
        bigint option_trait_id PK
        varchar name
        varchar label_code
        varchar option_trait_type
    }
    quantity {
        bigint quantity_id PK
        varchar quantity_type
        varchar label_code
    }
    option_quantity {
        bigint option_quantity_id PK
        bigint option_id FK
        bigint quantity_id FK
        double extra_calories
    }
    product_option {
        bigint product_option_id PK
        bigint product_id FK
        bigint option_id FK
        bigint custom_rule_id FK
        tinyint is_default
        varchar count_type
        int default_quantity
        int max_quantity
        decimal extra_price
        int order_index
    }
    product_option_trait {
        bigint product_option_trait_id PK
        bigint product_option_id FK
        bigint option_trait_id FK
        int default_selection
        decimal extra_price
        double extra_calories
    }
    product_option_option_quantity {
        bigint product_option_option_quantity_id PK
        bigint product_option_id FK
        bigint option_quantity_id FK
        decimal extra_price
        tinyint is_default
    }

```

### 주요 엔티티

| 엔티티 | 역할 |
| --- | --- |
| `product` | 치즈버거, 불고기버거와 같은  상품 |
| `option` | 치즈, 양상추, 피클과 같은 선택 가능한 재료 |
| `trait` | 토스팅, 맵기 정도 등과 같은 상세 선택 |
| `quantity` | 정수가 아닌 수량 (Small, Medium, Large) |
| `customRule` | 최소/최대 선택 개수 등 선택 규칙 |

#### 1. 상품별 옵션 구성

하나의 상품은 특정 옵션과 상세 옵션만 선택할 수 있어야 합니다.

e.g. 빅맥에는 치즈와 피클 옵션이 존재하지만 아메리카노에는 존재하지
않습니다.

이를 위해 `product`, `option`, `trait`, `quantity`를 분리하고 조인
엔티티를 통해 상품별로 허용된 옵션을 정의했습니다. 이러한 구조를 통해
상품마다 다른 옵션 구성을 가질 수 있으며, 잘못된 옵션 선택을 방지할 수
있습니다.

#### 2. 옵션 선택 규칙

옵션에 대해서는 선택 방식에 대한 제약이 필요합니다.

e.g. 버거 주문 시 "빵" 분류에서는 일반 빵, 브리오슈 빵, 없음 중 하나만
선택할 수 있지만, "토핑" 분류에서는 여러 옵션을 동시에 선택할 수
있습니다.

이를 위해 `customRule` 엔티티를 도입하여 아래와 같은 사항을 관리하도록
설계하여 규칙을 데이터를 통해 관리할 수 있도록 설계했습니다.

- 옵션 그룹 분류
- 최소 선택 개수
- 최대 선택 개수
- 화면 표시 순서

# 상품 인벤토리

## ERD

```mermaid
erDiagram
    store ||--o{ store_inventory : ""
    ingredient ||--o{ store_inventory : ""

    options ||--o{ option_ingredient : ""
    ingredient ||--o{ option_ingredient : ""

    option_quantity ||--o{ option_quantity_ingredient : ""
    ingredient ||--o{ option_quantity_ingredient : ""

    options ||--o{ option_quantity : ""
    quantity ||--o{ option_quantity : ""

    store {
        bigint store_id PK
        varchar branch
        varchar city
        varchar zipcode
    }
    ingredient {
        bigint ingredient_id PK
        varchar name
        varchar unit
    }
    store_inventory {
        bigint store_inventory_id PK
        bigint store_id FK
        bigint ingredient_id FK
        int current_stock
    }
    options {
        bigint option_id PK
        varchar name
        double calories
    }
    option_ingredient {
        bigint option_ingredient_id PK
        bigint option_id FK
        bigint ingredient_id FK
        int required_quantity
    }
    quantity {
        bigint quantity_id PK
        varchar quantity_type
        varchar label_code
    }
    option_quantity {
        bigint option_quantity_id PK
        bigint option_id FK
        bigint quantity_id FK
        double extra_calories
    }
    option_quantity_ingredient {
        bigint option_quantity_ingredient_id PK
        bigint option_quantity_id FK
        bigint ingredient_id FK
        int required_quantity
    }

```

### 주요 엔티티

| 엔티티 | 역할 |
| --- | --- |
| `store_inventory` | 매장별 재료 재고 |
| `ingredient` | 치즈, 패티, 빵 등 실제 차감 대상 재료 |
| `option` | 사용자가 선택하는 옵션 |
| `quantity` | 셀 수 없는 옵션에 대한 수량 선택 옵션 |
| `option_ingredient` | 셀 수 있는 옵션에 대한 재료량 |
| `option_quantity_ingredient` | 셀 수 없는 옵션에 대한 재료량 |

#### 재고 차감

패스트푸드는 상품의 재고를 옵션 단위로 관리합니다. 즉, 하나의 옵션이
여러 재료를 사용하는 관계입니다.

e.g. 치즈버거의 치즈 옵션은 "치즈"를 재고로 사용합니다.

따라서 재고는 `store_inventory`에서 매장별 `ingredient` 단위로 관리하고,
옵션 선택 시 셀 수 있는 수량에 대한 소모량은 `option_ingredient`, 셀 수
없는 수량에 대한 소모량은 `option_quantity_ingredient`로 분리하였습니다.

이를 통해 주문 시 선택된 옵션 조합을 기반으로 실제 차감해야 할 재료
재고를 계산할 수 있도록 설계하였습니다.

# 주문

### 주요 엔티티

| 엔티티 | 역할 |
| --- | --- |
| `orders` | 주문 기본 정보, 결제 상태, 주문 상태 |
| `order_product` | 주문한 상품의 스냅샷 |
| `order_custom_rule` | 주문 당시 옵션 그룹/선택 규칙 스냅샷 |
| `order_product_option` | 주문 당시 선택한 옵션 스냅샷 |
| `order_product_option_trait` | 주문 당시 선택한 상세 옵션 스냅샷 |

#### 주문 스냅샷

주문은 결제와 정산의 기준이 되므로, 상품 카탈로그 변경의 영향을 받으면
안 됩니다.

따라서 `orders`에 `store_product_id` 같은 참조값만 저장하지 않고, 조인
엔티티를 활용하여 주문 당시의 상품명, 가격, 옵션명, 선택값을 함께
저장하였습니다. 이를 통해 상품 정보가 변경되거나 숨김 처리되더라도 과거
주문 내역은 결제 당시 기준으로 안정적으로 조회할 수 있습니다.

## ERD

![image.png](docs/image%202.png)

![image.png](docs/image%203.png)

---

# 4. 핵심 설계 의사결정 - 재고 차감

### **문제**

사용자가 주문을 확정하면, 주문한 상품과 선택한 옵션에 필요한 재료 재고가 주문 수량만큼 차감됩니다. 서비스의 특성에 따라 "차감 흐름"과 "동시성 로직 제어"에 대해 의사결정을 내렸습니다.

### **재고 차감 시점**

| 재고 차감 시점 | 장점 | 단점 |
| :--- | :--- | :--- |
| **장바구니 담기 / 주문 시작 시점** | • 결제 전 재고 확보 가능<br>• 사용자의 주문 가능 여부 보장 | • 재고 장기 점유 가능성<br>• 예약 만료/복구 로직 필요<br>• 수정 시 재고 재계산 필요<br>• 동시성 처리 및 상태 관리 복잡 |
| **주문 확정 시점** | • 재고 점유 없음<br>• 빠른 재고 회전율 유지<br>• 낮은 재고 관리 복잡성 | • 결제 직전 품절 가능성<br>• 결제 후 취소 및 환불 처리 필요 |

#### **선택**
- 주문 확정 시점 차감

#### **이유**

패스트푸드 도메인에서는 옵션 재고 부족 가능성이 비교적 낮고, 상품/옵션
수정이 자주 발생합니다.

따라서 재고 예약 방식은 재고 차감 외에도 수정, 삭제, 만료 시점마다 재고
상태를 관리해야 하므로 구현 복잡도가 크게 증가합니다.

반면 주문 확정 시점 차감은 재고 홀딩 로직을 줄이고, 빠른 재고 회전율을
유지할 수 있습니다.

### **재고 차감 동시성 처리**

#### **선택**

- 비관적 락

#### **이유**

결제 흐름에서의 병목을 확인하던 중, 기존의 반복 원자적 업데이트 방식의 비효율을 확인했습니다. 사용자에게 주문 실패에 대한 피드백을 제공하기 위해서는, 원자적 업데이트 사용 시에는 반복문을 통해 쿼리를 여러번 날려야 합니다.
이러한 문제는 DB에 여러번 접근을 하게 되어 요청이 많아질수록 문제가 될 수 있다고 여겼습니다.

그래서, 재고를 잠금에 따라 로직을 수행하는 비관적 락과 비교하면서 어떤 방식이 더 확장성과 성능 측면에서 더 효율적인지 측정하였고, 그 결과 비관적 락을 적용해 결제 흐름을 최적화 했습니다.

#### **검증**

여러 재고를 동시에 차감해야 하는 환경에서 RPS를 기반으로 실험한 결과, 비관적 락이 2배 많은 요청량에서 2배 빠른 응답속도를 보이는 것을 확인했습니다.

1. **원자적 업데이트 - 주문 당 재고 10**
   ![image.png](docs/atomic_update_multistock_perf.png)
2. **비관적 락 - 주문 당 재고 10**
   ![image.png](docs/pessimistic_multistock_perf.png)

# 🔗 링크

[https://app.notion.com/p/06dc0bf300058383969b815cc8f257b3?source=copy_link](https://app.notion.com/p/06dc0bf300058383969b815cc8f257b3?pvs=21)

---

# **5. 핵심 설계 의사결정 - 결제 Webhook 멱등성 처리**

### **문제**

Stripe Webhook은 서버로부터 정상적인 응답을 받지 못하면 동일한 이벤트를 재전송할 수 있습니다. 따라서 동일한 결제 완료 이벤트가 여러 번 전달되더라도 **주문 생성 및 재고 차감이 중복으로 수행되지 않도록 멱등성을 보장**해야 합니다.

단순히 동일한 요청을 차단하는 것만으로는 충분하지 않았습니다. 서버의 지연이나 장애로 인해 이전 요청이 일부만 처리된 상태에서 재요청이 발생할 수 있기 때문입니다.

특히 다음과 같은 실패 상황을 고려했습니다.

| **실패 시점** | **상태** | **재요청 시 필요한 처리** |
| --- | --- | --- |
| 주문 생성 전 장애 | 주문 데이터 없음 | 결제 처리 전체 재실행 |
| 주문 생성 후 장애 | 주문 데이터 존재, 후처리 미완료 가능 | 주문 생성 제외 후처리 재실행 |
| 모든 처리 완료 후 응답 실패 | 모든 작업 완료 | 중복 작업 방지 후 정상 응답 |

### **멱등성 보장**

#### **선택**

`checkout_session_id`에 대한 **Unique 제약 조건을 통해 주문 생성을 멱등하게 처리**

#### **이유**

처음에는 Redis에 멱등성 키의 처리 상태를 저장하여 중복 요청을 차단하는 방식을 고려했습니다.

하지만 서버 장애가 발생하면 Redis의 `Processing` 상태와 실제 DB 트랜잭션의 성공 여부가 불일치할 수 있습니다. TTL을 통해 복구할 수도 있지만, TTL 이전에는 정상적인 재처리가 제한되고 TTL 이후에는 이전 요청이 여전히 실행 중일 가능성을 추가로 고려해야 했습니다.

반면 결제마다 Stripe의 고유한 `checkout_session_id`가 존재하기 때문에, 이를 주문 데이터의 Unique 제약 조건으로 관리하면 **DB가 최종적으로 보장해야 하는 "하나의 결제에 하나의 주문"이라는 불변식을 직접 보장**할 수 있습니다.

따라서 별도의 멱등성 상태를 관리하기보다 DB의 Unique 제약 조건을 최종 방어선으로 사용했습니다.

### **부분 실패에 대한 재처리**

주문 생성과 재고 차감은 `completePaidOrder()`의 하나의 트랜잭션으로 처리하지만, 주문 상태 스케줄링, 장바구니 정리, 주문 세션 업데이트 등의 후처리는 트랜잭션 외부에서 수행합니다.

모든 후처리를 하나의 DB 트랜잭션에 포함하면 트랜잭션과 DB 커넥션 점유 시간이 길어지고, 향후 후처리를 비동기 작업으로 확장하기도 어려워진다고 판단했습니다.

따라서 재요청이 발생하면 **주문 데이터의 존재 여부를 기준으로 이전 요청의 처리 지점을 판단**하도록 설계했습니다.

#### **처리 흐름**

```mermaid
flowchart TD
    start([Stripe webhook POST]) --> receive["CheckoutController<br/>webhook 수신"]
    receive --> verifySig{Signature 검증}
    verifySig -->|실패| bad400[400 Bad Request]
    verifySig -->|성공| deserialize[Session deserialize]
    deserialize --> handle["CheckoutService<br/>handleCheckoutSessionCompleted"]
    handle --> buildKey["멱등성 키 생성<br/>eventType:sessionId"]
    buildKey --> tryAcquire{"Redis SET NX<br/>TTL 5분"}

    tryAcquire -->|최초 획득| firstPath["completePaidOrder<br/>신규 Order 생성"]
    tryAcquire -->|키 이미 존재| dupPath[loadOrderByCheckoutSessionId]

    dupPath --> orderExists{Order 존재?}
    orderExists -->|Yes| commonPost
    orderExists -->|No race fallback| fallback["completePaidOrder<br/>fallback"]

    firstPath --> commonPost["scheduleOrder<br/>+ cart cleanUp<br/>+ addOrderToOrderSession"]
    fallback --> saveResult{saveOrder 결과}
    saveResult -->|성공| commonPost
    saveResult -->|checkoutSessionId unique violation| rollback["@Transactional rollback<br/>재고 차감 포함"]
    rollback --> err500[500 Internal Server Error]
    err500 --> stripeRetry[Stripe 재전송]
    stripeRetry --> dupPath

    commonPost --> ok200([200 OK])

    subgraph redisLayer [Redis]
        buildKey
        tryAcquire
    end

    subgraph dbLayer [DB]
        saveResult
        rollback
    end
```

1. **주문이 존재하지 않는 경우**
    - 주문 생성 및 재고 차감 수행
    - 후처리 수행
2. **주문이 이미 존재하는 경우**
    - 주문 생성 및 재고 차감 생략
    - 누락 가능성이 있는 후처리만 재실행
3. **후처리 작업**
    - 재실행될 수 있으므로 각 작업 역시 중복 실행에 안전하도록 처리

이를 통해 동일한 Webhook이 반복해서 전달되더라도 **주문과 재고의 정합성을 유지하면서, 장애로 인해 누락된 후처리는 복구할 수 있도록 설계했습니다.**

### **지연된 중복 요청 처리**

이전 Webhook 요청이 아직 처리 중인 상황에서 동일한 요청이 다시 들어올 수도 있습니다.

최초 요청이 성공하면 생성된 주문을 기준으로 중복 생성을 방지할 수 있고, 실패하면 이후 Stripe의 재요청을 통해 다시 처리할 수 있습니다. 최종적으로 Unique 제약 조건이 동시에 들어온 요청에 대해서도 중복 주문 생성을 방지합니다.

### **결과**

Webhook의 중복 요청을 단순 차단하는 방식에서 벗어나 **서버 장애와 부분 실패를 고려하여 재처리 가능한 결제 흐름**을 구성했습니다.

- 동일 결제에 대한 중복 주문 및 재고 차감 방지
- 주문 생성 전 장애 발생 시 전체 결제 처리 재시도
- 주문 생성 후 장애 발생 시 누락된 후처리 복구
- DB Unique 제약 조건을 통한 최종 데이터 정합성 보장


# 6. **핵심 설계 의사결정 - 프랜차이즈 상품 데이터 구조**

### 문제

프랜차이즈 매장의 공통 상품 정보는 재사용하면서 매장별 다른 구성을 지원하도록 설계했습니다.

### 후보 비교

| 방식 | 장점 | 단점 |
| :--- | :--- | :--- |
| **단일 카탈로그 참조** | • 구조 단순<br>• 데이터 중복 없음<br>• 본사 상품 변경 즉시 반영 | • 매장별 상품 추가, 가격 변경, 판매 숨김 처리 어려움 |
| **매장별 전체 카탈로그 복제** | • 매장별 독립 구성 가능<br>• 조회 로직 단순 | • 데이터 중복 큼<br>• 본사 상품 변경 시 전체 매장 동기화 필요<br>• 동기화 누락 시 불일치 발생 |
| **카탈로그 + 추가/변경 테이블** | • 데이터 중복 감소<br>• 매장별 독립 구성 가능<br>• 본사 상품 변경 즉시 반영 | • 조회 로직 복잡도 및 조회 비용 증가 |

### 선택

카탈로그 + 추가/변경 테이블 구조

### 이유

프랜차이즈 매장은 대부분 본사에서 승인한 공통 상품을 판매하지만, 일부
매장은 지역 한정 상품을 추가하거나 상품 가격과 판매 여부를 다르게 운영할
수 있습니다.

단일 카탈로그 참조 방식은 데이터 중복이 없지만 매장별 운영 차이를
반영하기 어렵고, 매장별 전체 카탈로그 복제 방식은 유연하지만 데이터
중복과 동기화 비용이 큽니다.

따라서 공통 상품 정보는 카탈로그 테이블에 저장하고, 매장별 추가 상품은
StoreAdd, 상품의 변경사항은 StoreDelta에 분리하여 저장했습니다. 이를
통해 데이터 중복을 줄이면서도 매장별 구성을 유연하게 변동할 수 있도록
설계했습니다.

### 트레이드오프

해당 방식은 조회 시 Catalog와 StoreAdd/StoreDelta를 병합해야 하므로 쿼리
및 서비스 로직이 복잡해지고 조회 비용이 증가합니다. 대신 공통 데이터의
중복 저장을 피하면서 매장별 운영 정책을 유연하게 반영할 수 있습니다.

# ERD

```mermaid
erDiagram
    product ||--o{ product_option : ""
    product ||--o{ store_product : ""

    options ||--o{ product_option : ""
    custom_rule ||--o{ product_option : ""

    store ||--o{ store_product : ""

    store_product ||--o{ store_option_add : ""
    store_product ||--o{ store_option_delta : ""

    options ||--o{ store_option_add : ""
    custom_rule ||--o{ store_option_add : ""

    product_option ||--o{ store_option_delta : ""

    product {
        bigint product_id PK
        varchar name
        decimal price
        varchar brief_info
        varchar image_source
        double calories
        varchar product_type
    }

    product_option {
        bigint product_option_id PK
        bigint product_id FK
        bigint option_id FK
        bigint custom_rule_id FK
        tinyint is_default
        varchar count_type
        int default_quantity
        int max_quantity
        decimal extra_price
        int order_index
    }

    store {
        bigint store_id PK
        varchar branch
        varchar city
        varchar state
        varchar zipcode
        double latitude
        double longitude
    }

    store_product {
        bigint store_product_id PK
        bigint store_id FK
        bigint product_id FK
        decimal override_price
        tinyint is_active
    }

    store_option_add {
        bigint store_option_add_id PK
        bigint store_product_id FK
        bigint option_id FK
        bigint custom_rule_id FK
        tinyint is_default
        varchar count_type
        int default_quantity
        int max_quantity
        decimal extra_price
        int order_index
    }

    store_option_delta {
        bigint store_option_delta_id PK
        bigint store_product_id FK
        bigint product_option_id FK
        decimal override_price
        varchar delta_type
    }

    options {
        bigint option_id PK
        varchar name
        varchar image_source
        double calories
    }

    custom_rule {
        bigint custom_rule_id PK
        varchar name
        varchar custom_rule_type
        int order_index
        int min_selection
        int max_selection
    }
```

# 🔗 링크

[https://app.notion.com/p/386c0bf3000580ccabb5f5ac81440da8?source=copy_link](https://app.notion.com/p/386c0bf3000580ccabb5f5ac81440da8?pvs=21)

---
