package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservation.adapter.out.persistence.JdbcReservationRepository;
import roomescape.reservation.application.port.out.projection.ReservationDetailProjection;
import roomescape.reservationtime.ReservationTime;
import roomescape.slot.domain.Slot;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@JdbcTest
@ActiveProfiles("test")
@Import(JdbcReservationRepository.class)
class JdbcReservationRepositoryTest {

    private static final long MEMBER_ID = 1L;

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @Test
    void 예약_저장_레포지토리_테스트() {
        Reservation reservation = Reservation.create(MEMBER_ID, slot(4L));

        Reservation savedReservation = reservationRepository.save(reservation);

        assertSoftly(softly -> {
            softly.assertThat(savedReservation).isNotNull();
            softly.assertThat(savedReservation.getId()).isNotNull();
            softly.assertThat(savedReservation.getMemberId()).isEqualTo(MEMBER_ID);
            softly.assertThat(savedReservation.getSlotId()).isEqualTo(4L);
        });
    }

    @Test
    void 전체_예약_상세_조회_레포지토리_테스트() {
        Reservation reservation = Reservation.create(MEMBER_ID, slot(4L));
        Reservation savedReservation = reservationRepository.save(reservation);

        List<ReservationDetailProjection> reservations = reservationRepository.findAll();

        assertThat(reservations).hasSize(5);
        assertThat(reservations).extracting(ReservationDetailProjection::id)
                .contains(savedReservation.getId());
    }

    @Test
    void 예약_삭제_레포지토리_테스트() {
        Reservation reservation = Reservation.create(MEMBER_ID, slot(4L));
        Reservation savedReservation = reservationRepository.save(reservation);

        reservationRepository.deleteByIdAndMemberId(savedReservation.getId(), MEMBER_ID);

        List<ReservationDetailProjection> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(4);
        assertThat(reservations).extracting(ReservationDetailProjection::id)
                .doesNotContain(savedReservation.getId());
    }

    @Test
    @DisplayName("이용 가능한 시간을 조회할 수 있다.")
    void findTimeIdByDateAndThemeId_테스트() {
        Set<Long> result = reservationRepository.findTimeIdByDateAndThemeId(LocalDate.parse("2026-05-05"), 1L);

        assertThat(result).containsExactlyInAnyOrder(1L);
    }

    @Test
    @DisplayName("특정 회원의 모든 예약 상세를 조회할 수 있다.")
    void findAllReservationDetailsByMemberId_테스트() {
        List<ReservationDetailProjection> result = reservationRepository.findAllReservationDetailsByMemberId(MEMBER_ID);

        assertThat(result).hasSize(4);
        assertThat(result).extracting(ReservationDetailProjection::memberId)
                .containsOnly(MEMBER_ID);
    }

    @Test
    @DisplayName("예약 id와 회원 id가 일치하면 예약을 삭제할 수 있다.")
    void deleteByIdAndMemberId_테스트() {
        reservationRepository.deleteByIdAndMemberId(1L, MEMBER_ID);

        assertThat(reservationRepository.findAll())
                .extracting(ReservationDetailProjection::id)
                .doesNotContain(1L);
    }

    @Test
    @DisplayName("회원 id가 일치하지 않으면 예약은 삭제되지 않는다.")
    void deleteByIdAndMemberId_회원불일치_테스트() {
        reservationRepository.deleteByIdAndMemberId(1L, 999L);

        assertThat(reservationRepository.findAll())
                .extracting(ReservationDetailProjection::id)
                .contains(1L);
    }

    private Slot slot(long slotId) {
        return Slot.of(
                slotId,
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "theme", "description", "thumbnail")
        );
    }
}
