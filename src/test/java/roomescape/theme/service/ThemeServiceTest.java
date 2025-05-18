package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import roomescape.config.TestConfig;
import roomescape.global.auth.service.MyPasswordEncoder;
import roomescape.member.domain.Member;
import roomescape.member.dto.request.SignupRequest;
import roomescape.member.dto.response.SignUpResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberService;
import roomescape.reservation.fixture.TestFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations="
})
class ThemeServiceTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MyPasswordEncoder myPasswordEncoder;

    @Autowired
    private MemberRepository memberRepository;

    private ThemeService themeService;

    private MemberService memberService;

    private ReservationService reservationService;

    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(themeRepository, reservationRepository);
        memberService = new MemberService(memberRepository, myPasswordEncoder);
        reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository);
        reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeRepository,
                memberRepository);
    }

    @Test
    void delete() {
        ThemeResponse themeResponse = themeService.create(
                new ThemeCreateRequest("논리", "논리 게임 with Vector", "image.png"));
        themeService.delete(themeResponse.id());
        assertThat(themeService.getThemes().size()).isEqualTo(0);
    }

    @Test
    void create() {
        ThemeResponse themeResponse = themeService.create(
                new ThemeCreateRequest("논리", "논리 게임 with Vector", "image.png"));

        Optional<Theme> optionalTheme = themeRepository.findById(themeResponse.id());
        assertThat(optionalTheme.get().getName()).isEqualTo("논리");
    }

    @Test
    void getTop10PopularThemesLastWeek() {
        String email = "user2@gmail.com";
        String password = "password";
        String name = "user2";
        SignUpResponse signUpResponse = memberService.signup(new SignupRequest(email, password, name));
        Member member = memberRepository.findById(signUpResponse.id()).get();

        ThemeResponse themeResponse1 = themeService.create(
                new ThemeCreateRequest("추리", "셜록 홈즈: 실종된 보석의 비밀", "sherlock_jewel.png"));
        ThemeResponse themeResponse2 = themeService.create(
                new ThemeCreateRequest("논리", "양자 코드: 암호 해독 게임", "quantum_code.png"));

        ThemeResponse themeResponse3 = themeService.create(
                new ThemeCreateRequest("공포", "저주받은 병원: 13호실의 비밀", "cursed_hospital.png"));
        ThemeResponse themeResponse4 = themeService.create(
                new ThemeCreateRequest("모험", "잃어버린 문명: 마야의 유산", "maya_civilization.png"));

        ThemeResponse themeResponse5 = themeService.create(
                new ThemeCreateRequest("판타지", "드래곤의 동굴: 마법의 보물", "dragon_cave.png"));
        ThemeResponse themeResponse6 = themeService.create(
                new ThemeCreateRequest("역사", "타임머신: 조선 왕조의 비밀", "joseon_dynasty.png"));

        ThemeResponse themeResponse7 = themeService.create(
                new ThemeCreateRequest("SF", "화성 기지: 레드 플래닛의 음모", "mars_base.png"));
        ThemeResponse themeResponse8 = themeService.create(
                new ThemeCreateRequest("스릴러", "연쇄 살인마의 게임", "serial_killer_game.png"));

        ThemeResponse themeResponse9 = themeService.create(
                new ThemeCreateRequest("코미디", "유머 수사대: 웃음을 찾아서", "humor_detective.png"));

        ThemeResponse themeResponse10 = themeService.create(
                new ThemeCreateRequest("액션", "비밀 요원: 마지막 미션", "secret_agent.png"));
        ThemeResponse themeResponse11 = themeService.create(
                new ThemeCreateRequest("신비", "신화의 미궁: 그리스 신들의 시험", "greek_gods.png"));
        ThemeResponse themeResponse12 = themeService.create(
                new ThemeCreateRequest("음악", "멜로디 탐정: 잃어버린 노래", "lost_melody.png"));

        ReservationTimeResponse reservationTime1 = reservationTimeService.create(
                new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
        ReservationTimeResponse reservationTime2 = reservationTimeService.create(
                new ReservationTimeCreateRequest(LocalTime.of(11, 0)));

        LocalDate nowDate = LocalDate.now();
        reservationRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(1), makeReservationTime(reservationTime1), member,
                        makeTheme(themeResponse1)));

        reservationRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(2), makeReservationTime(reservationTime1), member,
                        makeTheme(themeResponse2)));
        reservationRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(2), makeReservationTime(reservationTime2), member,
                        makeTheme(themeResponse2)));

        reservationRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(3), makeReservationTime(reservationTime1), member,
                        makeTheme(themeResponse4)));
        reservationRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(3), makeReservationTime(reservationTime2), member,
                        makeTheme(themeResponse5)));
        reservationRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(4), makeReservationTime(reservationTime1), member,
                        makeTheme(themeResponse6)));
        reservationRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(4), makeReservationTime(reservationTime2), member,
                        makeTheme(themeResponse7)));
        reservationRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(5), makeReservationTime(reservationTime1), member,
                        makeTheme(themeResponse8)));
        reservationRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(5), makeReservationTime(reservationTime2), member,
                        makeTheme(themeResponse9)));
        reservationRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(6), makeReservationTime(reservationTime1), member,
                        makeTheme(themeResponse10)));
        reservationRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(6), makeReservationTime(reservationTime2), member,
                        makeTheme(themeResponse11)));

        List<ThemeResponse> popularThemes = themeService.getTop10PopularThemesLastWeek();

        Assertions.assertAll(
                () -> assertThat(popularThemes.size()).isEqualTo(10),
                () -> assertThat(popularThemes).doesNotContain(themeResponse3, themeResponse12),
                () -> assertThat(popularThemes.getFirst()).isEqualTo(themeResponse2)
        );
    }

    private ReservationTime makeReservationTime(final ReservationTimeResponse reservationTimeResponse) {
        return new ReservationTime(reservationTimeResponse.id(), reservationTimeResponse.startAt());
    }

    private Theme makeTheme(final ThemeResponse themeResponse) {
        return new Theme(themeResponse.id(), themeResponse.name(), themeResponse.description(),
                themeResponse.thumbnail());
    }
}
