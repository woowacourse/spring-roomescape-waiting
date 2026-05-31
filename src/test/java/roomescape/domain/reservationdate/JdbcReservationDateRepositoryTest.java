package roomescape.domain.reservationdate;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
class JdbcReservationDateRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationDateRepository reservationDateRepository;

    @BeforeEach
    void setUp() {
        reservationDateRepository = new JdbcReservationDateRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("날짜를 저장한다.")
    void save() {
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.parse("2026-05-15"));
        ReservationDate saved = reservationDateRepository.save(date);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPlayDay()).isEqualTo(LocalDate.parse("2026-05-15"));
    }

    @Test
    @DisplayName("모든 날짜를 조회한다.")
    void findAll() {
        int beforeSize = reservationDateRepository.findAll().size();
        reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.parse("2026-05-15")));

        List<ReservationDate> dates = reservationDateRepository.findAll();

        assertThat(dates).hasSize(beforeSize + 1);
    }

    @Test
    @DisplayName("ID로 날짜를 삭제한다.")
    void deleteById() {
        ReservationDate saved = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.parse("2026-05-15")));
        int beforeSize = reservationDateRepository.findAll().size();

        int deletedCount = reservationDateRepository.deleteById(saved.getId());

        assertThat(deletedCount).isEqualTo(1);
        assertThat(reservationDateRepository.findAll()).hasSize(beforeSize - 1);
    }

    @Test
    @DisplayName("특정 날짜가 존재하는지 확인한다.")
    void existsByPlayDay() {
        LocalDate playDay = LocalDate.parse("2026-05-15");
        reservationDateRepository.save(ReservationDate.createWithoutId(playDay));

        boolean exists = reservationDateRepository.existsByPlayDay(playDay);

        assertThat(exists).isTrue();
    }
}
