package roomescape.application.reservation.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.reservation.query.dto.ThemeResult;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.domain.reservation.repository.ThemeRepository;

class ThemeQueryServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ThemeQueryService themeQueryService;

    @BeforeEach
    void setUp() {
        themeQueryService = new ThemeQueryService(themeRepository, clock);
    }

    @Test
    void 모든_테마를_조회할_수_있다() {
        // given
        themeRepository.save(new Theme("테마1", "설명1", "image1.png"));
        themeRepository.save(new Theme("테마2", "설명2", "image2.png"));

        // when
        List<ThemeResult> results = themeQueryService.findAll();

        // then
        assertThat(results)
                .extracting(ThemeResult::name)
                .containsExactlyInAnyOrder("테마1", "테마2");
    }

    @Test
    void 테마_예약_랭킹을_조회할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme1 = themeRepository.save(new Theme("테마1", "설명1", "image1.png"));
        Theme theme2 = themeRepository.save(new Theme("테마2", "설명2", "image2.png"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));

        reservationRepository.save(new Reservation(member, LocalDate.now(clock).minusDays(2), reservationTime, theme1));
        reservationRepository.save(new Reservation(member, LocalDate.now(clock).minusDays(3), reservationTime, theme1));
        reservationRepository.save(new Reservation(member, LocalDate.now(clock).minusDays(4), reservationTime, theme2));

        // when
        List<ThemeResult> results = themeQueryService.findWeeklyPopularThemes();

        // then
        assertThat(results).extracting(ThemeResult::name)
                .containsExactly("테마1", "테마2");
    }
}
