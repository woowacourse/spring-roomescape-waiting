package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationSlotFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationSlotRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    ReservationSlotRepository slotRepository;

    @Autowired
    ReservationRepository reservationRepository;


    @DisplayName("성공 : 예약을 등록한다.")
    @Test
    void addReservation() {
        //given
        ReservationSlot nextDayReservationSlot = ReservationSlotFixture.getTestYearReservationSlot(ReservationTimeFixture.get2PM(), ThemeFixture.getTheme2());

        //when
        ReservationSlot reservationSlot = slotRepository.save(nextDayReservationSlot);
        Reservation beforeSaveReservation = new Reservation(MemberFixture.getMemberTacan(), reservationSlot);
        Reservation savedReservation = reservationRepository.save(beforeSaveReservation);

        //then
        assertThat(savedReservation.getId()).isNotNull();
    }
    
    @DisplayName("성공 : 예약 대기 순서를 조회한다.")
    @Test
    void addWaitingReservation() {
        //given
        ReservationSlot reservedSlot = ReservationSlotFixture.getReservationSlot1();
        Reservation reservation = new Reservation(MemberFixture.getMemberTacan(), reservedSlot);

        //when
        Reservation savedReservation = reservationRepository.save(reservation);
        int waitingReservationCount = reservationRepository.countMyWaitingOrderByReservationId(savedReservation.getId());

        //then
        assertThat(waitingReservationCount).isEqualTo(1);
    }
    
}
