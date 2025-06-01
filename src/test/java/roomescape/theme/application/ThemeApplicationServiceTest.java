package roomescape.theme.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.common.config.TestConfig;
import roomescape.common.security.application.MyPasswordEncoder;
import roomescape.fixture.TestFixture;
import roomescape.member.application.MemberApplicationService;
import roomescape.member.application.MemberDataService;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.member.presentation.dto.request.SignupWebRequest;
import roomescape.member.presentation.dto.response.SignUpWebResponse;
import roomescape.reservation.application.ConfirmedReservationApplicationService;
import roomescape.reservation.application.ReservationDataService;
import roomescape.reservation.application.dto.request.ConfirmedReservationCreateRequest;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationslot.infrastructure.ReservationSlotRepository;
import roomescape.reservationtime.application.ReservationTimeApplicationService;
import roomescape.reservationtime.application.ReservationTimeDataService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.exception.ReservationTimeInUseException;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.reservationtime.presentation.dto.request.ReservationTimeCreateWebRequest;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeWebResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeRepository;
import roomescape.theme.presentation.dto.request.ThemeCreateWebRequest;
import roomescape.theme.presentation.dto.response.ThemeWebResponse;

@DataJpaTest
@Import(TestConfig.class)
class ThemeApplicationServiceTest {

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

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

    private ThemeDataService themeDataService;

    private ReservationTimeDataService reservationTimeDataService;

    private ReservationDataService reservationDataService;

    private ThemeApplicationService themeApplicationService;

    private MemberApplicationService memberApplicationService;

    private ReservationTimeApplicationService reservationTimeApplicationService;

    private ConfirmedReservationApplicationService confirmedReservationApplicationService;


