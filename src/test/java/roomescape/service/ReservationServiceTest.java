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
import roomescape.controller.dto.WaitingReservationResponse;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Time;
import roomescape.domain.reservationStatus.CancelledStatus;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.global.exception.CustomException;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeThemeRepository;
import roomescape.repository.FakeThemeSlotRepository;
import roomescape.repository.FakeTimeRepository;

class ReservationServiceTest {

    private ReservationService reservationService;
    private FakeThemeSlotRepository fakeThemeSlotRepository;
    private Time savedTime;
    private Theme savedTheme;
    private ThemeSlot savedThemeSlot1;
    private ThemeSlot savedThemeSlot2;
    private ThemeSlot savedPastThemeSlot;

    @BeforeEach
    void setUp() {
        FakeTimeRepository fakeReservationTimeDao = new FakeTimeRepository();
        FakeThemeRepository fakeThemeRepository = new FakeThemeRepository();
        FakeReservationRepository fakeReservationRepository = new FakeReservationRepository();
        fakeThemeSlotRepository = new FakeThemeSlotRepository(fakeReservationRepository);

        reservationService = new ReservationService(
                fakeReservationRepository,
                fakeThemeSlotRepository
        );
        savedTime = fakeReservationTimeDao.save(Time.of(LocalTime.of(10, 0)));
        savedTheme = fakeThemeRepository.save(new Theme("이름", "설명", "test.com", 10000L));
        savedThemeSlot1 = fakeThemeSlotRepository.save(new ThemeSlot(savedTheme, LocalDate.now().plusDays(1), savedTime, false));
        savedThemeSlot2 = fakeThemeSlotRepository.save(new ThemeSlot(savedTheme, LocalDate.now().plusDays(2), savedTime, false));
        savedPastThemeSlot = fakeThemeSlotRepository.save(new ThemeSlot(savedTheme, LocalDate.now().minusDays(1), savedTime, false));
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
        assertThat(reservationService.allReservations()).isEmpty();
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
        assertThat(fakeThemeSlotRepository.findById(savedThemeSlot1.getId()).get().isReserved()).isTrue();
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

        List<WaitingReservationResponse> responses = reservationService.findWaitingReservationWithOrder(savedThemeSlot1.getId());
        assertThat(responses).extracting(WaitingReservationResponse::waitingOrder)
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
        assertThat(fakeThemeSlotRepository.findById(savedThemeSlot1.getId()).get().isReserved()).isTrue();
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
        assertThat(findReservation.getReservationStatus()).isEqualTo(CancelledStatus.getInstance());
        assertThat(pendingReservation.getReservationStatus()).isEqualTo(ConfirmedStatus.getInstance());
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
        assertThat(findReservation.getReservationStatus()).isEqualTo(CancelledStatus.getInstance());
        assertThat(fakeThemeSlotRepository.findById(savedThemeSlot1.getId()).get().isReserved()).isFalse();
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
    @DisplayName("과거 날짜 슬롯으로 예약 등록시 예외가 발생한다.")
    void throwsExceptionWhenSavingReservationWithPastDate() {
        assertThatThrownBy(() -> reservationService.saveReservation("브라운", savedPastThemeSlot.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage("과거 날짜 시간은 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("과거 날짜 슬롯으로 예약 변경시 예외가 발생한다.")
    void throwsExceptionWhenModifyingReservationToPastDate() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        assertThatThrownBy(() -> reservationService.modifyReservation(reservation.getId(), savedPastThemeSlot.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage("과거 날짜 시간은 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 예약을 취소하면 예외가 발생한다.")
    void throwsExceptionWhenCancellingNonExistentReservation() {
        assertThatThrownBy(() -> reservationService.cancelReservation(999L))
                .isInstanceOf(CustomException.class)
                .hasMessage("예약이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 예약을 변경하면 예외가 발생한다.")
    void throwsExceptionWhenModifyingNonExistentReservation() {
        assertThatThrownBy(() -> reservationService.modifyReservation(999L, savedThemeSlot1.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage("예약이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 테마 슬롯으로 예약 변경시 예외가 발생한다.")
    void throwsExceptionWhenModifyingReservationToNonExistentThemeSlot() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        assertThatThrownBy(() -> reservationService.modifyReservation(reservation.getId(), 999L))
                .isInstanceOf(CustomException.class)
                .hasMessage("예약 가능한 시간이 존재하지 않습니다.");
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
