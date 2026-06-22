package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaReservationWaitingRepository;
import roomescape.repository.jpa.JpaThemeRepository;

class ReservationServiceTest {

    private final JpaReservationRepository reservationRepository = mock();
    private final JpaReservationTimeRepository reservationTimeRepository = mock();
    private final JpaReservationWaitingRepository reservationWaitingRepository = mock();
    private final JpaThemeRepository themeRepository = mock();
    private final ReservationValidator reservationValidator = new ReservationValidator(reservationRepository);
    private final ReservationService service = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            reservationWaitingRepository,
            themeRepository,
            reservationValidator);

    private final LocalDate date = LocalDate.now().plusDays(1);

    @Test
    void 이름으로_예약_목록을_조회한다() {
        // given
        String name = "브라운";
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));
        Theme theme = new Theme(1L, "테스트 테마", "테마 설명", "썸네일 주소");
        List<Reservation> reservations = List.of(
                new Reservation(1L, name, date, time, theme),
                new Reservation(2L, name, date.plusDays(1), time, theme));
        when(reservationRepository.findByName(name))
                .thenReturn(reservations);

        // when
        List<Reservation> result = service.findByName(name);

        // then
        assertThat(result).isEqualTo(reservations);
    }

    @Test
    void 전체_예약_목록을_조회한다() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));
        Theme theme = new Theme(1L, "테스트 테마", "테마 설명", "썸네일 주소");
        List<Reservation> reservations = List.of(
                new Reservation(1L, "브라운", date, time, theme),
                new Reservation(2L, "구구", date, time, theme));
        when(reservationRepository.findAll())
                .thenReturn(reservations);

        // when
        List<Reservation> result = service.findAll();

        // then
        assertThat(result).isEqualTo(reservations);
    }

    @Test
    void 사용자_예약을_생성한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        Long timeId = 1L;
        Long themeId = 1L;
        ReservationTime time = new ReservationTime(timeId, LocalTime.parse("08:00"));
        Theme theme = new Theme(themeId, "테스트 테마", "테마 설명", "썸네일 주소");
        Reservation savedReservation = new Reservation(id, name, date, time, theme);
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(time));
        when(reservationRepository.existsByDateAndTime_IdAndTheme_Id(date, timeId, themeId))
                .thenReturn(false);
        when(themeRepository.findById(themeId))
                .thenReturn(Optional.of(theme));
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(savedReservation);
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(savedReservation));

        // when
        Reservation result = service.create(name, date, timeId, themeId);

        // then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        assertThat(result).isEqualTo(savedReservation);
        verify(reservationRepository, times(1)).save(captor.capture());
        Reservation captured = captor.getValue();
        assertAll(
                () -> assertThat(captured.getId()).isNull(),
                () -> assertThat(captured.getName()).isEqualTo(name),
                () -> assertThat(captured.getDate()).isEqualTo(date),
                () -> assertThat(captured.getTime()).isEqualTo(time),
                () -> assertThat(captured.getTheme()).isEqualTo(theme));
    }

    @Test
    void 관리자_예약을_생성한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        Long timeId = 1L;
        Long themeId = 1L;
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime time = new ReservationTime(timeId, LocalTime.parse("08:00"));
        Theme theme = new Theme(themeId, "테스트 테마", "테마 설명", "썸네일 주소");
        Reservation savedReservation = new Reservation(id, name, pastDate, time, theme);
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(time));
        when(reservationRepository.existsByDateAndTime_IdAndTheme_Id(pastDate, timeId, themeId))
                .thenReturn(false);
        when(themeRepository.findById(themeId))
                .thenReturn(Optional.of(theme));
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(savedReservation);
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(savedReservation));

        // when
        Reservation result = service.createByAdmin(name, pastDate, timeId, themeId);

        // then
        assertThat(result).isEqualTo(savedReservation);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void 사용자는_지난_날짜나_시간으로_예약_생성시_예외_발생() {
        // given
        Long timeId = 1L;
        Long themeId = 1L;
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime time = new ReservationTime(timeId, LocalTime.parse("08:00"));
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(time));

        // when & then
        assertThatThrownBy(() -> service.create("브라운", pastDate, timeId, themeId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 지난 시간으로는 예약할 수 없습니다.");

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 사용자_본인_예약을_삭제한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));
        Reservation reservation = createReservation(id, name, date, time);
        ReservationWaiting waiting = new ReservationWaiting(1L, "구구", date, time, reservation.getTheme());
        Reservation promotedReservation = createReservation(2L, "구구", date, time);
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation));
        when(reservationWaitingRepository.findFirstByDateAndTime_IdAndTheme_IdOrderByCreatedAtAscIdAsc(date,
                time.getId(), reservation.getTheme().getId()))
                .thenReturn(Optional.of(waiting));
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(promotedReservation);
        when(reservationRepository.findById(promotedReservation.getId()))
                .thenReturn(Optional.of(promotedReservation));

        // when
        service.delete(id, name);

        // then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).deleteById(id);
        verify(reservationRepository, times(1)).save(captor.capture());
        verify(reservationWaitingRepository, times(1)).delete(waiting);

        Reservation captured = captor.getValue();
        assertAll(
                () -> assertThat(captured.getName()).isEqualTo(waiting.getName()),
                () -> assertThat(captured.getDate()).isEqualTo(waiting.getDate()),
                () -> assertThat(captured.getTime()).isEqualTo(waiting.getTime()),
                () -> assertThat(captured.getTheme()).isEqualTo(waiting.getTheme()));
    }

    @Test
    void 사용자_본인_예약_삭제시_다음_대기가_없으면_승격하지_않는다() {
        // given
        Long id = 1L;
        String name = "브라운";
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));
        Reservation reservation = createReservation(id, name, date, time);
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation));
        when(reservationWaitingRepository.findFirstByDateAndTime_IdAndTheme_IdOrderByCreatedAtAscIdAsc(date,
                time.getId(), reservation.getTheme().getId()))
                .thenReturn(Optional.empty());

        // when
        service.delete(id, name);

        // then
        verify(reservationRepository, times(1)).deleteById(id);
    }

    @Test
    void 예약_삭제_후_대기_승격이_충돌하면_요청이_완료되지_않았음을_안내한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));
        Reservation reservation = createReservation(id, name, date, time);
        ReservationWaiting waiting = new ReservationWaiting(1L, "구구", date, time, reservation.getTheme());
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation));
        when(reservationWaitingRepository.findFirstByDateAndTime_IdAndTheme_IdOrderByCreatedAtAscIdAsc(date,
                time.getId(), reservation.getTheme().getId()))
                .thenReturn(Optional.of(waiting));
        when(reservationRepository.save(any(Reservation.class)))
                .thenThrow(new DuplicateKeyException("duplicate reservation"));

        // when & then
        assertThatThrownBy(() -> service.delete(id, name))
                .isInstanceOfSatisfying(BusinessException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESERVATION_OPERATION_CONFLICT);
                    assertThat(exception).hasMessage(
                            "일시적인 문제로 예약 작업을 완료하지 못했습니다. 잠시 후 다시 시도해 주세요.");
                });

        verify(reservationWaitingRepository, never()).delete(waiting);
    }

    @Test
    void 관리자_예약_삭제_후_대기_승격이_충돌하면_예약_삭제가_완료되지_않았음을_안내한다() {
        // given
        Long id = 1L;
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));
        Reservation reservation = createReservation(id, "브라운", date, time);
        ReservationWaiting waiting = new ReservationWaiting(1L, "구구", date, time, reservation.getTheme());
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation));
        when(reservationWaitingRepository.findFirstByDateAndTime_IdAndTheme_IdOrderByCreatedAtAscIdAsc(date,
                time.getId(), reservation.getTheme().getId()))
                .thenReturn(Optional.of(waiting));
        when(reservationRepository.save(any(Reservation.class)))
                .thenThrow(new DuplicateKeyException("duplicate reservation"));

        // when & then
        assertThatThrownBy(() -> service.deleteByAdmin(id))
                .isInstanceOfSatisfying(BusinessException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESERVATION_OPERATION_CONFLICT);
                    assertThat(exception).hasMessage(
                            "일시적인 문제로 예약 작업을 완료하지 못했습니다. 잠시 후 다시 시도해 주세요.");
                });
    }

    @Test
    void 관리자_예약을_삭제한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));
        Reservation reservation = createReservation(id, name, date, time);
        ReservationWaiting waiting = new ReservationWaiting(1L, "구구", date, time, reservation.getTheme());
        Reservation promotedReservation = createReservation(2L, "구구", date, time);
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation));
        when(reservationWaitingRepository.findFirstByDateAndTime_IdAndTheme_IdOrderByCreatedAtAscIdAsc(date,
                time.getId(), reservation.getTheme().getId()))
                .thenReturn(Optional.of(waiting));
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(promotedReservation);
        when(reservationRepository.findById(promotedReservation.getId()))
                .thenReturn(Optional.of(promotedReservation));

        // when
        service.deleteByAdmin(id);

        // then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).deleteById(id);
        verify(reservationRepository, times(1)).save(captor.capture());
        verify(reservationWaitingRepository, times(1)).delete(waiting);

        Reservation captured = captor.getValue();
        assertAll(
                () -> assertThat(captured.getName()).isEqualTo(waiting.getName()),
                () -> assertThat(captured.getDate()).isEqualTo(waiting.getDate()),
                () -> assertThat(captured.getTime()).isEqualTo(waiting.getTime()),
                () -> assertThat(captured.getTheme()).isEqualTo(waiting.getTheme()));
    }

    @Test
    void 관리자가_과거_예약을_삭제하면_대기를_승격하지_않는다() {
        // given
        Long id = 1L;
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));
        Reservation reservation = createReservation(id, "브라운", LocalDate.now().minusDays(1), time);
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation));

        // when
        service.deleteByAdmin(id);

        // then
        verify(reservationRepository, times(1)).deleteById(id);
        verify(reservationWaitingRepository, never()).deleteById(any(Long.class));
    }

    @Test
    void 사용자_본인_예약을_변경한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        Long timeId = 2L;
        LocalDate updateDate = date.plusDays(1);
        ReservationTime originalTime = new ReservationTime(1L, LocalTime.parse("08:00"));
        ReservationTime updateTime = new ReservationTime(timeId, LocalTime.parse("10:00"));
        Reservation reservation = createReservation(id, name, date, originalTime);
        Reservation updatedReservation = createReservation(id, name, updateDate, updateTime);
        ReservationWaiting waiting = new ReservationWaiting(1L, "구구", date, originalTime, reservation.getTheme());
        Reservation promotedReservation = createReservation(2L, "구구", date, originalTime);
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation), Optional.of(updatedReservation));
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(updateTime));
        when(reservationRepository.existsByDateAndTime_IdAndTheme_Id(updateDate, timeId,
                reservation.getTheme().getId()))
                .thenReturn(false);
        when(reservationWaitingRepository.findFirstByDateAndTime_IdAndTheme_IdOrderByCreatedAtAscIdAsc(
                date,
                originalTime.getId(),
                reservation.getTheme().getId()))
                .thenReturn(Optional.of(waiting));
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(promotedReservation);

        // when
        Reservation result = service.update(id, name, updateDate, timeId);

        // then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        assertAll(
                () -> assertThat(result.getId()).isEqualTo(id),
                () -> assertThat(result.getName()).isEqualTo(name),
                () -> assertThat(result.getDate()).isEqualTo(updateDate),
                () -> assertThat(result.getTime()).isEqualTo(updateTime),
                () -> assertThat(result.getTheme()).isEqualTo(reservation.getTheme()));
        verify(reservationRepository, times(1)).flush();
        verify(reservationRepository, times(1)).save(captor.capture());
        verify(reservationWaitingRepository, times(1)).delete(waiting);
        Reservation captured = captor.getValue();
        assertAll(
                () -> assertThat(captured.getName()).isEqualTo(waiting.getName()),
                () -> assertThat(captured.getDate()).isEqualTo(date),
                () -> assertThat(captured.getTime()).isEqualTo(originalTime),
                () -> assertThat(captured.getTheme()).isEqualTo(reservation.getTheme()));
    }

    @Test
    void 사용자_본인_예약_변경시_기존_슬롯에_대기가_없으면_승격하지_않는다() {
        // given
        Long id = 1L;
        String name = "브라운";
        Long timeId = 2L;
        LocalDate updateDate = date.plusDays(1);
        ReservationTime originalTime = new ReservationTime(1L, LocalTime.parse("08:00"));
        ReservationTime updateTime = new ReservationTime(timeId, LocalTime.parse("10:00"));
        Reservation reservation = createReservation(id, name, date, originalTime);
        Reservation updatedReservation = createReservation(id, name, updateDate, updateTime);
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation), Optional.of(updatedReservation));
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(updateTime));
        when(reservationRepository.existsByDateAndTime_IdAndTheme_Id(updateDate, timeId,
                reservation.getTheme().getId()))
                .thenReturn(false);
        when(reservationWaitingRepository.findFirstByDateAndTime_IdAndTheme_IdOrderByCreatedAtAscIdAsc(
                date,
                originalTime.getId(),
                reservation.getTheme().getId()))
                .thenReturn(Optional.empty());

        // when
        Reservation result = service.update(id, name, updateDate, timeId);

        // then
        assertAll(
                () -> assertThat(result.getDate()).isEqualTo(updateDate),
                () -> assertThat(result.getTime()).isEqualTo(updateTime));
        verify(reservationRepository, times(1)).flush();
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(reservationWaitingRepository, never()).delete(any());
    }

    @Test
    void 예약_변경_후_대기_승격이_충돌하면_예약_변경이_완료되지_않았음을_안내한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        Long timeId = 2L;
        LocalDate updateDate = date.plusDays(1);
        ReservationTime originalTime = new ReservationTime(1L, LocalTime.parse("08:00"));
        ReservationTime updateTime = new ReservationTime(timeId, LocalTime.parse("10:00"));
        Reservation reservation = createReservation(id, name, date, originalTime);
        ReservationWaiting waiting = new ReservationWaiting(1L, "구구", date, originalTime, reservation.getTheme());
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation));
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(updateTime));
        when(reservationRepository.existsByDateAndTime_IdAndTheme_Id(updateDate, timeId,
                reservation.getTheme().getId()))
                .thenReturn(false);
        when(reservationWaitingRepository.findFirstByDateAndTime_IdAndTheme_IdOrderByCreatedAtAscIdAsc(
                date,
                originalTime.getId(),
                reservation.getTheme().getId()))
                .thenReturn(Optional.of(waiting));
        when(reservationRepository.save(any(Reservation.class)))
                .thenThrow(new DuplicateKeyException("duplicate reservation"));

        // when & then
        assertThatThrownBy(() -> service.update(id, name, updateDate, timeId))
                .isInstanceOfSatisfying(BusinessException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESERVATION_OPERATION_CONFLICT);
                    assertThat(exception).hasMessage(
                            "일시적인 문제로 예약 작업을 완료하지 못했습니다. 잠시 후 다시 시도해 주세요.");
                });
    }

    @Test
    void 사용자_본인_예약_변경시_날짜만_변경한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        LocalDate updateDate = date.plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));
        Reservation reservation = createReservation(id, name, date, time);
        Reservation updatedReservation = createReservation(id, name, updateDate, time);
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation), Optional.of(updatedReservation));
        when(reservationRepository.existsByDateAndTime_IdAndTheme_Id(updateDate, time.getId(),
                reservation.getTheme().getId()))
                .thenReturn(false);

        // when
        Reservation result = service.update(id, name, updateDate, null);

        // then
        assertAll(
                () -> assertThat(result.getDate()).isEqualTo(updateDate),
                () -> assertThat(result.getTime()).isEqualTo(time));
        verify(reservationRepository, times(1)).flush();
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 사용자_본인_예약_변경시_변경할_값이_없으면_예외_발생() {
        // given
        Long id = 1L;
        String name = "브라운";
        Reservation reservation = createReservation(id, name, date, new ReservationTime(1L, LocalTime.parse("08:00")));
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> service.update(id, name, null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("변경할 날짜 또는 시간이 필요합니다.");

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_예약_변경시_예외_발생() {
        // given
        Long id = 999L;
        when(reservationRepository.findById(id))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.update(id, "브라운", date, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 예약입니다.");

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_시간으로_예약_변경시_예외_발생() {
        // given
        Long id = 1L;
        String name = "브라운";
        Long timeId = 999L;
        Reservation reservation = createReservation(id, name, date, new ReservationTime(1L, LocalTime.parse("08:00")));
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation));
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.update(id, name, date.plusDays(1), timeId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    private Reservation createReservation(Long id, String name, LocalDate date, ReservationTime time) {
        return new Reservation(
                id,
                name,
                date,
                time,
                new Theme(1L, "테스트 테마", "테마 설명", "썸네일 주소"));
    }
}
