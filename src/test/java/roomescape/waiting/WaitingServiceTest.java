package roomescape.waiting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.auth.AuthorizationException;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.member.MemberRole;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.reservation.ReservationStatus;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.waiting.dto.WaitingRequest;
import roomescape.waiting.dto.WaitingResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static roomescape.util.TestFactory.*;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    private WaitingService waitingService;
    private WaitingRepository waitingRepository;
    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;

    private WaitingRequest REQUEST;
    private LoginMember LOGIN_MEMBER;
    private Optional<ReservationTime> RESERVATION_TIME;
    private Optional<Theme> THEME;
    private Optional<Member> MEMBER;
    private Reservation RESERVATION;

    @BeforeEach
    void setUp() {
        REQUEST = new WaitingRequest(LocalDate.now().plusDays(1), 1L, 1L);
        LOGIN_MEMBER = new LoginMember("boogie", "asd@email.com", MemberRole.MEMBER);
        RESERVATION_TIME = Optional.of(reservationTimeWithId(REQUEST.timeId(), new ReservationTime(LocalTime.of(12, 40))));
        THEME = Optional.of(themeWithId(REQUEST.themeId(), new Theme("야당", "야당당", "123")));
        MEMBER = Optional.of(memberWithId(1L, new Member(LOGIN_MEMBER.email(), "password", "boogie", MemberRole.MEMBER)));
        RESERVATION = reservationWithId(1L, new Reservation(
                REQUEST.date(),
                MEMBER.get(),
                RESERVATION_TIME.get(),
                THEME.get(),
                ReservationStatus.PENDING
        ));

        waitingRepository = mock(WaitingRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        reservationTimeRepository = mock(ReservationTimeRepository.class);
        themeRepository = mock(ThemeRepository.class);
        memberRepository = mock(MemberRepository.class);
        waitingService = new WaitingService(waitingRepository, reservationRepository, reservationTimeRepository, themeRepository, memberRepository);
    }

    @Test
    @DisplayName("웨이팅을 할 수 있다.")
    void createWaiting() {
        // given
        given(reservationTimeRepository.findById(REQUEST.timeId()))
                .willReturn(RESERVATION_TIME);
        given(themeRepository.findById(REQUEST.themeId()))
                .willReturn(THEME);
        given(memberRepository.findByEmail(LOGIN_MEMBER.email()))
                .willReturn(MEMBER);
        given(reservationRepository.save(new Reservation(REQUEST.date(), MEMBER.get(), RESERVATION_TIME.get(), THEME.get(), ReservationStatus.PENDING)))
                .willReturn(RESERVATION);
        List<Reservation> waitings = List.of(RESERVATION, RESERVATION, RESERVATION);
        given(reservationRepository.findAllByDateAndReservationTimeAndThemeAndReservationStatus(REQUEST.date(), RESERVATION_TIME.get(), THEME.get(), ReservationStatus.WAITING))
                .willReturn(waitings);
        Waiting waiting = new Waiting(RESERVATION, (long) waitings.size() + 1);
        Waiting createdWaiting = waitingWithId(1L, waiting);
        given(waitingRepository.save(waiting))
                .willReturn(createdWaiting);

        // when
        WaitingResponse waitingResponse = waitingService.create(REQUEST, LOGIN_MEMBER);

        // then
        assertThat(WaitingResponse.of(createdWaiting, ReservationResponse.from(RESERVATION)))
                .isEqualTo(waitingResponse);
    }

    @Test
    @DisplayName("웨이팅을 취소할 경우, 뒤의 웨이팅들은 순서가 앞당겨진다")
    void deleteWaiting1() {
        // given
        Long rank = 3L;
        Waiting waiting = new Waiting(RESERVATION, rank);
        Waiting waiting2 = new Waiting(RESERVATION, rank + 1);
        Waiting waiting3 = new Waiting(RESERVATION, rank + 2);

        given(reservationRepository.findById(RESERVATION.getId()))
                .willReturn(Optional.of(RESERVATION));
        given(waitingRepository.findByReservation(RESERVATION))
                .willReturn(Optional.of(waiting));
        given(waitingRepository.findWaitingGreaterThanRank(RESERVATION.getDate(), RESERVATION_TIME.get(), THEME.get(), rank))
                .willReturn(List.of(waiting2, waiting3));

        // when
        waitingService.deleteById(RESERVATION.getId(), LOGIN_MEMBER);

        // then
        assertAll(
                () -> assertThat(waiting2.getRank()).isEqualTo(rank),
                () -> assertThat(waiting3.getRank()).isEqualTo(rank + 1)
        );
    }

    @Test
    @DisplayName("권한이 없는 경우 웨이팅을 취소할 수 없다")
    void deleteWaiting2() {
        // given
        LOGIN_MEMBER = new LoginMember("may", "may@email.com", MemberRole.MEMBER);
        Waiting waiting = new Waiting(RESERVATION, 1L);

        given(reservationRepository.findById(RESERVATION.getId()))
                .willReturn(Optional.of(RESERVATION));
        given(waitingRepository.findByReservation(RESERVATION))
                .willReturn(Optional.of(waiting));

        // when & then
        assertThatThrownBy(() -> waitingService.deleteById(RESERVATION.getId(), LOGIN_MEMBER))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("관리자는 고객의 예약 대기를 삭제할 수 있다")
    void deleteWaiting3() {
        // given
        LOGIN_MEMBER = new LoginMember("may", "may@email.com", MemberRole.ADMIN);
        Waiting waiting = new Waiting(RESERVATION, 1L);

        given(reservationRepository.findById(RESERVATION.getId()))
                .willReturn(Optional.of(RESERVATION));
        given(waitingRepository.findByReservation(RESERVATION))
                .willReturn(Optional.of(waiting));
        given(waitingRepository.findWaitingGreaterThanRank(RESERVATION.getDate(), RESERVATION_TIME.get(), THEME.get(), 1L))
                .willReturn(List.of());

        // when & then
        assertDoesNotThrow(() -> waitingService.deleteById(RESERVATION.getId(), LOGIN_MEMBER));
    }
}
