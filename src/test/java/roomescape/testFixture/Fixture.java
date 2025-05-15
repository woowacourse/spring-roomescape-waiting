package roomescape.testFixture;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.application.dto.ReservationCreateDto;
import roomescape.application.dto.UserReservationCreateDto;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public class Fixture {

    public static final Theme THEME_1 = Theme.of(1L, "테마1", "테마 1입니다.", "썸네일1");
    public static final ReservationTime RESERVATION_TIME_1 = ReservationTime.of(1L, LocalTime.of(10, 0));
    public static final ReservationTime RESERVATION_TIME_2 = ReservationTime.of(2L, LocalTime.of(11, 0));
    public static final ReservationTime RESERVATION_TIME_3 = ReservationTime.of(3L, LocalTime.of(12, 0));
    public static final Member MEMBER1_ADMIN = Member.of(1L, "어드민", "admin@email.com", "password", Role.ADMIN);
    public static final Member MEMBER2_USER = Member.of(2L, "브라운", "brown@email.com", "brown");
    public static final Reservation RESERVATION_1 = Reservation.of(
            1L,
            MEMBER1_ADMIN,
            THEME_1,
            LocalDate.now().plusDays(1),
            RESERVATION_TIME_1,
            new Waiting(ReservationStatus.RESERVED)
    );

    public static final UserReservationCreateDto RESERVATION_BODY = createUserReservationBody();

    public static UserReservationCreateDto createUserReservationBody() {
        return new UserReservationCreateDto(1L, LocalDate.now().plusDays(1), 1L);
    }

    public static ReservationCreateDto createReservationBody(Long memberId) {
        return new ReservationCreateDto(LocalDate.now().plusDays(1), 1L, 1L, memberId);
    }

    public static void resetH2TableIds(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute((Connection connection) -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET REFERENTIAL_INTEGRITY FALSE");
                statement.execute("TRUNCATE TABLE reservation");
                statement.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
                statement.execute("TRUNCATE TABLE reservation_time");
                statement.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
                statement.execute("TRUNCATE TABLE theme");
                statement.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
                statement.execute("TRUNCATE TABLE member");
                statement.execute("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");
                statement.execute("SET REFERENTIAL_INTEGRITY TRUE");
            }
            return null;
        });
    }
}
