package roomescape.theme.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepositoryInterface;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationRepositoryInterface;
import roomescape.reservation.repository.ReservationTimeRepositoryInterface;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepositoryInterface;
import roomescape.theme.service.facade.ThemeServiceFacade;

@SpringBootTest
@Transactional
class ThemeServiceFacadeTest {

    @Autowired
    private ThemeServiceFacade themeServiceFacade;

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

        final ThemeRequest themeRequest = new ThemeRequest(name, description, thumbnail);

        // when
        final ThemeResponse themeResponse = themeServiceFacade.saveTheme(themeRequest);

        // then
        SoftAssertions.assertSoftly(
                softly -> {
                    softly.assertThat(themeResponse.id()).isNotNull();
                    softly.assertThat(themeResponse.name()).isEqualTo(name);
                    softly.assertThat(themeResponse.description()).isEqualTo(description);
                    softly.assertThat(themeResponse.thumbnail()).isEqualTo(thumbnail);
                }
        );
    }

    @Test
    void 테마를_삭제한다() {
        // given
        final String name = "통합 우가";
        final String description = "통합 설명";
        final String thumbnail = "통합썸네일.jpg";

        final Theme theme = new Theme(name, description, thumbnail);
        final Theme savedTheme = themeRepository.save(theme);

        final Long themeId = savedTheme.getId();

        // when & then
        Assertions.assertThatCode(() -> themeServiceFacade.deleteTheme(themeId)).doesNotThrowAnyException();
    }

    @Test
    void 테마_전체를_조회한다() {
        // given
        final String name = "통합 우가";
        final String description = "통합 설명";
        final String thumbnail = "통합썸네일.jpg";

        final Theme theme = new Theme(name, description, thumbnail);
        final Theme savedTheme = themeRepository.save(theme);

        // when
        final List<ThemeResponse> themes = themeServiceFacade.getThemes();

        // then
        SoftAssertions.assertSoftly(
                softly -> {
                    softly.assertThat(themes).hasSize(1);
                    softly.assertThat(themes.getFirst().id()).isEqualTo(savedTheme.getId());
                    softly.assertThat(themes.getFirst().name()).isEqualTo(name);
                    softly.assertThat(themes.getFirst().description()).isEqualTo(description);
                    softly.assertThat(themes.getFirst().thumbnail()).isEqualTo(thumbnail);
                }
        );
    }

    @Test
    void 인기있는_테마_조회() {
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
        final List<ThemeResponse> popularThemes = themeServiceFacade.getPopularThemes();

        // then
        SoftAssertions.assertSoftly(
                softly -> {
                    softly.assertThat(popularThemes).hasSize(2);
                    softly.assertThat(popularThemes.getFirst().id()).isEqualTo(theme2.getId());
                    softly.assertThat(popularThemes.getLast().id()).isEqualTo(theme1.getId());
                }
        );
    }
}
