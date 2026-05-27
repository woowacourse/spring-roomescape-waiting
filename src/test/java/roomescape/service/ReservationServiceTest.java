package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.dto.WaitingReservationResponse;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Time;
import roomescape.domain.reservationStatus.CancelledStatus;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.global.exception.CustomException;
import roomescape.repository.FakeReservationDao;
import roomescape.repository.FakeThemeDao;
import roomescape.repository.FakeThemeSlotDao;
import roomescape.repository.FakeTimeDao;
import roomescape.service.ReservationService;

class ReservationServiceTest {

    private ReservationService reservationService;
    private Time savedTime;
    private Theme savedTheme;
    private ThemeSlot savedThemeSlot;

    @BeforeEach
    void setUp() {
        FakeTimeDao fakeReservationTimeDao = new FakeTimeDao();
        FakeThemeDao fakeThemeDao = new FakeThemeDao();
        FakeThemeSlotDao fakeThemeSlotDao = new FakeThemeSlotDao();

        reservationService = new ReservationService(
                new FakeReservationDao(),
                fakeThemeSlotDao
        );
        savedTime = fakeReservationTimeDao.save(Time.of(LocalTime.of(10, 0)));
        savedTheme = fakeThemeDao.save(new Theme("이름", "설명", "test.com"));
        savedThemeSlot = fakeThemeSlotDao.save(new ThemeSlot(savedTheme, LocalDate.now().plusDays(1), savedTime, false));
    }

