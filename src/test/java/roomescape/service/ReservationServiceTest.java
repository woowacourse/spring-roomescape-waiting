package roomescape.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.dao.QueryTimeoutException;
import roomescape.domain.*;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private final ReservationRepository reservationRepository = mock();
    private final ReservationTimeRepository reservationTimeRepository = mock();
    private final ThemeRepository themeRepository = mock();
    private final ReservationWaitingRepository waitingRepository = mock();
    private final ReservationValidator reservationValidator = new ReservationValidator(reservationRepository);
    private final ReservationService service = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            themeRepository,
            waitingRepository,
            reservationValidator);

    private final LocalDate date = LocalDate.now().plusDays(1);
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void 이름으로_예약_목록을_조회한다() {
        // given
        String name = "브라운";
        ReservationTime time = time(1L);
        List<Reservation> reservations = List.of(
                reservation(1L, name, date, time),
                reservation(2L, name, date.plusDays(1), time));
        when(reservationRepository.findByReserver(new Reserver(name)))
                .thenReturn(reservations);

        // when
        List<Reservation> result = service.findByName(name);

        // then
        assertThat(result).isEqualTo(reservations);
        verify(reservationRepository, times(1)).findByReserver(new Reserver(name));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
    }

    @Test
    void 전체_예약_목록을_조회한다() {
        // given
        ReservationTime time = time(1L);
        List<Reservation> reservations = List.of(
                reservation(1L, "브라운", date, time),
                reservation(2L, "구구", date, time));
        when(reservationRepository.findAll())
                .thenReturn(reservations);

        // when
        List<Reservation> result = service.findAll();

        // then
        assertThat(result).isEqualTo(reservations);
        verify(reservationRepository, times(1)).findAll();
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
    }

    @Test
    void 사용자_예약을_생성한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        Long timeId = 1L;
        Long themeId = 1L;
        ReservationTime time = time(timeId);
        Theme theme = theme(themeId);
        Reservation savedReservation = reservation(id, name, date, time, theme);
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(time));
        when(reservationRepository.existsBySlot(any(ReservationSlot.class)))
                .thenReturn(false);
        when(themeRepository.findById(themeId))
                .thenReturn(Optional.of(theme));
        when(reservationRepository.insert(any(Reservation.class)))
                .thenReturn(savedReservation);

        // when
        Reservation result = service.createByUser(name, date, timeId, themeId, now);

        // then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        assertThat(result).isEqualTo(savedReservation);
        verify(reservationTimeRepository, times(1)).findById(timeId);
        verify(reservationRepository, times(1)).existsBySlot(any(ReservationSlot.class));
        verify(themeRepository, times(1)).findById(themeId);
        verify(reservationRepository, times(1)).insert(captor.capture());
        Reservation captured = captor.getValue();
        assertAll(
                () -> assertThat(captured.getId()).isNull(),
                () -> assertThat(captured.getName()).isEqualTo(name),
                () -> assertThat(captured.getSlot().getDate()).isEqualTo(date),
                () -> assertThat(captured.getSlot().getTime()).isEqualTo(time),
                () -> assertThat(captured.getSlot().getTheme()).isEqualTo(theme));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
    }

    @Test
    void 관리자_예약을_생성한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        Long timeId = 1L;
        Long themeId = 1L;
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime time = time(timeId);
        Theme theme = theme(themeId);
        Reservation savedReservation = reservation(id, name, pastDate, time, theme);
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(time));
        when(reservationRepository.existsBySlot(any(ReservationSlot.class)))
                .thenReturn(false);
        when(themeRepository.findById(themeId))
                .thenReturn(Optional.of(theme));
        when(reservationRepository.insert(any(Reservation.class)))
                .thenReturn(savedReservation);

        // when
        Reservation result = service.createByAdmin(name, pastDate, timeId, themeId);

        // then
        assertThat(result).isEqualTo(savedReservation);
        verify(reservationTimeRepository, times(1)).findById(timeId);
        verify(reservationRepository, times(1)).existsBySlot(any(ReservationSlot.class));
        verify(themeRepository, times(1)).findById(themeId);
        verify(reservationRepository, times(1)).insert(any(Reservation.class));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
    }

    @Test
    void 사용자는_지난_날짜나_시간으로_예약_생성시_예외_발생() {
        // given
        Long timeId = 1L;
        Long themeId = 1L;
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime time = time(timeId);
        Theme theme = theme(themeId);
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(themeId))
                .thenReturn(Optional.of(theme));

        // when & then
        assertThatThrownBy(() -> service.createByUser("브라운", pastDate, timeId, themeId, now))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAST_SCHEDULE)
                .hasMessage("이미 지난 시간으로는 예약할 수 없습니다.");

        verify(reservationTimeRepository, times(1)).findById(timeId);
        verify(themeRepository, times(1)).findById(themeId);
        verify(reservationRepository, never()).existsBySlot(any(ReservationSlot.class));
        verify(reservationRepository, never()).insert(any(Reservation.class));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
    }

    @Test
    void 사용자_본인_예약을_삭제한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        Reservation reservation = pendingReservation(id, name, date);
        when(reservationRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(reservation));
        when(waitingRepository.findFirstBySlotForUpdate(reservation.getSlot()))
                .thenReturn(Optional.empty());

        // when
        service.deleteByUser(id, name, now);

        // then
        verify(reservationRepository, times(1)).findByIdForUpdate(id);
        verify(waitingRepository, times(1)).findFirstBySlotForUpdate(reservation.getSlot());
        verify(reservationRepository, times(1)).delete(id);
        verify(reservationRepository, never()).insert(any(Reservation.class));
        verify(waitingRepository, never()).delete(any());
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    @Test
    void 사용자_본인_예약_삭제시_첫번째_대기를_예약으로_자동_승격한다() {
        // given
        Long id = 1L;
        Long waitingId = 2L;
        String name = "브라운";
        Reservation reservation = pendingReservation(id, name, date);
        ReservationWaiting waiting = waiting(waitingId, "구구", reservation.getSlot());
        when(reservationRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(reservation));
        when(waitingRepository.findFirstBySlotForUpdate(reservation.getSlot()))
                .thenReturn(Optional.of(waiting));
        when(reservationRepository.insert(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.<Reservation>getArgument(0).withId(3L));

        // when
        service.deleteByUser(id, name, now);

        // then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        InOrder inOrder = inOrder(reservationRepository, waitingRepository);
        inOrder.verify(reservationRepository).findByIdForUpdate(id);
        inOrder.verify(waitingRepository).findFirstBySlotForUpdate(reservation.getSlot());
        inOrder.verify(reservationRepository).delete(id);
        inOrder.verify(reservationRepository).insert(captor.capture());
        inOrder.verify(waitingRepository).delete(waitingId);

        Reservation promotedReservation = captor.getValue();
        assertAll(
                () -> assertThat(promotedReservation.getId()).isNull(),
                () -> assertThat(promotedReservation.getName()).isEqualTo(waiting.getName()),
                () -> assertThat(promotedReservation.getSlot()).isEqualTo(waiting.getSlot()),
                () -> assertThat(promotedReservation.getStatus()).isEqualTo(ReservationStatus.PENDING));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    @Test
    void 결제_확정_예약은_사용자가_삭제할_수_없다() {
        Long id = 1L;
        Reservation reservation = reservation(id, "브라운", date);
        when(reservationRepository.findByIdForUpdate(id)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.deleteByUser(id, "브라운", now))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_CANCELLATION_REQUIRED)
                .hasMessage("결제가 완료된 예약은 결제 취소 후 삭제할 수 있습니다.");

        verify(reservationRepository).findByIdForUpdate(id);
        verify(waitingRepository, never()).findFirstBySlotForUpdate(any(ReservationSlot.class));
        verify(reservationRepository, never()).delete(any());
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    @Test
    void 관리자_예약을_삭제한다() {
        // given
        Long id = 1L;
        Reservation reservation = reservation(id, "브라운", date);
        when(reservationRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(reservation));
        when(waitingRepository.findFirstBySlotForUpdate(reservation.getSlot()))
                .thenReturn(Optional.empty());

        // when
        service.deleteByAdmin(id, now);

        // then
        verify(reservationRepository, times(1)).findByIdForUpdate(id);
        verify(waitingRepository, times(1)).findFirstBySlotForUpdate(reservation.getSlot());
        verify(reservationRepository, times(1)).delete(id);
        verify(reservationRepository, never()).insert(any(Reservation.class));
        verify(waitingRepository, never()).delete(any());
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    @Test
    void 관리자_예약_삭제시_첫번째_대기를_예약으로_자동_승격한다() {
        // given
        Long id = 1L;
        Long waitingId = 2L;
        Reservation reservation = reservation(id, "브라운", date);
        ReservationWaiting waiting = waiting(waitingId, "구구", reservation.getSlot());
        when(reservationRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(reservation));
        when(waitingRepository.findFirstBySlotForUpdate(reservation.getSlot()))
                .thenReturn(Optional.of(waiting));
        when(reservationRepository.insert(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.<Reservation>getArgument(0).withId(3L));

        // when
        service.deleteByAdmin(id, now);

        // then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        InOrder inOrder = inOrder(reservationRepository, waitingRepository);
        inOrder.verify(reservationRepository).findByIdForUpdate(id);
        inOrder.verify(waitingRepository).findFirstBySlotForUpdate(reservation.getSlot());
        inOrder.verify(reservationRepository).delete(id);
        inOrder.verify(reservationRepository).insert(captor.capture());
        inOrder.verify(waitingRepository).delete(waitingId);

        Reservation promotedReservation = captor.getValue();
        assertAll(
                () -> assertThat(promotedReservation.getId()).isNull(),
                () -> assertThat(promotedReservation.getName()).isEqualTo(waiting.getName()),
                () -> assertThat(promotedReservation.getSlot()).isEqualTo(waiting.getSlot()));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    @Test
    void 관리자가_지난_예약을_삭제하면_대기를_자동_승격하지_않는다() {
        // given
        Long id = 1L;
        Reservation reservation = reservation(id, "브라운", now.toLocalDate().minusDays(1));
        when(reservationRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(reservation));

        // when
        service.deleteByAdmin(id, now);

        // then
        verify(reservationRepository, times(1)).findByIdForUpdate(id);
        verify(reservationRepository, times(1)).delete(id);
        verify(waitingRepository, never()).findFirstBySlotForUpdate(any(ReservationSlot.class));
        verify(reservationRepository, never()).insert(any(Reservation.class));
        verify(waitingRepository, never()).delete(any());
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    @Test
    void 존재하지_않는_예약을_관리자가_삭제하면_아무것도_하지_않는다() {
        // given
        Long id = 999L;
        when(reservationRepository.findByIdForUpdate(id))
                .thenReturn(Optional.empty());

        // when
        service.deleteByAdmin(id, now);

        // then
        verify(reservationRepository, times(1)).findByIdForUpdate(id);
        verify(waitingRepository, never()).findFirstBySlotForUpdate(any(ReservationSlot.class));
        verify(reservationRepository, never()).delete(any());
        verify(reservationRepository, never()).insert(any(Reservation.class));
        verify(waitingRepository, never()).delete(any());
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    @Test
    void 자동_승격할_예약_등록이_실패하면_대기는_삭제하지_않는다() {
        // given
        Long id = 1L;
        Long waitingId = 2L;
        Reservation reservation = reservation(id, "브라운", date);
        ReservationWaiting waiting = waiting(waitingId, "구구", reservation.getSlot());
        when(reservationRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(reservation));
        when(waitingRepository.findFirstBySlotForUpdate(reservation.getSlot()))
                .thenReturn(Optional.of(waiting));
        when(reservationRepository.insert(any(Reservation.class)))
                .thenThrow(new QueryTimeoutException("insert failed"));

        // when & then
        assertThatThrownBy(() -> service.deleteByAdmin(id, now))
                .isInstanceOf(QueryTimeoutException.class);

        verify(reservationRepository, times(1)).findByIdForUpdate(id);
        verify(waitingRepository, times(1)).findFirstBySlotForUpdate(reservation.getSlot());
        verify(reservationRepository, times(1)).delete(id);
        verify(reservationRepository, times(1)).insert(any(Reservation.class));
        verify(waitingRepository, never()).delete(any());
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    @Test
    void 사용자_본인_예약을_변경한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        Long timeId = 2L;
        LocalDate updateDate = date.plusDays(1);
        ReservationTime originalTime = time(1L);
        ReservationTime updateTime = time(timeId, "10:00");
        Reservation reservation = reservation(id, name, date, originalTime);
        when(reservationRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(reservation));
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(updateTime));
        when(reservationRepository.existsBySlot(any(ReservationSlot.class)))
                .thenReturn(false);
        when(waitingRepository.findFirstBySlotForUpdate(reservation.getSlot()))
                .thenReturn(Optional.empty());
        when(reservationRepository.update(any(Reservation.class)))
                .thenReturn(1);

        // when
        Reservation result = service.updateByUser(id, name, updateDate, timeId, now);

        // then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        assertAll(
                () -> assertThat(result.getId()).isEqualTo(id),
                () -> assertThat(result.getName()).isEqualTo(name),
                () -> assertThat(result.getSlot().getDate()).isEqualTo(updateDate),
                () -> assertThat(result.getSlot().getTime()).isEqualTo(updateTime),
                () -> assertThat(result.getSlot().getTheme()).isEqualTo(reservation.getSlot().getTheme()));
        verify(reservationRepository, times(1)).findByIdForUpdate(id);
        verify(reservationTimeRepository, times(1)).findById(timeId);
        verify(reservationRepository, times(1)).existsBySlot(any(ReservationSlot.class));
        verify(waitingRepository, times(1)).findFirstBySlotForUpdate(reservation.getSlot());
        verify(reservationRepository, times(1)).update(captor.capture());
        verify(reservationRepository, never()).insert(any(Reservation.class));
        verify(waitingRepository, never()).delete(any());
        Reservation captured = captor.getValue();
        assertAll(
                () -> assertThat(captured.getId()).isEqualTo(id),
                () -> assertThat(captured.getName()).isEqualTo(name),
                () -> assertThat(captured.getSlot().getDate()).isEqualTo(updateDate),
                () -> assertThat(captured.getSlot().getTime()).isEqualTo(updateTime),
                () -> assertThat(captured.getSlot().getTheme()).isEqualTo(reservation.getSlot().getTheme()));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    @Test
    void 사용자_본인_예약_변경시_기존_슬롯의_첫번째_대기를_예약으로_자동_승격한다() {
        // given
        Long id = 1L;
        Long waitingId = 2L;
        String name = "브라운";
        Long timeId = 2L;
        LocalDate updateDate = date.plusDays(1);
        ReservationTime originalTime = time(1L);
        ReservationTime updateTime = time(timeId, "10:00");
        Reservation reservation = reservation(id, name, date, originalTime);
        ReservationWaiting waiting = waiting(waitingId, "구구", reservation.getSlot());
        when(reservationRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(reservation));
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(updateTime));
        when(reservationRepository.existsBySlot(any(ReservationSlot.class)))
                .thenReturn(false);
        when(waitingRepository.findFirstBySlotForUpdate(reservation.getSlot()))
                .thenReturn(Optional.of(waiting));
        when(reservationRepository.update(any(Reservation.class)))
                .thenReturn(1);
        when(reservationRepository.insert(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.<Reservation>getArgument(0).withId(3L));

        // when
        Reservation result = service.updateByUser(id, name, updateDate, timeId, now);

        // then
        ArgumentCaptor<Reservation> updateCaptor = ArgumentCaptor.forClass(Reservation.class);
        ArgumentCaptor<Reservation> insertCaptor = ArgumentCaptor.forClass(Reservation.class);
        InOrder inOrder = inOrder(reservationRepository, waitingRepository);
        inOrder.verify(reservationRepository).findByIdForUpdate(id);
        inOrder.verify(reservationRepository).existsBySlot(any(ReservationSlot.class));
        inOrder.verify(waitingRepository).findFirstBySlotForUpdate(reservation.getSlot());
        inOrder.verify(reservationRepository).update(updateCaptor.capture());
        inOrder.verify(reservationRepository).insert(insertCaptor.capture());
        inOrder.verify(waitingRepository).delete(waitingId);

        Reservation updatedReservation = updateCaptor.getValue();
        Reservation promotedReservation = insertCaptor.getValue();
        assertAll(
                () -> assertThat(result).isEqualTo(updatedReservation),
                () -> assertThat(updatedReservation.getId()).isEqualTo(id),
                () -> assertThat(updatedReservation.getSlot().getDate()).isEqualTo(updateDate),
                () -> assertThat(updatedReservation.getSlot().getTime()).isEqualTo(updateTime),
                () -> assertThat(promotedReservation.getId()).isNull(),
                () -> assertThat(promotedReservation.getName()).isEqualTo(waiting.getName()),
                () -> assertThat(promotedReservation.getSlot()).isEqualTo(waiting.getSlot()),
                () -> assertThat(promotedReservation.getStatus()).isEqualTo(ReservationStatus.PENDING));
        verify(reservationTimeRepository, times(1)).findById(timeId);
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    @Test
    void 사용자_본인_예약_변경시_날짜만_변경한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        LocalDate updateDate = date.plusDays(1);
        ReservationTime time = time(1L);
        Reservation reservation = reservation(id, name, date, time);
        when(reservationRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(reservation));
        when(reservationRepository.existsBySlot(any(ReservationSlot.class)))
                .thenReturn(false);
        when(waitingRepository.findFirstBySlotForUpdate(reservation.getSlot()))
                .thenReturn(Optional.empty());
        when(reservationRepository.update(any(Reservation.class)))
                .thenReturn(1);

        // when
        Reservation result = service.updateByUser(id, name, updateDate, null, now);

        // then
        assertAll(
                () -> assertThat(result.getSlot().getDate()).isEqualTo(updateDate),
                () -> assertThat(result.getSlot().getTime()).isEqualTo(time));
        verify(reservationRepository, times(1)).findByIdForUpdate(id);
        verify(reservationRepository, times(1)).existsBySlot(any(ReservationSlot.class));
        verify(waitingRepository, times(1)).findFirstBySlotForUpdate(reservation.getSlot());
        verify(reservationRepository, times(1)).update(any(Reservation.class));
        verify(reservationRepository, never()).insert(any(Reservation.class));
        verify(waitingRepository, never()).delete(any());
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    @Test
    void 결제_대기_예약은_사용자가_변경할_수_없다() {
        Long id = 1L;
        Reservation reservation = pendingReservation(id, "브라운", date);
        when(reservationRepository.findByIdForUpdate(id)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.updateByUser(id, "브라운", date.plusDays(1), 2L, now))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PENDING_RESERVATION_LOCKED)
                .hasMessage("결제 대기 중인 예약은 변경할 수 없습니다.");

        verify(reservationRepository).findByIdForUpdate(id);
        verify(reservationTimeRepository, never()).findById(any());
        verify(waitingRepository, never()).findFirstBySlotForUpdate(any(ReservationSlot.class));
        verify(reservationRepository, never()).update(any());
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    @Test
    void 존재하지_않는_예약_변경시_예외_발생() {
        // given
        Long id = 999L;
        when(reservationRepository.findByIdForUpdate(id))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.updateByUser(id, "브라운", date, 1L, now))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
                .hasMessage("존재하지 않는 예약입니다.");

        verify(reservationRepository, times(1)).findByIdForUpdate(id);
        verify(waitingRepository, never()).findFirstBySlotForUpdate(any(ReservationSlot.class));
        verify(reservationRepository, never()).update(any(Reservation.class));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    @Test
    void 존재하지_않는_시간으로_예약_변경시_예외_발생() {
        // given
        Long id = 1L;
        String name = "브라운";
        Long timeId = 999L;
        Reservation reservation = reservation(id, name, date);
        when(reservationRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(reservation));
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.updateByUser(id, name, date.plusDays(1), timeId, now))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
                .hasMessage("존재하지 않는 예약 시간입니다.");

        verify(reservationRepository, times(1)).findByIdForUpdate(id);
        verify(reservationTimeRepository, times(1)).findById(timeId);
        verify(waitingRepository, never()).findFirstBySlotForUpdate(any(ReservationSlot.class));
        verify(reservationRepository, never()).update(any(Reservation.class));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository);
    }

    private Reservation reservation(Long id, String name, LocalDate date) {
        return reservation(id, name, date, time(1L));
    }

    private Reservation reservation(Long id, String name, LocalDate date, ReservationTime time) {
        return reservation(id, name, date, time, theme(1L));
    }

    private Reservation reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(
                id,
                new Reserver(name),
                new ReservationSlot(date, time, theme));
    }

    private Reservation pendingReservation(Long id, String name, LocalDate date) {
        return new Reservation(id, new Reserver(name), new ReservationSlot(date, time(1L), theme(1L)),
                ReservationStatus.PENDING);
    }

    private ReservationWaiting waiting(Long id, String name, ReservationSlot slot) {
        return new ReservationWaiting(id, new Reserver(name), slot);
    }

    private ReservationTime time(Long id) {
        return time(id, "08:00");
    }

    private ReservationTime time(Long id, String startAt) {
        return new ReservationTime(id, LocalTime.parse(startAt));
    }

    private Theme theme(Long id) {
        return new Theme(id, "테스트 테마", "테마 설명", "썸네일 주소");
    }
}
