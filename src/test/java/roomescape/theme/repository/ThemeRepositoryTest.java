package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.theme.domain.Theme;

@JdbcTest
class ThemeRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    ThemeRepository themeRepository;

    @Autowired
    public ThemeRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.themeRepository = new ThemeRepository(new ThemeDao(jdbcTemplate));
    }

    @Test
    @DisplayName("새로운 테마를 저장하고 반환된 객체의 ID를 확인한다.")
    void save_validTheme_returnsWithId() {
        // given
        Theme theme = Theme.of("테마", "설명", "thumbnailUrl");

        // when
        Theme saved = themeRepository.save(theme);

        //then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(theme.getName());
    }

    @Test
    @DisplayName("기존에 이미 테마 이름이 겹치는 테마가 있으면 예외가 발생한다.")
    void save_duplicateThemeName_throwsDataIntegrityViolation() {
        // given
        themeRepository.save(Theme.of("테마", "설명", "thumbnailUrl"));

        // when & then
        assertThatThrownBy(() -> themeRepository.save(Theme.of("테마", "other", "otherThumbnailUrl")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("ID를 통해 저장된 테마를 조회한다.")
    void findById_existingTheme_returnsTheme() {
        // given
        Theme saved = themeRepository.save(Theme.of("테마", "설명", "thumbnailUrl"));

        // when
        Theme found = themeRepository.findById(saved.getId())
                .orElseThrow(() -> new AssertionError("조회된 결과가 없습니다. id: " + saved.getId()));

        // then
        assertThat(found.getName()).isEqualTo(saved.getName());
        assertThat(found.getDescription()).isEqualTo(saved.getDescription());
        assertThat(found.getThumbnailUrl()).isEqualTo(saved.getThumbnailUrl());
    }


    @Test
    @DisplayName("존재하는 모든 테마 목록을 리스트로 조회한다.")
    void findAll_multipleThemes_returnsAllThemes() {
        // given
        Theme saved1 = themeRepository.save(Theme.of("테마1", "설명", "thumbnailUrl"));
        Theme saved2 = themeRepository.save(Theme.of("테마2", "설명", "thumbnailUrl"));

        // when
        List<Theme> result = themeRepository.findAll();

        // then
        assertThat(result).containsExactly(saved1, saved2);
    }

    @Test
    @DisplayName("삭제에 성공하고 아무런 값도 반환하지 않는다.")
    void delete_success() {
        // given
        Theme saved1 = themeRepository.save(Theme.of("테마1", "설명", "thumbnailUrl"));
        themeRepository.save(Theme.of("테마2", "설명", "thumbnailUrl"));

        // when
        Assertions.assertDoesNotThrow(() -> themeRepository.delete(saved1));

        // then
        assertThat(themeRepository.findById(saved1.getId())).isEmpty();
        assertThat(themeRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("테마가 사용되고 있는데 삭제를 시도하면 예외가 발생한다.")
    void delete_themeInUse_throwsDataIntegrityViolation() {
        //given
        Time testTime = Time.valueOf(LocalTime.of(10, 0));
        Theme testTheme = Theme.of("테마", "테마 설명", "썸네일_url");

        long timeId = insertTestReservationTime(testTime);
        Long themeId = insertTestTheme(testTheme);

        insertTestReservation(timeId, themeId);
        Theme savedTheme = new Theme(themeId, testTheme.getName(), testTheme.getDescription(),
                testTheme.getThumbnailUrl());

        //when & then
        assertThatThrownBy(
                () -> themeRepository.delete(savedTheme)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertTestReservation(long timeId, Long themeId) {
        jdbcTemplate.update("""
                    insert into reservation(name, reservation_date, time_id, theme_id)
                    values (?, ?, ?, ?)
                """, "브라운", LocalDate.of(2026, 5, 6), timeId, themeId
        );
    }

    private Long insertTestTheme(Theme testTheme) {
        themeRepository.save(testTheme);

        return jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?",
                Long.class,
                testTheme.getName()
        );
    }

    private long insertTestReservationTime(Time testTime) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                testTime
        );

        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?",
                Long.class,
                testTime
        );
    }
}
