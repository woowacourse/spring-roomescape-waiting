package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservationtime.AvailableReservationTimeDto;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;

@JdbcTest
class JdbcReservationTimeRepositoryTest {

    private final JdbcTemplate jdbcTemplate;
    private final ReservationTimeRepository reservationTimeRepository;

    @Autowired
    public JdbcReservationTimeRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("예약 시간을 추가한다.")
    void save() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        assertThat(savedReservationTime.getId()).isEqualTo(1L);
        assertThat(savedReservationTime.getStartAt()).isEqualTo("10:00");
    }

    @Test
    @DisplayName("모든 예약 시간들을 조회한다.")
    void findAll() {
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00')");

        assertThat(reservationTimeRepository.findAll()).hasSize(1);
        assertThat(reservationTimeRepository.findAll().get(0).getStartAt()).isEqualTo("10:00");
    }

    @Test
    @DisplayName("예약 시간을 조회한다.")
    void findById() {
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00')");

        Optional<ReservationTime> foundReservationTime = reservationTimeRepository.findById(1L);
        assertThat(foundReservationTime).isPresent();
        assertThat(foundReservationTime.get().getStartAt()).isEqualTo("10:00");
    }

    @Test
    @DisplayName("예약 가능한 시간들을 조회한다.")
    void findAvailableReservationTimes() {
        String insertMemberSQL = """
                INSERT INTO member (id, email, password, name, role)
                VALUES ('1', 'example@gmail.com', 'password', 'name1', 'USER');
                """;

        String insertTimeSQL = """
                INSERT INTO reservation_time (id, start_at)
                VALUES (1, '10:00'), 
                       (2, '11:00'), 
                       (3, '12:00');
                """;

        String insertThemeSQL = """
                INSERT INTO theme (id, name, description, thumbnail)
                VALUES (1, '테마1', '테마1 설명', 'https://example1.com');
                """;

        String insertReservationSQL = """
                INSERT INTO reservation (id, date, member_id, time_id, theme_id)
                VALUES (1, '2024-05-04', 1, 1, 1),
                       (2, '2024-05-04', 1, 3, 1);
                """;

        jdbcTemplate.update(insertMemberSQL);
        jdbcTemplate.update(insertTimeSQL);
        jdbcTemplate.update(insertThemeSQL);
        jdbcTemplate.update(insertReservationSQL);

        LocalDate date = LocalDate.of(2024, 5, 4);
        List<AvailableReservationTimeDto> availableReservationTimes = reservationTimeRepository
                .findAvailableReservationTimes(date, 1L);

        assertThat(availableReservationTimes).containsExactly(
                new AvailableReservationTimeDto(1L, LocalTime.of(10, 0), true),
                new AvailableReservationTimeDto(2L, LocalTime.of(11, 0), false),
                new AvailableReservationTimeDto(3L, LocalTime.of(12, 0), true)
        );
    }

    @Test
    @DisplayName("예약 시간을 삭제한다.")
    void deleteById() {
    }

    @Test
    @DisplayName("id에 해당하는 예약 시간이 존재하는지 확인한다.")
    void existsById() {
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00')");

        assertThat(reservationTimeRepository.existsById(1L)).isTrue();
        assertThat(reservationTimeRepository.existsById(2L)).isFalse();
    }

    @Test
    @DisplayName("startAt에 해당하는 예약 시간이 존재하는지 확인한다.")
    void existsByStartAt() {
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00')");

        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).isTrue();
        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(11, 0))).isFalse();
    }
}
