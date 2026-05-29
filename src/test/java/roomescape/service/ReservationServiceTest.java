package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.dto.MyReservationResponse;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Time;
import roomescape.domain.WaitingReservation;
import roomescape.domain.reservationStatus.CancelledStatus;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.global.exception.CustomException;
import roomescape.repository.FakeReservationDao;
import roomescape.repository.FakeThemeDao;
import roomescape.repository.FakeThemeSlotDao;
import roomescape.repository.FakeTimeDao;
class ReservationServiceTest {

    private ReservationService reservationService;
    private Time savedTime;
    private Theme savedTheme;
    private ThemeSlot savedThemeSlot1;
    private ThemeSlot savedThemeSlot2;
    private FakeThemeSlotDao fakeThemeSlotDao;

    @BeforeEach
    void setUp() {
        FakeTimeDao fakeReservationTimeDao = new FakeTimeDao();
        FakeThemeDao fakeThemeDao = new FakeThemeDao();
        fakeThemeSlotDao = new FakeThemeSlotDao();

        reservationService = new ReservationService(
                new FakeReservationDao(),
                fakeThemeSlotDao
        );
        savedTime = fakeReservationTimeDao.save(Time.of(LocalTime.of(10, 0)));
        savedTheme = fakeThemeDao.save(new Theme("이름", "설명", "test.com"));
        savedThemeSlot1 = fakeThemeSlotDao.save(new ThemeSlot(savedTheme, LocalDate.now().plusDays(1), savedTime, false));
        savedThemeSlot2 = fakeThemeSlotDao.save(new ThemeSlot(savedTheme, LocalDate.now().plusDays(2), savedTime, false));
    }

    @Test
    @DisplayName("원시값을 받아 연관된 객체를 조회하여 조립한 뒤 예약을 생성한다.")
    void saveReservation() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        assertThat(reservation.getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("존재하는 예약을 식별자를 통해 삭제하면 목록에서 사라진다.")
    void removeReservation() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        reservationService.removeReservation(reservation.getId());
        ThemeSlot themeSlot = fakeThemeSlotDao.findById(savedThemeSlot1.getId()).orElseThrow();
        assertThat(reservationService.allReservations()).isEmpty();
        assertThat(themeSlot.isReserved()).isFalse();
    }

    @Test
    @DisplayName("대기 예약을 삭제해도 같은 슬롯에 확정 예약이 남아 있으면 예약 상태를 유지한다.")
    void keepThemeSlotReservedWhenRemovePendingReservationWithConfirmedReservation() {
        reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        Reservation pendingReservation = reservationService.saveReservation("김대기", savedThemeSlot1.getId());

        reservationService.removeReservation(pendingReservation.getId());

        ThemeSlot themeSlot = fakeThemeSlotDao.findById(savedThemeSlot1.getId()).orElseThrow();
        assertThat(themeSlot.isReserved()).isTrue();
    }

