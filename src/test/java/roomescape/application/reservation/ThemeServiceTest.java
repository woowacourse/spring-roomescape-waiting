package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.reservation.dto.CreateThemeParam;
import roomescape.application.reservation.dto.ThemeResult;
import roomescape.application.support.exception.NotFoundEntityException;
import roomescape.domain.BusinessRuleViolationException;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

class ThemeServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(themeRepository, reservationRepository, clock);
    }

    @Test
    void 테마를_생성할_수_있다() {
        // given
        CreateThemeParam param = new CreateThemeParam("방탈출", "재밌는 방", "image.png");

        // when
        Long id = themeService.create(param);

        // then
        assertThat(themeRepository.findById(id))
                .isPresent()
                .hasValueSatisfying(theme -> {
                    assertThat(theme.getName()).isEqualTo("방탈출");
                    assertThat(theme.getDescription()).isEqualTo("재밌는 방");
                    assertThat(theme.getThumbnail()).isEqualTo("image.png");
                });
    }

    @Test
    void 같은_이름의_테마를_생성할_경우_예외가_발생한다() {
        // given
        themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        CreateThemeParam param = new CreateThemeParam("테마1", "재밌는 방", "image.png");

        // when
        // then
        assertThatCode(() -> themeService.create(param))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("이미 같은 이름의 테마가 존재합니다.");
    }

    @Test
    void 모든_테마를_조회할_수_있다() {
        // given
        themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));

        // when
        List<ThemeResult> results = themeService.findAll();

        // then
        assertThat(results)
                .extracting(ThemeResult::name)
                .containsExactlyInAnyOrder("테마1", "테마2");
    }

    @Test
    void 테마를_id로_조회할_수_있다() {
        // given
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));

        // when
        ThemeResult result = themeService.findById(theme.getId());

        // then
        assertThat(result.name()).isEqualTo("테마1");
        assertThat(result.description()).isEqualTo("설명1");
        assertThat(result.thumbnail()).isEqualTo("image1.png");
    }

    @Test
    void 존재하지_않는_테마는_조회할_수_없다() {
        // given
        Long invalidId = 999L;

        // when & then
        assertThatThrownBy(() -> themeService.findById(invalidId))
                .isInstanceOf(NotFoundEntityException.class)
                .hasMessage("999에 해당하는 theme 튜플이 없습니다.");
    }

    @Test
    void 테마를_삭제할_수_있다() {
        // given
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));

        // when
        themeService.deleteById(theme.getId());

        // then
        assertThat(themeRepository.findById(theme.getId())).isNotPresent();
    }

    @Test
    void 예약이_존재하는_테마는_삭제할_수_없다() {
        // given
        Member member = memberRepository.save(Member.create("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(13, 0)));
        reservationRepository.save(Reservation.create(member, LocalDate.now(clock), reservationTime, theme));

        // when
        // then
        assertThatThrownBy(() -> themeService.deleteById(theme.getId()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("해당 테마에 예약이 존재합니다.");
    }

    @Test
    void 테마_예약_랭킹을_조회할_수_있다() {
        // given
        Member member = memberRepository.save(Member.create("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        Theme theme1 = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        Theme theme2 = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(13, 0)));

        reservationRepository.save(Reservation.create(member, LocalDate.now(clock).minusDays(2), reservationTime, theme1));
        reservationRepository.save(Reservation.create(member, LocalDate.now(clock).minusDays(3), reservationTime, theme1));
        reservationRepository.save(Reservation.create(member, LocalDate.now(clock).minusDays(4), reservationTime, theme2));

        // when
        List<ThemeResult> results = themeService.findRankBetweenDate();

        // then
        assertThat(results).extracting(ThemeResult::name)
                .containsExactly("테마1", "테마2");
    }
}
