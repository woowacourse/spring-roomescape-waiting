package roomescape;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import roomescape.domain.policy.ReservationPolicy;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.exception.client.InvalidCommandException;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.support.ReservationTestHelper;
import roomescape.support.TestFutureOnlyPolicy;

/*
 * 미션2 사이클2 - 서비스 정책 검증 테스트.
 *
 * IntegrationTest 상속으로 매 테스트 빈 DB 보장.
 * 고정 Clock(2026-05-13 12:00)을 주입해 "과거/현재/미래"의 의미를 결정적으로 만듦.
 *
 * 각 @Nested 클래스가 정책 하나에 대응 — 정책이 늘어나면 @Nested가 늘어남 !
 */
public class ReservationPolicyStepTest extends IntegrationTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 13);
    private static final LocalTime NOW_TIME = LocalTime.of(12, 0);

    @TestConfiguration
    static class FixedPolicyConfig {
        @Bean
        @Primary
        public ReservationPolicy fixedReservationPolicy() {
            Clock fixed = Clock.fixed(
                    TODAY.atTime(NOW_TIME).atZone(ZoneId.systemDefault()).toInstant(),
                    ZoneId.systemDefault()
            );
            return new TestFutureOnlyPolicy(fixed);
        }
    }

    @Autowired
    private ReservationTestHelper helper;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    private Long timeId10;
    private Long themeId;

    @BeforeEach
    void setUp() {
        timeId10 = helper.insertTime(LocalTime.of(10, 0));
        themeId = helper.insertTheme("테마A", "설명", "https://example.com/a.jpg");
    }

    @Nested
    @DisplayName("과거 시점 예약 거부 정책")
    class PastReservationPolicy {

        @Test
        @DisplayName("어제 날짜로 예약 시도 시 거부된다")
        void 어제_날짜는_거부() {
            ReservationCreateCommand command = new ReservationCreateCommand(
                    "브라운", TODAY.minusDays(1), timeId10, themeId
            );

            assertThatThrownBy(() -> reservationService.create(command))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("지나간 날짜, 시간으로는 예약할 수 없습니다.");
        }

        @Test
        @DisplayName("오늘 + 이미 지난 시간(10:00, 현재 12:00)은 거부된다")
        void 오늘이지만_이미_지난_시간은_거부() {
            ReservationCreateCommand command = new ReservationCreateCommand(
                    "브라운", TODAY, timeId10, themeId
            );

            assertThatThrownBy(() -> reservationService.create(command))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("지나간 날짜, 시간으로는 예약할 수 없습니다.");
        }

        @Test
        @DisplayName("오늘 + 미래 시간(15:00, 현재 12:00)은 허용된다")
        void 오늘이지만_미래_시간은_허용() {
            Long timeId15 = helper.insertTime(LocalTime.of(15, 0));
            ReservationCreateCommand command = new ReservationCreateCommand(
                    "브라운", TODAY, timeId15, themeId
            );

            assertThatCode(() -> reservationService.create(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("내일 날짜는 허용된다")
        void 내일_날짜는_허용() {
            ReservationCreateCommand command = new ReservationCreateCommand(
                    "브라운", TODAY.plusDays(1), timeId10, themeId
            );

            assertThatCode(() -> reservationService.create(command))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("중복 예약 거부 정책")
    class DuplicateReservationPolicy {

        @Test
        @DisplayName("같은 날짜+시간+테마에 이미 예약이 있으면 거부된다")
        void 같은_날짜_시간_테마_중복은_거부() {
            LocalDate futureDate = TODAY.plusDays(1);
            // 첫 예약은 성공
            reservationService.create(new ReservationCreateCommand(
                    "브라운", futureDate, timeId10, themeId
            ));

            // 같은 조합으로 두 번째 시도 → 거부
            assertThatThrownBy(() -> reservationService.create(new ReservationCreateCommand(
                    "모오카", futureDate, timeId10, themeId
            )))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("해당 시간은 이미 예약되었습니다. 다른 시간을 선택해 주세요.");
        }

        @Test
        @DisplayName("같은 날짜+시간이라도 테마가 다르면 허용된다")
        void 같은_날짜_시간이지만_테마가_다르면_허용() {
            LocalDate futureDate = TODAY.plusDays(1);
            Long anotherThemeId = helper.insertTheme("테마B", "설명B", "https://example.com/b.jpg");

            reservationService.create(new ReservationCreateCommand(
                    "브라운", futureDate, timeId10, themeId
            ));

            assertThatCode(() -> reservationService.create(new ReservationCreateCommand(
                    "모오오카", futureDate, timeId10, anotherThemeId
            )))
                    .doesNotThrowAnyException();
        }

    }

    @Nested
    @DisplayName("시간 삭제 거부 정책")
    class TimeInUsePolicy {

        @Test
        @DisplayName("예약이 존재하는 시간은 삭제할 수 없다")
        void 예약이_있는_시간은_삭제_거부() {
            reservationService.create(new ReservationCreateCommand(
                    "브라운", TODAY.plusDays(1), timeId10, themeId
            ));

            assertThatThrownBy(() -> reservationTimeService.delete(timeId10))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("예약이 존재하는 시간은 삭제할 수 없습니다.");
        }

        @Test
        @DisplayName("예약이 없는 시간은 삭제할 수 있다")
        void 예약이_없는_시간은_삭제_허용() {
            Long unusedTimeId = helper.insertTime(LocalTime.of(15, 0));

            assertThatCode(() -> reservationTimeService.delete(unusedTimeId))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("예약을 삭제한 후에는 그 시간을 삭제할 수 있다")
        void 예약을_삭제한_뒤에는_시간_삭제_허용() {
            Long reservationId = reservationService.create(new ReservationCreateCommand(
                    "브라운", TODAY.plusDays(1), timeId10, themeId
            )).getId();

            reservationService.delete(reservationId);

            assertThatCode(() -> reservationTimeService.delete(timeId10))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("입력값 검증 정책")
    class InputValidationPolicy {

        @Test
        @DisplayName("이름이 null이면 거부된다")
        void 이름_null_거부() {
            assertThatThrownBy(() -> reservationService.create(new ReservationCreateCommand(
                    null, TODAY.plusDays(1), timeId10, themeId
            )))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasMessage("예약자 이름은 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("날짜가 null이면 거부된다")
        void 날짜_null_거부() {
            assertThatThrownBy(() -> reservationService.create(new ReservationCreateCommand(
                    "브라운", null, timeId10, themeId
            )))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasMessage("예약 날짜는 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("시간 ID가 null이면 거부된다")
        void 시간Id_null_거부() {
            assertThatThrownBy(() -> reservationService.create(new ReservationCreateCommand(
                    "브라운", TODAY.plusDays(1), null, themeId
            )))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasMessage("예약 시간을 선택해 주세요.");
        }

        @Test
        @DisplayName("테마 ID가 null이면 거부된다")
        void 테마Id_null_거부() {
            assertThatThrownBy(() -> reservationService.create(new ReservationCreateCommand(
                    "브라운", TODAY.plusDays(1), timeId10, null
            )))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasMessage("예약 테마를 선택해 주세요.");
        }
    }

}
