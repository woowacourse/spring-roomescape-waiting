package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.result.ReservationTimeResult;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.support.ServiceIntegrationTest;

/**
 * ReservationTimeService 통합 테스트.
 *
 * <p>검증 대상:
 * <ul>
 *   <li>삭제 거부: 예약이 존재하는 시간은 삭제할 수 없다 (existsByTimeId 상태에 의존)</li>
 *   <li>예약 가능 시간 조회: 특정 날짜·테마에 이미 예약된 시간은 목록에서 빠진다 (findAvailable의 NOT IN)</li>
 * </ul>
 * 둘 다 "이미 저장된 예약" 상태에 의존하므로 실제 H2로 검증한다.
 *
 * <p>시간 규칙은 미래/과거와 무관한 순수 상태 의존이라 FixedClockConfig가 필요 없다.
 * (시점 판정은 ReservationService의 책임이고, 여기서는 시간 슬롯의 가용성만 본다.)
 */
class ReservationTimeServiceTest extends ServiceIntegrationTest {

    private static final LocalDate DATE = LocalDate.of(2050, 12, 31);

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Nested
    @DisplayName("시간 삭제 거부 정책")
    class DeletePolicy {

        @Test
        @DisplayName("[상태 의존] 예약이 존재하는 시간은 삭제할 수 없다")
        void 예약_있는_시간_삭제_거부() {
            Long timeId = fixture.insertTime(LocalTime.of(10, 0));
            Long themeId = fixture.insertTheme("테마A");
            fixture.insertReservation("브라운", DATE, timeId, themeId);

            assertThatThrownBy(() -> reservationTimeService.delete(timeId))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("예약이 존재하는 시간은 삭제할 수 없습니다.");
        }

        @Test
        @DisplayName("예약이 없는 시간은 삭제할 수 있다")
        void 예약_없는_시간_삭제_허용() {
            Long timeId = fixture.insertTime(LocalTime.of(15, 0));

            assertThatCode(() -> reservationTimeService.delete(timeId))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("예약 가능 시간 조회 (findAvailable)")
    class Available {

        @Test
        @DisplayName("해당 날짜·테마에 이미 예약된 시간은 가능 목록에서 빠진다")
        void 예약된_시간_제외() {
            Long time10 = fixture.insertTime(LocalTime.of(10, 0));
            Long time11 = fixture.insertTime(LocalTime.of(11, 0));
            Long themeId = fixture.insertTheme("테마A");
            // 10:00은 이미 예약됨 → 11:00만 가능해야 함
            fixture.insertReservation("브라운", DATE, time10, themeId);

            List<ReservationTimeResult> available =
                    reservationTimeService.findAvailable(DATE, themeId);

            // 10:00은 예약되어 빠지고 11:00만 남는다 (정렬 비의존으로 검증)
            assertThat(available).extracting(ReservationTimeResult::getStartAt)
                    .containsExactlyInAnyOrder(LocalTime.of(11, 0));
        }

        @Test
        @DisplayName("같은 시간이라도 다른 테마에는 여전히 예약 가능하다")
        void 다른_테마는_가능() {
            Long time10 = fixture.insertTime(LocalTime.of(10, 0));
            Long themeA = fixture.insertTheme("테마A");
            Long themeB = fixture.insertTheme("테마B");
            // 테마A의 10:00만 예약
            fixture.insertReservation("브라운", DATE, time10, themeA);

            // 테마B는 10:00이 여전히 가능
            List<ReservationTimeResult> availableB =
                    reservationTimeService.findAvailable(DATE, themeB);

            assertThat(availableB).extracting(ReservationTimeResult::getStartAt)
                    .contains(LocalTime.of(10, 0));
        }

        @Test
        @DisplayName("예약이 하나도 없으면 등록된 모든 시간이 가능하다")
        void 예약_없으면_전부_가능() {
            fixture.insertTime(LocalTime.of(10, 0));
            fixture.insertTime(LocalTime.of(11, 0));
            Long themeId = fixture.insertTheme("테마A");

            List<ReservationTimeResult> available =
                    reservationTimeService.findAvailable(DATE, themeId);

            assertThat(available).hasSize(2);
        }
    }
}
