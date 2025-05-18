package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.util.DateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.service.FakeReservationRepository;
import roomescape.reservation.service.FakeThemeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.dto.response.PopularThemeResponse;

class ThemeServiceTest {


    private DateTime dateTime = new DateTime() {
        @Override
        public LocalDateTime now() {
            return LocalDateTime.of(2025, 12, 7, 10, 0);
        }

        @Override
        public LocalDate nowDate() {
            return LocalDate.of(2025, 12, 7);
        }
    };
    private List<Reservation> reservations = new ArrayList<>();
    private List<Theme> themes = new ArrayList<>();
    private ReservationRepository reservationRepository = new FakeReservationRepository(reservations);
    private ThemeRepository themeRepository = new FakeThemeRepository(themes, reservations);
    private ThemeService themeService = new ThemeService(dateTime, themeRepository, reservationRepository);

    @BeforeEach
    void beforeEach() {
        Theme theme1 = Theme.createWithId(1L, "테스트1", "설명", "썸네일");
        Theme theme2 = Theme.createWithId(2L, "테스트2", "설명", "썸네일");
        Theme theme3 = Theme.createWithId(3L, "테스트3", "설명", "썸네일");
        Member member = Member.createWithId(1L, "a", "a@com", "a", Role.USER);
        reservationRepository.save(
                Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member, LocalDate.of(2025, 12, 5),
                        null, theme1));
        reservationRepository.save(
                Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member, LocalDate.of(2025, 12, 6),
                        null, theme1));
        reservationRepository.save(
                Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member, LocalDate.of(2025, 12, 4),
                        null, theme3));
        themeRepository.save(theme1);
        themeRepository.save(theme2);
        themeRepository.save(theme3);
    }

    @DisplayName("존재하는 예약의 테마는 삭제할 수 없다.")
    @Test
    void can_not_remove_exists_reservation() {
        assertThatThrownBy(() -> themeService.deleteThemeById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("인기 테마를 가져올 수 있다.")
    @Test
    void can_get_popular_theme() {
        // given
        List<PopularThemeResponse> popularThemes = themeService.getPopularThemes();
        // when & then
        assertThat(popularThemes).containsExactly(
                new PopularThemeResponse("테스트1", "썸네일", "설명"),
                new PopularThemeResponse("테스트3", "썸네일", "설명")
        );
    }
}
