package roomescape.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.dto.response.ReservationTimeCreateResponse;
import roomescape.reservation.dto.response.ReservationTimeFindAllResponse;

@JdbcTest
class ReservationTimeDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationTimeDao reservationTimeDao;

    @BeforeEach
    void setUp() {
        reservationTimeDao = new ReservationTimeDao(jdbcTemplate);
    }

    @Nested
    class 예약_시간을_저장한다 {

        @Test
        void 새로운_시간을_저장한다() {
            // given
            ReservationTimeCreateRequest time = new ReservationTimeCreateRequest(LocalTime.of(10, 0));

            // when
            ReservationTime reservationTime = reservationTimeDao.insert(time);
            ReservationTimeCreateResponse saved = ReservationTimeCreateResponse.of(reservationTime.getId(), LocalTime.of(10, 0));

            // then
            assertAll(
                    () -> assertThat(saved.id()).isNotNull(),
                    () -> assertThat(saved.startAt()).isEqualTo(LocalTime.of(10, 0))
            );
        }

        @Test
        void 저장_후_전체_조회에_포함된다() {
            // given
            reservationTimeDao.insert(new ReservationTimeCreateRequest(LocalTime.of(10, 0)));

            // when
            List<ReservationTimeFindAllResponse> all = reservationTimeDao.findAll().stream()
                    .map(it -> ReservationTimeFindAllResponse.of(it.getId(), it.getStartAt()))
                    .toList();

            // then
            assertThat(all).hasSize(1);
        }
    }

    @Test
    void 저장된_모든_시간을_조회한다() {
        // given
        reservationTimeDao.insert(new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
        reservationTimeDao.insert(new ReservationTimeCreateRequest(LocalTime.of(11, 0)));
        reservationTimeDao.insert(new ReservationTimeCreateRequest(LocalTime.of(12, 0)));

        // when
        List<ReservationTimeFindAllResponse> all = reservationTimeDao.findAll().stream()
                .map(it -> ReservationTimeFindAllResponse.of(it.getId(), it.getStartAt()))
                .toList();

        // then
        assertThat(all).hasSize(3);
    }

    @Nested
    class ID로_시간을_조회한다 {

        @Test
        void 존재하는_시간을_조회한다() {
            // given
            ReservationTime reservationTime = reservationTimeDao.insert(new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
            ReservationTimeCreateResponse saved = ReservationTimeCreateResponse.of(reservationTime.getId(), LocalTime.of(10, 10, 10));

            // when
            ReservationTime found = reservationTimeDao.findById(saved.id());

            // then
            assertThat(found.getStartAt()).isEqualTo(LocalTime.of(10, 0));
        }

        @Test
        void 존재하지_않는_ID로_조회하면_예외를_던진다() {
            // when // then
            assertThatThrownBy(() -> reservationTimeDao.findById(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 예약 시간");
        }
    }

    @Test
    void ID로_시간을_삭제한다() {
        // given
        ReservationTime reservationTime = reservationTimeDao.insert(new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
        ReservationTimeCreateResponse saved = ReservationTimeCreateResponse.of(reservationTime.getId(), LocalTime.of(10, 10, 10));

        // when
        reservationTimeDao.delete(saved.id());

        // then
        List<ReservationTimeFindAllResponse> all = reservationTimeDao.findAll().stream()
                .map(it -> ReservationTimeFindAllResponse.of(it.getId(), it.getStartAt()))
                .toList();
        assertThat(all).isEmpty();
    }
}
