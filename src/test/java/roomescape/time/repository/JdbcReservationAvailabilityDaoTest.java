package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeDao;
import roomescape.time.domain.ReservationAvailability;

@JdbcTest
@Import({JdbcReservationAvailabilityDao.class, JdbcThemeDao.class})
class JdbcReservationAvailabilityDaoTest {

    @Autowired
    private JdbcReservationAvailabilityDao jdbcReservationAvailabilityDao;

    @Autowired
    private JdbcThemeDao jdbcThemeDao;

    @DisplayName("주어진 날짜와 테마의 예약 가능한 시간 목록을 반환할 수 있다.")
    @Test
    @Sql({"/test-schema.sql", "/test-reservation-availability-data.sql"})
    void testFindAllByDateAndTheme() {
        // given
        LocalDate date = LocalDate.of(2025, 5, 1);
        Theme theme = jdbcThemeDao.findById(1L).orElseThrow();
        // when
        List<ReservationAvailability> actual = jdbcReservationAvailabilityDao.findAllByDateAndTheme(date, theme);
        // then
        assertAll(
                () -> assertThat(actual).hasSize(3),
                () -> assertThat(actual.getFirst().isBooked()).isTrue(),
                () -> assertThat(actual.get(1).isBooked()).isTrue(),
                () -> assertThat(actual.get(2).isBooked()).isFalse()
        );
    }
}
