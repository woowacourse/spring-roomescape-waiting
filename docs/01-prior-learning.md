# 📖 방탈출 예약 대기

<details><summary><h2>미션</h2></summary>

<div class="max-width-md mx-auto pt-10 px-5"><h1 class="display-2 pt-5 pb-7 text-bold">📘 사이클1 - 사전학습</h1> <div class="mt-5"><div><div class="tui-editor-contents"><h1>사이클1: 사전학습</h1>
<blockquote>
<p><strong>혼자서 실험하고 관찰한다. 토론 활동의 재료를 만드는 단계.</strong></p>
</blockquote>
<hr>
<h2>이번 사이클에서 답할 질문</h2>
<ul>
<li>레벨1에서 익숙했던 테스트 방식이 웹 코드에서는 왜 그대로 안 통하는가?</li>
<li>DB가 있는 코드, HTTP가 있는 코드는 무엇으로 어떻게 검증할 것인가?</li>
<li>단위·통합·E2E는 같은 기능에 대해 무엇을 다르게 검증하는가?</li>
<li>무엇을 테스트하지 않을 것인가? 그 기준은 무엇인가?</li>
</ul>
<hr>
<h2>사전 학습 키워드</h2>
<table>
<thead>
<tr>
<th>목적</th>
<th>키워드</th>
</tr>
</thead>
<tbody>
<tr>
<td>테스트 도구</td>
<td>JUnit 5, AssertJ, Mockito</td>
</tr>
<tr>
<td>스프링 테스트</td>
<td><code data-backticks="1">@SpringBootTest</code>, <code data-backticks="1">@WebMvcTest</code>, <code data-backticks="1">@DataJdbcTest</code>, MockMvc, RestAssured</td>
</tr>
<tr>
<td>테스트 전략</td>
<td>단위 테스트, 통합 테스트, E2E, 테스트 더블</td>
</tr>
</tbody>
</table>
<blockquote>
<p><strong>키워드 사용법</strong>: 위 키워드를 검색해서 기본 사용법을 익힌다.<br>
깊이 파지 않아도 된다. "이런 게 있구나" 수준이면 충분하다.<br>
모르는 것이 있어도 실험을 먼저 해본다. 막히면 그때 찾아본다.</p>
</blockquote>
<hr>
<h2>실험 범위</h2>
<p><strong>지금까지 만든 코드에서 기능 1개를 골라 두 가지 방식으로 테스트</strong> 해본다. 1시간 이내에 두 시도가 끝나는 분량이 적당하다.</p>
<blockquote>
<p><strong>왜 두 방식인가</strong>: "이 도구를 쓰는 게 맞다"는 결론은 비교 없이 나오지 않는다. 같은 기능을 두 방식으로 시도해야 "어디서 안 되는지", "왜 안 되는지", "그럼 어떤 상황에서 어떤 방식이 맞는지"가 보인다.</p>
</blockquote>
<hr>
<h2>실험1: 레벨1 방식으로 테스트 시도</h2>
<h3>해볼 것</h3>
<ol>
<li>지금까지 만든 코드에서 테스트하고 싶은 기능을 1개 고른다 (예약 생성, 변경 검증, 인기 테마 조회 등)</li>
<li><strong>레벨1에서 했던 방식</strong>(순수한 단위 테스트, <code data-backticks="1">new</code>로 객체 생성, 외부 의존 없는 검증)으로 그 기능의 테스트를 작성해본다</li>
<li>작성하다 막히면 어디서 막혔는지 그대로 기록한다</li>
</ol>
<h3>관찰할 것</h3>
<ul>
<li>레벨1 방식으로 테스트가 끝까지 작성되는가?</li>
<li>안 된다면, 무엇 때문에 안 되는가? (DB? 스프링 컨테이너? HTTP? 객체 그래프?)</li>
<li>레벨1의 콘솔 프로그램과 무엇이 달라서 안 되는가?</li>
</ul>
<h3>기록하기</h3>
<p><strong>형식</strong>: <strong>고른 기능 / 레벨1 방식으로 시도한 결과 / 막힌 지점과 이유</strong></p>
                <pre id="default-v3epankyqn" class="hljs-code-block overflow-hidden relative d-flex mt-3 mb-8">                    <button type="button" class="clipboard-btn v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--default absolute right-10 top-10 z-1 grey--text text--darken-3">
                      <span class="v-btn__content">
                        <i aria-hidden="true" class="v-icon notranslate mdi mdi-clipboard theme--light"></i>
                      </span>
                    </button>
                    <code class="relative pa-4 lh-1-3 text-normal width-100 ls-0
                   language-plaintext hljs">예시:
