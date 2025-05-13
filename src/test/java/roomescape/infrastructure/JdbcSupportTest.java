package roomescape.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

public abstract class JdbcSupportTest {

    protected static final DataSource TEST_DATASOURCE = DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:database-test")
            .username("sa")
            .build();

    protected final JdbcTemplate jdbcTemplate = new JdbcTemplate(TEST_DATASOURCE);

    @BeforeEach
    void setUp() {
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setSchemaLocations(List.of("classpath:schema.sql"));
        SqlDataSourceScriptDatabaseInitializer sqlDataSourceScriptDatabaseInitializer =
                new SqlDataSourceScriptDatabaseInitializer(TEST_DATASOURCE, settings);
        sqlDataSourceScriptDatabaseInitializer.initializeDatabase();
    }

    public void insertReservationTime() {
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES ('12:00')");
    }

    public void insertMember(String name, String email, String password) {
        jdbcTemplate.update(
                "INSERT INTO member(name, email, password, role) VALUES (?, ?, ?, ?)", name, email, password, "NORMAL");
    }

    public void insertMember() {
        jdbcTemplate.update(
                "INSERT INTO member(name, email, password, role) VALUES (?, ?, ?, ?)", "member1", "email", "password",
                "NORMAL");
    }

    public void insertMember(Member member) {
        jdbcTemplate.update(
                "INSERT INTO member(name, email, password, role) VALUES (?, ?, ?, ?)",
                member.getName(),
                member.getEmail().email(),
                member.getPassword(),
                member.getRole().toString());
    }


    public void insertReservationTime(LocalTime startTime) {
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", startTime);
    }

    public void insertReservationTime(ReservationTime reservationTime) {
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", reservationTime.startAt());
    }

    public void insertTheme() {
        jdbcTemplate.update(
                "INSERT INTO theme(name, description, thumbnail) VALUES ('test1', 'description1', 'thumbnail1')");
    }

    public void insertTheme(Theme theme) {
        jdbcTemplate.update(
                "INSERT INTO theme(name, description, thumbnail) VALUES (?, ?, ?)", theme.getName(),
                theme.getDescription(), theme.getThumbnail());
    }

    public void insertTheme(String name, String description, String thumbnail) {
        jdbcTemplate.update(
                "INSERT INTO theme(name, description, thumbnail) VALUES (?, ?, ?)", name, description, thumbnail);
    }

    public void insertReservation(Long memberId, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation(member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)", memberId, date,
                timeId,
                themeId);
    }
}
