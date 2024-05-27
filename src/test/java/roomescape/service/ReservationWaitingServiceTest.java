package roomescape.service;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION;
import static roomescape.Fixture.VALID_RESERVATION_DATE;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;
import static roomescape.Fixture.VALID_USER_EMAIL;
import static roomescape.Fixture.VALID_USER_NAME;
import static roomescape.Fixture.VALID_USER_PASSWORD;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.ReservationWaitingRepository;
import roomescape.domain.exception.PastReservationException;
import roomescape.service.request.ReservationWaitingAppRequest;
import roomescape.service.response.ReservationWaitingAppResponse;
import roomescape.service.response.ReservationWaitingAppResponseWithRank;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingServiceTest {

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    @Mock
    private ReservationWaitingRepository reservationWaitingRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private MemberRepository memberRepository;

    private final long memberId = 1L;
    private final long timeId = 1L;
    private final long themeId = 1L;
    private final long reservationId = 1L;
    private final long priority = 3L;

    @DisplayName("회원 ID를 가진 예약 대기 정보를 불러온다.")
    @Test
    void findAllByMemberId() {
        long memberId = 1L;
        ReservationWaiting data = new ReservationWaiting(
                new Member(memberId, VALID_USER_NAME, VALID_USER_EMAIL, VALID_USER_PASSWORD, MemberRole.USER),
                new Reservation(reservationId, VALID_MEMBER, VALID_RESERVATION_DATE, VALID_RESERVATION_TIME,
                        VALID_THEME),
                3L);
        when(reservationWaitingRepository.findAllByMemberId(memberId))
                .thenReturn(List.of(data));
        when(reservationWaitingRepository.getRankByReservationAndPriority(reservationId, priority))
                .thenReturn(1L);
        List<ReservationWaitingAppResponseWithRank> actual = reservationWaitingService.findAllByMemberId(memberId);

        assertAll(
                () -> assertEquals(1, actual.size()),
                () -> assertEquals(3L, actual.get(0).priority()),
                () -> assertEquals(1L, actual.get(0).rank())
        );
    }

    @DisplayName("예약 대기를 생성하고 저장한다.")
    @Test
    void save() {
        Member findMember = new Member(memberId, VALID_USER_NAME, VALID_USER_EMAIL, VALID_USER_PASSWORD,
                MemberRole.USER);
        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(findMember));
        Reservation findReservation = new Reservation(
                reservationId,
                new Member(2L, VALID_USER_NAME, VALID_USER_EMAIL, VALID_USER_PASSWORD, MemberRole.USER),
                VALID_RESERVATION_DATE,
                VALID_RESERVATION_TIME,
                VALID_THEME);
        when(reservationRepository.findByDateAndTimeIdAndThemeId(VALID_RESERVATION_DATE, timeId, themeId))
                .thenReturn(Optional.of(findReservation));
        ReservationWaiting savedWaiting = new ReservationWaiting(1L, VALID_MEMBER, VALID_RESERVATION, priority);
        when(reservationWaitingRepository.save(any(ReservationWaiting.class)))
                .thenReturn(savedWaiting);

        ReservationWaitingAppRequest appRequest = new ReservationWaitingAppRequest(VALID_RESERVATION_DATE.getDate(),
                timeId, themeId, memberId);

        ReservationWaitingAppResponse actual = reservationWaitingService.save(appRequest);
        ReservationWaitingAppResponse expected = ReservationWaitingAppResponse.from(savedWaiting);

        assertAll(
                () -> assertEquals(expected.id(), actual.id()),
                () -> assertEquals(expected.name(), actual.name()),
                () -> assertEquals(expected.priority(), actual.priority())
        );
    }

    @DisplayName("중복 예약 대기가 발생하면 예외가 발생한다.")
    @Test
    void save_Duplicate() {
        Member findMember = new Member(memberId, VALID_USER_NAME, VALID_USER_EMAIL, VALID_USER_PASSWORD,
                MemberRole.USER);
        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(findMember));
        Reservation findReservation = new Reservation(reservationId, VALID_MEMBER, VALID_RESERVATION_DATE,
                VALID_RESERVATION_TIME,
                VALID_THEME);
        when(reservationRepository.findByDateAndTimeIdAndThemeId(VALID_RESERVATION_DATE, timeId, themeId))
                .thenReturn(Optional.of(findReservation));

        when(reservationWaitingRepository.existsByReservationIdAndMemberId(reservationId, memberId))
                .thenReturn(true);

        ReservationWaitingAppRequest appRequest = new ReservationWaitingAppRequest(VALID_RESERVATION_DATE.getDate(),
                timeId, themeId, memberId);

        assertThatThrownBy(() -> reservationWaitingService.save(appRequest))
                .isInstanceOf(IllegalStateException.class);

    }

    @DisplayName("예약 대기가 과거면, 예외가 발생한다.")
    @Test
    void save_Past() {
        Member findMember = new Member(memberId, VALID_USER_NAME, VALID_USER_EMAIL, VALID_USER_PASSWORD,
                MemberRole.USER);
        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(findMember));
        Reservation findReservation = new Reservation(reservationId, VALID_MEMBER,
                new ReservationDate(LocalDate.MIN.toString()),
                VALID_RESERVATION_TIME,
                VALID_THEME);
        when(reservationRepository.findByDateAndTimeIdAndThemeId(VALID_RESERVATION_DATE, timeId, themeId))
                .thenReturn(Optional.of(findReservation));

        ReservationWaitingAppRequest appRequest = new ReservationWaitingAppRequest(VALID_RESERVATION_DATE.getDate(),
                timeId, themeId, memberId);

        assertThatThrownBy(() -> reservationWaitingService.save(appRequest))
                .isInstanceOf(PastReservationException.class);

    }

    @DisplayName("예약자와 예약 대기자가 동일하면, 예외가 발생한다.")
    @Test
    void save_SameMember() {
        Member findMember = new Member(memberId, VALID_USER_NAME, VALID_USER_EMAIL, VALID_USER_PASSWORD,
                MemberRole.USER);
        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(findMember));
        Reservation findReservation = new Reservation(reservationId, findMember,
                VALID_RESERVATION_DATE,
                VALID_RESERVATION_TIME,
                VALID_THEME);
        when(reservationRepository.findByDateAndTimeIdAndThemeId(VALID_RESERVATION_DATE, timeId, themeId))
                .thenReturn(Optional.of(findReservation));

        ReservationWaitingAppRequest appRequest = new ReservationWaitingAppRequest(VALID_RESERVATION_DATE.getDate(),
                timeId, themeId, memberId);

        assertThatThrownBy(() -> reservationWaitingService.save(appRequest))
                .isInstanceOf(IllegalStateException.class);

    }
}
