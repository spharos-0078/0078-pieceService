# 🍰 Piece Service

조각 거래 플랫폼의 핵심 서비스로, 조각 생성, 분배, 거래 매칭 및 결제를 담당하는 MSA(Microservice Architecture) 기반 서비스입니다.

## 🏗️ 아키텍처

### 기술 스택
- **Framework**: Spring Boot 3.5.0
- **Language**: Java 17
- **Database**: MySQL 8.0
- **Cache**: Redis + Redisson
- **Message Queue**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Build Tool**: Gradle

### MSA 구성
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │    │  User Service   │    │ Payment Service │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  PIECE SERVICE  │  ← This Repository
                    └─────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Kafka/Redis   │
                    └─────────────────┘
```

## 🚀 주요 기능

### 1. 조각 관리
- **조각 생성**: 상품별 조각 생성 및 UUID 할당
- **조각 분배**: 사용자별 조각 할당/해제
- **조각 조회**: 사용자별 보유 조각 현황

### 2. 거래 시스템
- **매수/매도 예약**: 실시간 호가 등록
- **매칭 엔진**: 가격/시간 우선순위 기반 체결
- **소유권 이전**: 거래 완료 시 조각 소유권 이전
- **평균단가 관리**: 사용자별 평균 매수/매도 가격 계산

### 3. 결제 연동
- **예치금 검증**: 매수 시 예치금 충분성 확인
- **결제 처리**: 매수자 차감, 매도자 입금
- **실패 처리**: 결제 실패 시 보상 트랜잭션

### 4. 실시간 데이터
- **호가창**: Redis 기반 실시간 호가 정보
- **거래량**: 실시간 거래량 및 가격 변동
- **시장가**: 최신 체결가 기반 시장가 갱신

## 📁 프로젝트 구조

```
src/main/java/com/pieceofcake/piece_service/
├── common/                          # 공통 모듈
│   ├── config/                      # 설정 클래스
│   ├── entity/                      # 공통 엔티티
│   └── exception/                   # 예외 처리
├── piece/                          # 조각 관리 도메인
│   ├── application/                 # 비즈니스 로직
│   ├── dto/                        # 데이터 전송 객체
│   ├── entity/                     # 도메인 엔티티
│   ├── infrastructure/             # 인프라스트럭처
│   ├── presentation/               # 컨트롤러
│   └── vo/                         # 뷰 객체
├── trade/                          # 거래 도메인
│   ├── application/                # 비즈니스 로직
│   │   └── domain/                 # 도메인 서비스
│   ├── dto/                        # 데이터 전송 객체
│   ├── entity/                     # 도메인 엔티티
│   ├── infrastructure/             # 인프라스트럭처
│   │   ├── feign/                  # Feign 클라이언트
│   │   └── redis/                  # Redis 관련
│   ├── presentation/               # 컨트롤러
│   ├── scheduler/                  # 스케줄러
│   ├── util/                       # 유틸리티
│   └── vo/                         # 뷰 객체
└── kafka/                          # Kafka 관련
    ├── application/                # Kafka 서비스
    ├── config/                     # Kafka 설정
    ├── controller/                 # Kafka 컨트롤러
    ├── dto/                        # Kafka DTO
    ├── event/                      # 이벤트
    └── producer/                   # 프로듀서
```

## 🔧 설치 및 실행

### 1. 환경 설정
```bash
# 환경 변수 설정 (.env 파일)
EC2_DB=your-database-host
SPRING_DATASOURCE_USERNAME=your-username
SPRING_DATASOURCE_PASSWORD=your-password
REDIS_PASSWORD=your-redis-password
EC2_HOST=your-eureka-host
EC2_HOST2=your-kafka-host
```

### 2. 빌드 및 실행
```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

### 3. Docker 실행
```bash
# Docker 이미지 빌드
docker build -t piece-service .

# Docker 컨테이너 실행
docker run -p 8087:8087 piece-service
```

## 📡 API 문서

### Swagger UI
- **URL**: `http://localhost:8087/swagger-ui/index.html`
- **API Docs**: `http://localhost:8087/v3/api-docs`

### 주요 API 엔드포인트

#### 조각 관리
```http
POST   /api/v1/piece                    # 조각 생성
PUT    /api/v1/piece/distribute         # 조각 분배
DELETE /api/v1/piece/delete-all/{uuid}  # 조각 전체 삭제
```

#### 거래 시스템
```http
POST   /api/v1/trade/buy                # 매수 예약
POST   /api/v1/trade/sell               # 매도 예약
DELETE /api/v1/trade/reservation/{uuid} # 예약 취소
GET    /api/v1/trade/reservations       # 예약 목록 조회
```

## 🗄️ 데이터베이스 스키마

### 주요 테이블
- `piece`: 조각 정보
- `piece_product`: 조각 상품 정보
- `piece_trade_reservation`: 거래 예약
- `owned_piece`: 보유 조각
- `owned_piece_average`: 평균단가
- `piece_traded_history`: 거래 이력
- `piece_matched_history`: 매칭 이력

## 🔄 트랜잭션 관리

### Saga 패턴 적용
- **매칭 → 소유권 이전 → 결제** 순서로 진행
- **보상 트랜잭션**: 결제 실패 시 소유권 이전 롤백
- **비동기 처리**: 외부 API 호출은 트랜잭션 외부에서 처리

### 동시성 제어
- **Redis Lock**: 매칭 시 동시성 제어
- **Pessimistic Lock**: 소유권 이전 시 DB 락
- **Optimistic Lock**: 버전 기반 충돌 방지

## 📊 모니터링

### 로깅
- **Structured Logging**: JSON 형태 로그 출력
- **MDC**: 요청별 추적 ID 설정
- **Log Level**: 환경별 로그 레벨 설정

### 메트릭
- **Micrometer**: 애플리케이션 메트릭 수집
- **Health Check**: `/actuator/health` 엔드포인트

## 🧪 테스트

### 테스트 실행
```bash
# 단위 테스트
./gradlew test

# 통합 테스트
./gradlew integrationTest

# 전체 테스트
./gradlew check
```

## 🚀 배포

### CI/CD 파이프라인
1. **코드 커밋** → GitHub
2. **자동 빌드** → GitHub Actions
3. **테스트 실행** → 단위/통합 테스트
4. **Docker 이미지 빌드** → Docker Hub
5. **배포** → Kubernetes/ECS

### 환경별 설정
- **Development**: `application-dev.yml`
- **Staging**: `application-staging.yml`
- **Production**: `application-prod.yml`

## 🤝 기여 가이드

### 브랜치 전략
- `main`: 프로덕션 브랜치
- `develop`: 개발 브랜치
- `feature/*`: 기능 개발 브랜치
- `fix/*`: 버그 수정 브랜치

### 커밋 컨벤션
```
feat: 새로운 기능 추가
fix: 버그 수정
refactor: 코드 리팩토링
docs: 문서 수정
test: 테스트 코드 추가
chore: 빌드 프로세스 또는 보조 도구 변경
```

## 📞 문의

- **개발팀**: piece-dev@company.com
- **이슈 리포트**: [GitHub Issues](https://github.com/spharos-0078/0078-pieceService/issues)
- **문서**: [Wiki](https://github.com/spharos-0078/0078-pieceService/wiki)

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

**Version**: 1.0.0  
**Last Updated**: 2024년 12월  
**Maintainer**: Piece Service Team
