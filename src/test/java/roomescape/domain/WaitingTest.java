package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.domain.vo.Name;

class WaitingTest {

    private Theme theme;
    private Time time;

    @BeforeEach
    void setUp() {
        theme = new Theme(1L, new Name("테마"), "http://thumbnail", "설명");
        time = new Time(1L, LocalTime.of(10, 0));
    }

    @Nested
    class IsOwnedBy {

        @Test
        @DisplayName("회원 id가 같으면 true를 반환한다")
        void returnsTrueForSameMember() {
            Member member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);
            Waiting waiting = new Waiting(member, LocalDate.now().plusDays(1), time, theme, null);

            assertThat(waiting.isOwnedBy(1L)).isTrue();
        }

        @Test
        @DisplayName("회원 id가 다르면 false를 반환한다")
        void returnsFalseForDifferentMember() {
            Member member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);
            Waiting waiting = new Waiting(member, LocalDate.now().plusDays(1), time, theme, null);

            assertThat(waiting.isOwnedBy(2L)).isFalse();
        }
    }

    @Nested
    class ToReservation {

        @Test
        @DisplayName("예약 대기를 BOOKED 상태의 예약으로 전환한다")
        void convertsToBookedReservation() {
            Member member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);
            LocalDate date = LocalDate.now().plusDays(1);
            Waiting waiting = new Waiting(member, date, time, theme, null);

            Reservation reservation = waiting.toReservation(LocalDateTime.now());

            assertThat(reservation.getMember()).isEqualTo(member);
            assertThat(reservation.getDate()).isEqualTo(date);
            assertThat(reservation.getTime()).isEqualTo(time);
            assertThat(reservation.getTheme()).isEqualTo(theme);
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.BOOKED);
        }

        @Test
        @DisplayName("지난 시간의 예약 대기는 예약으로 전환할 수 없다")
        void throwsForPastSlot() {
            Member member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);
            Waiting waiting = new Waiting(member, LocalDate.now().minusDays(1), time, theme, null);

            assertThatThrownBy(() -> waiting.toReservation(LocalDateTime.now()))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }
}
