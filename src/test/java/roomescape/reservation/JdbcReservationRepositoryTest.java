package roomescape.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.infrastructure.JdbcReservationRepository;
import roomescape.reservation.infrastructure.projection.ReservationDetailProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@JdbcTest
@Transactional
@ActiveProfiles("test")
@Import(JdbcReservationRepository.class)
class JdbcReservationRepositoryTest {

    private static final long MEMBER_ID = 1L;
    private static final long STORE_ID = 1L;

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @Test
    void 예약_저장_레포지토리_테스트() {
        Reservation reservation = new Reservation(null, MEMBER_ID, 4L);

        Reservation savedReservation = reservationRepository.save(reservation);

        assertSoftly(softly -> {
            softly.assertThat(savedReservation).isNotNull();
            softly.assertThat(savedReservation.getId()).isNotNull();
            softly.assertThat(savedReservation.getMemberId()).isEqualTo(MEMBER_ID);
            softly.assertThat(savedReservation.getScheduleId()).isEqualTo(4L);
        });
    }

    @Test
    void 전체_예약_상세_조회_레포지토리_테스트() {
        Reservation reservation = new Reservation(null, MEMBER_ID, 4L);
        Reservation savedReservation = reservationRepository.save(reservation);

        List<ReservationDetailProjection> reservations = reservationRepository.findAllDetailsByStoreId(STORE_ID);

        assertThat(reservations).hasSize(5);
        assertThat(reservations).extracting(ReservationDetailProjection::id)
                .contains(savedReservation.getId());
    }

    @Test
    void 예약_삭제_레포지토리_테스트() {
        Reservation reservation = new Reservation(null, MEMBER_ID, 4L);
        Reservation savedReservation = reservationRepository.save(reservation);

        reservationRepository.deleteByIdAndMemberId(savedReservation.getId(), MEMBER_ID);

        List<ReservationDetailProjection> reservations = reservationRepository.findAllDetailsByStoreId(STORE_ID);
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

        assertThat(reservationRepository.findAllDetailsByStoreId(STORE_ID))
                .extracting(ReservationDetailProjection::id)
                .doesNotContain(1L);
    }

    @Test
    @DisplayName("회원 id가 일치하지 않으면 예약은 삭제되지 않는다.")
    void deleteByIdAndMemberId_회원불일치_테스트() {
        reservationRepository.deleteByIdAndMemberId(1L, 999L);

        assertThat(reservationRepository.findAllDetailsByStoreId(STORE_ID))
                .extracting(ReservationDetailProjection::id)
                .contains(1L);
    }

    @Test
    @DisplayName("같은 스케줄에 본인 제외 다른 예약이 있으면 true를 반환한다.")
    void existsByScheduleIdAndIdNot_true반환_테스트() {
        boolean result = reservationRepository.existsByScheduleIdAndIdNot(2L, 1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("같은 스케줄에 본인 제외 다른 예약이 없으면 false를 반환한다.")
    void existsByScheduleIdAndIdNot_false반환_테스트() {
        boolean result = reservationRepository.existsByScheduleIdAndIdNot(4L, 1L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("회원 본인의 기존 예약을 변경 가능한 스케줄로 변경할 수 있다.")
    void updateScheduleByIdAndMemberId_테스트() {
        int affectedRow = reservationRepository.updateScheduleById(1L, 4L);

        assertThat(affectedRow).isEqualTo(1);
        assertThat(reservationRepository.findById(1L))
                .get()
                .extracting(Reservation::getScheduleId)
                .isEqualTo(4L);
    }
}
