package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.role.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;

@ExtendWith(MockitoExtension.class)
public class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ThemeService themeService;

    @Test
    void 테마를_삭제한다() {

        Theme theme = new Theme(1L, "공포", "공포 테마", "공포.jpg");
        when(reservationRepository.existsByThemeId(1L)).thenReturn(false);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        themeService.deleteById(1L);
        assertThat(themeService.findAll()).isEmpty();
    }

    @Test
    void 이미_해당_테마의_예약이_존재한다면_삭제할_수_없다() {
        Theme theme = new Theme(1L, "공포", "공포 테마", "공포.jpg");
        when(reservationRepository.existsByThemeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> themeService.deleteById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_테마를_삭제할_수_없다() {
        when(reservationRepository.existsByThemeId(3L)).thenReturn(false);
        when(themeRepository.findById(3L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> themeService.deleteById(3L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 인기_테마를_최근_9일_이내_예약기준으로_조회한다() {

        // given
        LocalDate now = LocalDate.now();
        Theme themeRecentMost = new Theme(1L, "최근인기", "desc", "img");
        Theme themeOldMost = new Theme(2L, "예전인기", "desc", "img");
        Theme themeMedium = new Theme(3L, "보통", "desc", "img");

        Member member = new Member(1L, new Name("테스터"), new Email("test@test.com"), new Password("1234"), Role.MEMBER);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        Reservation r1 = Reservation.create(now.minusDays(1), time, themeRecentMost, member);
        Reservation r2 = Reservation.create(now.minusDays(2), time, themeRecentMost, member);
        Reservation r3 = Reservation.create(now.minusDays(3), time, themeRecentMost, member);

        Reservation r4 = Reservation.create(now.minusDays(5), time, themeMedium, member);
        Reservation r5 = Reservation.create(now.minusDays(6), time, themeMedium, member);

        Reservation o1 = Reservation.create(now.minusDays(10), time, themeOldMost, member);
        Reservation o2 = Reservation.create(now.minusDays(11), time, themeOldMost, member);
        Reservation o3 = Reservation.create(now.minusDays(12), time, themeOldMost, member);
        Reservation o4 = Reservation.create(now.minusDays(13), time, themeOldMost, member);
        Reservation o5 = Reservation.create(now.minusDays(14), time, themeOldMost, member);

        List<Reservation> allReservations = List.of(r1, r2, r3, r4, r5, o1, o2, o3, o4, o5);

        when(reservationRepository.findAll()).thenReturn(allReservations);

        // when
        List<ThemeResponse> responses = themeService.findPopularThemes();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(themeRecentMost.getId());
        assertThat(responses.get(1).id()).isEqualTo(themeMedium.getId());
    }
}
