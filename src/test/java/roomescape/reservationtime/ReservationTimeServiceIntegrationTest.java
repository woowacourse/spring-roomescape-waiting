package roomescape.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationtime.exception.ReservationTimeInUseException;

@JdbcTest
@ActiveProfiles("test")
@Import({ReservationTimeService.class, ReservationTimeDao.class})
public class ReservationTimeServiceIntegrationTest {

    private static final String INSERT_DEFAULT_STORE_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점');
            """;

    private static final String INSERT_DEFAULT_MEMBER_SQL = """
            INSERT INTO member (id, email, password, name, role)
            VALUES (1, 'brown@email.com', 'password', '브라운', 'USER');
            """;

    private static final String INSERT_DEFAULT_THEME_SQL = """
            INSERT INTO theme (id, name, description, img_url)
            VALUES (1, '테마', '설명', 'https://example.com/img.jpg');
            """;

    private static final String INSERT_DEFAULT_TIME_SQL = """
            INSERT INTO reservation_time (id, start_at)
            VALUES (1, '10:00');
            """;

    private static final String INSERT_DEFAULT_RESERVATION_SQL = """
            INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
            VALUES (1, 1, '2026-12-01', 1, 1, 1);
            """;

    private final ReservationTimeService reservationTimeService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReservationTimeServiceIntegrationTest(ReservationTimeService reservationTimeService,
                                                 JdbcTemplate jdbcTemplate) {
        this.reservationTimeService = reservationTimeService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void 존재하지_않는_시간_삭제는_멱등하게_성공한다() {
        // given: 비어 있는 DB

        // when & then
        assertThatCode(() -> reservationTimeService.deleteReservationTime(999L))
                .doesNotThrowAnyException();
    }

    @Test
    @Sql(statements = {
            INSERT_DEFAULT_STORE_SQL,
            INSERT_DEFAULT_MEMBER_SQL,
            INSERT_DEFAULT_THEME_SQL,
            INSERT_DEFAULT_TIME_SQL,
            INSERT_DEFAULT_RESERVATION_SQL
    })
    void 예약이_있는_시간은_삭제할_수_없다() {
        // given: time(1) 을 reservation(1) 이 참조 중

        // when & then: 삭제 시도 → FK 위반 예외
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1L))
                .isInstanceOf(ReservationTimeInUseException.class);

        // then: time row 는 그대로 유지
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_time WHERE id = 1", Integer.class);
        assertThat(count).isEqualTo(1);
    }
}
