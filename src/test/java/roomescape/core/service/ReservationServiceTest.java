package roomescape.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.core.domain.Member;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Theme;
import roomescape.core.domain.Waiting;
import roomescape.core.dto.member.LoginMember;
import roomescape.core.dto.reservation.ReservationRequest;
import roomescape.core.dto.reservation.ReservationResponse;
import roomescape.core.repository.MemberRepository;
import roomescape.core.repository.ReservationTimeRepository;
import roomescape.core.repository.ThemeRepository;
import roomescape.core.repository.WaitingRepository;
import roomescape.utils.DatabaseCleaner;
import roomescape.utils.TestFixture;

@ServiceTest
class ReservationServiceTest {
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestFixture testFixture;

    @BeforeEach
    void setUp() {
        databaseCleaner.executeTruncate();
        testFixture.initTestData();
    }

    @Test
    @DisplayName("예약을 생성한다.")
    void create() {
        final ReservationRequest request = new ReservationRequest(1L, TestFixture.getTomorrowDate(), 1L, 1L);

        final ReservationResponse response = reservationService.create(request);

        assertAll(
                () -> assertThat(response.getTheme().getId()).isEqualTo(request.getThemeId()),
                () -> assertThat(response.getDate()).isEqualTo(request.getDate()),
                () -> assertThat(response.getMember().getId()).isEqualTo(request.getMemberId()),
                () -> assertThat(response.getTime().getId()).isEqualTo(request.getTimeId())
        );
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 예약을 생성하려고 하면 예외가 발생한다.")
    void createWithInvalidMember() {
        final ReservationRequest request = new ReservationRequest(0L, TestFixture.getTomorrowDate(), 1L, 1L);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ReservationService.MEMBER_NOT_EXISTS_EXCEPTION_MESSAGE);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 예약을 생성하려고 하면 예외가 발생한다.")
    void createWithInvalidTime() {
        final ReservationRequest request = new ReservationRequest(1L, TestFixture.getTomorrowDate(), 0L, 1L);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ReservationService.TIME_NOT_EXISTS_EXCEPTION_MESSAGE);
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약을 생성하려고 하면 예외가 발생한다.")
    void createWithInvalidTheme() {
        final ReservationRequest request = new ReservationRequest(1L, TestFixture.getTomorrowDate(), 1L, 0L);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ReservationService.THEME_NOT_EXISTS_EXCEPTION_MESSAGE);
    }

    @Test
    @DisplayName("이미 예약된 날짜, 시간, 테마로 예약을 생성하려고 하면 예외가 발생한다.")
    void createWithDuplicatedReservation() {
        final ReservationRequest request = new ReservationRequest(1L, TestFixture.getTomorrowDate(), 1L, 1L);
        reservationService.create(request);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ReservationService.ALREADY_BOOKED_TIME_EXCEPTION_MESSAGE);
    }

    @Test
    @DisplayName("예약 목록을 조회한다.")
    void findAll() {
        final ReservationRequest request = new ReservationRequest(1L, TestFixture.getTomorrowDate(), 1L, 1L);
        reservationService.create(request);

        assertThat(reservationService.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("로그인한 회원의 예약 목록을 조회한다.")
    void findAllByMember() {
        final ReservationRequest request = new ReservationRequest(1L, TestFixture.getTomorrowDate(), 1L, 1L);
        final LoginMember loginMember = new LoginMember(1L);
        reservationService.create(request);

        assertThat(reservationService.findAllByMember(loginMember)).hasSize(2);
    }

    @Test
    @DisplayName("특정 회원의 기간 내 예약 목록을 조회한다.")
    void findAllByMemberAndThemeAndPeriod() {
        final ReservationRequest request = new ReservationRequest(1L, TestFixture.getTomorrowDate(), 1L, 1L);
        reservationService.create(request);

        assertThat(reservationService.findAllByMemberAndThemeAndPeriod(1L, 1L, TestFixture.getTodayDate(),
                TestFixture.getTomorrowDate())).hasSize(2);
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void delete() {
        final ReservationRequest request = new ReservationRequest(1L, TestFixture.getTomorrowDate(), 1L, 1L);
        reservationService.create(request);
        final LoginMember loginMember = new LoginMember(1L);

        reservationService.delete(2, loginMember);

        assertThat(reservationService.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("예약을 삭제할 때, 존재하지 않는 예약이면 예외가 발생한다.")
    void deleteWithInvalidReservation() {
        final LoginMember loginMember = new LoginMember(1L);

        assertThatThrownBy(() -> reservationService.delete(0L, loginMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ReservationService.RESERVATION_NOT_EXISTS_EXCEPTION_MESSAGE);
    }

    @Test
    @DisplayName("예약을 삭제한 후, 해당 날짜, 시간, 테마에 예약 대기가 있다면 자동으로 예약으로 전환한다.")
    void deleteAndAcceptWaiting() {
        final ReservationRequest request = new ReservationRequest(1L, TestFixture.getTomorrowDate(), 1L, 1L);
        final ReservationResponse response = reservationService.create(request);
        saveWaiting(TestFixture.getTomorrowDate());
        final LoginMember loginMember = new LoginMember(1L);

        reservationService.delete(response.getId(), loginMember);

        assertThat(reservationService.findAll()).hasSize(2);
        assertThat(waitingRepository.findAll()).isEmpty();
    }

    private void saveWaiting(final String date) {
        final Member member = memberRepository.findById(1L).orElseThrow();
        final ReservationTime time = reservationTimeRepository.findById(1L).orElseThrow();
        final Theme theme = themeRepository.findById(1L).orElseThrow();

        waitingRepository.save(new Waiting(member, date, time, theme));
    }
}