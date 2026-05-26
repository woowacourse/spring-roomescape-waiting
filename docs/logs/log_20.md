# 학습 로그 #20

**학습 범위**: EventListener 작동 방식, 옵저버 패턴, Java GC 도달 가능성

## 1. 막힌 것의 종류

- [x] 개념 자체를 모르겠다
- [ ] 개념은 알겠는데 코드로 어떻게 쓰는지 모르겠다
- [ ] 코드는 돌아가는데 이게 맞는 건지 모르겠다
- [ ] 기타: ___

`sessionDestroyed`가 어떻게 호출되는지 — blocking/await 방식으로 대기하고 있다고 추측했다가 수정.

## 2. 이번 타임의 학습 내용

### EventListener 작동 방식 — 옵저버 패턴

- Listener가 Tomcat을 기다리는 게 아니라, **Tomcat이 Listener를 직접 호출**
- `@WebListener`로 등록 = Tomcat의 리스너 목록(옵저버 목록)에 등록

```
session.invalidate() 호출
  → HttpSession 구현체 내부에서 Manager 참조로 remove 호출
  → Manager가 리스너 목록 순회
  → sessionRegistry.sessionDestroyed(event) 직접 호출
  → invalidate()를 호출한 스레드가 그대로 실행
```

- blocking/await 방식이 아님 → 동기 호출, 같은 스레드에서 실행

### ConcurrentHashMap을 쓰는 이유

- 여러 요청이 동시에 들어오면 각각 다른 스레드에서 리스너 호출
- 같은 map에 동시 접근 → `ConcurrentHashMap` 필요

### HttpSession이 Tomcat을 아는 이유

- `HttpSession`은 인터페이스 — Tomcat이 구현체를 만들 때 Manager 참조를 내부에 심음
- `session.invalidate()` 호출 시 내부적으로 `this.manager.remove(this)` 실행
- `HttpSession`이 인터페이스인 이유: 구현체에 Tomcat 내부 참조를 자유롭게 넣기 위함

### 순환 참조와 Java GC

- `Manager → Session`, `Session → Manager` 양방향 참조 존재
- Java GC는 참조 수가 아닌 **GC 루트 도달 가능성**으로 회수 판단
- 외부에서 참조가 없으면 순환 참조여도 회수 대상 → 문제 없음

## 3. 전략 평가

- 효과적이었던 것: "blocking이면 수백 개 요청에서 어떻게 되나요?" 반례로 빠르게 수정됐다
- 아쉬운 것: 옵저버 패턴 이름이 바로 안 나온 것 — 패턴 이름과 구조를 연결하는 연습 필요

## 4. 이전 "바꿀 것" 적용 여부

- 바꿀 것: 오개념처럼 보일 때 AI가 반례 시나리오를 먼저 제시
- 적용: blocking 추측 → 수백 개 요청 반례, IP/Port 추측 → 두 탭 반례. 잘 적용됨 ✅

## 5. 다음 타임에 바꿀 것

- 유지할 것: 반례로 오개념을 빠르게 수정하는 방식
- 바꿀 것: 패턴 이름과 구조를 연결해서 기억하기 — 구조는 설명할 수 있는데 이름이 안 나오는 경우가 반복됨

## 다음 사이클 키워드

```
✅ Java GC 동작 방식 심화
   → 맥락: GC 루트란 무엇인가, Minor GC / Major GC 차이
   → 종류: 흐름 파악 (도달 가능성 개념은 이해했으니 GC 전체 흐름으로)

✅ 디자인 패턴 — 옵저버, 프록시, 전략 패턴 구조 비교
   → 맥락: 옵저버 패턴 이름이 바로 안 나옴. 자주 쓰는 패턴 3개 구조로 비교
   → 종류: 개념 이해
```
