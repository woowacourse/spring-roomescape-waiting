package roomescape.domain;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import roomescape.fake.FixedCurrentTimeService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WaitingTest {

    private final LocalDateTime now = new FixedCurrentTimeService().now();

    @Test
    void 대기순위와_예약정보를_변경한다() {
        Reservation reservation = createReservation();
        ReservationInfo reservationInfo = ReservationInfo.create(reservation);

        Member waitingMember = Member.create("제로", Role.USER, "zero@test.com", "zero");
        Waiting waiting = Waiting.create(reservationInfo, waitingMember, 2L);

        Reservation newReservation = createNewReservation();
        ReservationInfo newReservationInfo = ReservationInfo.create(newReservation);

        waiting.updateRankAndReservationInfo(newReservationInfo);

        assertAll(
                () -> assertThat(waiting.getRank()).isEqualTo(1L),
                () -> assertThat(waiting.getReservationInfo().getDate()).isEqualTo(now.toLocalDate().minusDays(1L)),
                () -> assertThat(waiting.getReservationInfo().getTime().getStartAt()).isEqualTo(now.toLocalTime().minusHours(1L)),
                () -> assertThat(waiting.getReservationInfo().getTheme().getName()).isEqualTo("테마2")
        );
    }

    private Reservation createReservation() {
        Member member = Member.create("듀이", Role.USER, "test@test.com", "password");
        LocalDate date = now.toLocalDate();
        ReservationTime time = ReservationTime.create(now.toLocalTime());
        Theme theme = Theme.create("테마1", "테마1 설명", "테마1.jpg");
        return Reservation.create(member, date, time, theme);
    }

    private Reservation createNewReservation() {
        Member waitingToReservationMember = Member.create("제프", Role.USER, "jeff@test.com", "jeff");
        LocalDate date = now.toLocalDate().minusDays(1);
        ReservationTime time = ReservationTime.create(now.toLocalTime().minusHours(1L));
        Theme newTheme = Theme.create("테마2", "테마2 설명", "테마2.jpg");
        return Reservation.create(waitingToReservationMember, date, time, newTheme);
    }
}
