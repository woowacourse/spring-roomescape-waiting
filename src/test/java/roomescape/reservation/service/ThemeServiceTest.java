package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.CurrentDateTime;
import roomescape.fake.FakeReservationRepository;
import roomescape.fake.FakeThemeRepository;
import roomescape.fake.TestCurrentDateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.service.dto.ThemeCreateCommand;
import roomescape.reservation.service.dto.ThemeInfo;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    private ThemeRepository fakeThemeRepository;
    private ReservationRepository fakeReservationRepository;
    private CurrentDateTime currentDateTime;
    private ThemeService themeService;
    private ThemeCreateCommand createCommand;

    @BeforeEach
    void setUp() {
        fakeThemeRepository = new FakeThemeRepository();
        fakeReservationRepository = new FakeReservationRepository();
        currentDateTime = new TestCurrentDateTime(LocalDateTime.of(2025, 4, 30, 10, 00));
        themeService = new ThemeService(fakeThemeRepository, fakeReservationRepository, currentDateTime);
        createCommand = new ThemeCreateCommand("woooteco", "우테코를 탈출하라", "https://www.woowacourse.io/");
    }

    @DisplayName("저장할 테마 이름이 중복될 경우 예외가 발생한다.")
    @Test
    void validateNameDuplication() {
        // given
        themeService.createTheme(createCommand);
        ThemeCreateCommand createCommand2 = new ThemeCreateCommand("woooteco", "우테코에서 살아남기",
                "https://www.woowacourse2.io/");

        // when
        // then
        assertThatThrownBy(() -> themeService.createTheme(createCommand2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 이름의 테마는 이미 존재합니다.");
    }

    @DisplayName("테마를 저장할 수 있다.")
    @Test
    void create() {
        // when
        ThemeInfo result = themeService.createTheme(createCommand);

        // then
        Theme saved = fakeThemeRepository.findById(result.id()).get();
        assertAll(
                () -> assertThat(result.id()).isEqualTo(1L),
                () -> assertThat(result.name()).isEqualTo(createCommand.name()),
                () -> assertThat(result.description()).isEqualTo(createCommand.description()),
                () -> assertThat(result.thumbnail()).isEqualTo(createCommand.thumbnail()),
                () -> assertThat(saved.getName()).isEqualTo(createCommand.name()),
                () -> assertThat(saved.getDescription()).isEqualTo(createCommand.description()),
                () -> assertThat(saved.getThumbnail()).isEqualTo(createCommand.thumbnail())
        );
    }

    @DisplayName("테마 목록을 조회할 수 있다.")
    @Test
    void findAll() {
        // given
        themeService.createTheme(createCommand);
        themeService.createTheme(new ThemeCreateCommand("woooteco2", "우테코에 합격하라", "https://www.woowacourse.io/"));

        // when
        List<ThemeInfo> result = themeService.findAll();

        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("예약이 존재하는 테마를 삭제할 경우 예외가 발생한다.")
    @Test
    void illegalDelete() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(11, 0));
        Theme savedTheme = fakeThemeRepository.save(new Theme(null, "우테코방탈출", "탈출탈출탈출", "포비솔라브라운"));
        Member member = new Member(1L, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN);
        Reservation reservation = new Reservation(null, member, LocalDate.now().plusDays(1), time, savedTheme);
        fakeReservationRepository.save(reservation);

        // when
        // then
        assertThatThrownBy(() -> themeService.deleteThemeById(savedTheme.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약이 존재하는 테마는 삭제할 수 없습니다.");
    }

    @DisplayName("테마를 삭제할 수 있다.")
    @Test
    void deleteThemeById() {
        // given
        Theme theme = new Theme(null, "우테코방탈출", "탈출탈출탈출", "포비솔라브라운");
        Theme savedTheme = fakeThemeRepository.save(theme);

        // when
        themeService.deleteThemeById(savedTheme.getId());

        // then
        List<Theme> themes = fakeThemeRepository.findAll();
        assertThat(themes).isEmpty();
    }

    @DisplayName("인기 테마를 조회할 수 있다")
    @Test
    void findPopularThemes(@Mock ThemeRepository themeRepository) {
        // given
        LocalDate from = currentDateTime.getDate().minusDays(7);
        LocalDate to = currentDateTime.getDate().minusDays(1);
        Theme theme1 = new Theme(1L, "theme1", "description1", "thumbnail1");
        Theme theme2 = new Theme(2L, "theme2", "description2", "thumbnail2");
        ThemeService themeService = new ThemeService(themeRepository, fakeReservationRepository, currentDateTime);

        when(themeRepository.findPopularThemes(from, to, 10)).thenReturn(List.of(theme1, theme2));

        // when
        List<ThemeInfo> result = themeService.findPopularThemes();

        // then
        assertAll(
                () -> verify(themeRepository).findPopularThemes(from, to, 10),
                () -> assertThat(result).hasSize(2)
        );
    }

}
