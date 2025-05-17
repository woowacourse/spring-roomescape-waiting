package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


class ReservationTest {

    private final Member member = new Member(1L, "Alice", MemberRole.USER, "alice@example.com", "Password1!");
    private final ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
    private final Theme theme = new Theme(1L, "Escape Room", "A thrilling escape room experience", "thumbnail.jpg");
    private final LocalDate date = LocalDate.of(2024, 6, 1);

    @Nested
    class SuccessTest {

        @Test
        @DisplayName("동일한 id를 가진 예약은 동등하다")
        void reservations_with_same_id_are_equal() {
            Reservation reservation1 = new Reservation(1L, member, date, time, theme, ReservationStatus.RESERVED);
            Reservation reservation2 = new Reservation(1L, member, date, time, theme, ReservationStatus.RESERVED);

            assertThat(reservation1).isEqualTo(reservation2);
        }

        @Test
        @DisplayName("다른 id를 가진 예약은 동등하지 않다")
        void reservations_with_different_id_are_not_equal() {
            Reservation reservation1 = new Reservation(1L, member, date, time, theme, ReservationStatus.RESERVED);
            Reservation reservation2 = new Reservation(2L, member, date, time, theme, ReservationStatus.RESERVED);

            assertThat(reservation1).isNotEqualTo(reservation2);
        }

        @Test
        @DisplayName("id가 null인 예약은 동등하지 않다")
        void reservations_with_null_id_are_not_equal() {
            Reservation reservation1 = new Reservation(null, member, date, time, theme, ReservationStatus.RESERVED);
            Reservation reservation2 = new Reservation(null, member, date, time, theme, ReservationStatus.RESERVED);

            assertThat(reservation1).isNotEqualTo(reservation2);
        }
    }
}
