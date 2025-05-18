package roomescape.unit.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.time.CurrentDateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.application.dto.ThemeCreateCommand;
import roomescape.reservation.application.dto.ThemeInfo;
import roomescape.reservation.application.service.ThemeService;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.reservation.ReservationRepository;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.theme.ThemeRepository;
import roomescape.reservation.domain.time.ReservationTime;
import roomescape.reservation.domain.time.ReservationTimeRepository;
import roomescape.support.fake.FakeReservationRepository;
import roomescape.support.fake.FakeReservationTimeRepository;
import roomescape.support.fake.FakeThemeRepository;
import roomescape.support.util.TestCurrentDateTime;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    private final CurrentDateTime currentDateTime = new TestCurrentDateTime(LocalDateTime.of(2025, 4, 30, 10, 0));
    private final ThemeRepository fakeThemeRepository = new FakeThemeRepository();
    private final ReservationRepository fakeReservationRepository = new FakeReservationRepository();
    private final ReservationTimeRepository fakeReservationTimeRepository = new FakeReservationTimeRepository();
    private final ThemeService themeService = new ThemeService(fakeThemeRepository, fakeReservationRepository,
            currentDateTime);

    @DisplayName("저장할 테마 이름이 중복될 경우 예외가 발생한다.")
    @Test
    void validateNameDuplication() {
        // given
        final ThemeCreateCommand request1 = new ThemeCreateCommand("테마", "설명1", "썸네일1.png");
        final ThemeCreateCommand request2 = new ThemeCreateCommand("테마", "설명2", "썸네일2.png");
        themeService.createTheme(request1);
        // when
        // then
        assertThatThrownBy(() -> themeService.createTheme(request2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 이름의 테마는 이미 존재합니다.");
    }

    @DisplayName("테마를 저장할 수 있다.")
    @Test
    void create() {
        // given
        final ThemeCreateCommand request = new ThemeCreateCommand("테마1", "설명1", "썸네일1.png");
        final ThemeInfo result = themeService.createTheme(request);
        // then
        final Theme saved = fakeThemeRepository.findById(result.id()).get();
        assertAll(
                () -> assertThat(result.id()).isEqualTo(1L),
                () -> assertThat(result.name()).isEqualTo(request.name()),
                () -> assertThat(result.description()).isEqualTo(request.description()),
                () -> assertThat(result.thumbnail()).isEqualTo(request.thumbnail()),
                () -> assertThat(saved.getNameOfTheme()).isEqualTo(request.name()),
                () -> assertThat(saved.getDescriptionOfTheme()).isEqualTo(request.description()),
                () -> assertThat(saved.getThumbnailOfTheme()).isEqualTo(request.thumbnail())
        );
    }

    @DisplayName("테마 목록을 조회할 수 있다.")
    @Test
    void findAll() {
        // given
        final ThemeCreateCommand request1 = new ThemeCreateCommand("테마1", "설명1", "썸네일1.png");
        final ThemeCreateCommand request2 = new ThemeCreateCommand("테마2", "설명2", "썸네일2.png");
        themeService.createTheme(request1);
        themeService.createTheme(request2);
        // when
        final List<ThemeInfo> result = themeService.findAll();
        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("예약이 존재하는 테마를 삭제할 경우 예외가 발생한다.")
    @Test
    void illegalDelete() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(11, 0));
        final ReservationTime saveTime = fakeReservationTimeRepository.save(reservationTime);
        final Member member = new Member(null, "멤버", "member@email.com", "memberpw", MemberRole.ADMIN);

        final ThemeCreateCommand request = new ThemeCreateCommand("테마1", "설명1", "썸네일1.png");
        final Theme savedTheme = fakeThemeRepository.save(request.convertToTheme());
        fakeReservationRepository.save(
                new Reservation(null, member, LocalDate.now().plusDays(1), saveTime, savedTheme));

        // when & then
        assertThatThrownBy(() -> themeService.deleteThemeById(savedTheme.id()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약이 존재하는 테마는 삭제할 수 없습니다.");
    }

    @DisplayName("테마를 삭제할 수 있다.")
    @Test
    void deleteThemeById() {
        // given
        final ThemeCreateCommand request = new ThemeCreateCommand("테마1", "설명1", "썸네일1.png");
        final Theme savedTheme = fakeThemeRepository.save(request.convertToTheme());
        // when
        themeService.deleteThemeById(savedTheme.id());
        // then
        final List<Theme> themes = fakeThemeRepository.findAll();
        assertThat(themes).isEmpty();
    }

    @DisplayName("인기 테마를 조회할 수 있다")
    @Test
    void findPopularThemes(@Mock ThemeRepository themeRepository) {
        // given
        final LocalDate to = currentDateTime.getDate().minusDays(1);
        final LocalDate from = currentDateTime.getDate().minusDays(7);
        final Theme theme1 = new Theme(1L, "테마1", "설명1", "썸네일1.png");
        final Theme theme2 = new Theme(2L, "테마2", "설명2", "썸네일2.png");
        when(themeRepository.findPopularThemes(from, to, 10)).thenReturn(List.of(theme1, theme2));
        final ThemeService themeService = new ThemeService(themeRepository, fakeReservationRepository, currentDateTime);
        // when
        final List<ThemeInfo> result = themeService.findPopularThemes();
        // then
        assertAll(
                () -> verify(themeRepository).findPopularThemes(from, to, 10),
                () -> assertThat(result).hasSize(2)
        );
    }

}