- 기능: 같은 시간·테마 중복 예약 거부
- 시도: ReservationService를 new로 만들어 단위 테스트
- 막힌 지점: ReservationService가 JdbcTemplate을 의존해서 new로 만들 수 없었다.
- 이유: 로직이 DB 조회와 분리돼 있지 않다.
  </code>
  </pre>
<hr>
<h2>실험2: 다른 방식으로 같은 것 테스트</h2>
<h3>해볼 것</h3>
<ol>
<li>실험1에서 막혔던 부분을 다른 방식으로 테스트한다
<ul>
<li>스프링 테스트 슬라이스 (<code data-backticks="1">@SpringBootTest</code> / <code data-backticks="1">@WebMvcTest</code> / <code data-backticks="1">@DataJdbcTest</code> 등)</li>
<li>또는 모킹 (Mockito)</li>
<li>또는 인메모리 DB / 실제 DB</li>
</ul>
</li>
<li>검증이 끝까지 되도록 만든다</li>
<li>같은 검증이 두 방식에서 <strong>어떤 점이 같고 어떤 점이 다른지</strong> 기록한다</li>
</ol>
<h3>관찰할 것</h3>
<ul>
<li>실행 시간이 레벨1 테스트와 비교해 어떤가?</li>
<li>이 방식이 검증하는 것은 무엇인가? 검증하지 못하는 것은 무엇인가?</li>
<li>실험1처럼 단위로 분리할 수 있게 코드를 바꾼다면 어떻게 바꾸고 싶은가?</li>
</ul>
<h3>기록하기</h3>
<p>관찰 기록에 추가:</p>
<ul>
<li>두 방식의 비교 1줄 (속도·신뢰도·작성 비용 중 무엇이 어떻게 다른지)</li>
<li>"이 기능은 이 방식으로 테스트하겠다"는 내 결정과 이유</li>
</ul>
<hr>
<h2>사전 학습 준비 가이드</h2>
<p>사전 학습 시 다음을 목표로 한다:</p>
<ul>
<li class="task-list-item" data-te-task="">같은 기능을 두 방식으로 시도했다</li>
<li class="task-list-item" data-te-task="">관찰 기록을 작성했다</li>
<li class="task-list-item" data-te-task="">토론 활동에서 말할 근거가 1개 이상 있다</li>
</ul>
<hr>
<h2>산출물</h2>
<blockquote>
<p>사전 학습 산출물의 제출은 토론 활동 전 완료까지 완료한다.<br>
산출물은 <a href="https://techcourse-lms-plus-web.woowahan.com/crew">LMS+</a> 에 제출한다.</p>
</blockquote>
<ul>
<li class="task-list-item" data-te-task="">두 방식 시도 기록 (막힌 지점 + 끝까지 만든 결과)</li>
<li class="task-list-item" data-te-task="">두 방식 비교와 내 결정</li>
<li class="task-list-item" data-te-task="">토론에서 가장 묻고 싶은 질문 1개</li>
</ul>
</div></div></div></div>

</details>

## 🚀 사이클1 - 미션 (예약 대기)

---

# `1. 사전 학습`

- Q. 레벨1에서 익숙했던 테스트 방식이 웹 코드에서는 왜 그대로 안 통하는가?
- A. 도메인 객체간의 연계/의존은 동일하지만 스프링 프레임워크의 흐름 제어가 존재하기에
- Q. DB가 있는 코드, HTTP가 있는 코드는 무엇으로 어떻게 검증할 것인가?
- A. 스프링의 테스트 도구를 활용해서
- Q. 단위·통합·E2E는 같은 기능에 대해 무엇을 다르게 검증하는가?
- A. `말단/조합/완결`
    - 동일 기능의 최하단 부분적인 기능의 유효성을
    - 그 최하단의 부분의 조합이 동합적으로 동작하는 기능의 유효성을
    - 기능의 시작부터 끝까지 모든 흐름과 결과를
- Q. 무엇을 테스트하지 않을 것인가? 그 기준은 무엇인가?
- A. 하위 레이어에서 이미 테스트된 기능을 단순 호출하는 메서드

