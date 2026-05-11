package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.ErrorCode;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeServiceTest {

    @Autowired
    ThemeService themeService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("이미 예약 정보가 존재하는 테마는 삭제할 수 없다.")
    public void delete_fail() {
        // given
        ReservationTime reservationTime = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertReservation("브라운", LocalDate.of(2023, 8, 5), reservationTime, theme);

        // when
        assertThatThrownBy(() -> themeService.delete(theme.getId()))
                .isInstanceOf(DomainException.class)
                .hasMessage(ErrorCode.THEME_HAS_RESERVATION.message());
    }

    @Test
    @DisplayName("해당 테마가 존재하지 않으면 삭제할 수 없기 때문에 예외가 발생한다.")
    public void delete_fail2() {
        // given
        Long id = 1L;

        // when, then
        assertThatThrownBy(() -> themeService.delete(id))
                .isInstanceOf(DomainException.class)
                .hasMessage(ErrorCode.THEME_NOT_FOUND.message());
    }

    private Reservation insertReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation (name, date, time_id, theme_id)
                    VALUES (?, ?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, name);
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.setLong(3, time.getId());
            preparedStatement.setLong(4, theme.getId());
            return preparedStatement;
        }, keyHolder);

        return new Reservation(getGeneratedId(keyHolder), name, date, time, theme);
    }

    private ReservationTime insertReservationTime(LocalTime startAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation_time (start_at)
                    VALUES (?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, startAt.toString());
            return preparedStatement;
        }, keyHolder);

        return new ReservationTime(getGeneratedId(keyHolder), startAt);
    }

    private Theme insertTheme(String name, String description, String thumbnail) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, thumbnail);
            return preparedStatement;
        }, keyHolder);

        return new Theme(getGeneratedId(keyHolder), name, description, thumbnail);
    }

    private Long getGeneratedId(KeyHolder keyHolder) {
        return keyHolder.getKey().longValue();
    }
  
}
