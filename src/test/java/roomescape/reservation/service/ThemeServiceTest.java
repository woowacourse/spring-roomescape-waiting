package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.ThemeService;

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
        when(reservationRepository.existReservationByThemeId(1L)).thenReturn(false);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        themeService.deleteById(1L);
        assertThat(themeService.getAll()).isEmpty();
    }

    @Test
    void 이미_해당_테마의_예약이_존재한다면_삭제할_수_없다() {
        Theme theme = new Theme(1L, "공포", "공포 테마", "공포.jpg");
        when(reservationRepository.existReservationByThemeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> themeService.deleteById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_테마를_삭제할_수_없다() {
        when(reservationRepository.existReservationByThemeId(3L)).thenReturn(false);
        when(themeRepository.findById(3L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> themeService.deleteById(3L))
                .isInstanceOf(NoSuchElementException.class);
    }

    //TODO

//    @Test
//    void 지난_일주일_간_인기_테마_10개를_조회한다() {
//        memberJdbcRepository.save(new Name("매트"), new Email("matt.kakao"), new Password("1234"));
//
//        List<Theme> themes = new ArrayList<>();
//        for (int i = 0; i < 20; i++) {
//            themes.add(themeDbFixture.커스텀_테마("테마" + i));
//        }
//
//        for (int i = 0; i < 20; i++) {
//            addReservation(i, ReservationDateFixture.예약날짜_오늘, reservationTimeDbFixture.예약시간_10시(), themes.get(i));
//            addReservation(19 - i, ReservationDateFixture.예약날짜_7일전, reservationTimeDbFixture.예약시간_10시(), themes.get(i));
//        }
//
//        List<ThemeResponse> popularThemes = themeService.getPopularThemes(LocalDate.now());
//
//        assertThat(popularThemes)
//                .hasSize(10)
//                .extracting(ThemeResponse::id)
//                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
//    }
//
//    private void addReservation(int count, ReservationDate date, ReservationTime time, Theme theme) {
//        for (int i = 0; i < count; i++) {
//            reservationDbFixture.예약_생성_한스(date, time, theme);
//        }
//    }
}
