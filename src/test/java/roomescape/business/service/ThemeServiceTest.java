package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.business.domain.Theme;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.persistence.dao.JdbcReservationDao;
import roomescape.persistence.dao.JdbcThemeDao;
import roomescape.persistence.dao.ReservationDao;
import roomescape.persistence.dao.ThemeDao;
import roomescape.presentation.dto.ThemeRequest;
import roomescape.presentation.dto.ThemeResponse;
import roomescape.util.CurrentUtil;

@JdbcTest
@Sql("classpath:data-themeService.sql")
class ThemeServiceTest {

    private ThemeService themeService;
    private final JdbcTemplate jdbcTemplate;
    private final ThemeDao themeDao;

    @Autowired
    public ThemeServiceTest(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeDao = new JdbcThemeDao(jdbcTemplate);
    }

    @BeforeEach
    void setUp() {
        final ReservationDao reservationDao = new JdbcReservationDao(jdbcTemplate);
        final CurrentUtil currentUtil = () -> LocalDate.of(2025, 5, 10);
        themeService = new ThemeService(themeDao, reservationDao, currentUtil);
    }

    @Test
    @DisplayName("테마 요청 객체로 테마를 저장한다")
    void insert() {
        // given
        final ThemeRequest themeRequest = new ThemeRequest("테마", "소개", "썸네일");

        // when
        final ThemeResponse themeResponse = themeService.insert(themeRequest);

        // then
        assertAll(
                () -> assertThat(themeResponse.name()).isEqualTo(themeRequest.name()),
                () -> assertThat(themeResponse.description()).isEqualTo(themeRequest.description()),
                () -> assertThat(themeResponse.thumbnail()).isEqualTo(themeRequest.thumbnail())
        );
    }

    @Test
    @DisplayName("저장하려는 테마의 이름과 동일한 이름이 이미 존재한다면 예외가 발생한다")
    void insertWhenNameIsDuplicate() {
        // given
        final ThemeRequest themeRequest = new ThemeRequest("테마", "소개", "썸네일");
        themeService.insert(themeRequest);

        // when & then
        assertThatThrownBy(() -> themeService.insert(themeRequest))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("모든 테마를 조회한다")
    void findAll() {
        // given
        // data-themeService.sql

        // when
        final List<ThemeResponse> themeResponses = themeService.findAll();

        // then
        assertThat(themeResponses).hasSize(4);
    }

    @Test
    @DisplayName("id를 통해 방탈출 예약 시간을 조회한다")
    void findByIdById() {
        // given
        final ThemeRequest themeRequest = new ThemeRequest("테마", "소개", "썸네일");
        final ThemeResponse themeResponse = themeService.insert(themeRequest);
        final Long id = themeResponse.id();

        // when
        final ThemeResponse findThemeResponse = themeService.findById(id);

        // then
        assertAll(
                () -> assertThat(findThemeResponse.id()).isEqualTo(id),
                () -> assertThat(findThemeResponse.name()).isEqualTo(themeRequest.name()),
                () -> assertThat(findThemeResponse.description()).isEqualTo(themeRequest.description()),
                () -> assertThat(findThemeResponse.thumbnail()).isEqualTo(themeRequest.thumbnail())
        );
    }

    @Test
    @DisplayName("id를 통해 테마를 조회할 때 대상이 없다면 예외가 발생한다")
    void findByIdWhenNotExists() {
        // given
        final Long notExistsId = 999L;

        // when & then
        assertThatThrownBy(() -> themeService.findById(notExistsId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("id를 통해 테마를 삭제한다")
    void deleteById() {
        // given
        final ThemeRequest themeRequest = new ThemeRequest("테마", "소개", "썸네일");
        final ThemeResponse themeResponse = themeService.insert(themeRequest);
        final Long id = themeResponse.id();

        // when
        themeService.deleteById(id);

        // then
        Optional<Theme> findTheme = themeDao.findById(id);
        assertThat(findTheme).isEmpty();
    }

    @Test
    @DisplayName("id를 통해 테마를 삭제할 때 대상이 없다면 예외가 발생한다")
    void deleteByIdWhenNotExists() {
        // given
        final Long notExistsId = 999L;

        // when & then
        assertThatThrownBy(() -> themeService.deleteById(notExistsId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("인기 테마를 조회한다")
    void findPopularThemes() {
        // given
        // data-themeService.sql
        // 총 4개의 테마가 있고, `강추`, `추천`, `평범`, `미예약` 이름 순으로 인기가 많다.

        // when
        final List<ThemeResponse> themeResponses = themeService.findPopularThemes();

        // then
        assertAll(
                () -> assertThat(themeResponses.get(0)
                        .name()).isEqualTo("강추"),
                () -> assertThat(themeResponses.get(1)
                        .name()).isEqualTo("추천"),
                () -> assertThat(themeResponses.get(2)
                        .name()).isEqualTo("평범"),
                () -> assertThat(themeResponses.get(3)
                        .name()).isEqualTo("미예약")
        );
    }
}