    @Test
    @DisplayName("모든 예약 목록을 조회하여 반환한다.")
    void allReservations() {
        reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        List<Reservation> reservations = reservationService.allReservations();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 특정 예약 객체를 조회한다.")
    void findReservation() {
        Reservation savedReservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        Reservation foundReservation = reservationService.findReservation(savedReservation.getId());
        assertThat(foundReservation.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("예약 테이블에 themeSlot에 해당하는 예약이 없다면, reservation 상태가 confirm으로 변경된다.")
    void saveReservationByNotExistsThemeSlot() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        assertThat(reservation.getReservationStatus()).isEqualTo(ConfirmedStatus.getInstance());
        assertThat(reservation.getThemeSlot().isReserved()).isEqualTo(true);
    }

    @Test
    @DisplayName("예약 테이블에 themeSlot에 해당하는 예약이 존재한다면, reservation 상태가 pending이다.")
    void saveReservationByExistsThemeSlot() {
        reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        Reservation reservation2 = reservationService.saveReservation("브라운1", savedThemeSlot1.getId());
        assertThat(reservation2.getReservationStatus()).isEqualTo(PendingStatus.getInstance());
    }

    @Test
    @DisplayName("같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다")
    void giveOrderByApplicationOrder() {
        reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        Reservation reservation1 = reservationService.saveReservation("김대기", savedThemeSlot1.getId());
        Reservation reservation2 = reservationService.saveReservation("나피리", savedThemeSlot1.getId());
        Reservation reservation3 = reservationService.saveReservation("드레이븐", savedThemeSlot1.getId());

        List<WaitingReservation> responses = reservationService.findWaitingReservationsWithOrder(savedThemeSlot1.getId());
        assertThat(responses).extracting(WaitingReservation::waitingOrder)
                .containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("같은 사용자가 같은 예약 슬롯에 중복 대기 예약을 한다면 예외를 발생한다.")
    void throwsExceptionWhenSameUserDuplicatesWaitingReservation() {
        reservationService.saveReservation("김대기", savedThemeSlot1.getId());
        assertThatThrownBy(() -> {
            reservationService.saveReservation("김대기", savedThemeSlot1.getId());
        }).isInstanceOf(CustomException.class).hasMessage("이미 같은 시간에 예약 또는 대기를 신청했습니다.");
    }

    @Test
    @DisplayName("취소 대상 상태가 PENDING인 경우, 대상 reservation의 status가 CANCEL된다.")
    void reservationStatusCancelWhenReservationIsPending(){
        // given
        reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        Reservation reservation = reservationService.saveReservation("김대기", savedThemeSlot1.getId());

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
        Reservation confirmReservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        Reservation pendingReservation = reservationService.saveReservation("김대기", savedThemeSlot1.getId());

        //when
        reservationService.cancelReservation(confirmReservation.getId());

        //then
        Reservation findReservation = reservationService.findReservation(confirmReservation.getId());
        Reservation findPendingReservation = reservationService.findReservation(pendingReservation.getId());
        assertThat(findReservation.getReservationStatus()).isEqualTo(CancelledStatus.getInstance());
        assertThat(findPendingReservation.getReservationStatus()).isEqualTo(ConfirmedStatus.getInstance());
    }

    @Test
    @DisplayName("취소 대상 상태가 CONFIRM이고 대기중인 예약이 없는 경우, 대상 reservation의 status가 CANCEL되고, themeSlot is_reserved가 false로 변경된다.")
    void reservationStatusCancelWhenReservationIsConfirmAndNotExistsPendingReservation(){
        // given
        Reservation confirmReservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());

        //when
        reservationService.cancelReservation(confirmReservation.getId());

        //then
        Reservation findReservation = reservationService.findReservation(confirmReservation.getId());
        ThemeSlot findThemeSlot = fakeThemeSlotDao.findById(savedThemeSlot1.getId()).orElseThrow();
        assertThat(findReservation.getReservationStatus()).isEqualTo(CancelledStatus.getInstance());
        assertThat(findThemeSlot.isReserved()).isFalse();
    }

    @Test
    @DisplayName("확정 예약이 다른 슬롯으로 변경되면 기존 슬롯의 첫 번째 대기 예약이 확정된다.")
    void promoteFirstPendingReservationWhenConfirmedReservationIsModified(){
        Reservation confirmReservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        Reservation firstPendingReservation = reservationService.saveReservation("김대기", savedThemeSlot1.getId());
        Reservation secondPendingReservation = reservationService.saveReservation("나대기", savedThemeSlot1.getId());

        reservationService.modifyReservation(confirmReservation.getId(), savedThemeSlot2.getId());

        Reservation modifiedReservation = reservationService.findReservation(confirmReservation.getId());
        Reservation promotedReservation = reservationService.findReservation(firstPendingReservation.getId());
        Reservation waitingReservation = reservationService.findReservation(secondPendingReservation.getId());
        ThemeSlot previousThemeSlot = fakeThemeSlotDao.findById(savedThemeSlot1.getId()).orElseThrow();
        ThemeSlot modifiedThemeSlot = fakeThemeSlotDao.findById(savedThemeSlot2.getId()).orElseThrow();
        assertThat(modifiedReservation.getThemeSlot().getId()).isEqualTo(savedThemeSlot2.getId());
        assertThat(promotedReservation.getReservationStatus()).isEqualTo(ConfirmedStatus.getInstance());
        assertThat(waitingReservation.getReservationStatus()).isEqualTo(PendingStatus.getInstance());
        assertThat(previousThemeSlot.isReserved()).isTrue();
        assertThat(modifiedThemeSlot.isReserved()).isTrue();
    }

    @Test
    @DisplayName("이미 CANCELLED된 예약을 취소 요청 하는 경우, INVALID_CANCELLED_COMMAND 예외를 반환한다.")
    void returnInvalidCancelledCommandWhenCancelCancelledReservation(){
        Reservation cancelledReservation = reservationService.saveReservation("김대기", savedThemeSlot1.getId());
        reservationService.cancelReservation(cancelledReservation.getId());
        assertThatThrownBy(() -> {
            reservationService.cancelReservation(cancelledReservation.getId());
        }).isInstanceOf(CustomException.class).hasMessage("취소할 수 없는 예약입니다.");
    }

    @Test
    @DisplayName("이미 COMPLETED된 예약을 취소 요청 하는 경우, INVALID_CANCELLED_COMMAND 예외를 반환한다.")
    void returnInvalidCancelledCommandWhenCancelCompletedReservation(){
        Reservation completedReservation = reservationService.saveReservation("김대기", savedThemeSlot1.getId());
        reservationService.completeReservation(completedReservation.getId());
        assertThatThrownBy(() -> {
            reservationService.cancelReservation(completedReservation.getId());
        }).isInstanceOf(CustomException.class).hasMessage("취소할 수 없는 예약입니다.");
    }

    @Test
    @DisplayName("내 예약 조회시, PENDING 예약인 경우 순번과 함께 조회되고, PENDING이 아닌 예약은 순번 없이 조회 된다.")
    void showWaitingOrderForPendingReservationsOnly(){
        reservationService.saveReservation("김대기1", savedThemeSlot1.getId());
        reservationService.saveReservation("김대기2", savedThemeSlot1.getId());
        reservationService.saveReservation("김대기3", savedThemeSlot1.getId());
        reservationService.saveReservation("김대기1", savedThemeSlot2.getId());
        reservationService.saveReservation("김대기3", savedThemeSlot2.getId());
        reservationService.saveReservation("김대기2", savedThemeSlot2.getId());

        MyReservationResponse reservation2Response = reservationService.findReservationBy("김대기2");
        assertThat(reservation2Response.waitingReservationResponses().get(0).waitingOrder()).isEqualTo(1);
        assertThat(reservation2Response.waitingReservationResponses().get(1).waitingOrder()).isEqualTo(2);

        MyReservationResponse reservation3Response = reservationService.findReservationBy("김대기3");
        assertThat(reservation3Response.waitingReservationResponses().get(0).waitingOrder()).isEqualTo(2);
        assertThat(reservation3Response.waitingReservationResponses().get(1).waitingOrder()).isEqualTo(1);
    }
}
