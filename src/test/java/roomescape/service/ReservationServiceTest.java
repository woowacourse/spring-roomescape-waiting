package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Time;
import roomescape.domain.reservationStatus.CancelledStatus;
import roomescape.domain.reservationStatus.CompletedStatus;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.global.exception.CustomException;
import roomescape.repository.FakeReservationDao;
import roomescape.repository.FakeThemeDao;
import roomescape.repository.FakeThemeSlotDao;
import roomescape.repository.FakeTimeDao;
import roomescape.repository.WaitingRepository;

class ReservationServiceTest {

    private ReservationService reservationService;
    private ThemeSlot savedThemeSlot1;
    private ThemeSlot savedThemeSlot2;
    private FakeReservationDao fakeReservationDao;
    private FakeThemeSlotDao fakeThemeSlotDao;
    private WaitingRepository waitingRepository;

    @BeforeEach
    void setUp() {
        FakeTimeDao fakeReservationTimeDao = new FakeTimeDao();
        FakeThemeDao fakeThemeDao = new FakeThemeDao();
        fakeReservationDao = new FakeReservationDao();
        fakeThemeSlotDao = new FakeThemeSlotDao();
        waitingRepository = mock(WaitingRepository.class);

        when(waitingRepository.existsByMemberNameAndThemeAndDateAndTime(any(), any(), any(), any()))
                .thenReturn(false);
        when(waitingRepository.findFirstByThemeAndDateAndTimeOrderByIdAsc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(waitingRepository.findWithRankByMemberName(any()))
                .thenReturn(List.of());

        reservationService = new ReservationService(
                fakeReservationDao,
                fakeThemeSlotDao,
                waitingRepository
        );
        Time savedTime = fakeReservationTimeDao.save(Time.of(LocalTime.of(10, 0)));
        Theme savedTheme = fakeThemeDao.save(new Theme("이름", "설명", "test.com"));
        savedThemeSlot1 = fakeThemeSlotDao.save(new ThemeSlot(savedTheme, LocalDate.now().plusDays(1), savedTime, false));
        savedThemeSlot2 = fakeThemeSlotDao.save(new ThemeSlot(savedTheme, LocalDate.now().plusDays(2), savedTime, false));
    }

    @Test
    @DisplayName("원시값을 받아 연관된 객체를 조회하여 확정 예약을 생성한다.")
    void saveReservation() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());

        assertThat(reservation.getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(reservation.getReservationStatus()).isEqualTo(ConfirmedStatus.getInstance());
        assertThat(reservation.getThemeSlot().isReserved()).isTrue();
    }

