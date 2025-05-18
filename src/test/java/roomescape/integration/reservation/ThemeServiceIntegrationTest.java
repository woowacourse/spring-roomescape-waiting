package roomescape.integration.reservation;

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
import roomescape.reservation.application.dto.ThemeCreateCommand;
import roomescape.reservation.application.dto.ThemeInfo;
import roomescape.reservation.application.service.ThemeService;
import roomescape.reservation.domain.reservation.ReservationRepository;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.theme.ThemeRepository;
import roomescape.support.util.TestCurrentDateTime;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = {"/schema.sql", "/test-data.sql"})
public class ThemeServiceIntegrationTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;
    private ThemeService themeService;
    private TestCurrentDateTime currentDateTime;

    @BeforeEach
    void init() {
        final LocalDateTime now = LocalDateTime.of(2025, 5, 1, 10, 00);
        currentDateTime = new TestCurrentDateTime(now);
        themeService = new ThemeService(themeRepository, reservationRepository, currentDateTime);
    }

    @DisplayName("테마를 생성할 수 있다")
    @Test
    void createTheme() {
        // given
        final ThemeCreateCommand request = new ThemeCreateCommand("테마", "설명", "썸네일.png");
        // when
        final ThemeInfo result = themeService.createTheme(request);
        // then
        final Theme savedTheme = themeRepository.findById(result.id()).get();
        assertAll(
                () -> assertThat(result.id()).isNotNull(),
                () -> assertThat(result.name()).isEqualTo(request.name()),
                () -> assertThat(result.description()).isEqualTo(request.description()),
                () -> assertThat(result.thumbnail()).isEqualTo(request.thumbnail()),
                () -> assertThat(savedTheme.id()).isNotNull(),
                () -> assertThat(savedTheme.getNameOfTheme()).isEqualTo(request.name()),
                () -> assertThat(savedTheme.getDescriptionOfTheme()).isEqualTo(request.description()),
                () -> assertThat(savedTheme.getThumbnailOfTheme()).isEqualTo(request.thumbnail())
        );
    }

    @DisplayName("테마의 이름이 중복되면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDuplicateThemeName() {
        // given
        final ThemeCreateCommand request1 = new ThemeCreateCommand("테마", "설명1", "썸네일1.png");
        final ThemeCreateCommand request2 = new ThemeCreateCommand("테마", "설명2", "썸네일2.png");
        themeService.createTheme(request1);
        // when & then
        assertThatThrownBy(() -> themeService.createTheme(request2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 이름의 테마는 이미 존재합니다.");
    }

    @DisplayName("모든 테마를 조회할 수 있다")
    @Test
    void findAll() {
        // when
        final List<ThemeInfo> result = themeService.findAll();
        // then
        assertThat(result).hasSize(11);
    }

    @DisplayName("id를 기반으로 테마를 삭제할 수 있다")
    @Test
    void deleteThemeById() {
        // when
        themeService.deleteThemeById(10L);
        // then
        final List<ThemeInfo> responses = themeService.findAll();
        assertThat(responses).hasSize(10);
    }

    @DisplayName("예약이 존재하는 테마를 삭제하면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDeleteThemeWithinReservation() {
        // when & then
        assertThatThrownBy(() -> themeService.deleteThemeById(7L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약이 존재하는 테마는 삭제할 수 없습니다.");
    }

    @DisplayName("최근 일주일 간 예약이 많은 순으로 인기 테마를 조회할 수 있다")
    @Test
    void findPopularThemes() {
        // when
        final List<ThemeInfo> result = themeService.findPopularThemes();
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
        final List<ThemeInfo> result = themeService.findPopularThemes();
        // then
        assertThat(result).isEmpty();
    }
}
