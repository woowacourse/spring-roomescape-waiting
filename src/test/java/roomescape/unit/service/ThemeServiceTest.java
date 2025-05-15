package roomescape.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ExistedThemeException;
import roomescape.service.ThemeService;
import roomescape.unit.fake.FakeReservationRepository;
import roomescape.unit.fake.FakeThemeRepository;

public class ThemeServiceTest {

    private ThemeService themeService;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeServiceTest() {
        this.themeRepository = new FakeThemeRepository();
        this.reservationRepository = new FakeReservationRepository();
        this.themeService = new ThemeService(themeRepository, reservationRepository);
    }

    @Test
    void 테마를_생성할_수_있다() {
        // given
        ThemeRequest theme = new ThemeRequest("name3", "description3", "thumbnail3");
        // when
        ThemeResponse savedTheme = themeService.createTheme(theme);
        // then
        assertThat(savedTheme.name()).isEqualTo("name3");
        assertThat(savedTheme.description()).isEqualTo("description3");
        assertThat(savedTheme.thumbnail()).isEqualTo("thumbnail3");
    }

    @Test
    void 테마_목록을_조회할_수_있다() {
        // given
        themeRepository.save(Theme.createWithoutId("name1", "description1", "thumbnail1"));
        themeRepository.save(Theme.createWithoutId("name2", "description2", "thumbnail2"));
        // when
        List<ThemeResponse> themes = themeService.findAllThemes();
        // then
        assertThat(themes).hasSize(2);
        assertThat(themes.getFirst().name()).isEqualTo("name1");
        assertThat(themes.get(1).name()).isEqualTo("name2");
    }

    @Test
    void 예약이_존재하는_테마를_삭제하면_예외가_발생한다() {
        // given
        Theme theme = themeRepository.save(Theme.createWithoutId("name1", "description1", "thumbnail1"));
        Member member = new Member(1L, "name1", "email@domain.com", "password1", Role.MEMBER);
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        reservationRepository.save(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), time, theme));
        // when & then
        assertThatThrownBy(() -> themeService.deleteThemeById(theme.getId()))
                .isInstanceOf(ExistedReservationException.class);
    }

    @Test
    void 테마를_삭제할_수_있다() {
        // given
        Theme savedTheme = themeRepository.save(Theme.createWithoutId("name1", "description1", "thumbnail1"));
        // when
        themeService.deleteThemeById(savedTheme.getId());
        // then
        assertThat(themeRepository.findAll()).hasSize(0);
    }

    @Test
    void 중복된_이름으로_테마를_생성할_수_없다() {
        // given
        themeRepository.save(Theme.createWithoutId("name1", "desc1", "thumb1"));
        ThemeRequest themeRequest = new ThemeRequest("name1", "desc2", "thumb2");
        // when & then
        assertThatThrownBy(() -> themeService.createTheme(themeRequest))
                .isInstanceOf(ExistedThemeException.class);
    }


    @Test
    void 인기_테마를_조회한다() {
        // given
        Theme theme1 = themeRepository.save(Theme.createWithoutId("name1", "desc", "thumb"));
        Theme theme2 = themeRepository.save(Theme.createWithoutId("name2", "desc", "thumb"));
        Theme theme3 = themeRepository.save(Theme.createWithoutId("name3", "desc", "thumb"));

        Member member1 = new Member(1L, "name1", "email1@email.com", "password1", Role.MEMBER);
        Member member2 = new Member(2L, "name2", "email1@email.com", "password1", Role.MEMBER);

        ReservationTime time1 = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        ReservationTime time2 = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime time3 = ReservationTime.createWithoutId(LocalTime.of(11, 0));

        Reservation reservation1 = Reservation.createWithoutId(member1, LocalDate.now().minusDays(1), time1, theme1);
        Reservation reservation2 = Reservation.createWithoutId(member1, LocalDate.now().minusDays(2), time2, theme1);
        Reservation reservation3 = Reservation.createWithoutId(member1, LocalDate.now().minusDays(3), time3, theme1);

        Reservation reservation4 = Reservation.createWithoutId(member1, LocalDate.now().minusDays(1), time1, theme2);
        Reservation reservation5 = Reservation.createWithoutId(member1, LocalDate.now().minusDays(2), time2, theme2);

        Reservation reservation6 = Reservation.createWithoutId(member1, LocalDate.now().minusDays(3), time3, theme3);

        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
        reservationRepository.save(reservation4);
        reservationRepository.save(reservation5);
        reservationRepository.save(reservation6);

        // when
        List<ThemeResponse> rank = themeService.getTopThemes();
        // then
        assertThat(rank).hasSize(3);
        assertThat(rank).isEqualTo(
                List.of(ThemeResponse.from(theme1), ThemeResponse.from(theme2), ThemeResponse.from(theme3)));
    }
}
