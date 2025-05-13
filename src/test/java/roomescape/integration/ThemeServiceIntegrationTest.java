package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.fake.TestCurrentDateTime;
import roomescape.reservation.service.dto.ThemeCreateCommand;
import roomescape.reservation.service.dto.ThemeInfo;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ThemeDao;
import roomescape.reservation.service.ThemeService;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = {"/schema.sql", "/test-data.sql"})
public class ThemeServiceIntegrationTest {

    @Autowired
    ThemeDao themeDao;

    @Autowired
    ReservationDao reservationDao;
    ThemeService themeService;
    TestCurrentDateTime currentDateTime;

    @BeforeEach
    void init() {
        LocalDateTime now = LocalDateTime.of(2025, 5, 1, 10, 00);
        currentDateTime = new TestCurrentDateTime(now);
        themeService = new ThemeService(themeDao, reservationDao, currentDateTime);
    }

    @DisplayName("테마를 생성할 수 있다")
    @Test
    void createTheme() {
        // given
        ThemeCreateCommand request = new ThemeCreateCommand("우테코방탈출", "우테코를 탈출해라", "www.naver.com");
        // when
        ThemeInfo result = themeService.createTheme(request);
        // then
        Theme savedTheme = themeDao.findById(result.id()).get();
        assertAll(
                () -> assertThat(result.id()).isNotNull(),
                () -> assertThat(result.name()).isEqualTo(request.name()),
                () -> assertThat(result.description()).isEqualTo(request.description()),
                () -> assertThat(result.thumbnail()).isEqualTo(request.thumbnail()),
                () -> assertThat(savedTheme.getId()).isNotNull(),
                () -> assertThat(savedTheme.getName()).isEqualTo(request.name()),
                () -> assertThat(savedTheme.getDescription()).isEqualTo(request.description()),
                () -> assertThat(savedTheme.getThumbnail()).isEqualTo(request.thumbnail())
        );
    }

    @DisplayName("테마의 이름이 중복되면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDuplicateThemeName() {
        // given
        ThemeCreateCommand request = new ThemeCreateCommand("테마1", "우테코를 탈출해라", "www.naver.com");
        // when
        // then
        assertThatThrownBy(() -> themeService.createTheme(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 이름의 테마는 이미 존재합니다.");
    }

    @DisplayName("모든 테마를 조회할 수 있다")
    @Test
    void findAll() {
        // when
        List<ThemeInfo> result = themeService.findAll();
        // then
        assertThat(result).hasSize(11);
    }

    @DisplayName("id를 기반으로 테마를 삭제할 수 있다")
    @Test
    void deleteThemeById() {
        // when
        themeService.deleteThemeById(10L);
        // then
        List<ThemeInfo> responses = themeService.findAll();
        assertThat(responses).hasSize(10);
    }

    @DisplayName("예약이 존재하는 테마를 삭제하면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDeleteThemeWithinReservation() {
        // when
        // then
        assertThatThrownBy(() -> themeService.deleteThemeById(7L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약이 존재하는 테마는 삭제할 수 없습니다.");
    }

    @DisplayName("최근 일주일 간 예약이 많은 순으로 인기 테마를 조회할 수 있다")
    @Test
    void findPopularThemes() {
        // when
        List<ThemeInfo> result = themeService.findPopularThemes();
        // then
        assertThat(result).hasSize(10);
        assertThat(result.getFirst().name()).isEqualTo("테마11");
        assertThat(result.get(1).name()).isEqualTo("테마9");
    }

    @DisplayName("최근 일주일 간 예약이 존재하지 않는 테마는 인기 테마에 포함되지 않는다")
    @Test
    void findPopularThemes2() {
        // given
        currentDateTime.changeDateTime(LocalDateTime.of(2025, 4, 12, 10, 0));
        // when
        List<ThemeInfo> result = themeService.findPopularThemes();
        // then
        assertThat(result).isEmpty();
    }
}
