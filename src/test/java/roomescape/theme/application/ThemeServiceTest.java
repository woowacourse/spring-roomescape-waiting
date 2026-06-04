package roomescape.theme.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.fixture.ReservationFixture;
import roomescape.time.domain.fixture.ReservationTimeFixture;
import roomescape.theme.domain.fixture.ThemeFixture;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.fake.FakeReservationRepository;
import roomescape.theme.domain.fake.FakeThemeRepository;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.dto.ThemeCommand;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.theme.application.exception.DuplicateThemeException;
import roomescape.theme.application.exception.ThemeInUseException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

class ThemeServiceTest {

    private ThemeRepository themeRepository;
    private ReservationRepository reservationRepository;
    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        this.themeRepository = new FakeThemeRepository();
        this.reservationRepository = new FakeReservationRepository();
        this.themeService = new ThemeService(themeRepository, reservationRepository);
    }

    @Test
    void 새로운_테마를_정상적으로_등록한다() {
        // given
        ThemeCommand command = new ThemeCommand("공포테마", "https://image.com/image.png", "무서운 테마입니다.");

        // when
        ThemeInfo response = themeService.create(command);

        // then
        assertThat(response).extracting(ThemeInfo::id, ThemeInfo::name, ThemeInfo::thumbnailImageUrl,
                        ThemeInfo::description, ThemeInfo::isActive)
                .containsExactly(1L, "공포테마", "https://image.com/image.png", "무서운 테마입니다.", true);
    }

    @Test
    void 이미_존재하는_테마_이름으로_등록하면_예외가_발생한다() {
        // given
        themeRepository.save(ThemeFixture.createDefaultTheme());
        ThemeCommand command = new ThemeCommand("공포테마", "https://image.com/image.png", "다른 설명입니다.");

        // when & then
        assertThatThrownBy(() -> themeService.create(command)).isInstanceOf(DuplicateThemeException.class);
    }

    @Test
    void 페이징_정보에_따른_활성_테마_목록을_조회한다() {
        // given
        themeRepository.save(ThemeFixture.createDefaultTheme());
        themeRepository.save(Theme.create("놀이동산테마", "https://image.com/image2.png", "즐거운 테마입니다."));

        // when
        List<ThemeInfo> responses = themeService.getThemes(0, 10);

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    void 예약이_없는_테마를_비활성화한다() {
        // given
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());

        // when
        themeService.deactivate(theme.getId());

        // then
        assertThat(themeRepository.getById(theme.getId()).isActive()).isFalse();
    }

    @Test
    void 존재하지_않는_테마를_비활성화하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> themeService.deactivate(1L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void 예약이_존재하는_테마를_비활성화하면_예외가_발생한다() {
        // given
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        reservationRepository.save(ReservationFixture.createDefaultReservation("바니", theme,
                ReservationTimeFixture.createDefaultReservationTime()));

        // when & then
        assertThatThrownBy(() -> themeService.deactivate(theme.getId())).isInstanceOf(ThemeInUseException.class);
    }

    @Test
    void 대기_예약이_존재하는_테마를_비활성화하면_예외가_발생한다() {
        // given
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        reservationRepository.save(ReservationFixture.createWaitingReservation("바니", theme,
                ReservationTimeFixture.createDefaultReservationTime()));

        // when & then
        assertThatThrownBy(() -> themeService.deactivate(theme.getId())).isInstanceOf(ThemeInUseException.class);
    }

    @Test
    void 인기_테마_목록을_조회한다() {
        // given
        themeRepository.save(ThemeFixture.createDefaultTheme());
        themeRepository.save(Theme.create("놀이동산테마", "https://image.com/image2.png", "즐거운 테마입니다."));

        // when
        List<ThemeInfo> responses = themeService.getWeeksTopThemes(LocalDate.now().minusDays(7), LocalDate.now(), 1);

        // then
        assertThat(responses).hasSize(1);
    }
}
