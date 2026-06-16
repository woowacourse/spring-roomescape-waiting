package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservation.adapter.out.persistence.JpaReservationRepository;
import roomescape.reservation.application.port.out.projection.ReservationDetailProjection;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.slot.domain.Slot;
import roomescape.theme.domain.Theme;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaReservationRepository.class)
class JpaReservationRepositoryTest {

    private static final long MEMBER_ID = 1L;

    @Autowired
    private JpaReservationRepository reservationRepository;

    @Test
    @DisplayName("예약을 저장할 수 있다.")
    void saves_reservation_successfully() {
        Reservation reservation = Reservation.create(roomescape.TestFixtures.member(MEMBER_ID), slot(4L));

        Reservation savedReservation = reservationRepository.save(reservation);

        assertSoftly(softly -> {
            softly.assertThat(savedReservation).isNotNull();
            softly.assertThat(savedReservation.getId()).isNotNull();
            softly.assertThat(savedReservation.getMemberId()).isEqualTo(MEMBER_ID);
            softly.assertThat(savedReservation.getSlotId()).isEqualTo(4L);
        });
    }

    private Slot slot(long slotId) {
        return Slot.of(
                slotId,
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "theme", "description", "thumbnail")
        );
    }

    @Test
    @DisplayName("전체 예약 상세를 조회할 수 있다.")
    void finds_all_reservation_details_successfully() {
        Reservation reservation = Reservation.create(roomescape.TestFixtures.member(MEMBER_ID), slot(4L));
        Reservation savedReservation = reservationRepository.save(reservation);

        List<ReservationDetailProjection> reservations = reservationRepository.findAll();

        assertThat(reservations).hasSize(5);
        assertThat(reservations).extracting(ReservationDetailProjection::id)
                .contains(savedReservation.getId());
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다.")
    void deletes_reservation_successfully() {
        Reservation reservation = Reservation.create(roomescape.TestFixtures.member(MEMBER_ID), slot(4L));
        Reservation savedReservation = reservationRepository.save(reservation);

        reservationRepository.deleteById(savedReservation.getId());

        List<ReservationDetailProjection> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(4);
        assertThat(reservations).extracting(ReservationDetailProjection::id)
                .doesNotContain(savedReservation.getId());
    }

    @Test
    @DisplayName("이용 가능한 시간을 조회할 수 있다.")
    void finds_available_time_ids_successfully() {
        Set<Long> result = reservationRepository.findTimeIdByDateAndThemeId(LocalDate.parse("2026-05-05"), 1L);

        assertThat(result).containsExactlyInAnyOrder(1L);
    }

    @Test
    @DisplayName("특정 회원의 모든 예약 상세를 조회할 수 있다.")
    void finds_all_reservation_details_by_member_id_successfully() {
        List<ReservationDetailProjection> result = reservationRepository.findAllReservationDetailsByMemberId(MEMBER_ID);

        assertThat(result).hasSize(4);
        assertThat(result).extracting(ReservationDetailProjection::memberId)
                .containsOnly(MEMBER_ID);
    }

}
