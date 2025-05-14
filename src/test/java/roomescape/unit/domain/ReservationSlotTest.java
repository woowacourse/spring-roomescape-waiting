package roomescape.unit.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationSlots;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.global.Role;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationSlotTest {

    @Test
    void 선택된_테마와_날짜에_대해서_가능한_시간들을_확인할_수_있다() {
        // given
        Member beforeAddMember = new Member("Hula", "test@test.com", "test", Role.USER);
        LocalDate today = LocalDate.now();
        LocalTime firstTime = LocalTime.now().plusHours(1L);
        LocalTime secondTime = LocalTime.now().plusHours(2L);
        Theme theme = new Theme("테마", "설명", "image.png");

        ReservationTime reservationTime1 = new ReservationTime(1L, firstTime);
        ReservationTime reservationTime2 = new ReservationTime(2L, secondTime);

        Reservation reservation1 = new Reservation(1L, beforeAddMember, today, reservationTime1, theme);

        List<ReservationTime> times = List.of(reservationTime1, reservationTime2);
        List<Reservation > alreadyReservedReservation = List.of(reservation1);


        // when
        ReservationSlots reservationSlots = new ReservationSlots(times, alreadyReservedReservation);

        //then
        List<ReservationSlot> expected = List.of(new ReservationSlot(1L, firstTime, true),
                new ReservationSlot(2L, secondTime, false));

        assertThat(reservationSlots.getReservationSlots()).containsExactlyInAnyOrderElementsOf(expected);
    }
}
