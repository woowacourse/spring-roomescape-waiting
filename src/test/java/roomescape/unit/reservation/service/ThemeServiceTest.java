package roomescape.unit.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ExistedThemeException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.dto.request.ThemeRequest;
import roomescape.reservation.dto.response.ThemeResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ThemeRepository;
import roomescape.reservation.service.ThemeService;
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
        themeRepository.save(Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build());
        themeRepository.save(Theme.builder()
                .name("theme2")
                .description("desc2")
                .thumbnail("thumb2").build());
        // when
        List<ThemeResponse> themes = themeService.findAllThemes();
        // then
        assertThat(themes).hasSize(2);
        assertThat(themes.getFirst().name()).isEqualTo("theme1");
        assertThat(themes.get(1).name()).isEqualTo("theme2");
    }

    @Test
    void 예약이_존재하는_테마를_삭제하면_예외가_발생한다() {
        // given
        Theme theme = themeRepository.save(Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build());
        Member member = Member.builder()
                .id(1L)
                .name("name1")
                .email("email@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        TimeSlot time = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        reservationRepository.save(Reservation.builder()
                .member(member)
                .date(LocalDate.of(2025, 1, 1))
                .timeSlot(time)
                .theme(theme).build());
        // when & then
        assertThatThrownBy(() -> themeService.deleteThemeById(theme.getId()))
                .isInstanceOf(ExistedReservationException.class);
    }

    @Test
    void 테마를_삭제할_수_있다() {
        // given
        Theme theme = themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .description("desc1")
                        .thumbnail("thumb1").build()
        );
        // when
        themeService.deleteThemeById(theme.getId());
        // then
        assertThat(themeRepository.findAll()).hasSize(0);
    }

    @Test
    void 중복된_이름으로_테마를_생성할_수_없다() {
        // given
        themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .description("desc1")
                        .thumbnail("thumb1").build()
        );
        ThemeRequest themeRequest = new ThemeRequest("theme1", "desc2", "thumb2");
        // when & then
        assertThatThrownBy(() -> themeService.createTheme(themeRequest))
                .isInstanceOf(ExistedThemeException.class);
    }


    @Test
    void 인기_테마를_조회한다() {
        // given
        Theme theme1 = themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .description("desc1")
                        .thumbnail("thumb1").build()
        );
        Theme theme2 = themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .description("desc1")
                        .thumbnail("thumb1").build()
        );
        Theme theme3 = themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .description("desc1")
                        .thumbnail("thumb1").build()
        );
        Member member1 = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();

        TimeSlot time1 = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        TimeSlot time2 = TimeSlot.builder()
                .startAt(LocalTime.of(10, 0)).build();
        TimeSlot time3 = TimeSlot.builder()
                .startAt(LocalTime.of(11, 0)).build();

        Reservation reservation1 = Reservation.builder()
                .member(member1)
                .date(LocalDate.now().minusDays(1))
                .timeSlot(time1)
                .theme(theme1).build();
        Reservation reservation2 = Reservation.builder()
                .member(member1)
                .date(LocalDate.now().minusDays(2))
                .timeSlot(time2)
                .theme(theme1).build();
        Reservation reservation3 = Reservation.builder()
                .member(member1)
                .date(LocalDate.now().minusDays(3))
                .timeSlot(time3)
                .theme(theme1).build();
        Reservation reservation4 = Reservation.builder()
                .member(member1)
                .date(LocalDate.now().minusDays(1))
                .timeSlot(time1)
                .theme(theme2).build();
        Reservation reservation5 = Reservation.builder()
                .member(member1)
                .date(LocalDate.now().minusDays(2))
                .timeSlot(time2)
                .theme(theme2).build();
        Reservation reservation6 = Reservation.builder()
                .member(member1)
                .date(LocalDate.now().minusDays(3))
                .timeSlot(time3)
                .theme(theme3).build();

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
