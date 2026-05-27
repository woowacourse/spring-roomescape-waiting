package roomescape.theme.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservation.application.ThemeReferenceAdapter;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.fake.FakeReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.application.dto.ThemeCreateCommand;
import roomescape.theme.domain.PopularThemeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.fake.FakePopularThemeRepository;
import roomescape.theme.fake.FakeThemeRepository;

class ThemeServiceTest {

    private static final String TEST_THEME_NAME = "테스트 테마";
    private static final String TEST_THEME_DESCRIPTION = "테스트 테마 설명";
    private static final String TEST_THEME_THUMBNAIL = "https://good.com/thumb-nail";

    private List<Reservation> reservations;
    private ThemeRepository themeRepository;
    private PopularThemeRepository popularThemeRepository;
    private ThemeService themeService;
    private ReservationRepository reservationRepository;
    private ThemeReference themeReference;

    @BeforeEach
    void setUp() {
        reservations = new ArrayList<>();
        themeRepository = new FakeThemeRepository();
        popularThemeRepository = new FakePopularThemeRepository(
                reservations,
                themeRepository.findAll()
        );
        reservationRepository = new FakeReservationRepository();
        themeReference = new ThemeReferenceAdapter(reservationRepository);
        themeService = new ThemeService(themeReference, themeRepository, popularThemeRepository);
    }

    @Test
    @DisplayName("테마를 저장한다")
    void save_success() {
        // when
        Theme theme = saveTheme(TEST_THEME_NAME, TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);

        // then
        assertThat(theme.getId()).isNotNull();
        assertThat(theme.getName()).isEqualTo(TEST_THEME_NAME);
        assertThat(theme.getDescription()).isEqualTo(TEST_THEME_DESCRIPTION);
        assertThat(theme.getThumbnailUrl()).isEqualTo(TEST_THEME_THUMBNAIL);
        assertThat(themeRepository.findById(theme.getId())).contains(theme);
    }

    @Test
    @DisplayName("아이디 기반으로 테마를 조회한다")
    void findById_success() {
        // given
        Theme saved = saveTheme(TEST_THEME_NAME, TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);

        // when
        var theme = themeService.findById(saved.getId());

        // then
        assertThat(theme).contains(saved);
    }

    @Test
    @DisplayName("전체 테마 목록을 조회한다")
    void findAll_success() {
        // given
        Theme saved = saveTheme(TEST_THEME_NAME, TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);
        Theme saved2 = saveTheme(TEST_THEME_NAME + "2", TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);

        // when
        List<Theme> themes = themeService.findAll();

        // then
        assertThat(themes).containsExactly(saved, saved2);
    }

    @Test
    @DisplayName("아이디 기반으로 테마를 삭제한다")
    void deleteById_success() {
        // given
        Theme saved = saveTheme(TEST_THEME_NAME, TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);
        Long deleteTargetId = saved.getId();

        // when
        themeService.deleteById(deleteTargetId);

        // then
        assertThat(themeRepository.findById(deleteTargetId)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 테마 ID로 삭제하면 예외가 발생한다")
    void deleteById_fail_with_not_found_theme() {
        // when & then
        assertThatThrownBy(() -> themeService.deleteById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("테마를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("정렬 기준으로 인기 테마를 조회한다")
    void findTopNByPeriod_success() {
        // given
        Theme firstTheme = themeRepository.save(Theme.create("인기 테마", "설명", "https://good.com/thumb-nail/1"));
        Theme secondTheme = themeRepository.save(Theme.create("보통 테마", "설명", "https://good.com/thumb-nail/2"));
        ReservationTime reservationTime = ReservationTime.createRow(1L, LocalTime.now().plusHours(1));
        LocalDate today = LocalDate.now();
        reservations.add(Reservation.create("테스터1", today, reservationTime, firstTheme));
        reservations.add(Reservation.create("테스터2", today, reservationTime, firstTheme));
        reservations.add(Reservation.create("테스터3", today, reservationTime, secondTheme));
        popularThemeRepository = new FakePopularThemeRepository(reservations, themeRepository.findAll());
        themeService = new ThemeService(themeReference, themeRepository, popularThemeRepository);

        // when
        List<Theme> themes = themeService.findTopNByPeriod(
                today.minusDays(1),
                today,
                "POPULAR",
                2L
        );

        // then
        assertThat(themes).containsExactly(firstTheme, secondTheme);
    }

    @Test
    @DisplayName("예약에서 참조 중인 테마를 삭제하면 예외가 발생한다")
    void deleteById_fail_with_referenced_theme() {
        // given
        ReservationTime time = ReservationTime.create(LocalTime.now().plusHours(1));
        Theme theme = themeRepository.save(Theme.create("인기 테마", "설명", "https://good.com/thumb-nail/1"));
        reservationRepository.save(Reservation.create("테스터", LocalDate.now(), time, theme));

        // when & then
        assertThatThrownBy(() -> themeService.deleteById(theme.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("예약이 연결된 테마는 삭제할 수 없습니다.");
        assertThat(themeRepository.findById(theme.getId())).contains(theme);
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeService.save(new ThemeCreateCommand(name, description, thumbnail));
    }
}
