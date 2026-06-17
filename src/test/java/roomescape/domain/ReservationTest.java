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
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.store.Store;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.Time;
import roomescape.common.vo.Name;

class ReservationTest {

    private Member member;
    private Theme theme;
    private Store store;

    @BeforeEach
    void setUp() {
        member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);
        theme = new Theme(1L, new Name("테마"), "http://thumbnail", "설명");
        store = new Store(1L, "강남점");
    }

    @Nested
    class CreateByUser {

        @Test
        @DisplayName("미래 시간으로 생성하면 성공한다")
        void createsWithFutureTime() {
            Time time = new Time(1L, LocalTime.of(10, 0));
            LocalDate futureDate = LocalDate.now().plusDays(1);
            LocalDateTime now = LocalDateTime.now();

            Reservation reservation = Reservation.createByUser(member, futureDate, time, theme, store, now);

            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        }

        @Test
        @DisplayName("과거 시간으로 생성하면 예외를 던진다")
        void throwsWhenCreatingWithPastTime() {
            Time time = new Time(1L, LocalTime.of(10, 0));
            LocalDate pastDate = LocalDate.now().minusDays(1);
            LocalDateTime now = LocalDateTime.now();

            assertThatThrownBy(() -> Reservation.createByUser(member, pastDate, time, theme, store, now))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    class CancelByUser {

        @Test
        @DisplayName("미래 예약을 취소하면 상태가 CANCELED가 된다")
        void cancelsFutureReservation() {
            Reservation reservation = bookedReservation(LocalDate.now().plusDays(1));

            reservation.cancelByUser(LocalDateTime.now());

            assertThat(reservation.isActive()).isFalse();
        }

        @Test
        @DisplayName("과거 예약을 취소하면 예외를 던진다")
        void throwsWhenCancelingPastReservation() {
            Reservation reservation = bookedReservation(LocalDate.now().minusDays(1));

            assertThatThrownBy(() -> reservation.cancelByUser(LocalDateTime.now()))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    class CancelByAdmin {

        @Test
        @DisplayName("과거 예약도 어드민은 취소할 수 있다")
        void adminCanCancelPastReservation() {
            Reservation reservation = bookedReservation(LocalDate.now().minusDays(1));

            reservation.cancelByAdmin(LocalDateTime.now());

            assertThat(reservation.isActive()).isFalse();
        }


    }

    @Nested
    class IsActive {

        @Test
        @DisplayName("BOOKED 상태이면 true를 반환한다")
        void returnsTrueWhenBooked() {
            Reservation reservation = bookedReservation(LocalDate.now().plusDays(1));

            assertThat(reservation.isActive()).isTrue();
        }

        @Test
        @DisplayName("CANCELED 상태이면 false를 반환한다")
        void returnsFalseWhenCanceled() {
            Time time = new Time(1L, LocalTime.of(10, 0));
            Reservation reservation = Reservation.reconstruct(
                    1L, member, LocalDate.now().plusDays(1), time, theme,
                    ReservationStatus.CANCELED, LocalDateTime.now(), 0L, store);

            assertThat(reservation.isActive()).isFalse();
        }
    }

    @Nested
    class IsSameMember {

        @Test
        @DisplayName("같은 멤버면 true를 반환한다")
        void returnsTrueForSameMember() {
            Reservation reservation = bookedReservation(LocalDate.now().plusDays(1));

            assertThat(reservation.isSameMember(member)).isTrue();
        }

        @Test
        @DisplayName("다른 멤버면 false를 반환한다")
        void returnsFalseForDifferentMember() {
            Reservation reservation = bookedReservation(LocalDate.now().plusDays(1));
            Member other = new Member(2L, "다른유저", "other@test.com", "password", MemberRole.USER);

            assertThat(reservation.isSameMember(other)).isFalse();
        }
    }

    @Nested
    class Update {

        @Test
        @DisplayName("미래 날짜와 시간으로 수정하면 성공한다")
        void updatesWithFutureDateTime() {
            Reservation reservation = bookedReservation(LocalDate.now().plusDays(1));

            LocalDate newDate = LocalDate.now().plusDays(3);
            Time newTime = new Time(2L, LocalTime.of(14, 0));
            reservation.update(newDate, newTime, LocalDateTime.now());

            assertThat(reservation.getDate()).isEqualTo(newDate);
            assertThat(reservation.getTime()).isEqualTo(newTime);
        }

        @Test
        @DisplayName("과거 날짜로 수정하면 예외를 던진다")
        void throwsWhenUpdatingWithPastDateTime() {
            Reservation reservation = bookedReservation(LocalDate.now().plusDays(1));

            LocalDate pastDate = LocalDate.now().minusDays(1);
            Time pastTime = new Time(2L, LocalTime.of(14, 0));

            assertThatThrownBy(() -> reservation.update(pastDate, pastTime, LocalDateTime.now()))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    private Reservation bookedReservation(LocalDate date) {
        Time time = new Time(1L, LocalTime.of(10, 0));
        return Reservation.createByAdmin(member, date, time, theme, store);
    }
}
