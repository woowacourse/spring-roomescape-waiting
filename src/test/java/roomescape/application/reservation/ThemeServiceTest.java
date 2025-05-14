package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
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

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ThemeServiceTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Clock clock = Clock.fixed(Instant.parse("2025-05-08T13:00:00Z"), ZoneId.of("Asia/Seoul"));

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1;");
        themeService = new ThemeService(themeRepository, reservationRepository, clock);
    }

    @DisplayName("테마를 등록할 수 있다")
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

    @DisplayName("등록된 모든 테마를 조회할 수 있다")
    @Test
    void 모든_테마를_조회할_수_있다() {
        // given
        themeRepository.save(new Theme("테마1", "설명1", "image1.png"));
        themeRepository.save(new Theme("테마2", "설명2", "image2.png"));

        // when
        List<ThemeResult> results = themeService.findAll();

        // then
        assertThat(results)
                .extracting(ThemeResult::name)
                .containsExactlyInAnyOrder("테마1", "테마2");
    }

    @DisplayName("테마 id로 조회할 수 있다")
    @Test
    void 테마를_id로_조회할_수_있다() {
        // given
        Theme theme = themeRepository.save(new Theme("테마1", "설명1", "image1.png"));

        // when
        ThemeResult result = themeService.findById(theme.getId());

        // then
        assertThat(result.name()).isEqualTo("테마1");
        assertThat(result.description()).isEqualTo("설명1");
        assertThat(result.thumbnail()).isEqualTo("image1.png");
    }

    @DisplayName("존재하지 않는 테마 조회 시 예외가 발생한다")
    @Test
    void 존재하지_않는_테마는_조회할_수_없다() {
        // given
        Long invalidId = 999L;

        // when & then
        assertThatThrownBy(() -> themeService.findById(invalidId))
                .isInstanceOf(NotFoundEntityException.class)
                .hasMessage("id에 해당하는 Theme이 없습니다.");
    }

    @DisplayName("예약이 없는 테마는 삭제할 수 있다")
    @Test
    void 테마를_삭제할_수_있다() {
        // given
        Theme theme = themeRepository.save(new Theme("테마1", "설명1", "image1.png"));

        // when
        themeService.deleteById(theme.getId());

        // then
        assertThat(themeRepository.findById(theme.getId())).isNotPresent();
    }

    @DisplayName("예약이 있는 테마는 삭제할 수 없다")
    @Test
    void 예약이_존재하는_테마는_삭제할_수_없다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마1", "설명1", "image1.png"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, LocalDate.now(clock), reservationTime, theme));

        // when
        // then
        assertThatThrownBy(() -> themeService.deleteById(theme.getId()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("해당 테마에 예약이 존재합니다.");
    }

    @DisplayName("지난 일주일 간 가장 많이 예약된 테마를 조회할 수 있다")
    @Test
    void 테마_예약_랭킹을_조회할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        Theme theme1 = themeRepository.save(new Theme("테마1", "설명1", "image1.png"));
        Theme theme2 = themeRepository.save(new Theme("테마2", "설명2", "image2.png"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));

        reservationRepository.save(new Reservation(member, LocalDate.now(clock).minusDays(2), reservationTime, theme1));
        reservationRepository.save(new Reservation(member, LocalDate.now(clock).minusDays(3), reservationTime, theme1));
        reservationRepository.save(new Reservation(member, LocalDate.now(clock).minusDays(4), reservationTime, theme2));

        // when
        List<ThemeResult> results = themeService.findRankBetweenDate();

        // then
        assertThat(results).extracting(ThemeResult::name)
                .containsExactly("테마1", "테마2");
    }
}
