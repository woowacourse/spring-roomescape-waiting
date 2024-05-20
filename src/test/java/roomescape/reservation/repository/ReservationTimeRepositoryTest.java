package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.InitialReservationTimeFixture.INITIAL_RESERVATION_TIME_COUNT;
import static roomescape.InitialReservationTimeFixture.NOT_RESERVATED_TIME;
import static roomescape.InitialReservationTimeFixture.NOT_SAVED_TIME;
import static roomescape.InitialReservationTimeFixture.RESERVATION_TIME_1;

import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exceptions.NotFoundException;
import roomescape.reservation.domain.ReservationTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("ReservationTime을 저장한다.")
    void save() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 0));

        ReservationTime saved = reservationTimeRepository.save(reservationTime);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("id에 맞는 ReservationTime을 제거한다.")
    void delete() {
        reservationTimeRepository.deleteById(NOT_RESERVATED_TIME.getId());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation_time", Integer.class);

        assertThat(count).isEqualTo(INITIAL_RESERVATION_TIME_COUNT - 1);
    }

    @Test
    @DisplayName("참조되어 있는 시간을 삭제하는 경우 예외가 발생한다.")
    void deleteReferencedGetTime() {
        assertThatThrownBy(() -> reservationTimeRepository.deleteById(RESERVATION_TIME_1.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("모든 ReservationTime을 찾는다.")
    void findAll() {
        Iterable<ReservationTime> found = reservationTimeRepository.findAll();

        assertThat(found).hasSize(INITIAL_RESERVATION_TIME_COUNT);
    }

    @Test
    @DisplayName("id에 맞는 ReservationTime을 찾는다.")
    void findById() {
        ReservationTime found = reservationTimeRepository.findById(RESERVATION_TIME_1.getId()).get();

        assertThat(found).isEqualTo(RESERVATION_TIME_1);
    }

    @Test
    @DisplayName("존재하지 않는 id가 들어오면 빈 Optional 객체를 반환한다.")
    void EmptyIfNotExistId() {
        Optional<ReservationTime> reservationTime = reservationTimeRepository.findById(-1L);

        assertThat(reservationTime.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("id에 맞는 ReservationTime을 찾는다.")
    void getById() {
        ReservationTime found = reservationTimeRepository.getById(RESERVATION_TIME_1.getId());

        assertThat(found).isEqualTo(RESERVATION_TIME_1);
    }

    @Test
    @DisplayName("존재하지 않는 id가 들어오면 예외를 발생시킨다.")
    void throwExceptionIfIdNotExists() {
        assertThatThrownBy(() -> reservationTimeRepository.getById(-1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("특정 예약시간이 존재하면 true를 반환한다.")
    void trueIfStartAtExists() {
        boolean isExist = reservationTimeRepository.existsByStartAt(RESERVATION_TIME_1.getStartAt());

        assertThat(isExist).isTrue();
    }

    @Test
    @DisplayName("특정 예약시간이 존재하지 않으면 false를 반환한다.")
    void falseIfStartAtNotExists() {
        boolean isExist = reservationTimeRepository.existsByStartAt(NOT_SAVED_TIME.getStartAt());

        assertThat(isExist).isFalse();
    }
}
