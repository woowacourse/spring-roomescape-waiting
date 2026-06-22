<div class="wmde-markdown wmde-markdown-color " style="background-color: transparent;"><h2 id="user-content-요구사항"><a tabindex="-1" href="#요구사항"></a>요구사항</h2>
<h3 id="user-content-api"><a tabindex="-1" href="#api"></a>API</h3>
<ul>
<li><code>POST /waitings</code> — 예약 대기 요청</li>
<li><code>DELETE /waitings/{id}</code> — 예약 대기 취소</li>
<li><code>GET /reservations-mine</code> 응답에 예약 대기 목록 함께 포함 (status="N번째 예약대기")</li>
</ul>
<h3 id="user-content-도메인-규칙"><a tabindex="-1" href="#도메인-규칙"></a>도메인 규칙</h3>
<ul>
<li>같은 테마·날짜·시간에 <strong>중복 예약 방지</strong></li>
<li><strong>심화</strong>: 내 예약 대기가 몇 번째인지 표시</li>
</ul>
<hr>
<h2 id="user-content-3-1-n1과-fetch-join-본격-비교-관찰-과제-2"><a tabindex="-1" href="#3-1-n1과-fetch-join-본격-비교-관찰-과제-2"></a>3-1. N+1과 fetch join 본격 비교 (관찰 과제 2)</h2>
<p>새 도메인(<code>Waiting</code>)이 <code>Member</code>·<code>Theme</code>·<code>ReservationTime</code>을 모두 참조하면서 N+1이 자연스럽게 등장합니다.</p>
<h3 id="user-content-관찰-시나리오"><a tabindex="-1" href="#관찰-시나리오"></a>관찰 시나리오</h3>
<ul>
<li><code>GET /reservations-mine</code>에서 본인 예약 N개 + 본인 대기 M개를 가져온 뒤, 각 항목의 <code>getTheme().getName()</code>·<code>getTime().getStartAt()</code>을 응답 DTO로 변환할 때 SQL이 몇 번 나가는가?</li>
<li>같은 작업을 fetch join 또는 <code>@EntityGraph</code>로 묶었을 때 SQL이 어떻게 달라지는가?</li>
<li>join이 합쳐지면 row 중복은 어떻게 처리되는가?</li>
</ul>
<h3 id="user-content-기록에-남길-것"><a tabindex="-1" href="#기록에-남길-것"></a>기록에 남길 것</h3>
<p>두 SQL을 나란히 붙이는 것이 N+1과 fetch join의 차이를 가장 정직하게 보여줍니다. 다음 4가지가 함께 있으면 미션 끝난 후 회상 재료가 됩니다.</p>
<pre><code class="code-highlight"><span class="code-line">1. 시도한 코드
</span><span class="code-line">2. 예측 SQL
</span><span class="code-line">3. 실제 SQL
</span><span class="code-line">4. 왜 다른가
</span></code><div></div></pre>

---

## 관찰/기록

현재 구현했던 네임쿼리 구조에선 각각의 예약/대기에서 테마나 시간대를 조회할 때 마다  
새로운 쿼리가 발생할 것으로 보이고. 이는 앞선 실험과 관찰로 확인됨.

페치 조인 / 엔티티그래프?

- `FETCH JOIN` : JPQL에서 연관 엔티티를 한 쿼리로 함께 로드

```java

@Query("SELECT w FROM Waiting w JOIN FETCH w.session WHERE w.session = :session")
List<Waiting> findBySessionWithFetch(@Param("session") Session session);
```

JPQL 로 연관 엔티티를 하나의 쿼리로 함께 로드.

- `@EntityGraph` : JPQL 없이 어노테이션으로 FETCH JOIN 효과

```java

@EntityGraph(attributePaths = {"session"})
List<Waiting> findBySessionOrderByIdAsc(Session session);
```

JPA가 내부적으로 위와 동일한 LEFT JOIN 쿼리를 생성.

`영기 학습 참조`  
트랜잭션에서 레이지가 설정된 상태에서 새로운 값을 `생성` 하는 쿼리를 실행한다면  
이 쿼리는 캐시를 거칠까 바로 실행될까?

바로 실행. 이유는 딥하게 봐야겠지만 일단 JPA 영속성 컨텍스트가 엔티티를 캐시로 관리하는 방법 자체가  
해당 식별자로 Map 만들어서 조회해주는 거기 때문이랑 연관이 있었던걸로


<hr>
<h2 id="user-content-3-2-jpql-본격"><a tabindex="-1" href="#3-2-jpql-본격"></a>3-2. JPQL 본격</h2>
<p>N번째 대기 계산은 메서드 이름 쿼리로 풀리지 않습니다. JPQL을 씁니다.</p>
<pre class="language-sql"><code class="language-sql code-highlight"><span class="code-line"><span class="token keyword">SELECT</span> new <span class="token punctuation">.</span><span class="token punctuation">.</span><span class="token punctuation">.</span>WaitingWithRank<span class="token punctuation">(</span>
</span><span class="code-line">  w<span class="token punctuation">,</span>
</span><span class="code-line">  <span class="token punctuation">(</span><span class="token keyword">SELECT</span> <span class="token function">COUNT</span><span class="token punctuation">(</span>w2<span class="token punctuation">)</span> <span class="token keyword">FROM</span> Waiting w2
</span><span class="code-line">   <span class="token keyword">WHERE</span> w2<span class="token punctuation">.</span>theme <span class="token operator">=</span> w<span class="token punctuation">.</span>theme
</span><span class="code-line">     <span class="token operator">AND</span> w2<span class="token punctuation">.</span><span class="token keyword">date</span> <span class="token operator">=</span> w<span class="token punctuation">.</span><span class="token keyword">date</span>
</span><span class="code-line">     <span class="token operator">AND</span> w2<span class="token punctuation">.</span><span class="token keyword">time</span> <span class="token operator">=</span> w<span class="token punctuation">.</span><span class="token keyword">time</span>
</span><span class="code-line">     <span class="token operator">AND</span> w2<span class="token punctuation">.</span>id <span class="token operator">&lt;</span> w<span class="token punctuation">.</span>id<span class="token punctuation">)</span><span class="token punctuation">)</span>
</span><span class="code-line"><span class="token keyword">FROM</span> Waiting w
</span><span class="code-line"><span class="token keyword">WHERE</span> w<span class="token punctuation">.</span>memberId <span class="token operator">=</span> :memberId
</span></code><div></div></pre>
<p>LMS 힌트를 그대로 쓰거나 본인 작성으로 풀어도 됩니다.</p>
<h2 id="user-content-확인-과제"><a tabindex="-1" href="#확인-과제"></a>확인 과제</h2></div>