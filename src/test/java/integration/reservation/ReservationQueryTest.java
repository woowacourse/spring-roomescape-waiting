package integration.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import integration.BaseIntegrationTest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.Page;
import roomescape.common.Pageable;
import roomescape.controller.client.api.dto.condition.ReservationSearchCondition;
import roomescape.controller.client.api.dto.response.ReservationDetailResponse;
import roomescape.controller.client.api.dto.response.ReservationSearchResponse;
import roomescape.controller.client.api.query.ReservationQuery;

@Sql("/reservation-test-query.sql") // 총 21개 데이터
class ReservationQueryTest extends BaseIntegrationTest {

    @Autowired
    private ReservationQuery reservationQuery;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 검색_필터가_없을_때_전체_카운트를_계산하고_페이징과_정렬이_적용된_데이터를_반환한다() {
        // given: 필터 없음, 한 페이지에 5개씩 보기로 설정하고 2페이지 요청
        ReservationSearchCondition noCondition = new ReservationSearchCondition(null);
        Pageable pageable = new Pageable(5, 5);

        // when
        Page<ReservationSearchResponse> result = reservationQuery.search(noCondition, pageable);

        // then: 전체 카운트, 페이지 계산, 데이터 개수, 그리고 정렬(내림차순)을 한 번에 검증
        // 총 5페이지(21/5 + 1)로 구성되고 마지막 페이지는 1개임
        assertAll(
                () -> assertThat(result.totalElements()).isEqualTo(21),
                () -> assertThat(result.totalPages()).isEqualTo(5),
                () -> assertThat(result.content()).hasSize(1)
        );
        // 시간 순으로 정렬된 마지막 페이지에는 채원이 존재
        assertThat(result.content().getFirst().name()).isEqualTo("채원");
    }

    @Test
    void 검색_필터가_있을_때_조건에_맞는_카운트를_계산하고_필터링된_데이터만_반환한다() {
        // given: '이프'라는 특정 조건으로 필터링 요청
        ReservationSearchCondition condition = new ReservationSearchCondition("이프");
        Pageable pageable = new Pageable(1, 10);

        // when
        Page<ReservationSearchResponse> result = reservationQuery.search(condition, pageable);

        // then: 필터링된 정보에 대해 추가 검증
        assertAll(
                () -> assertThat(result.totalElements()).isEqualTo(3),
                () -> assertThat(result.totalPages()).isEqualTo(1),
                () -> assertThat(result.content()).hasSize(3)
        );

        // 꺼내온 데이터가 전부 '이프'가 맞는지 확인
        boolean allMatch = result.content().stream()
                .allMatch(res -> res.name().equals("이프"));
        assertThat(allMatch).isTrue();
    }

    @Test
    void 예약_식별자로_예약_상세_정보를_조회한다() {
        // when
        ReservationDetailResponse result = reservationQuery.findByReservationId(1L);

        // then
        assertAll(
                () -> assertThat(result.slotId()).isEqualTo(1L),
                () -> assertThat(result.theme().name()).isEqualTo("화이트노이즈"),
                () -> assertThat(result.time().startAt()).isNotNull(),
                () -> assertThat(result.reservation().id()).isEqualTo(1L),
                () -> assertThat(result.reservation().status()).isEqualTo("RESERVED")
        );
    }

    @Test
    void 예약_상태의_대기_순번은_null이다() {
        // when
        ReservationSearchResponse result = reservationQuery.search(
                new ReservationSearchCondition("채원"),
                new Pageable(1, 10)
        ).content().getFirst();

        // then
        assertAll(
                () -> assertThat(result.status()).isEqualTo("RESERVED"),
                () -> assertThat(result.waitingRank()).isNull()
        );
    }

    @Test
    void 먼저_생성된_대기_예약이_더_낮은_대기_순번을_가진다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("""
                INSERT INTO reservation (name, slot_id, status, active_status, created_at)
                VALUES (?, ?, ?, 'ACTIVE', ?)
                """, "라텔", 2L, "WAITING", Timestamp.valueOf(now.minusMinutes(2)));
        jdbcTemplate.update("""
                INSERT INTO reservation (name, slot_id, status, active_status, created_at)
                VALUES (?, ?, ?, 'ACTIVE', ?)
                """, "찰리", 2L, "WAITING", Timestamp.valueOf(now.minusMinutes(1)));

        // when
        ReservationSearchResponse firstWaiting = reservationQuery.search(
                new ReservationSearchCondition("라텔"),
                new Pageable(1, 10)
        ).content().getFirst();
        ReservationSearchResponse secondWaiting = reservationQuery.search(
                new ReservationSearchCondition("찰리"),
                new Pageable(1, 10)
        ).content().getFirst();

        // then
        assertAll(
                () -> assertThat(firstWaiting.waitingRank()).isEqualTo(1),
                () -> assertThat(secondWaiting.waitingRank()).isEqualTo(2)
        );
    }
}
