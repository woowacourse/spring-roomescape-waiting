<div class="tui-editor-contents"><h1>사이클1: 미션 (예약 대기)</h1>
<blockquote>
<p><strong>테스트 전략을 적용하면서 이미 예약된 슬롯에 대기를 신청하고, 본인의 예약·대기를 함께 관리할 수 있게 만든다.</strong></p>
</blockquote>
<hr>
------------------------------------------전략------------------------------------------
<h2>목표</h2>
<p>단순히 예약 대기 기능을 만드는 것이 아니라, <strong>"이걸 어떻게 테스트할 것인가"의 판단 근거를 가지는 것</strong>이 핵심이다.</p>
<ul>

### <li>이번 토론 활동에서 정한 <strong>테스트 전략을 코드에 적용</strong>한다.</li>

- 테스트는 도메인 로직을 보호한다
- 도구(Mock, Fake)를 활용한 단위 테스트, 통합 테스트를 적용한다
- 실제 DB의 제약조건과 CRUD 로직이라면 실제 DB를 사용한다
- 실제 DB와 무관하게 그 결과값에 대한 도메인 로직이라면 Mock, Fake DB를 사용한다
- 프레임워크 자체 기능은 테스트하지 않는다
- 하위 레이어에서 테스트된 로직에 대한 단순 호출은 테스트하지 않는다

### <li>테스트 결정마다 <strong>"왜 이 방식으로 테스트했는가"</strong> 를 PR 본문에 한두 줄로 남긴다.</li>

- 많지 않은 테스트, 사용한 방식과 이유 정리하기

### <li>새로 추가하는 대기 도메인이 기존 예약 도메인과 어떤 관계인지 의식한다.</li>

- 사실상 동일한 정보를 중복해서 저장한 부분이 많다.
- 이유는? 예약이 삭제되더라도 대기 -> 예약에 정보가 필요하다.
- 그렇다고 하나의 DB로 통합하거나 중복 데이터를 별도 테이블로 추출?
    - 일부 중복을 감수하더라도 각 테이블의 정합성을 보장해야 한다.

> 예약 도메인이 이미 가진 필드를 중복해서 보유하고 있지만,  
> 명확한 기준으로 다른 목적이 존재하기에 감안한다.

</ul>

<hr>

<h2>기능 요구 사항</h2>
<p>방탈출 사용자 예약 미션까지는 한 슬롯(날짜+시간+테마)에 한 명만 예약할 수 있었고, 이미 예약된 슬롯은 사용자에게 보이지 않았다. 이번 사이클부터는 <strong>이미 예약된 슬롯에 대기를 신청</strong>할 수 있고, 사용자는 본인의 <strong>예약과 대기를 함께 조회</strong>할 수 있다.</p>
<p>이번 사이클의 작업도 <strong>백엔드 API 추가</strong>와 <strong>사용자가 보는 화면을 만드는 것</strong> 두 가지를 함께 진행한다. API에 맞춰 페어가 함께 사용자가 사용하는 클라이언트 화면을 만들고, 각 단계의 화면이 브라우저에서 정상 동작하는 것까지 확인한다. 화면 작성에는 AI를 활용해도 좋다.</p>
<h3>1단계 - 예약 대기 신청/취소</h3>
<ul>

<li>이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에 <strong>대기를 신청</strong>할 수 있다.</li>

<li>같은 슬롯에 대한 대기는 <strong>신청 순서대로 순번</strong>이 부여된다.</li>

<li>같은 사용자가 같은 슬롯에 <strong>중복 대기할 수 없다</strong>.</li>

<li>사용자는 본인의 <strong>대기를 취소</strong>할 수 있다.</li>

> ### `대기`
> - 예약의 필드(날짜+시간+테마) 와 동일한 필드를 가진다
> - 순번 필드를 가진다. 신청 순서대로 부여된다.
> - 중복될 수 없다. `UNIQUE`
> - 사용자가 자신의 대기를 관리한다. `CRUD` - UPDATE?