## 키워드

<table>
<thead>
<tr>
<th>목적</th>
<th>키워드</th>
</tr>
</thead>
<tbody>
<tr>
<td>테스트 도구</td>
<td>JUnit 5, AssertJ, Mockito</td>
</tr>
<tr>
<td>스프링 테스트</td>
<td><code data-backticks="1">@SpringBootTest</code>, <code data-backticks="1">@WebMvcTest</code>, <code data-backticks="1">@DataJdbcTest</code>, MockMvc, RestAssured</td>
</tr>
<tr>
<td>테스트 전략</td>
<td>단위 테스트, 통합 테스트, E2E, 테스트 더블</td>
</tr>
</tbody>
</table>

## 실험 범위

**지금까지 만든 코드에서 기능 1개를 골라 두 가지 방식으로 테스트 해본다. 1시간 이내에 두 시도가 끝나는 분량이 적당하다.**

<blockquote>
<p><strong>왜 두 방식인가</strong>: "이 도구를 쓰는 게 맞다"는 결론은 비교 없이 나오지 않는다. 같은 기능을 두 방식으로 시도해야 "어디서 안 되는지", "왜 안 되는지", "그럼 어떤 상황에서 어떤 방식이 맞는지"가 보인다.</p>
</blockquote>

## **실험1: 레벨1 방식으로 테스트 시도**

### **해볼 것**

- [x] 지금까지 만든 코드에서 테스트하고 싶은 기능을 1개 고른다 (예약 생성, 변경 검증, 인기 테마 조회 등)

- [x] 레벨1에서 했던 방식(순수한 단위 테스트, new로 객체 생성, 외부 의존 없는 검증)으로 그 기능의 테스트를 작성해본다

- [x] 작성하다 막히면 어디서 막혔는지 그대로 기록한다

### **관찰할 것**

- 레벨1 방식으로 테스트가 끝까지 작성되는가?
- 안 된다면, 무엇 때문에 안 되는가? (DB? 스프링 컨테이너? HTTP? 객체 그래프?)
- 레벨1의 콘솔 프로그램과 무엇이 달라서 안 되는가?

### **기록하기**

- <img width="728" height="140" alt="image" src="https://github.com/user-attachments/assets/67f0a9e4-16d6-4e6d-90f8-45c21e07103a" />
  - 예약 생성 테스트를 위해 서비스 객체를 생성할 때, 의존 없는 순수한 단위 테스트가 불가능하다.
  - 서비스는 레포지토리를 의존하기에 new 로 생성이 불가능하다.
  - 도메인 서비스가 아닌 애플리케이션 서비스
  - 스프링 컨테이너가 주입해 주는 레포지토리를 의존, 레포지토리는 DB에 의존

## **실험2: 다른 방식으로 같은 것 테스트**

### **해볼 것**

<ol>
<li>실험1에서 막혔던 부분을 다른 방식으로 테스트한다
<ul>
<li>스프링 테스트 슬라이스 (<code data-backticks="1">@SpringBootTest</code> / <code data-backticks="1">@WebMvcTest</code> / <code data-backticks="1">@DataJdbcTest</code> 등)</li>
<li>또는 모킹 (Mockito)</li>
<li>또는 인메모리 DB / 실제 DB</li>
</ul>
</li>
<li>검증이 끝까지 되도록 만든다</li>
<li>같은 검증이 두 방식에서 <strong>어떤 점이 같고 어떤 점이 다른지</strong> 기록한다</li>
</ol>

### **관찰/기록**

- 실행 시간이 레벨1 테스트와 비교해 어떤가?
    - 약간 오래걸린다? Mock 이라그런지 크게 증가하진 않은 느낌
- 이 방식이 검증하는 것은 무엇인가? 검증하지 못하는 것은 무엇인가?
    - 하위 레이어에 대한 호출이 성공한다 가정하고 순수하게 테스트할 메서드만 검증
- 실험1처럼 단위로 분리할 수 있게 코드를 바꾼다면 어떻게 바꾸고 싶은가?
    - 하위 레이어를 분리한 최소 단위 아닌가?
- 서비스같이 하위 레이어가 여럿 필요하면 Mock 으로 순수 기능 테스트
- 레포지토리같이 실제 기능을 수행하는 레이어는 직접(더미데이터로) 순수 기능 테스트

---
