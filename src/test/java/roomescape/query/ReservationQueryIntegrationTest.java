package roomescape.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.Page;
import roomescape.common.Pageable;
import roomescape.service.command.ReservationSearchCommand;
import roomescape.service.result.ReservationSearchResult;
import roomescape.support.BaseIntegrationTest;

@Sql("/reservation-test-query.sql") // 총 21개 데이터
class ReservationQueryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ReservationQueryRepository reservationQueryRepository;

    @Test
    void 검색_필터가_없을_때_전체_카운트를_계산하고_페이징과_정렬이_적용된_데이터를_반환한다() {
        // given: 필터 없음, 한 페이지에 5개씩 보기로 설정하고 2페이지 요청
        ReservationSearchCommand noCondition = new ReservationSearchCommand(null);
        Pageable pageable = new Pageable(5, 5);

        // when
        Page<ReservationSearchResult> result = reservationQueryRepository.search(noCondition, pageable);

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
        ReservationSearchCommand condition = new ReservationSearchCommand("이프");
        Pageable pageable = new Pageable(1, 10);

        // when
        Page<ReservationSearchResult> result = reservationQueryRepository.search(condition, pageable);

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
}
