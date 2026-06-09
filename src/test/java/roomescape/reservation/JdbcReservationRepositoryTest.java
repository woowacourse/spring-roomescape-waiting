package roomescape.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.infrastructure.JdbcReservationRepository;
import roomescape.reservation.infrastructure.projection.ReservationDetailProjection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@JdbcTest
@Transactional
@ActiveProfiles("test")
@Import(JdbcReservationRepository.class)
class JdbcReservationRepositoryTest {

    private static final long MEMBER_ID = 1L;

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
    @DisplayName("같은 스케줄에는 확정 예약을 중복 저장할 수 없다.")
    void save_중복예약_DB제약_테스트() {
        Reservation duplicatedReservation = new Reservation(null, 2L, 1L);

        assertThatThrownBy(() -> reservationRepository.save(duplicatedReservation))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 전체_예약_상세_조회_레포지토리_테스트() {
        Reservation reservation = new Reservation(null, MEMBER_ID, 4L);
        Reservation savedReservation = reservationRepository.save(reservation);

        List<ReservationDetailProjection> reservations = reservationRepository.findAll();

        assertThat(reservations).hasSize(6);
        assertThat(reservations).extracting(ReservationDetailProjection::id)
                .contains(savedReservation.getId());
    }

    @Test
    @DisplayName("예약 id로 예약을 삭제할 수 있다.")
    void deleteById_테스트() {
        Reservation reservation = new Reservation(null, MEMBER_ID, 4L);
        Reservation savedReservation = reservationRepository.save(reservation);

        reservationRepository.deleteById(savedReservation.getId());

        assertThat(reservationRepository.findById(savedReservation.getId())).isEmpty();
    }

    @Test
    @DisplayName("이용 가능한 시간을 조회할 수 있다.")
    void findTimeIdByDateAndThemeId_테스트() {
        Set<Long> result = reservationRepository.findTimeIdByDateAndThemeId(LocalDate.parse("2026-05-05"), 1L);

        assertThat(result).containsExactlyInAnyOrder(1L);
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

    @Test
    @DisplayName("특정 회원의 다가오는 예약 상세를 조회할 수 있다.")
    void findUpcomingReservationDetailsByMemberId_테스트() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 11, 0);

        List<ReservationDetailProjection> result =
                reservationRepository.findUpcomingReservationDetailsByMemberId(MEMBER_ID, now);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(ReservationDetailProjection::id)
                .containsExactly(2L, 3L, 4L);
        assertThat(result).extracting(ReservationDetailProjection::date)
                .isSorted();
        assertThat(result.get(0).getTime()).isEqualTo(LocalTime.of(11, 0));
    }

    @Test
    @DisplayName("특정 회원의 지난 예약 상세를 조회할 수 있다.")
    void findPastReservationDetailsByMemberId_테스트() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 11, 0);

        List<ReservationDetailProjection> result =
                reservationRepository.findPastReservationDetailsByMemberId(MEMBER_ID, now);

        assertThat(result).hasSize(1);
        assertThat(result).extracting(ReservationDetailProjection::id)
                .containsExactly(1L);
        assertThat(result.get(0).getTime()).isEqualTo(LocalTime.of(10, 0));
    }
}
