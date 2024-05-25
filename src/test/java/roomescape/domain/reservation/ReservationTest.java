package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.domain.member.Member;
import roomescape.domain.reservationtime.ReservationTime;

public class ReservationTest {
    @ParameterizedTest
    @CsvSource({
            "2000-04-07T01:30, true",
            "2000-04-08T00:00, true",
            "2000-04-07T00:30, false",
            "2000-04-06T00:00, false"
    })
    void 예약_날짜와_시간이_특정_시간보다_전인지_확인할_수_있다(LocalDateTime targetDateTime, boolean expected) {
        Reservation reservation = new Reservation(
                LocalDate.of(2000, 4, 7),
                new ReservationTime(LocalTime.of(1, 0)),
                null,
                null
        );

        assertThat(reservation.isPast(targetDateTime))
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "1, true",
            "2, false"
    })
    void 예약한_사용자가_맞는지_확인할_수_있다(Long targetMemberId, boolean expected) {
        Reservation reservation = new Reservation(
                null,
                null,
                null,
                new Member(1L, null, null, null, null)
        );
        Member targetMember = new Member(targetMemberId, null, null, null, null);

        assertThat(reservation.isOwnedBy(targetMember))
                .isEqualTo(expected);
    }
}