    @BeforeEach
    void setUp() {
        ReservationSlotDataService reservationSlotDataService = new ReservationSlotDataService(
                reservationSlotRepository);
        Clock clock = Clock.fixed(FUTURE_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
        themeDataService = new ThemeDataService(themeRepository);
        themeApplicationService = new ThemeApplicationService(themeDataService, reservationSlotDataService, clock);
        MemberDataService memberDataService = new MemberDataService(memberRepository);
        memberApplicationService = new MemberApplicationService(memberDataService, myPasswordEncoder);
        reservationTimeApplicationService = new ReservationTimeApplicationService(
                new ReservationTimeDataService(reservationTimeRepository,
                        reservationSlotDataService));
        reservationTimeDataService = new ReservationTimeDataService(reservationTimeRepository,
                reservationSlotDataService);
        confirmedReservationApplicationService = new ConfirmedReservationApplicationService(reservationSlotDataService,
                reservationTimeDataService, themeDataService, memberDataService, reservationDataService);
    }

    @Test
    void delete_whenValidRequest_removesSuccessfully() {
        // given
        ThemeWebResponse themeWebResponse = themeApplicationService.create(
                new ThemeCreateWebRequest("논리", "논리 게임 with Vector", "image.png"));

        // when
        themeApplicationService.delete(themeWebResponse.id());

        // then
        assertThat(themeApplicationService.findAll().size()).isEqualTo(0);
    }

    @Test
    void delete_throwsException_removesSuccessfully() {
        // given
        LocalTime now = LocalTime.now();
        ReservationTimeWebResponse reservationTimeWebResponse = reservationTimeApplicationService.create(
                new ReservationTimeCreateWebRequest(now));
        ThemeWebResponse themeWebResponse = themeApplicationService.create(
                new ThemeCreateWebRequest("논리", "논리 게임 with Vector", "image.png"));
        SignUpWebResponse signup = memberApplicationService.signup(new SignupWebRequest("Mint", "password", "mint"));
        confirmedReservationApplicationService.create(
                new ConfirmedReservationCreateRequest(FUTURE_DATE, reservationTimeWebResponse.id(),
                        themeWebResponse.id(), signup.id(), LocalDateTime.now()));

        // when & then
        Assertions.assertThatThrownBy(() -> themeApplicationService.delete(themeWebResponse.id()))
                .isInstanceOf(ReservationTimeInUseException.class)
                .hasMessageContaining("해당 테마에 대한 예약이 존재하여 삭제할 수 없습니다.");
    }

    @Test
    void create_whenValidRequest_findSuccessfully() {
        ThemeWebResponse themeWebResponse = themeApplicationService.create(
                new ThemeCreateWebRequest("논리", "논리 게임 with Vector", "image.png"));

        Optional<Theme> optionalTheme = themeRepository.findById(themeWebResponse.id());
        assertThat(optionalTheme.get().getName()).isEqualTo("논리");
    }

    @Test
    void findPopular_whenValidRequest_findSuccessfully() {
        String email = "regular2@gmail.com";
        String password = "password";
        String name = "regular2";
        SignUpWebResponse signUpWebResponse = memberApplicationService.signup(
                new SignupWebRequest(email, password, name));
        Member member = memberRepository.findById(signUpWebResponse.id()).get();

        ThemeWebResponse themeWebResponse1 = themeApplicationService.create(
                new ThemeCreateWebRequest("추리", "셜록 홈즈: 실종된 보석의 비밀", "sherlock_jewel.png"));
        ThemeWebResponse themeWebResponse2 = themeApplicationService.create(
                new ThemeCreateWebRequest("논리", "양자 코드: 암호 해독 게임", "quantum_code.png"));

        ThemeWebResponse themeWebResponse3 = themeApplicationService.create(
                new ThemeCreateWebRequest("공포", "저주받은 병원: 13호실의 비밀", "cursed_hospital.png"));
        ThemeWebResponse themeWebResponse4 = themeApplicationService.create(
                new ThemeCreateWebRequest("모험", "잃어버린 문명: 마야의 유산", "maya_civilization.png"));

        ThemeWebResponse themeWebResponse5 = themeApplicationService.create(
                new ThemeCreateWebRequest("판타지", "드래곤의 동굴: 마법의 보물", "dragon_cave.png"));
        ThemeWebResponse themeWebResponse6 = themeApplicationService.create(
                new ThemeCreateWebRequest("역사", "타임머신: 조선 왕조의 비밀", "joseon_dynasty.png"));

        ThemeWebResponse themeWebResponse7 = themeApplicationService.create(
                new ThemeCreateWebRequest("SF", "화성 기지: 레드 플래닛의 음모", "mars_base.png"));
        ThemeWebResponse themeWebResponse8 = themeApplicationService.create(
                new ThemeCreateWebRequest("스릴러", "연쇄 살인마의 게임", "serial_killer_game.png"));

        ThemeWebResponse themeWebResponse9 = themeApplicationService.create(
                new ThemeCreateWebRequest("코미디", "유머 수사대: 웃음을 찾아서", "humor_detective.png"));

        ThemeWebResponse themeWebResponse10 = themeApplicationService.create(
                new ThemeCreateWebRequest("액션", "비밀 요원: 마지막 미션", "secret_agent.png"));
        ThemeWebResponse themeWebResponse11 = themeApplicationService.create(
                new ThemeCreateWebRequest("신비", "신화의 미궁: 그리스 신들의 시험", "greek_gods.png"));
        ThemeWebResponse themeWebResponse12 = themeApplicationService.create(
                new ThemeCreateWebRequest("음악", "멜로디 탐정: 잃어버린 노래", "lost_melody.png"));

        ReservationTimeWebResponse reservationTimeWebResponse1 = reservationTimeApplicationService.create(
                new ReservationTimeCreateWebRequest(LocalTime.of(10, 0)));
        ReservationTimeWebResponse reservationTimeWebResponse2 = reservationTimeApplicationService.create(
                new ReservationTimeCreateWebRequest(LocalTime.of(11, 0)));

        ReservationTime savedReservationTime = findReservationTime(reservationTimeWebResponse1);
        ReservationTime savedReservationTime2 = findReservationTime(reservationTimeWebResponse2);
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE.minusDays(1), savedReservationTime,
                        member,
                        findTheme(themeWebResponse1)));

        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE.minusDays(2), savedReservationTime,
                        member,
                        findTheme(themeWebResponse2)));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE.minusDays(2), savedReservationTime2,
                        member,
                        findTheme(themeWebResponse2)));

        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE.minusDays(3), savedReservationTime,
                        member,
                        findTheme(themeWebResponse4)));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE.minusDays(3), savedReservationTime2,
                        member,
                        findTheme(themeWebResponse5)));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE.minusDays(4), savedReservationTime,
                        member,
                        findTheme(themeWebResponse6)));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE.minusDays(4), savedReservationTime2,
                        member,
                        findTheme(themeWebResponse7)));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE.minusDays(5), savedReservationTime,
                        member,
                        findTheme(themeWebResponse8)));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE.minusDays(5), savedReservationTime2,
                        member,
                        findTheme(themeWebResponse9)));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE.minusDays(6), savedReservationTime,
                        member,
                        findTheme(themeWebResponse10)));
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE.minusDays(6), savedReservationTime2,
                        member,
                        findTheme(themeWebResponse11)));

        List<ThemeWebResponse> themes = themeApplicationService.findPopular(7, 10);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(themes.size()).isEqualTo(10);
            softAssertions.assertThat(themes).doesNotContain(themeWebResponse3, themeWebResponse12);
            softAssertions.assertThat(themes.getFirst()).isEqualTo(themeWebResponse2);
        });
    }

    private ReservationTime findReservationTime(final ReservationTimeWebResponse reservationTimeWebResponse) {
        return reservationTimeRepository.findById(reservationTimeWebResponse.id()).orElseThrow();
    }

    private Theme findTheme(final ThemeWebResponse themeWebResponse) {
        return themeRepository.findById(themeWebResponse.id()).orElseThrow();
    }
}
