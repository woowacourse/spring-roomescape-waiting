package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.enums.Role;
import roomescape.exception.waiting.WaitingFieldRequiredException;

class WaitingTest {

    @DisplayName("예약 대기는 빈 날짜로 생성할 수 없다")
    @Test
    void waitingDateTest() {
        // given
        LocalDate date = null;
        ReservationTime time = new ReservationTime(LocalTime.now().plusHours(1));
        Theme theme = new Theme("무서운 방", "무섭습니다", "/image/scary");
        Member member = new Member(1L, "슬링키", "이메일", "비밀번호", Role.ADMIN);

        // when & then
        assertThatThrownBy(() -> new Waiting(null, date, time, theme, member))
                .isInstanceOf(WaitingFieldRequiredException.class);
    }

    @DisplayName("예약 대기는 빈 시간으로 생성할 수 없다")
    @Test
    void waitingTimeTest() {
        // given
        LocalDate date = LocalDate.now();
        ReservationTime time = null;
        Theme theme = new Theme("무서운 방", "무섭습니다", "/image/scary");
        Member member = new Member(1L, "슬링키", "이메일", "비밀번호", Role.ADMIN);
        // when & then
        assertThatThrownBy(() -> new Waiting(null, date, time, theme, member))
                .isInstanceOf(WaitingFieldRequiredException.class);
    }

    @DisplayName("예약 대기는 빈 테마로 생성할 수 없다")
    @Test
    void waitingThemeTest() {
        // given
        LocalDate date = LocalDate.now();
        ReservationTime time = new ReservationTime(LocalTime.now().plusHours(1));
        Theme theme = null;
        Member member = new Member(1L, "슬링키", "이메일", "비밀번호", Role.ADMIN);
        // when & then
        assertThatThrownBy(() -> new Waiting(null, date, time, theme, member))
                .isInstanceOf(WaitingFieldRequiredException.class);
    }

    @DisplayName("예약 대기는 빈 멤버로 생성할 수 없다")
    @Test
    void waitingMemberTest() {
        // given
        LocalDate date = LocalDate.now();
        ReservationTime time = new ReservationTime(LocalTime.now().plusHours(1));
        Theme theme = new Theme("무서운 방", "무섭습니다", "/image/scary");
        Member member = null;
        // when & then
        assertThatThrownBy(() -> new Waiting(null, date, time, theme, member))
                .isInstanceOf(WaitingFieldRequiredException.class);
    }
} 
