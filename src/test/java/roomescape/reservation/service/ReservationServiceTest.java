package roomescape.reservation.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.admin.dto.AdminWaitingResponse;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.domain.Member;
import roomescape.member.exception.model.MemberNotFoundException;
import roomescape.member.repository.FakeMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.exception.ReservationExceptionCode;
import roomescape.reservation.repository.FakeReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.model.ThemeNotFoundException;
import roomescape.theme.repository.FakeThemeRepository;
import roomescape.time.domain.Time;
import roomescape.time.exception.model.TimeNotFoundException;
import roomescape.time.repository.FakeTimeRepository;


class ReservationServiceTest {

    private static final Time TIME_MOCK_DATA = Time.from(LocalTime.of(9, 0));
    private static final Theme THEME_MOCK_DATA = Theme.of("pollaBang", "폴라 방탈출", "thumbnail");
    private static final Member MEMBER_MOCK_DATA = Member.of("kyunellroll@gmail.com", "polla99");

    private static final Reservation reservation = Reservation.of(LocalDate.now().plusDays(1), TIME_MOCK_DATA,
            THEME_MOCK_DATA, MEMBER_MOCK_DATA, ReservationStatus.RESERVED);

    private final ReservationService reservationService;

    public ReservationServiceTest() {
        this.reservationService = new ReservationService(new FakeReservationRepository(), new FakeTimeRepository(),
                new FakeThemeRepository(), new FakeMemberRepository());
    }

    @Test
    @DisplayName("예약을 추가한다.")
    void addReservation() {
        ReservationRequest reservationRequest = new ReservationRequest(reservation.getDate(), 1, 1);

        ReservationResponse reservationResponse = reservationService.addReservation(reservationRequest, 1);

        assertAll(
                () -> assertEquals(reservationResponse.memberName(), MEMBER_MOCK_DATA.getName()),
                () -> assertEquals(reservationResponse.themeName(), THEME_MOCK_DATA.getName()),
                () -> assertEquals(reservationResponse.startAt(), TIME_MOCK_DATA.getStartAt()),
                () -> assertEquals(reservationResponse.date(), reservation.getDate())
        );
    }

    @Test
    @DisplayName("존재하는 시간이 없을 경우 에러가 발생한다.")
    void notExistTimeReservation() {
        ReservationRequest reservationRequest = new ReservationRequest(reservation.getDate(), 100, 1);

        Throwable notExistTime = assertThrows(TimeNotFoundException.class,
                () -> reservationService.addReservation(reservationRequest, 1));
        assertEquals(notExistTime.getMessage(), new TimeNotFoundException().getMessage());
    }

    @Test
    @DisplayName("존재하는 테마가 없을 경우 에러가 발생한다.")
    void notExistThemeReservation() {
        ReservationRequest reservationRequest = new ReservationRequest(reservation.getDate(), 1, 100);

        Throwable notExistTheme = assertThrows(ThemeNotFoundException.class,
                () -> reservationService.addReservation(reservationRequest, 1));
        assertEquals(notExistTheme.getMessage(), new ThemeNotFoundException().getMessage());
    }

    @Test
    @DisplayName("존재하는 멤버가 없을 경우 에러가 발생한다.")
    void notExistMemberReservation() {
        ReservationRequest reservationRequest = new ReservationRequest(reservation.getDate(), 1, 1);

        Throwable notExistMember = assertThrows(MemberNotFoundException.class,
                () -> reservationService.addReservation(reservationRequest, 100));
        assertEquals(notExistMember.getMessage(), new MemberNotFoundException().getMessage());
    }

    @Test
    @DisplayName("이미 예약한 경우, 예약 대기를 추가하지 못한다.")
    void tryAddDuplicateReservation() {
        ReservationRequest reservationRequest = new ReservationRequest(reservation.getDate(), 1, 1);

        reservationService.addReservation(reservationRequest, 1);

        Throwable duplicateReservation = assertThrows(RoomEscapeException.class,
                () -> reservationService.addWaitingReservation(reservationRequest, 1));

        assertEquals(duplicateReservation.getMessage(), ReservationExceptionCode.DUPLICATE_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("상태가 대기인 예약 목록만을 가져온다.")
    void getWaitingReservation() {
        ReservationResponse reservationResponse = reservationService.addWaitingReservation(
                new ReservationRequest(LocalDate.now().plusDays(1), 1, 1), 1);

        List<AdminWaitingResponse> waitingResponses = reservationService.findWaitings();
        AdminWaitingResponse adminWaitingResponse = waitingResponses.get(0);

        assertAll(
                () -> assertEquals(waitingResponses.size(), 1),
                () -> assertEquals(adminWaitingResponse.memberName(), reservationResponse.memberName()),
                () -> assertEquals(adminWaitingResponse.time(), reservationResponse.startAt()),
                () -> assertEquals(adminWaitingResponse.themeName(), reservationResponse.themeName())
        );
    }

    @Test
    @DisplayName("예약을 지운다.")
    void removeReservations() {
        assertDoesNotThrow(() -> reservationService.removeReservations(reservation.getId()));
    }

    @Test
    @DisplayName("최소 예약 취소 날짜를 넘어가는 경우 취소가 불가능하다.")
    void shouldThrowException_whenIsOverMinCancelDate() {
        Throwable overMinCancelDate = assertThrows(RoomEscapeException.class,
                () -> reservationService.removeReservations(1));

        assertEquals(overMinCancelDate.getMessage(),
                ReservationExceptionCode.CAN_NOT_CANCEL_AFTER_MIN_CANCEL_DATE.getMessage());
    }
}