</ul>
<h3>2단계 - 내 예약 목록 조회 (상태 구분)</h3>
<ul>

<li>이전 미션의 내 예약 목록 조회를 <strong>확장</strong>한다.</li>

<li>사용자의 <strong>예약과 대기가 상태로 구분</strong>되어 함께 표시된다.</li>

<li>대기에는 본인의 <strong>대기 순번</strong>도 함께 보여준다.</li>

> ### `예약 목록 조회`
> - `확장`
> - 기존 기능의 변경 없이 추가만? - 일단 불가
> - 예약/대기 상태로 구분(동일한 필드, 상태 추가, 다른 상태)
> - 대기 여부, 순번

</ul>
<hr>
<h2>프로그래밍 요구 사항</h2>
<ul>
<li>자바 코드 컨벤션을 지키면서 프로그래밍한다.
<ul>
<li>기본적으로 <a href="https://github.com/woowacourse/woowacourse-docs/tree/master/styleguide/java">Java Style Guide</a>을 원칙으로 한다.</li>
</ul>
</li>

# <li>레벨1에서 만든 규칙 중 좋은 코드 작성을 위해 의미있는 규칙을 정리하고 적용한다.</li>

- 자주 까먹곤 하지만 학습은 나선형, 점진적으로

<li>이번 사이클에서 구현한 API에 대한 요구사항 테스트를 작성한다.</li>
</ul>
<h3>추가된 요구 사항</h3>
<ul>
<li>토론에서 정한 <strong>테스트 전략에 맞춰 단위·통합 테스트 범위</strong>를 결정해 테스트를 작성한다.</li>
</ul>
<hr>
<h2>과제 진행 요구 사항</h2>
<ul>
<li>구현을 시작하기 전에 <strong>기능 요구 사항을 분석하여 기능 목록을 정리</strong>한다.</li>
<li>README.md 파일에 구현할 기능 목록과 <strong>페어가 결정한 자기 API 명세</strong>를 정리해 추가한다.</li>
<li>Git의 커밋 단위는 앞 단계에서 README.md 파일에 정리한 기능 목록 단위로 추가한다.
<ul>
<li><a href="https://gist.github.com/stephenparish/9941e89d80e2bc58a153">AngularJS Git Commit Message Conventions</a> 참고해 커밋 메시지를 작성한다.</li>
</ul>
</li>
<li>PR 본문에 <strong>예약과 대기의 데이터 모델 결정</strong>, <strong>테스트 전략 결정</strong>과 각각의 이유를 한두 줄씩 남긴다.</li>
</ul>
<hr>
<h2>미션 중 할 일</h2>
<ol>
<li>토론 활동에서 정한 테스트 전략을 의식하며 코드 작성</li>
<li>레벨1 방식의 테스트가 통하지 않는 순간 기록</li>
<li>페어와 설계·테스트 의견이 갈렸던 순간과 합의 과정 기록</li>
</ol>
<hr>
<h2>미션 중 기록</h2>
<p><strong>필수 기록</strong>:</p>
<ul>
<li class="task-list-item" data-te-task="">규칙을 적용해서 변경한 코드 1곳 이상</li>
<li class="task-list-item" data-te-task="">테스트 작성이 어려웠던 코드 1곳 이상</li>
<li class="task-list-item" data-te-task="">막힌 순간 1회 이상</li>
</ul>
<hr>
<h2>미션 완료 조건</h2>
<ul>
<li class="task-list-item" data-te-task="">요구사항 구현</li>
<li class="task-list-item" data-te-task="">규칙에 의한 코드 변경 1회 이상</li>
<li class="task-list-item" data-te-task="">미션 중 기록 작성</li>
</ul>
<h2>산출물</h2>
<ul>
<li class="task-list-item" data-te-task="">미션 코드 (PR)</li>
<li class="task-list-item" data-te-task="">미션 중 기록</li>
</ul>
</div>