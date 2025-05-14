package roomescape.reservationtime.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import roomescape.reservation.fixture.TestFixture;
import roomescape.reservationtime.domain.ReservationTime;

class JdbcReservationTimeRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScripts("/schema.sql", "/data.sql")
                .build();
        jdbcTemplate = new JdbcTemplate(database);
        reservationTimeRepository = new JDBCReservationTimeRepository(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DROP TABLE reservation IF EXISTS");
        jdbcTemplate.update("DROP TABLE reservation_time IF EXISTS");
        jdbcTemplate.update("DROP TABLE theme IF EXISTS");
        jdbcTemplate.update("DROP TABLE member IF EXISTS");
    }

    @Test
    void findAll() {
        // given

        // when
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        // then
        assertThat(reservationTimes.size()).isEqualTo(5);
    }

    @Test
    void save() {
        //given

        // when
        reservationTimeRepository.save(TestFixture.makeReservationTime(3L));

        // then
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        assertThat(reservationTimes.size()).isEqualTo(6);
    }

    @Test
    void deleteReservationTime_shouldReturnTrue() {
        // given
        reservationTimeRepository.save(TestFixture.makeReservationTime(6L));

        // when
        reservationTimeRepository.deleteById(6L);

        // then
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        assertThat(reservationTimes.size()).isEqualTo(5);
    }
}
