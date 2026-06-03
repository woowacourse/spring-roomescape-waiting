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
import roomescape.domain.reservationStatus.CancelledStatus;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.CompletedStatus;
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
    private FakeReservationDao fakeReservationDao;
    private FakeThemeSlotDao fakeThemeSlotDao;

    @BeforeEach
    void setUp() {
        FakeTimeDao fakeReservationTimeDao = new FakeTimeDao();
        FakeThemeDao fakeThemeDao = new FakeThemeDao();
        fakeReservationDao = new FakeReservationDao();
        fakeThemeSlotDao = new FakeThemeSlotDao();

        reservationService = new ReservationService(
                fakeReservationDao,
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
    @DisplayName("확정 예약을 삭제하면 첫 번째 대기 예약이 확정된다.")
    void promoteFirstPendingReservationWhenRemoveConfirmedReservation() {
        Reservation confirmedReservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        Reservation firstPendingReservation = reservationService.saveReservation("김대기", savedThemeSlot1.getId());
        Reservation secondPendingReservation = reservationService.saveReservation("나대기", savedThemeSlot1.getId());

        reservationService.removeReservation(confirmedReservation.getId());

        Reservation promotedReservation = findReservation(firstPendingReservation.getId());
        Reservation waitingReservation = findReservation(secondPendingReservation.getId());
        ThemeSlot themeSlot = fakeThemeSlotDao.findById(savedThemeSlot1.getId()).orElseThrow();
        assertThat(promotedReservation.getReservationStatus()).isEqualTo(ConfirmedStatus.getInstance());
        assertThat(waitingReservation.getReservationStatus()).isEqualTo(PendingStatus.getInstance());
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
        reservationService.saveReservation("김대기", savedThemeSlot1.getId());
        reservationService.saveReservation("나피리", savedThemeSlot1.getId());
        reservationService.saveReservation("드레이븐", savedThemeSlot1.getId());

        assertThat(reservationService.findReservationBy("김대기").waitingReservationResponses().get(0).waitingOrder())
                .isEqualTo(1);
        assertThat(reservationService.findReservationBy("나피리").waitingReservationResponses().get(0).waitingOrder())
                .isEqualTo(2);
        assertThat(reservationService.findReservationBy("드레이븐").waitingReservationResponses().get(0).waitingOrder())
                .isEqualTo(3);
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
    @DisplayName("취소된 대기 예약은 같은 사용자의 같은 슬롯 중복 대기 검사에서 제외된다.")
    void allowSameUserToWaitAgainAfterCancelWaitingReservation() {
        reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        Reservation pendingReservation = reservationService.saveReservation("김대기", savedThemeSlot1.getId());
        reservationService.cancelReservation(pendingReservation.getId(), "김대기");

        Reservation reservation = reservationService.saveReservation("김대기", savedThemeSlot1.getId());

        assertThat(reservation.getReservationStatus()).isEqualTo(PendingStatus.getInstance());
    }

    @Test
    @DisplayName("취소 대상 상태가 PENDING인 경우, 대상 reservation의 status가 CANCEL된다.")
    void reservationStatusCancelWhenReservationIsPending(){
        // given
        reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        Reservation reservation = reservationService.saveReservation("김대기", savedThemeSlot1.getId());

        //when
        reservationService.cancelReservation(reservation.getId(), "김대기");

        //then
        Reservation findReservation = findReservation(reservation.getId());
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
        reservationService.cancelReservation(confirmReservation.getId(), "브라운");

        //then
        Reservation findReservation = findReservation(confirmReservation.getId());
        Reservation findPendingReservation = findReservation(pendingReservation.getId());
        assertThat(findReservation.getReservationStatus()).isEqualTo(CancelledStatus.getInstance());
        assertThat(findPendingReservation.getReservationStatus()).isEqualTo(ConfirmedStatus.getInstance());
    }

    @Test
    @DisplayName("취소 대상 상태가 CONFIRM이고 대기중인 예약이 없는 경우, 대상 reservation의 status가 CANCEL되고, themeSlot is_reserved가 false로 변경된다.")
    void reservationStatusCancelWhenReservationIsConfirmAndNotExistsPendingReservation(){
        // given
        Reservation confirmReservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());

        //when
        reservationService.cancelReservation(confirmReservation.getId(), "브라운");

        //then
        Reservation findReservation = findReservation(confirmReservation.getId());
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

        Reservation modifiedReservation = findReservation(confirmReservation.getId());
        Reservation promotedReservation = findReservation(firstPendingReservation.getId());
        Reservation waitingReservation = findReservation(secondPendingReservation.getId());
        ThemeSlot previousThemeSlot = fakeThemeSlotDao.findById(savedThemeSlot1.getId()).orElseThrow();
        ThemeSlot modifiedThemeSlot = fakeThemeSlotDao.findById(savedThemeSlot2.getId()).orElseThrow();
        assertThat(modifiedReservation.getThemeSlot().getId()).isEqualTo(savedThemeSlot2.getId());
        assertThat(promotedReservation.getReservationStatus()).isEqualTo(ConfirmedStatus.getInstance());
        assertThat(waitingReservation.getReservationStatus()).isEqualTo(PendingStatus.getInstance());
        assertThat(previousThemeSlot.isReserved()).isTrue();
        assertThat(modifiedThemeSlot.isReserved()).isTrue();
    }

    @Test
    @DisplayName("대기 예약이 빈 슬롯으로 변경되면 확정 예약이 된다.")
    void confirmPendingReservationWhenModifiedToEmptyThemeSlot() {
        reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        Reservation pendingReservation = reservationService.saveReservation("김대기", savedThemeSlot1.getId());

        Reservation modifiedReservation = reservationService.modifyReservation(pendingReservation.getId(), savedThemeSlot2.getId());

        ThemeSlot previousThemeSlot = fakeThemeSlotDao.findById(savedThemeSlot1.getId()).orElseThrow();
        ThemeSlot modifiedThemeSlot = fakeThemeSlotDao.findById(savedThemeSlot2.getId()).orElseThrow();
        assertThat(modifiedReservation.getReservationStatus()).isEqualTo(ConfirmedStatus.getInstance());
        assertThat(previousThemeSlot.isReserved()).isTrue();
        assertThat(modifiedThemeSlot.isReserved()).isTrue();
    }

    @Test
    @DisplayName("예약이 이미 예약된 슬롯으로 변경되면 대기 예약이 된다.")
    void changeReservationToPendingWhenModifiedToReservedThemeSlot() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        reservationService.saveReservation("네오", savedThemeSlot2.getId());

        Reservation modifiedReservation = reservationService.modifyReservation(reservation.getId(), savedThemeSlot2.getId());

        ThemeSlot previousThemeSlot = fakeThemeSlotDao.findById(savedThemeSlot1.getId()).orElseThrow();
        ThemeSlot modifiedThemeSlot = fakeThemeSlotDao.findById(savedThemeSlot2.getId()).orElseThrow();
        assertThat(modifiedReservation.getThemeSlot().getId()).isEqualTo(savedThemeSlot2.getId());
        assertThat(modifiedReservation.getReservationStatus()).isEqualTo(PendingStatus.getInstance());
        assertThat(previousThemeSlot.isReserved()).isFalse();
        assertThat(modifiedThemeSlot.isReserved()).isTrue();
    }

    @Test
    @DisplayName("취소된 예약은 다른 슬롯으로 변경할 수 없다.")
    void throwExceptionWhenModifyCancelledReservation() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        reservationService.cancelReservation(reservation.getId(), "브라운");

        assertThatThrownBy(() -> reservationService.modifyReservation(reservation.getId(), savedThemeSlot2.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage("변경할 수 없는 예약입니다.");
    }

    @Test
    @DisplayName("완료된 예약은 다른 슬롯으로 변경할 수 없다.")
    void throwExceptionWhenModifyCompletedReservation() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        markCompleted(reservation);

        assertThatThrownBy(() -> reservationService.modifyReservation(reservation.getId(), savedThemeSlot2.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage("변경할 수 없는 예약입니다.");
    }

    @Test
    @DisplayName("이미 CANCELLED된 예약을 취소 요청 하는 경우, INVALID_CANCELLED_COMMAND 예외를 반환한다.")
    void returnInvalidCancelledCommandWhenCancelCancelledReservation(){
        Reservation cancelledReservation = reservationService.saveReservation("김대기", savedThemeSlot1.getId());
        reservationService.cancelReservation(cancelledReservation.getId(), "김대기");
        assertThatThrownBy(() -> {
            reservationService.cancelReservation(cancelledReservation.getId(), "김대기");
        }).isInstanceOf(CustomException.class).hasMessage("취소할 수 없는 예약입니다.");
    }

    @Test
    @DisplayName("이미 COMPLETED된 예약을 취소 요청 하는 경우, INVALID_CANCELLED_COMMAND 예외를 반환한다.")
    void returnInvalidCancelledCommandWhenCancelCompletedReservation(){
        Reservation completedReservation = reservationService.saveReservation("김대기", savedThemeSlot1.getId());
        markCompleted(completedReservation);
        assertThatThrownBy(() -> {
            reservationService.cancelReservation(completedReservation.getId(), "김대기");
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

    @Test
    @DisplayName("앞 순번 대기를 취소하면 다음 대기의 순번이 1로 재정렬된다.")
    void reorderWaitingOrderWhenFirstWaitingReservationIsCancelled() {
        reservationService.saveReservation("확정자", savedThemeSlot1.getId());
        Reservation firstWaitingReservation = reservationService.saveReservation("첫대기", savedThemeSlot1.getId());
        reservationService.saveReservation("둘대기", savedThemeSlot1.getId());

        reservationService.cancelReservation(firstWaitingReservation.getId(), "첫대기");

        MyReservationResponse response = reservationService.findReservationBy("둘대기");
        assertThat(response.waitingReservationResponses()).hasSize(1);
        assertThat(response.waitingReservationResponses().get(0).waitingOrder()).isEqualTo(1);
    }

    private Reservation findReservation(Long reservationId) {
        return fakeReservationDao.findById(reservationId).orElseThrow();
    }

    private void markCompleted(Reservation reservation) {
        Reservation completedReservation = new Reservation(
                reservation.getId(),
                reservation.getName(),
                reservation.getThemeSlot(),
                CompletedStatus.getInstance()
        );
        fakeReservationDao.updateStatus(completedReservation);
    }
}
