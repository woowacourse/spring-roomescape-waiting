package roomescape.reservation.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.ReservationAddRequest;
import roomescape.reservation.exception.ReservationExceptionCode;
import roomescape.reservation.repository.FakeReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeExceptionCode;
import roomescape.time.domain.Time;
import roomescape.time.exception.TimeExceptionCode;


class ReservationServiceTest {

    private static final Time TIME_MOCK_DATA = Time.from(LocalTime.of(9, 0));
    private static final Theme THEME_MOCK_DATA = Theme.of("pollaBang", "폴라 방탈출", "thumbnail");
    private static final Member MEMBER_MOCK_DATA = Member.of("kyunellroll@gmail.com", "polla99");

    private static final Reservation reservation = Reservation.of(LocalDate.now().plusDays(1), TIME_MOCK_DATA,
            THEME_MOCK_DATA, MEMBER_MOCK_DATA, ReservationStatus.RESERVED);

    private final ReservationService reservationService;

    public ReservationServiceTest() {
        this.reservationService = new ReservationService(new FakeReservationRepository());
    }

    @Test
    @DisplayName("예약을 추가한다.")
    void addReservation() {
        LocalDate expectedDate = reservation.getDate().plusDays(5);

        ReservationAddRequest reservationRequest = new ReservationAddRequest(expectedDate,
                TIME_MOCK_DATA,
                THEME_MOCK_DATA, MEMBER_MOCK_DATA);

        Reservation savedReservation = reservationService.addReservation(reservationRequest);

        assertAll(
                () -> assertEquals(savedReservation.getMember(), MEMBER_MOCK_DATA),
                () -> assertEquals(savedReservation.getTheme(), THEME_MOCK_DATA),
                () -> assertEquals(savedReservation.getReservationTime(), TIME_MOCK_DATA),
                () -> assertEquals(savedReservation.getDate(), expectedDate)
        );
    }

    @Test
    @DisplayName("이미 예약이 있는 경우 예약하지 못한다.")
    void throwException_WhenReservationExist() {
        ReservationAddRequest reservationRequest = new ReservationAddRequest(reservation.getDate(), TIME_MOCK_DATA,
                THEME_MOCK_DATA, MEMBER_MOCK_DATA);

        Throwable existReservation = assertThrows(RoomEscapeException.class,
                () -> reservationService.addReservation(reservationRequest));

        assertEquals(existReservation.getMessage(), ReservationExceptionCode.DUPLICATE_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("이미 예약한 경우, 예약 대기를 추가하지 못한다.")
    void tryAddDuplicateReservation() {
        ReservationAddRequest reservationRequest = new ReservationAddRequest(reservation.getDate().plusDays(5),
                TIME_MOCK_DATA,
                THEME_MOCK_DATA, MEMBER_MOCK_DATA);

        reservationService.addReservation(reservationRequest);

        Throwable duplicateReservation = assertThrows(RoomEscapeException.class,
                () -> reservationService.addWaitingReservation(reservationRequest, MEMBER_MOCK_DATA.getId()));

        assertEquals(duplicateReservation.getMessage(), ReservationExceptionCode.DUPLICATE_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("상태가 대기인 예약 목록만을 가져온다.")
    void getWaitingReservation() {
        List<Reservation> waitings = reservationService.findWaitings();
        Reservation topWaiting = waitings.get(0);

        assertEquals(topWaiting.getReservationStatus(), ReservationStatus.WAITING.getStatus());
    }

    @Test
    @DisplayName("최소 예약 취소 날짜를 넘어가는 경우 취소가 불가능하다.")
    void shouldThrowException_whenIsOverMinCancelDate() {
        Throwable overMinCancelDate = assertThrows(RoomEscapeException.class,
                () -> reservationService.removeReservation(1));

        assertEquals(overMinCancelDate.getMessage(),
                ReservationExceptionCode.CAN_NOT_CANCEL_AFTER_MIN_CANCEL_DATE.getMessage());
    }

    @Test
    @DisplayName("예약이 존재하는 예약 시간 삭제 요청시 예외를 던진다.")
    void validateReservationExistence_ShouldThrowException_WhenReservationExistAtTime() {
        Throwable reservationExistAtTime = assertThrows(
                RoomEscapeException.class,
                () -> reservationService.validateReservationExistence(TIME_MOCK_DATA.getId()));

        assertEquals(TimeExceptionCode.EXIST_RESERVATION_AT_CHOOSE_TIME.getMessage(),
                reservationExistAtTime.getMessage());
    }

    @Test
    @DisplayName("예약이 존재하는 테마는 삭제하지 못한다.")
    void validateReservationExistence_ShouldThrowException_WhenReservationExist() {
        Throwable reservationExistAtTime = assertThrows(RoomEscapeException.class,
                () -> reservationService.validateBeforeRemoveTheme(THEME_MOCK_DATA.getId()));

        assertEquals(ThemeExceptionCode.USING_THEME_RESERVATION_EXIST.getMessage(),
                reservationExistAtTime.getMessage());
    }
}
