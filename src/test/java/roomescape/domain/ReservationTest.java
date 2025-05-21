package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.test.fixture.DateFixture.NEXT_DAY;
import static roomescape.test.fixture.DateFixture.YESTERDAY;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Nested
    @DisplayName("예약을 생성할 때 검증을 수행한다.")
    public class validate {

        @DisplayName("비어있는 예약날짜로는 예약을 생성할 수 없다")
        @Test
        void cannotCreateReservationWithNullDate() {
            // given
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qweqw123!");
            LocalDate nullDate = null;
            ReservationStatus status = ReservationStatus.BOOKED;
            ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
            Theme theme = new Theme(1L, "이름", "설명", "썸네일");

            // when & then
            assertThatThrownBy(() -> new Reservation(1L, nullDate, status, time, theme, member))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비어있는 예약날짜로 예약을 생성할 수 없습니다.");
        }

        @DisplayName("비어있는 예약상태로는 예약을 생성할 수 없다")
        @Test
        void cannotCreateReservationWithNullStatus() {
            // given
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qweqw123!");
            LocalDate date = NEXT_DAY;
            ReservationStatus nullStatus = null;
            ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
            Theme theme = new Theme(1L, "이름", "설명", "썸네일");

            // when & then
            assertThatThrownBy(() -> new Reservation(1L, date, nullStatus, time, theme, member))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비어있는 예약상태로 예약을 생성할 수 없습니다.");
        }

        @DisplayName("비어있는 예약시간으로는 예약을 생성할 수 없다")
        @Test
        void cannotCreateReservationWithNullTime() {
            // given
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qweqw123!");
            LocalDate date = NEXT_DAY;
            ReservationStatus status = ReservationStatus.BOOKED;
            ReservationTime nullTime = null;
            Theme theme = new Theme(1L, "이름", "설명", "썸네일");

            // when & then
            assertThatThrownBy(() -> new Reservation(1L, date, status, nullTime, theme, member))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비어있는 예약시간으로는 예약을 생성할 수 없습니다.");
        }

        @DisplayName("비어있는 테마로는 예약을 생성할 수 없다")
        @Test
        void cannotCreateReservationWithNullTheme() {
            // given
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qweqw123!");
            LocalDate date = NEXT_DAY;
            ReservationStatus status = ReservationStatus.BOOKED;
            ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(10, 0));
            Theme nullTheme = null;

            // when & then
            assertThatThrownBy(
                    () -> new Reservation(1L, date, status, time, nullTheme, member))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비어있는 테마로는 예약을 생성할 수 없습니다.");
        }

        @DisplayName("비어있는 멤버로는 예약을 생성할 수 없다")
        @Test
        void cannotCreateReservationWithNullMember() {
            // given
            Member nullMember = null;
            LocalDate date = NEXT_DAY;
            ReservationStatus status = ReservationStatus.BOOKED;
            ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(10, 0));
            Theme theme = new Theme(1L, "이름", "설명", "썸네일");

            // when & then
            assertThatThrownBy(
                    () -> new Reservation(1L, date, status, time, theme, nullMember))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비어있는 멤버로는 예약을 생성할 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("과거의 예약인지 체크할 수 있다.")
    public class isPastDateTime {

        @DisplayName("과거의 예약인 경우 true 리턴")
        @Test
        void isPastDateTime() {
            // given
            ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
            Theme theme = new Theme(1L, "회원", "설명", "섬네일");
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qwer1234!");
            Reservation reservation = new Reservation(1L, YESTERDAY, ReservationStatus.BOOKED, time, theme, member);

            // when
            boolean isPast = reservation.isPastDateTime();

            // then
            assertThat(isPast).isTrue();
        }

        @DisplayName("과거가 아닌 예약인 경우 false 리턴")
        @Test
        void isNotPastDateTime() {
            // given
            ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
            Theme theme = new Theme(1L, "회원", "설명", "섬네일");
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qwer1234!");
            Reservation reservation = new Reservation(1L, NEXT_DAY, ReservationStatus.BOOKED, time, theme, member);

            // when
            boolean isPast = reservation.isPastDateTime();

            // then
            assertThat(isPast).isFalse();
        }
    }
}
