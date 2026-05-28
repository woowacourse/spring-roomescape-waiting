package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import roomescape.domain.Reservation;
import roomescape.domain.Status;
import roomescape.dto.ReservationResponse;
import roomescape.repository.ReservationDao;

@JdbcTest
@Import(ReservationDao.class)
class ReservationDaoTest {

    @Autowired
    private ReservationDao reservationDao;

    @DisplayName("예약을 저장하면 생성된 ID를 반환한다.")
    @Test
    void 예약_저장() {
        // schedule ID 15 (2026-05-27, theme 1, time 3) 사용
        Long id = reservationDao.save("테스터", 15L, LocalDateTime.of(2026, 6, 1, 12, 0));

        assertThat(id).isNotNull().isPositive();
    }

    @DisplayName("ID로 예약을 조회하면 저장한 이름·스케줄·상태가 반환된다.")
    @Test
    void ID로_예약_조회() {
        // data.sql: reservation ID 1 = 김철수 / schedule_id=1 / RESERVED
        Reservation reservation = reservationDao.findById(1L);

        assertThat(reservation.getId()).isEqualTo(1L);
        assertThat(reservation.getName()).isEqualTo("김철수");
        assertThat(reservation.getScheduleId()).isEqualTo(1L);
        assertThat(reservation.getStatus()).isEqualTo(Status.RESERVED);
    }

    @DisplayName("사용자 이름으로 예약을 조회하면 대기 순번이 포함된 응답이 반환된다.")
    @Test
    void 이름으로_예약_조회() {
        // 김철수(id=1)는 schedule 1에서 가장 먼저 등록 → waiting_order=0
        List<ReservationResponse> reservations = reservationDao.findByUserName("김철수");

        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).name()).isEqualTo("김철수");
        assertThat(reservations.get(0).order()).isZero();
    }

    @DisplayName("같은 스케줄에 먼저 등록된 예약이 있으면 대기 순번이 1 이상이다.")
    @Test
    void 대기_순번_조회() {
        // 과거대기(id=23)는 schedule 1에서 김철수(id=1) 뒤에 등록 → waiting_order=1
        List<ReservationResponse> responses = reservationDao.findByUserName("과거대기");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).order()).isEqualTo(1);
    }

    @DisplayName("같은 스케줄에서 처음 등록된 예약의 순번은 0이다.")
    @Test
    void findOrderByReservationId_첫번째_예약() {
        // reservation 1: 해당 schedule에서 updated_at이 가장 앞 → 0
        int order = reservationDao.findOrderByReservationId(1L, 1L);

        assertThat(order).isZero();
    }

    @DisplayName("대기 예약의 순번은 앞서 등록된 예약 수와 같다.")
    @Test
    void findOrderByReservationId_대기_예약() {
        // reservation 23(과거대기): schedule_id=1, reservation 1보다 updated_at이 늦음 → 1
        int order = reservationDao.findOrderByReservationId(23L, 1L);

        assertThat(order).isEqualTo(1);
    }

    @DisplayName("이름·스케줄·상태가 모두 일치하면 true를 반환한다.")
    @Test
    void existByNameScheduleIdStatus_존재() {
        boolean exists = reservationDao.existByNameScheduleIdStatus("김철수", 1L, Status.RESERVED);

        assertThat(exists).isTrue();
    }

    @DisplayName("조건에 맞는 예약이 없으면 false를 반환한다.")
    @Test
    void existByNameScheduleIdStatus_미존재() {
        boolean exists = reservationDao.existByNameScheduleIdStatus("없는사람", 1L, Status.RESERVED);

        assertThat(exists).isFalse();
    }

    @DisplayName("예약의 스케줄 ID를 다른 스케줄로 변경할 수 있다.")
    @Test
    void 예약_스케줄_수정() {
        reservationDao.update(1L, 2L);

        Reservation updated = reservationDao.findById(1L);

        assertThat(updated.getScheduleId()).isEqualTo(2L);
    }

    @DisplayName("예약을 취소하면 상태가 CANCELED로 변경된다.")
    @Test
    void 예약_취소_소프트딜리트() {
        reservationDao.delete(1L, Status.CANCELED);

        Reservation reservation = reservationDao.findById(1L);

        assertThat(reservation.getStatus()).isEqualTo(Status.CANCELED);
    }
}
