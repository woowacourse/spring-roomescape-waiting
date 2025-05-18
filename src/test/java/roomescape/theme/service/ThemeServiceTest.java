package roomescape.theme.service;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.DataExistException;
import roomescape.fake.FakeMemberRepository;
import roomescape.fake.FakeReservationRepository;
import roomescape.fake.FakeReservationTimeRepository;
import roomescape.fake.FakeThemeRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepositoryInterface;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepositoryInterface;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepositoryInterface;

class ThemeServiceTest {

    private final MemberRepositoryInterface memberRepository = new FakeMemberRepository();
    private final ReservationTimeRepositoryInterface reservationTimeRepository = new FakeReservationTimeRepository();
    private final ThemeRepositoryInterface themeRepository = new FakeThemeRepository();
    private final ReservationRepository reservationRepository = new FakeReservationRepository();
    private final ThemeService themeService = new ThemeService(themeRepository, reservationRepository);

    @Test
    void 테마를_저장한다() {
        // given
        final String name = "우가우가";
        final String description = "우가우가 설명";
        final String thumbnail = "따봉우가.jpg";

        // when & then
        Assertions.assertThatCode(() -> {
            themeService.save(name, description, thumbnail);
        }).doesNotThrowAnyException();
    }

    @Test
    void 테마를_삭제한다() {
        // given
        final String name = "우가우가";
        final String description = "우가우가 설명";
        final String thumbnail = "따봉우가.jpg";
        final Theme theme = new Theme(name, description, thumbnail);
        final Theme savedTheme = themeRepository.save(theme);

        // when & then
        Assertions.assertThatCode(() -> {
            themeService.deleteById(savedTheme.getId());
        }).doesNotThrowAnyException();
    }

    @Test
    void 테마를_조회한다() {
        // given
        final String name = "우가우가";
        final String description = "우가우가 설명";
        final String thumbnail = "따봉우가.jpg";
        final Theme theme = new Theme(name, description, thumbnail);
        final Theme savedTheme = themeRepository.save(theme);

        // when
        final Theme found = themeService.getById(savedTheme.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(found.getName()).isEqualTo(name);
            softly.assertThat(found.getDescription()).isEqualTo(description);
            softly.assertThat(found.getThumbnail()).isEqualTo(thumbnail);
        });
    }

    @Test
    void 테마_전체를_조회한다() {
        // when
        final List<Theme> themes = themeService.findAll();

        // then
        Assertions.assertThat(themes).hasSize(0);
    }

    @Test
    void 테마_이름은_중복_될_수_없다() {
        // given
        final String name = "우가우가";
        final String description = "우가우가 설명";
        final String thumbnail = "따봉우가.jpg";
        final Theme theme = new Theme(name, description, thumbnail);
        themeRepository.save(theme);

        // when & then
        Assertions.assertThatThrownBy(() -> {
            themeService.save(name, description, thumbnail);
        }).isInstanceOf(DataExistException.class);
    }

    @Test
    void 인기있는_테마_조회() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        reservationTimeRepository.save(reservationTime);
        final Theme theme1 = new Theme("name1", "description", "thumbnail");
        final Theme theme2 = new Theme("name2", "description", "thumbnail");
        themeRepository.save(theme1);
        themeRepository.save(theme2);
        final Member member = new Member("name", "email@email.com", "password", Role.USER);
        memberRepository.save(member);

        final Reservation inlineReservation = new Reservation(member, LocalDate.now().minusDays(7), reservationTime,
                theme1);
        final Reservation outlineReservation = new Reservation(member, LocalDate.now().plusDays(10), reservationTime,
                theme1);
        final Reservation inlineReservation2 = new Reservation(member, LocalDate.now().minusDays(5), reservationTime,
                theme1);
        final Reservation inlineReservation3 = new Reservation(member, LocalDate.now().minusDays(4), reservationTime,
                theme2);
        final Reservation inlineReservation4 = new Reservation(member, LocalDate.now().minusDays(3), reservationTime,
                theme2);
        final Reservation inlineReservation5 = new Reservation(member, LocalDate.now().minusDays(5), reservationTime,
                theme2);
        reservationRepository.save(inlineReservation);
        reservationRepository.save(outlineReservation);
        reservationRepository.save(inlineReservation2);
        reservationRepository.save(inlineReservation3);
        reservationRepository.save(inlineReservation4);
        reservationRepository.save(inlineReservation5);

        // when
        final List<Theme> popularThemes = themeService.findPopularThemes();

        // then
        Assertions.assertThat(popularThemes.getFirst().getId()).isEqualTo(theme2.getId());
    }
}
