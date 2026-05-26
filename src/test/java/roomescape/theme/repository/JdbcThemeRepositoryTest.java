package roomescape.theme.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.domain.Status;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@JdbcTest
@Import(JdbcThemeRepository.class)
class JdbcThemeRepositoryTest {

    @Autowired
    private JdbcThemeRepository jdbcThemeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Theme를 저장하고 조회한다.")
    public void saveAndFindById() {
        Theme theme = jdbcThemeRepository.save(Theme.create("kim", "desc1", "thumb1"));

        Optional<Theme> found = jdbcThemeRepository.findById(theme.getId());

        assertThat(found).isPresent();
        Theme savedTheme = found.get();
        assertThat(savedTheme.getId()).isEqualTo(theme.getId());
        assertThat(savedTheme.getName()).isEqualTo("kim");
        assertThat(savedTheme.getDescription()).isEqualTo("desc1");
        assertThat(savedTheme.getThumbnail()).isEqualTo("thumb1");
    }

    @Test
    @DisplayName("삭제된 Theme는 id로 조회되지 않는다.")
    public void findById_softDelete() {
        // given
        Theme theme = insertDeletedTheme("kim", "desc1", "thumb1");

        // when
        Optional<Theme> found = jdbcThemeRepository.findById(theme.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("모든 Theme를 불러온다.")
    public void findAll() {
        KeyHolder keyHolder2 = new GeneratedKeyHolder();

        jdbcTemplate.update(connection2 -> {
            PreparedStatement preparedStatement2 = connection2.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement2.setString(1, "kim");
            preparedStatement2.setString(2, "desc1");
            preparedStatement2.setString(3, "thumb1");
            return preparedStatement2;
        }, keyHolder2);

        Theme.of(getGeneratedId(keyHolder2), "kim", "desc1", "thumb1");
        KeyHolder keyHolder1 = new GeneratedKeyHolder();

        jdbcTemplate.update(connection1 -> {
            PreparedStatement preparedStatement1 = connection1.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement1.setString(1, "lee");
            preparedStatement1.setString(2, "desc2");
            preparedStatement1.setString(3, "thumb2");
            return preparedStatement1;
        }, keyHolder1);

        Theme.of(getGeneratedId(keyHolder1), "lee", "desc2", "thumb2");
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, "park");
            preparedStatement.setString(2, "desc3");
            preparedStatement.setString(3, "thumb3");
            return preparedStatement;
        }, keyHolder);

        Theme.of(getGeneratedId(keyHolder), "park", "desc3", "thumb3");

        List<Theme> themes = jdbcThemeRepository.findAll();

