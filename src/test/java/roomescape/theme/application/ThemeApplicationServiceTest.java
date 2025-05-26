package roomescape.theme.application;

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
import roomescape.common.security.application.MyPasswordEncoder;
import roomescape.common.config.TestConfig;
import roomescape.fixture.TestFixture;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.member.presentation.dto.request.SignupRequest;
import roomescape.member.presentation.dto.response.SignUpResponse;
import roomescape.member.application.MemberApplicationService;
import roomescape.member.application.MemberDataService;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationslot.infrastructure.ReservationSlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.reservationtime.presentation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.application.ReservationTimeApplicationService;
import roomescape.reservationtime.application.ReservationTimeDataService;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeRepository;
import roomescape.theme.presentation.dto.request.ThemeCreateRequest;
import roomescape.theme.presentation.dto.response.ThemeResponse;

@DataJpaTest
@Import(TestConfig.class)
class ThemeApplicationServiceTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MyPasswordEncoder myPasswordEncoder;

    @Autowired
    private MemberRepository memberRepository;

    private ThemeApplicationService themeApplicationService;

    private MemberApplicationService memberApplicationService;

    private ReservationTimeApplicationService reservationTimeApplicationService;

    @BeforeEach
    void setUp() {
        ReservationSlotDataService reservationSlotDataService = new ReservationSlotDataService(reservationSlotRepository);
        themeApplicationService = new ThemeApplicationService(new ThemeDataService(themeRepository,
                reservationSlotRepository),
                reservationSlotDataService);
        MemberDataService memberDataService = new MemberDataService(memberRepository);
        memberApplicationService = new MemberApplicationService(memberDataService, myPasswordEncoder);
        reservationTimeApplicationService = new ReservationTimeApplicationService(new ReservationTimeDataService(reservationTimeRepository,
                reservationSlotDataService), reservationSlotDataService);
    }

    @Test
    void delete() {
        ThemeResponse themeResponse = themeApplicationService.create(
                new ThemeCreateRequest("논리", "논리 게임 with Vector", "image.png"));
        themeApplicationService.delete(themeResponse.id());
        assertThat(themeApplicationService.getThemes().size()).isEqualTo(0);
    }

    @Test
    void create() {
        ThemeResponse themeResponse = themeApplicationService.create(
                new ThemeCreateRequest("논리", "논리 게임 with Vector", "image.png"));

        Optional<Theme> optionalTheme = themeRepository.findById(themeResponse.id());
        assertThat(optionalTheme.get().getName()).isEqualTo("논리");
    }

    @Test
    void getPopularThemes() {
        String email = "regular2@gmail.com";
        String password = "password";
        String name = "regular2";
        SignUpResponse signUpResponse = memberApplicationService.signup(new SignupRequest(email, password, name));
        Member member = memberRepository.findById(signUpResponse.id()).get();

        ThemeResponse themeResponse1 = themeApplicationService.create(
                new ThemeCreateRequest("추리", "셜록 홈즈: 실종된 보석의 비밀", "sherlock_jewel.png"));
        ThemeResponse themeResponse2 = themeApplicationService.create(
                new ThemeCreateRequest("논리", "양자 코드: 암호 해독 게임", "quantum_code.png"));

        ThemeResponse themeResponse3 = themeApplicationService.create(
                new ThemeCreateRequest("공포", "저주받은 병원: 13호실의 비밀", "cursed_hospital.png"));
        ThemeResponse themeResponse4 = themeApplicationService.create(
                new ThemeCreateRequest("모험", "잃어버린 문명: 마야의 유산", "maya_civilization.png"));

        ThemeResponse themeResponse5 = themeApplicationService.create(
                new ThemeCreateRequest("판타지", "드래곤의 동굴: 마법의 보물", "dragon_cave.png"));
        ThemeResponse themeResponse6 = themeApplicationService.create(
                new ThemeCreateRequest("역사", "타임머신: 조선 왕조의 비밀", "joseon_dynasty.png"));

        ThemeResponse themeResponse7 = themeApplicationService.create(
                new ThemeCreateRequest("SF", "화성 기지: 레드 플래닛의 음모", "mars_base.png"));
        ThemeResponse themeResponse8 = themeApplicationService.create(
                new ThemeCreateRequest("스릴러", "연쇄 살인마의 게임", "serial_killer_game.png"));

        ThemeResponse themeResponse9 = themeApplicationService.create(
                new ThemeCreateRequest("코미디", "유머 수사대: 웃음을 찾아서", "humor_detective.png"));

        ThemeResponse themeResponse10 = themeApplicationService.create(
                new ThemeCreateRequest("액션", "비밀 요원: 마지막 미션", "secret_agent.png"));
        ThemeResponse themeResponse11 = themeApplicationService.create(
                new ThemeCreateRequest("신비", "신화의 미궁: 그리스 신들의 시험", "greek_gods.png"));
        ThemeResponse themeResponse12 = themeApplicationService.create(
                new ThemeCreateRequest("음악", "멜로디 탐정: 잃어버린 노래", "lost_melody.png"));

        ReservationTimeResponse reservationTime1 = reservationTimeApplicationService.create(
                new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
        ReservationTimeResponse reservationTime2 = reservationTimeApplicationService.create(
                new ReservationTimeCreateRequest(LocalTime.of(11, 0)));

        LocalDate nowDate = LocalDate.now();
        reservationSlotRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(1), makeReservationTime(reservationTime1), member,
                        makeTheme(themeResponse1)));

        reservationSlotRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(2), makeReservationTime(reservationTime1), member,
                        makeTheme(themeResponse2)));
        reservationSlotRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(2), makeReservationTime(reservationTime2), member,
                        makeTheme(themeResponse2)));

        reservationSlotRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(3), makeReservationTime(reservationTime1), member,
                        makeTheme(themeResponse4)));
        reservationSlotRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(3), makeReservationTime(reservationTime2), member,
                        makeTheme(themeResponse5)));
        reservationSlotRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(4), makeReservationTime(reservationTime1), member,
                        makeTheme(themeResponse6)));
        reservationSlotRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(4), makeReservationTime(reservationTime2), member,
                        makeTheme(themeResponse7)));
        reservationSlotRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(5), makeReservationTime(reservationTime1), member,
                        makeTheme(themeResponse8)));
        reservationSlotRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(5), makeReservationTime(reservationTime2), member,
                        makeTheme(themeResponse9)));
        reservationSlotRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(6), makeReservationTime(reservationTime1), member,
                        makeTheme(themeResponse10)));
        reservationSlotRepository.save(
                TestFixture.makeReservation(nowDate.minusDays(6), makeReservationTime(reservationTime2), member,
                        makeTheme(themeResponse11)));

        List<ThemeResponse> themes = themeApplicationService.getPopularThemes(7, 10);

        Assertions.assertAll(
                () -> assertThat(themes.size()).isEqualTo(10),
                () -> assertThat(themes).doesNotContain(themeResponse3, themeResponse12),
                () -> assertThat(themes.getFirst()).isEqualTo(themeResponse2)
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