    @Test
    @DisplayName("이미 확정 예약이 있는 슬롯에는 예약을 생성할 수 없다.")
    void throwExceptionWhenThemeSlotAlreadyHasConfirmedReservation() {
        reservationService.saveReservation("브라운", savedThemeSlot1.getId());

        assertThatThrownBy(() -> reservationService.saveReservation("네오", savedThemeSlot1.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 해당 날짜, 시간, 테마에 예약이 존재합니다.");
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 예약이나 대기를 이미 가지고 있으면 예약할 수 없다.")
    void throwsExceptionWhenSameUserDuplicatesReservationOrWaiting() {
        when(waitingRepository.existsByMemberNameAndThemeAndDateAndTime(any(), any(), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.saveReservation("김대기", savedThemeSlot1.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 같은 시간에 예약 또는 대기를 신청했습니다.");
    }

    @Test
    @DisplayName("존재하는 예약을 식별자를 통해 삭제하면 목록에서 사라지고 슬롯이 해제된다.")
    void removeReservation() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());

        reservationService.removeReservation(reservation.getId());

        ThemeSlot themeSlot = fakeThemeSlotDao.findById(savedThemeSlot1.getId()).orElseThrow();
        assertThat(reservationService.allReservations()).isEmpty();
        assertThat(themeSlot.isReserved()).isFalse();
    }

    @Test
    @DisplayName("확정 예약을 취소하면 취소 상태가 되고 대기가 없으면 슬롯이 해제된다.")
    void cancelConfirmedReservationWithoutWaiting() {
        Reservation confirmReservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());

        reservationService.cancelReservation(confirmReservation.getId(), "브라운");

        Reservation findReservation = findReservation(confirmReservation.getId());
        ThemeSlot findThemeSlot = fakeThemeSlotDao.findById(savedThemeSlot1.getId()).orElseThrow();
        assertThat(findReservation.getReservationStatus()).isEqualTo(CancelledStatus.getInstance());
        assertThat(findThemeSlot.isReserved()).isFalse();
    }

    @Test
    @DisplayName("확정 예약이 다른 빈 슬롯으로 변경된다.")
    void modifyConfirmedReservationToEmptyThemeSlot() {
        Reservation confirmReservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());

        Reservation modifiedReservation = reservationService.modifyReservation(confirmReservation.getId(), savedThemeSlot2.getId());

        ThemeSlot previousThemeSlot = fakeThemeSlotDao.findById(savedThemeSlot1.getId()).orElseThrow();
        ThemeSlot modifiedThemeSlot = fakeThemeSlotDao.findById(savedThemeSlot2.getId()).orElseThrow();
        assertThat(modifiedReservation.getThemeSlot().getId()).isEqualTo(savedThemeSlot2.getId());
        assertThat(modifiedReservation.getReservationStatus()).isEqualTo(ConfirmedStatus.getInstance());
        assertThat(previousThemeSlot.isReserved()).isFalse();
        assertThat(modifiedThemeSlot.isReserved()).isTrue();
    }

    @Test
    @DisplayName("예약이 이미 예약된 슬롯으로 변경되면 예외가 발생한다.")
    void throwExceptionWhenModifyReservationToReservedThemeSlot() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        reservationService.saveReservation("네오", savedThemeSlot2.getId());

        assertThatThrownBy(() -> reservationService.modifyReservation(reservation.getId(), savedThemeSlot2.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 해당 날짜, 시간, 테마에 예약이 존재합니다.");
    }

    @Test
    @DisplayName("예약 변경 시 기존 슬롯과 대상 슬롯을 식별자 순서대로 잠근다.")
    void lockThemeSlotsInIdOrderWhenModifyReservation() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        fakeReservationDao.clearFindByIdForUpdateHistory();
        fakeThemeSlotDao.clearFindByIdForUpdateHistory();

        reservationService.modifyReservation(reservation.getId(), savedThemeSlot2.getId());

        assertThat(fakeReservationDao.findByIdForUpdateHistory()).containsExactly(reservation.getId());
        assertThat(fakeThemeSlotDao.findByIdForUpdateHistory())
                .containsExactly(savedThemeSlot1.getId(), savedThemeSlot2.getId());
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
    @DisplayName("예약 취소 시 예약 식별자를 잠금 조회한다.")
    void lockReservationWhenCancelReservation() {
        Reservation reservation = reservationService.saveReservation("브라운", savedThemeSlot1.getId());
        fakeReservationDao.clearFindByIdForUpdateHistory();

        reservationService.cancelReservation(reservation.getId(), "브라운");

        assertThat(fakeReservationDao.findByIdForUpdateHistory()).containsExactly(reservation.getId());
    }

    @Test
    @DisplayName("내 예약 조회는 예약 목록과 Waiting 테이블의 대기 목록을 분리해서 반환한다.")
    void findReservationByName() {
        reservationService.saveReservation("브라운", savedThemeSlot1.getId());

        assertThat(reservationService.findReservationBy("브라운").reservationResponses()).hasSize(1);
        assertThat(reservationService.findReservationBy("브라운").waitingReservationResponses()).isEmpty();
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
        fakeReservationDao.updateStatus(completedReservation, reservation.getReservationStatusName());
    }
}
