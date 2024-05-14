package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;

@SpringBootTest
class JdbcReservationTimeRepositoryTest {
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        jdbcTemplate.update("delete from reservation");
        jdbcTemplate.update("ALTER TABLE reservation alter column id restart with 1");
        jdbcTemplate.update("delete from reservation_time");
        jdbcTemplate.update("ALTER TABLE reservation_time alter column id restart with 1");
    }

    @Test
    @DisplayName("ReservationTime 을 잘 저장하는지 확인한다.")
    void save() {
        var beforeSave = reservationTimeRepository.findAll().getReservationTimes().stream().map(ReservationTime::getId)
                .toList();
        ReservationTime saved = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        var afterSave = reservationTimeRepository.findAll().getReservationTimes().stream().map(ReservationTime::getId)
                .toList();

        Assertions.assertThat(afterSave)
                .containsAll(beforeSave)
                .contains(saved.getId());
    }

    @Test
    @DisplayName("ReservationTime 을 잘 조회하는지 확인한다.")
    void findAll() {
        List<ReservationTime> beforeSave = reservationTimeRepository.findAll().getReservationTimes();
        reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        reservationTimeRepository.save(new ReservationTime(LocalTime.now()));

        List<ReservationTime> afterSave = reservationTimeRepository.findAll().getReservationTimes();

        Assertions.assertThat(afterSave.size())
                .isEqualTo(beforeSave.size() + 2);
    }

    @Test
    @DisplayName("ReservationTime 을 잘 지우하는지 확인한다.")
    void delete() {
        List<ReservationTime> beforeSaveAndDelete = reservationTimeRepository.findAll().getReservationTimes();
        reservationTimeRepository.save(new ReservationTime(LocalTime.now()));

        reservationTimeRepository.delete(1L);

        List<ReservationTime> afterSaveAndDelete = reservationTimeRepository.findAll().getReservationTimes();

        Assertions.assertThat(beforeSaveAndDelete)
                .containsExactlyInAnyOrderElementsOf(afterSaveAndDelete);
    }
}
