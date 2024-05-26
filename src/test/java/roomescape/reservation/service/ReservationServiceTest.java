package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static roomescape.reservation.fixture.ReservationFixture.MEMBER_ID_1_RESERVATION;
import static roomescape.reservation.fixture.ReservationFixture.PAST_DATE_RESERVATION_REQUEST;
import static roomescape.reservation.fixture.ReservationFixture.RESERVATION_REQUEST_1;
import static roomescape.reservation.fixture.ReservationFixture.SAVED_RESERVATION_2;
import static roomescape.theme.fixture.ThemeFixture.THEME_1;
import static roomescape.time.fixture.ReservationTimeFixture.RESERVATION_TIME_10_00_ID_1;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.exception.IllegalRequestException;
import roomescape.member.fixture.MemberFixture;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationWithWaiting;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.theme.service.ThemeService;
import roomescape.time.service.ReservationTimeService;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private MemberService memberService;

    @Mock
    private ReservationTimeService reservationTimeService;

    @Mock
    private ThemeService themeService;

    @Mock
    private ReservationRepository reservationRepository;

    @DisplayName("전체 예약을 조회하고 응답 형태로 반환할 수 있다")
    @Test
    void should_return_response_when_requested_all() {
        when(reservationRepository.findAll()).thenReturn(List.of(MEMBER_ID_1_RESERVATION, SAVED_RESERVATION_2));

        assertThat(reservationService.findAllReservation())
                .contains(new ReservationResponse(MEMBER_ID_1_RESERVATION),
                        new ReservationResponse(SAVED_RESERVATION_2));
    }

    @DisplayName("특정 유저의 예약 목록을 읽는 요청을 처리할 수 있다")
    @Test
    void should_return_response_when_my_reservations_requested_all() {
        when(reservationRepository.findByMemberIdWithWaitingStatus(1L))
                .thenReturn(List.of(new ReservationWithWaiting(MEMBER_ID_1_RESERVATION, 0)));

        assertThat(reservationService.findMemberReservationWithWaitingStatus(1L))
                .containsExactly(new MemberReservationResponse(new ReservationWithWaiting(MEMBER_ID_1_RESERVATION, 0)));
    }

    @DisplayName("예약을 추가하고 응답을 반환할 수 있다")
    @Test
    void should_save_reservation_when_requested() {
        when(memberService.findById(any(Long.class))).thenReturn(MemberFixture.MEMBER_ID_1);
        when(reservationTimeService.findById(1L)).thenReturn(RESERVATION_TIME_10_00_ID_1);
        when(themeService.findById(1L)).thenReturn(THEME_1);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(MEMBER_ID_1_RESERVATION);

        ReservationResponse savedReservation = reservationService.saveMemberReservation(1L, RESERVATION_REQUEST_1);

        assertThat(savedReservation).isEqualTo(new ReservationResponse(MEMBER_ID_1_RESERVATION));
    }

    @DisplayName("현재보다 이전날짜로 예약 시 예외가 발생한다")
    @Test
    void should_throw_exception_when_request_with_past_date() {
        when(memberService.findById(any(Long.class))).thenReturn(MemberFixture.MEMBER_ID_1);
        when(reservationTimeService.findById(1L)).thenReturn(RESERVATION_TIME_10_00_ID_1);
        when(themeService.findById(1L)).thenReturn(THEME_1);

        assertThatThrownBy(() -> reservationService.saveMemberReservation(1L, PAST_DATE_RESERVATION_REQUEST))
                .isInstanceOf(IllegalRequestException.class);
    }

    @DisplayName("내 이름으로 진행되고 있는 예약이 이미 존재하는 경우 예약 대기를 할 수 없다")
    @Test
    void should_not_wait_when_my_reservation_is_exist() {
        when(reservationRepository.findByDateAndTimeAndTheme(any(LocalDate.class), any(Long.class),
                any(Long.class))).thenReturn(List.of(
                MEMBER_ID_1_RESERVATION));

        assertThatThrownBy(() -> reservationService.saveMemberReservation(1L, RESERVATION_REQUEST_1))
                .isInstanceOf(IllegalRequestException.class);
    }
}
