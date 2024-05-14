package roomescape.reservation.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRole;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTimeAvailabilitiesTest {

    @DisplayName("예약이 결정된 시간인지 검증후 결과를 저장한다.")
    @Test
    void checkIsAlreadyBookedTest() {
        // Given
        final ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(1, 30));
        final ReservationTime reservationTime2 = new ReservationTime(2L, LocalTime.of(2, 30));
        final ReservationTime reservationTime3 = new ReservationTime(3L, LocalTime.of(4, 30));
        final ReservationTime notBookedTime = new ReservationTime(4L, LocalTime.of(3, 30));
        final List<ReservationTime> reservationTimes = List.of(reservationTime1, reservationTime2, reservationTime3, notBookedTime);
        final Theme theme = Theme.of(1L, "켈리의 두근두근", "안녕", "사진 링크");
        final Member member = Member.createMemberWithId(1L, MemberRole.USER, "password1111", "kelly", "kelly6bf@mail.com");

        final LocalDate date = LocalDate.now().plusDays(3);
        final List<Reservation> reservations = List.of(
                Reservation.of(1L, date, reservationTime1, theme, member),
                Reservation.of(2L, date, reservationTime2, theme, member),
                Reservation.of(3L, date, reservationTime3, theme, member)
        );


        // When
        final ReservationTimeAvailabilities reservationTimeAvailabilities = ReservationTimeAvailabilities.of(reservationTimes, reservations);

        // Then
        final Map<ReservationTime, Boolean> values = reservationTimeAvailabilities.values();
        assertThat(values.get(reservationTime1)).isTrue();
        assertThat(values.get(reservationTime2)).isTrue();
        assertThat(values.get(reservationTime3)).isTrue();
        assertThat(values.get(notBookedTime)).isFalse();
    }
}