    @Test
    @DisplayName("원시값을 받아 연관된 객체를 조회하여 조립한 뒤 예약을 생성한다.")
    void saveReservation() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot.getId());
        assertThat(reservation.getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("존재하는 예약을 식별자를 통해 삭제하면 목록에서 사라진다.")
    void removeReservation() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot.getId());
        reservationService.removeReservation(reservation.getId());
        assertThat(reservationService.allReservations()).isEmpty();
    }

    @Test
    @DisplayName("모든 예약 목록을 조회하여 반환한다.")
    void allReservations() {
        reservationService.saveReservation("브라운", savedThemeSlot.getId());
        List<Reservation> reservations = reservationService.allReservations();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 특정 예약 객체를 조회한다.")
    void findReservation() {
        Reservation savedReservation = reservationService.saveReservation("브라운", savedThemeSlot.getId());
        Reservation foundReservation = reservationService.findReservation(savedReservation.getId());
        assertThat(foundReservation.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("예약 테이블에 themeSlot에 해당하는 예약이 없다면, reservation 상태가 confirm으로 변경된다.")
    void saveReservationByNotExistsThemeSlot() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot.getId());
        assertThat(reservation.getReservationStatus()).isEqualTo(ConfirmedStatus.getInstance());
        assertThat(reservation.getThemeSlot().isReserved()).isEqualTo(true);
    }

    @Test
    @DisplayName("예약 테이블에 themeSlot에 해당하는 예약이 존재한다면, reservation 상태가 pending이다.")
    void saveReservationByExistsThemeSlot() {
        reservationService.saveReservation("브라운", savedThemeSlot.getId());
        Reservation reservation2 = reservationService.saveReservation("브라운1", savedThemeSlot.getId());
        assertThat(reservation2.getReservationStatus()).isEqualTo(PendingStatus.getInstance());
    }

    @Test
    @DisplayName("같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다")
    void giveOrderByApplicationOrder() {
        reservationService.saveReservation("브라운", savedThemeSlot.getId());
        Reservation reservation1 = reservationService.saveReservation("김대기", savedThemeSlot.getId());
        Reservation reservation2 = reservationService.saveReservation("나피리", savedThemeSlot.getId());
        Reservation reservation3 = reservationService.saveReservation("드레이븐", savedThemeSlot.getId());

        List<WaitingReservationResponse> responses = reservationService.findWaitingReservationWithOrder(savedThemeSlot.getId());
        assertThat(responses).extracting(WaitingReservationResponse::waitingOrder)
                .containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("같은 사용자가 같은 예약 슬롯에 중복 대기 예약을 한다면 예외를 발생한다.")
    void throwsExceptionWhenSameUserDuplicatesWaitingReservation() {
        reservationService.saveReservation("김대기", savedThemeSlot.getId());
        assertThatThrownBy(() -> {
            reservationService.saveReservation("김대기", savedThemeSlot.getId());
        }).isInstanceOf(CustomException.class).hasMessage("이미 같은 시간에 예약 또는 대기를 신청했습니다.");
    }

    @Test
    @DisplayName("취소 대상 상태가 PENDING인 경우, 대상 reservation의 status가 CANCEL된다.")
    void reservationStatusCancelWhenReservationIsPending(){
        // given
        reservationService.saveReservation("브라운", savedThemeSlot.getId());
        Reservation reservation = reservationService.saveReservation("김대기", savedThemeSlot.getId());

        //when
        reservationService.cancelReservation(reservation.getId());

        //then
        Reservation findReservation = reservationService.findReservation(reservation.getId());
        assertThat(findReservation.getReservationStatus()).isEqualTo(CancelledStatus.getInstance());
        assertThat(findReservation.getThemeSlot().isReserved()).isTrue();
    }

    @Test
    @DisplayName("취소 대상 상태가 CONFIRM이고 대기중인 예약이 있는 경우, 대상 reservation의 status가 CANCEL되고, 대기중인 예약이 CONFIRM으로 변경된다.")
    void reservationStatusCancelWhenReservationIsConfirmAndExistsPendingReservation(){
        // given
        Reservation confirmReservation = reservationService.saveReservation("브라운", savedThemeSlot.getId());
        Reservation pendingReservation = reservationService.saveReservation("김대기", savedThemeSlot.getId());

        //when
        reservationService.cancelReservation(confirmReservation.getId());

        //then
        Reservation findReservation = reservationService.findReservation(confirmReservation.getId());
        assertThat(findReservation.getReservationStatus()).isEqualTo(CancelledStatus.getInstance());
        assertThat(pendingReservation.getReservationStatus()).isEqualTo(ConfirmedStatus.getInstance());
    }

    @Test
    @DisplayName("취소 대상 상태가 CONFIRM이고 대기중인 예약이 없는 경우, 대상 reservation의 status가 CANCEL되고, themeSlot is_reserved가 false로 변경된다.")
    void reservationStatusCancelWhenReservationIsConfirmAndNotExistsPendingReservation(){
        // given
        Reservation confirmReservation = reservationService.saveReservation("브라운", savedThemeSlot.getId());

        //when
        reservationService.cancelReservation(confirmReservation.getId());

        //then
        Reservation findReservation = reservationService.findReservation(confirmReservation.getId());
        assertThat(findReservation.getReservationStatus()).isEqualTo(CancelledStatus.getInstance());
        assertThat(confirmReservation.getThemeSlot().isReserved()).isFalse();
    }

    @Test
    @DisplayName("이미 CANCELLED된 예약을 취소 요청 하는 경우, INVALID_CANCELLED_COMMAND 예외를 반환한다.")
    void returnInvalidCancelledCommandWhenCancelCancelledReservation(){
        Reservation cancelledReservation = reservationService.saveReservation("김대기", savedThemeSlot.getId());
        reservationService.cancelReservation(cancelledReservation.getId());
        assertThatThrownBy(() -> {
            reservationService.cancelReservation(cancelledReservation.getId());
        }).isInstanceOf(CustomException.class).hasMessage("취소할 수 없는 예약입니다.");
    }

    @Test
    @DisplayName("이미 COMPLETED된 예약을 취소 요청 하는 경우, INVALID_CANCELLED_COMMAND 예외를 반환한다.")
    void returnInvalidCancelledCommandWhenCancelCompletedReservation(){
        Reservation completedReservation = reservationService.saveReservation("김대기", savedThemeSlot.getId());
        reservationService.completeReservation(completedReservation.getId());
        assertThatThrownBy(() -> {
            reservationService.cancelReservation(completedReservation.getId());
        }).isInstanceOf(CustomException.class).hasMessage("취소할 수 없는 예약입니다.");
    }
}
