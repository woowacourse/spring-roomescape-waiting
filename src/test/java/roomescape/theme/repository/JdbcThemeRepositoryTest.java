package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.theme.domain.Theme;

@JdbcTest
class JdbcThemeRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    JdbcThemeRepository themeRepository;

    @Autowired
    public JdbcThemeRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.themeRepository = new JdbcThemeRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("새로운 테마를 저장하고 반환된 객체의 ID를 확인한다.")
    void save_validTheme_returnsWithId() {
        // given
        Theme theme = Theme.of("테마", "설명", "thumbnailUrl");

        // when
        Theme saved = themeRepository.save(theme);

        //then
        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo(theme.name());
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
    @DisplayName("ID가 사용되고 있으면 예외가 발생한다.")
    void deleteById_themeInUse_throwsDataIntegrityViolation() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        long timeId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?",
                Long.class,
                Time.valueOf(LocalTime.of(10, 0))
        );

        themeRepository.save(
                Theme.of("테마", "테마 설명", "썸네일_url")
        );

        Long themeId = jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?",
                Long.class,
                "테마"
        );

        jdbcTemplate.update("""
            insert into reservation(name, reservation_date, time_id, theme_id)
            values (?, ?, ?, ?)
        """, "브라운", LocalDate.of(2026, 5, 6),timeId, themeId
        );

        //when & then
        assertThatThrownBy(
                () -> themeRepository.deleteById(themeId)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("테마 이름을 기준으로 조회한다.")
    void existsByName() {
        //given
        themeRepository.save(
                Theme.of("테마", "테마 설명", "썸네일_url")
        );

        //when & then
        assertThat(themeRepository.existsByName("테마"))
                .isTrue();

        assertThat(themeRepository.existsByName("없는_것"))
                .isFalse();
    }

    @Test
    @DisplayName("ID를 통해 저장된 테마를 조회한다.")
    void findById_existingTheme_returnsTheme() {
        // given
        Theme saved = themeRepository.save(Theme.of("테마", "설명", "thumbnailUrl"));

        // when
        Theme found = themeRepository.findById(saved.id())
                .orElseThrow(() -> new AssertionError("조회된 결과가 없습니다. id: " + saved.id()));

        // then
        assertThat(found.name()).isEqualTo(saved.name());
        assertThat(found.description()).isEqualTo(saved.description());
        assertThat(found.thumbnailUrl()).isEqualTo(saved.thumbnailUrl());
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
}
