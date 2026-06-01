package roomescape.reservationwaiting.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationWaitingTest {

    private ReservationTime time;
    private Theme theme;
    private Member member;
    private LocalDate futureDate;
    private LocalDate pastDate;

    @BeforeEach
    void setUp() {
        time = ReservationTime.restore(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        theme = Theme.restore(1L, "테마1", "설명", "https://image.com");
        member = Member.restore(1L, "현미밥", "test@test.com", "1234", Role.USER);
        futureDate = LocalDate.now().plusDays(1);
        pastDate = LocalDate.now().minusDays(1);
    }

    @Test
    @DisplayName("정상 대기 생성")
    void 정상_대기_생성() {
        ReservationWaiting waiting = ReservationWaiting.of(member, futureDate, time, theme);

        assertThat(waiting.getMember()).isEqualTo(member);
        assertThat(waiting.getDate()).isEqualTo(futureDate);
        assertThat(waiting.getTime()).isEqualTo(time);
        assertThat(waiting.getTheme()).isEqualTo(theme);
    }

    @Test
    @DisplayName("멤버가 null이면 예외 발생")
    void 멤버_null_예외() {
        assertThatThrownBy(() -> ReservationWaiting.of(null, futureDate, time, theme))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약자는 필수입니다.");
    }

    @Test
    @DisplayName("과거 날짜면 예외 발생")
    void 과거_날짜_예외() {
        assertThatThrownBy(() -> ReservationWaiting.of(member, pastDate, time, theme))
                .isInstanceOf(BusinessException.class);
    }
}
