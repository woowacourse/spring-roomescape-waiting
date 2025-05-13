package roomescape.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;

import javax.sql.DataSource;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcReservationTimeRepositoryTest {

    private static final DataSource TEST_DATASOURCE = DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:database-test")
            .username("sa")
            .build();


    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(TEST_DATASOURCE);
    private final JdbcReservationTimeRepository reservationTimeDao = new JdbcReservationTimeRepository(jdbcTemplate);


    @BeforeEach
    void setUp() {
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setSchemaLocations(List.of("classpath:schema.sql"));
        SqlDataSourceScriptDatabaseInitializer sqlDataSourceScriptDatabaseInitializer =
                new SqlDataSourceScriptDatabaseInitializer(TEST_DATASOURCE, settings);
        sqlDataSourceScriptDatabaseInitializer.initializeDatabase();
    }

    @Test
    void 예약_시간을_저장할_수_있다() {
        //when
        Long createdId = reservationTimeDao.create(new ReservationTime(LocalTime.of(12, 1)));

        //then
        assertThat(reservationTimeDao.findById(createdId))
                .hasValue(new ReservationTime(1L, LocalTime.of(12, 1)));
    }

    @Test
    void id로_예약_시간을_조회할_수_있다() {
        //given
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES ('12:00')");

        //when
        Optional<ReservationTime> reservationTime = reservationTimeDao.findById(1L);

        //then
        assertThat(reservationTime).hasValue(new ReservationTime(1L, LocalTime.of(12, 0)));
    }

    @Test
    void id값이_없다면_빈_Optional_값이_반환된다() {
        //when
        Optional<ReservationTime> reservationTime = reservationTimeDao.findById(1L);

        //then
        assertThat(reservationTime).isEmpty();
    }

    @Test
    void 전체_예약_시간을_조회할_수_있다() {
        //given
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES ('12:00')");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES ('12:01')");

        //when
        List<ReservationTime> reservationTimes = reservationTimeDao.findAll();

        //then
        assertThat(reservationTimes).isEqualTo(List.of(
                new ReservationTime(1L, LocalTime.of(12, 0)),
                new ReservationTime(2L, LocalTime.of(12, 1))
        ));
    }

    @Test
    void id값으로_예약_시간을_삭제한다() {
        //given
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES ('12:00')");

        //when
        reservationTimeDao.deleteById(1L);

        //then
        assertThat(reservationTimeDao.findById(1L)).isEmpty();
    }
}