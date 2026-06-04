package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
@Import(JdbcWaitingRepository.class)
class JdbcWaitingRepositoryTest {

    private static final LocalDate DATE = LocalDate.of(2099, 12, 31);

    @Autowired
    private JdbcWaitingRepository waitingRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("대기를 저장하고 조회한다")
    void saveAndFind() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Reservation reservation = new Reservation(null, "루드비코", DATE, time, theme);

        Reservation saved = waitingRepository.save(reservation);

        assertThat(saved.getId()).isNotNull();
        assertThat(waitingRepository.hasWaitingOnSlot("루드비코", DATE, time.getId(), theme.getId())).isTrue();
    }

    @Test
    @DisplayName("특정 슬롯의 첫 번째 대기자를 조회한다")
    void findFirstWaiting() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        insertWaiting("대기1", DATE, time, theme);
        insertWaiting("대기2", DATE, time, theme);

        Optional<Reservation> first = waitingRepository.findFirstWaiting(DATE, time.getId(), theme.getId());

        assertThat(first).isPresent();
        assertThat(first.get().getName()).isEqualTo("대기1");
    }

    @Test
    @DisplayName("내 앞의 대기자 수를 계산한다")
    void countWaitingsBefore() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Reservation first = insertWaiting("대기1", DATE, time, theme);
        Reservation second = insertWaiting("대기2", DATE, time, theme);

        assertThat(waitingRepository.countWaitingsBefore(first)).isEqualTo(0);
        assertThat(waitingRepository.countWaitingsBefore(second)).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 사용자는 같은 슬롯에 중복 대기할 수 없다")
    void failWhenDuplicateWaiting() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        insertWaiting("루드비코", DATE, time, theme);

        Reservation duplicate = new Reservation(null, "루드비코", DATE, time, theme);
        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.dao.DuplicateKeyException.class, 
                () -> waitingRepository.save(duplicate));
    }

    @Test
    @DisplayName("중간 대기자가 취소되면 뒤에 있는 대기자들의 순번이 자동으로 줄어든다")
    void shiftingOrderWhenWaitingDeleted() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Reservation first = insertWaiting("대기1", DATE, time, theme);
        Reservation second = insertWaiting("대기2", DATE, time, theme);
        Reservation third = insertWaiting("대기3", DATE, time, theme);

        assertThat(waitingRepository.countWaitingsBefore(third)).isEqualTo(2);

        waitingRepository.deleteById(second.getId());

        assertThat(waitingRepository.countWaitingsBefore(third)).isEqualTo(1);
    }

    private ReservationTime insertTime(LocalTime startAt) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", Time.valueOf(startAt));

        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new ReservationTime(id, startAt);
    }

    private Theme insertTheme(String name) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("description", "설명")
                .addValue("thumbnail_url", "https://example.com/thumb.jpg");

        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Theme(id, name, "설명", "https://example.com/thumb.jpg");
    }

    private Reservation insertWaiting(String name, LocalDate date, ReservationTime time, Theme theme) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("date", Date.valueOf(date))
                .addValue("time_id", time.getId())
                .addValue("theme_id", theme.getId());

        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(id, name, date, time, theme);
    }
}
