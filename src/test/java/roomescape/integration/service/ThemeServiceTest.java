package roomescape.integration.service;

import static org.assertj.core.api.Assertions.*;
import static roomescape.common.Constant.FIXED_CLOCK;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.ClockConfig;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.domain.time.ReservationTime;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.integration.fixture.ReservationDateFixture;
import roomescape.integration.fixture.ReservationDbFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.ThemeService;
import roomescape.service.request.CreateThemeRequest;
import roomescape.service.response.ThemeResponse;

@Transactional
@SpringBootTest
@Import(ClockConfig.class)
class ThemeServiceTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Test
    void 테마를_생성할_수_있다() {
        // given
        CreateThemeRequest request = new CreateThemeRequest("공포", "무섭다", "thumb.jpg");

        // when
        ThemeResponse response = themeService.createTheme(request);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.name()).isEqualTo("공포");
            softly.assertThat(response.description()).isEqualTo("무섭다");
            softly.assertThat(response.thumbnail()).isEqualTo("thumb.jpg");
        });
    }

    @Test
    void 모든_테마를_조회할_수_있다() {
        // given
        themeService.createTheme(new CreateThemeRequest("공포", "무섭다", "thumb.jpg"));
        themeService.createTheme(new CreateThemeRequest("로맨스", "달달하다", "love.jpg"));

        // when
        List<ThemeResponse> result = themeService.findAllThemes();

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 예약이_없는_테마는_삭제할_수_있다() {
        // given
        ThemeResponse saved = themeService.createTheme(new CreateThemeRequest("공포", "무섭다", "thumb.jpg"));

        // when & then
        assertThatCode(() -> themeService.deleteThemeById(saved.id()))
                .doesNotThrowAnyException();

        assertThat(themeRepository.findById(saved.id())).isEmpty();
    }

    @Test
    void 예약이_있는_테마는_삭제할_수_없다() {
        // given
        Theme theme = themeRepository.save(new Theme(
                        null,
                        new ThemeName("공포"),
                        new ThemeDescription("공포입니다."),
                        new ThemeThumbnail("썸네일")
                )
        );
        ReservationTime time = reservationTimeRepository.save(
                new ReservationTime(null, LocalTime.of(10, 0)));
        ReservationDateTime reservationDateTime = new ReservationDateTime(
                new ReservationDate(LocalDate.of(2025, 5, 5)), time, FIXED_CLOCK
        );
        Member member = memberRepository.save(new Member(
                null,
                new MemberName("한스"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword("dsa"),
                MemberRole.MEMBER
        ));

        Reservation reservation = reservationRepository.save(new Reservation(
                null,
                member,
                reservationDateTime.getReservationDate(),
                reservationDateTime.getReservationTime(),
                theme
        ));

        // when & then
        assertThatThrownBy(() -> themeService.deleteThemeById(theme.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 존재하지_않는_테마는_삭제할_수_없다() {
        // when & then
        assertThatThrownBy(() -> themeService.deleteThemeById(999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 최근_일주일_인기_테마를_조회할_수_있다() {  // ?? 이거 예약 없이 getWeeklyPopularThemes가 돌아가었던 건가?? 나머지는 다 고쳐놨는디 이거는 한번 같이 봐야할듯! - 머피
        // given
        Theme 공포 = themeDbFixture.공포();
        Theme 로맨스 = themeDbFixture.로맨스();
        Member 한스 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        ReservationDate 예약날짜_7일전 = ReservationDateFixture.예약날짜_7일전;
        Reservation 공포_예약 = reservationDbFixture.예약_생성(예약날짜_7일전, reservationTime, 공포, 한스);
        Reservation 로맨스_예약 = reservationDbFixture.예약_생성(예약날짜_7일전, reservationTime, 로맨스, 한스);

        // when
        List<ThemeResponse> result = themeService.getWeeklyPopularThemes();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result.get(0).name()).isEqualTo("공포");
        });
    }
}