        assertThat(themes).hasSize(3)
                .extracting(
                        Theme::getName,
                        Theme::getDescription,
                        Theme::getThumbnail
                ).containsExactlyInAnyOrder(
                        tuple("kim", "desc1", "thumb1"),
                        tuple("lee", "desc2", "thumb2"),
                        tuple("park", "desc3", "thumb3")
                );
    }

    @Test
    @DisplayName("Theme 목록은 삭제되지 않은 Theme만 조회한다.")
    public void findAll_softDelete() {
        // given
        insertDeletedTheme("kim", "desc1", "thumb1");
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, "lee");
            preparedStatement.setString(2, "desc2");
            preparedStatement.setString(3, "thumb2");
            return preparedStatement;
        }, keyHolder);

        Theme activeTheme = Theme.of(getGeneratedId(keyHolder), "lee", "desc2", "thumb2");

        // when
        List<Theme> themes = jdbcThemeRepository.findAll();

        // then
        assertThat(themes)
                .extracting(Theme::getId, Theme::getName)
                .containsExactly(tuple(activeTheme.getId(), activeTheme.getName()));
    }

    @Test
    @DisplayName("Theme 존재 여부를 조회한다.")
    public void existsById() {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, "kim");
            preparedStatement.setString(2, "desc1");
            preparedStatement.setString(3, "thumb1");
            return preparedStatement;
        }, keyHolder);

        Theme theme = Theme.of(getGeneratedId(keyHolder), "kim", "desc1", "thumb1");

        boolean exists = jdbcThemeRepository.existsById(theme.getId());
        boolean notExists = jdbcThemeRepository.existsById(theme.getId() + 1);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("삭제된 Theme는 존재하지 않는 것으로 조회한다.")
    public void existsById_softDelete() {
        // given
        Theme theme = insertDeletedTheme("kim", "desc1", "thumb1");

        // when
        boolean exists = jdbcThemeRepository.existsById(theme.getId());

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("지정된 날짜 기간의 지정된 갯수 만큼의 테마정보를 예약 수를 순서대로 불러온다.")
    @Sql(scripts = "/popular-theme-data.sql")
    public void findTopThemesByReservationCount() {
        // given - @/popular-theme-data.sql

        // when
        List<Theme> topThemes = jdbcThemeRepository.findTopThemesByReservationCount(
                LocalDate.of(2026, 4, 29),
                LocalDate.of(2026, 5, 5),
                10
        );

        // then
        assertThat(topThemes).hasSizeLessThanOrEqualTo(10)
                .extracting(Theme::getId)
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
    }

    @Test
    @DisplayName("인기 테마 조회는 삭제된 예약을 집계에서 제외한다.")
    public void findTopThemesByReservationCount_softDelete() {
        // given
        KeyHolder keyHolder1 = new GeneratedKeyHolder();

        jdbcTemplate.update(connection1 -> {
            PreparedStatement preparedStatement1 = connection1.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement1.setString(1, "레벨2 탈출");
            preparedStatement1.setString(2, "우테코 레벨2를 탈출하는 내용입니다.");
            preparedStatement1.setString(3, "https://example.com/theme.png");
            return preparedStatement1;
        }, keyHolder1);

        Theme activeTheme = Theme.of(getGeneratedId(keyHolder1), "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, "레벨3 탈출");
            preparedStatement.setString(2, "우테코 레벨3을 탈출하는 내용입니다.");
            preparedStatement.setString(3, "https://example.com/theme.png");
            return preparedStatement;
        }, keyHolder);

        Theme deletedTheme = Theme.of(getGeneratedId(keyHolder), "레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme.png");
        Long timeId = insertReservationTime(LocalTime.of(10, 0));
        Long otherTimeId = insertReservationTime(LocalTime.of(12, 0));
        LocalDate targetDate = LocalDate.of(2026, 5, 1);

        insertReservation("브라운", targetDate, timeId, activeTheme);
        insertDeletedReservation("포비", targetDate, otherTimeId, deletedTheme);

        // when
        List<Theme> topThemes = jdbcThemeRepository.findTopThemesByReservationCount(
                LocalDate.of(2026, 4, 29),
                LocalDate.of(2026, 5, 5),
                10
        );

        // then
        assertThat(topThemes)
                .extracting(Theme::getId)
                .containsExactly(activeTheme.getId())
                .doesNotContain(deletedTheme.getId());
    }

    @Test
    @DisplayName("인기 테마 조회는 삭제된 Theme를 집계에서 제외한다.")
    public void findTopThemesByReservationCount_deletedTheme() {
        // given
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, "레벨2 탈출");
            preparedStatement.setString(2, "우테코 레벨2를 탈출하는 내용입니다.");
            preparedStatement.setString(3, "https://example.com/theme.png");
            return preparedStatement;
        }, keyHolder);

        Theme activeTheme = Theme.of(getGeneratedId(keyHolder), "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Theme deletedTheme = insertDeletedTheme("레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme.png");
        Long timeId = insertReservationTime(LocalTime.of(10, 0));
        Long otherTimeId = insertReservationTime(LocalTime.of(12, 0));
        LocalDate targetDate = LocalDate.of(2026, 5, 1);

        insertReservation("브라운", targetDate, timeId, activeTheme);
        insertReservation("포비", targetDate, otherTimeId, deletedTheme);

        // when
        List<Theme> topThemes = jdbcThemeRepository.findTopThemesByReservationCount(
                LocalDate.of(2026, 4, 29),
                LocalDate.of(2026, 5, 5),
                10
        );

        // then
        assertThat(topThemes)
                .extracting(Theme::getId)
                .containsExactly(activeTheme.getId())
                .doesNotContain(deletedTheme.getId());
    }

    @Test
    @DisplayName("Theme를 삭제한다.")
    public void cancelById() {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, "kim");
            preparedStatement.setString(2, "desc1");
            preparedStatement.setString(3, "thumb1");
            return preparedStatement;
        }, keyHolder);

        Theme theme = Theme.of(getGeneratedId(keyHolder), "kim", "desc1", "thumb1");
        LocalDateTime now = LocalDateTime.of(2026, 5, 15, 10, 0);

        boolean deleted = jdbcThemeRepository.cancelById(theme.getId(), now);

        assertThat(deleted).isTrue();
        assertThat(jdbcThemeRepository.findAll()).isEmpty();

        Map<String, Object> deleteInfo = findDeleteInfoById(theme.getId());
        assertThat(((Timestamp) deleteInfo.get("deleted_at")).toLocalDateTime()).isEqualTo(now);
    }

    @Test
    @DisplayName("존재하지 않는 Theme는 삭제되지 않는다.")
    public void cancelById_fail() {
        // given
        Long id = 1L;

        // when
        boolean deleted = jdbcThemeRepository.cancelById(id, LocalDateTime.now());

        // then
        assertThat(deleted).isFalse();
    }

    private Theme insertDeletedTheme(String name, String description, String thumbnail) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail, deleted_at)
                    VALUES (?, ?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, thumbnail);
            preparedStatement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            return preparedStatement;
        }, keyHolder);

        return Theme.of(getGeneratedId(keyHolder), name, description, thumbnail);
    }

    private Long insertReservationTime(LocalTime startAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation_time (start_at)
                    VALUES (?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, startAt.toString());
            return preparedStatement;
        }, keyHolder);

        return getGeneratedId(keyHolder);
    }

    private void insertReservation(String guestName, LocalDate date, Long timeId, Theme theme) {
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation (guest_name, date, time_id, theme_id, status)
                    VALUES (?, ?, ?, ?, ?)
                    """);
            preparedStatement.setString(1, guestName);
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.setLong(3, timeId);
            preparedStatement.setLong(4, theme.getId());
            preparedStatement.setString(5, Status.WAITING.toString());
            return preparedStatement;
        });
    }

    private void insertDeletedReservation(String guestName, LocalDate date, Long timeId, Theme theme) {
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation (guest_name, date, time_id, theme_id, status)
                    VALUES (?, ?, ?, ?, ?)
                    """);
            preparedStatement.setString(1, guestName);
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.setLong(3, timeId);
            preparedStatement.setLong(4, theme.getId());
            preparedStatement.setString(5, Status.CANCELED.toString());
            return preparedStatement;
        });
    }

    private Map<String, Object> findDeleteInfoById(Long id) {
        return jdbcTemplate.queryForMap("""
                SELECT deleted_at
                FROM theme
                WHERE id = ?
                """, id);
    }

    private Long getGeneratedId(KeyHolder keyHolder) {
        return keyHolder.getKey().longValue();
    }

}
