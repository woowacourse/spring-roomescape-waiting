package roomescape.theme.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DataExistException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepositoryInterface;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationRepositoryInterface;
import roomescape.reservation.repository.ReservationTimeRepositoryInterface;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepositoryInterface;

@SpringBootTest
@Transactional // 테스트 후 롤백
class ThemeServiceIntegrationTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepositoryInterface themeRepository;

    @Autowired
    private ReservationRepositoryInterface reservationRepository;

    @Autowired
    private ReservationTimeRepositoryInterface reservationTimeRepository;

    @Autowired
    private MemberRepositoryInterface memberRepository;

    @Test
    void 테마를_저장한다() {
        // given
        final String name = "통합 우가";
        final String description = "통합 설명";
        final String thumbnail = "통합썸네일.jpg";

        // when
        final Theme savedTheme = themeService.save(name, description, thumbnail);

        // then
        Assertions.assertThat(savedTheme.getId()).isNotNull();
    }

    @Test
    void 테마를_삭제한다() {
        // given
        final String name = "테마 삭제";
        final String description = "테마 삭제 설명";
        final String thumbnail = "테마삭제썸네일.jpg";

        final Theme savedTheme = themeService.save(name, description, thumbnail);
        final Long themeId = savedTheme.getId();

        // when
        themeService.deleteById(themeId);

        // then
        Assertions.assertThat(themeRepository.findById(themeId)).isEmpty();
    }

    @Test
    void 테마를_조회한다() {
        // given
        final String name = "테마 삭제";
        final String description = "테마 삭제 설명";
        final String thumbnail = "테마삭제썸네일.jpg";

        final Theme savedTheme = themeService.save(name, description, thumbnail);
        final Long themeId = savedTheme.getId();

        // when
        final Theme found = themeService.getById(themeId);

        // then
        Assertions.assertThat(savedTheme).isEqualTo(found);
    }

    @Test
    void 테마_전체를_조회한다() {
        // given
        final String name = "테마 삭제";
        final String description = "테마 삭제 설명";
        final String thumbnail = "테마삭제썸네일.jpg";

        final Theme savedTheme = themeService.save(name, description, thumbnail);

        // when
        final List<Theme> themes = themeService.findAll();

        // then
        Assertions.assertThat(themes).containsOnly(savedTheme);
    }

    @Test
    void 테마_이름은_중복_될_수_없다() {
        // given
        final String name = "테마 삭제";
        final String description = "테마 삭제 설명";
        final String thumbnail = "테마삭제썸네일.jpg";

        final Theme savedTheme = themeService.save(name, description, thumbnail);

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
