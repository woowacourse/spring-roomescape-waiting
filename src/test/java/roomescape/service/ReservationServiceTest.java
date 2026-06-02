package roomescape.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
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
    private final ReservationValidator reservationValidator = new ReservationValidator(reservationRepository);
    private final ReservationService service = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
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
        verify(reservationRepository, times(1)).findByName(name);
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
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
        ReservationTime time = new ReservationTime(timeId, LocalTime.parse("08:00"));
        Theme theme = new Theme(themeId, "테스트 테마", "테마 설명", "썸네일 주소");
        Reservation savedReservation = new Reservation(id, name, date, time, theme);
        when(reservationTimeRepository.findBy(timeId))
                .thenReturn(Optional.of(time));
        when(reservationRepository.existsWith(date, timeId, themeId))
                .thenReturn(false);
        when(themeRepository.findBy(themeId))
                .thenReturn(Optional.of(theme));
        when(reservationRepository.insert(any(Reservation.class)))
                .thenReturn(id);
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(savedReservation));

        // when
        Reservation result = service.create(name, date, timeId, themeId);

        // then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        assertThat(result).isEqualTo(savedReservation);
        verify(reservationTimeRepository, times(1)).findBy(timeId);
        verify(reservationRepository, times(1)).existsWith(date, timeId, themeId);
        verify(themeRepository, times(1)).findBy(themeId);
        verify(reservationRepository, times(1)).insert(captor.capture());
        verify(reservationRepository, times(1)).findById(id);
        Reservation captured = captor.getValue();
        assertAll(
                () -> assertThat(captured.getId()).isNull(),
                () -> assertThat(captured.getName()).isEqualTo(name),
                () -> assertThat(captured.getDate()).isEqualTo(date),
                () -> assertThat(captured.getTime()).isEqualTo(time),
                () -> assertThat(captured.getTheme()).isEqualTo(theme));
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
        ReservationTime time = new ReservationTime(timeId, LocalTime.parse("08:00"));
        Theme theme = new Theme(themeId, "테스트 테마", "테마 설명", "썸네일 주소");
        Reservation savedReservation = new Reservation(id, name, pastDate, time, theme);
        when(reservationTimeRepository.findBy(timeId))
                .thenReturn(Optional.of(time));
        when(reservationRepository.existsWith(pastDate, timeId, themeId))
                .thenReturn(false);
        when(themeRepository.findBy(themeId))
                .thenReturn(Optional.of(theme));
        when(reservationRepository.insert(any(Reservation.class)))
                .thenReturn(id);
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(savedReservation));

        // when
        Reservation result = service.createByAdmin(name, pastDate, timeId, themeId);

        // then
        assertThat(result).isEqualTo(savedReservation);
        verify(reservationTimeRepository, times(1)).findBy(timeId);
        verify(reservationRepository, times(1)).existsWith(pastDate, timeId, themeId);
        verify(themeRepository, times(1)).findBy(themeId);
        verify(reservationRepository, times(1)).insert(any(Reservation.class));
        verify(reservationRepository, times(1)).findById(id);
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
    }

    @Test
    void 사용자는_지난_날짜나_시간으로_예약_생성시_예외_발생() {
        // given
        Long timeId = 1L;
        Long themeId = 1L;
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime time = new ReservationTime(timeId, LocalTime.parse("08:00"));
        when(reservationTimeRepository.findBy(timeId))
                .thenReturn(Optional.of(time));

        // when & then
        assertThatThrownBy(() -> service.create("브라운", pastDate, timeId, themeId))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAST_SCHEDULE)
                .hasMessage("이미 지난 시간으로는 예약할 수 없습니다.");

        verify(reservationTimeRepository, times(1)).findBy(timeId);
        verify(reservationRepository, never()).existsWith(pastDate, timeId, themeId);
        verify(reservationRepository, never()).insert(any(Reservation.class));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
    }

    @Test
    void 사용자_본인_예약을_삭제한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        Reservation reservation = createReservation(id, name, date, new ReservationTime(1L, LocalTime.parse("08:00")));
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation));

        // when
        service.delete(id, name);

        // then
        verify(reservationRepository, times(1)).findById(id);
        verify(reservationRepository, times(1)).delete(id);
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
    }

    @Test
    void 관리자_예약을_삭제한다() {
        // given
        Long id = 1L;

        // when
        service.deleteByAdmin(id);

        // then
        verify(reservationRepository, times(1)).delete(id);
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
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
        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation), Optional.of(updatedReservation));
        when(reservationTimeRepository.findBy(timeId))
                .thenReturn(Optional.of(updateTime));
        when(reservationRepository.existsWith(updateDate, timeId, reservation.getTheme().getId()))
                .thenReturn(false);

        // when
        Reservation result = service.update(id, name, updateDate, timeId);

        // then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        assertThat(result).isEqualTo(updatedReservation);
        verify(reservationRepository, times(2)).findById(id);
        verify(reservationTimeRepository, times(1)).findBy(timeId);
        verify(reservationRepository, times(1)).existsWith(updateDate, timeId, reservation.getTheme().getId());
        verify(reservationRepository, times(1)).update(captor.capture());
        Reservation captured = captor.getValue();
        assertAll(
                () -> assertThat(captured.getId()).isEqualTo(id),
                () -> assertThat(captured.getName()).isEqualTo(name),
                () -> assertThat(captured.getDate()).isEqualTo(updateDate),
                () -> assertThat(captured.getTime()).isEqualTo(updateTime),
                () -> assertThat(captured.getTheme()).isEqualTo(reservation.getTheme()));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
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
        when(reservationRepository.existsWith(updateDate, time.getId(), reservation.getTheme().getId()))
                .thenReturn(false);

        // when
        Reservation result = service.update(id, name, updateDate, null);

        // then
        assertAll(
                () -> assertThat(result.getDate()).isEqualTo(updateDate),
                () -> assertThat(result.getTime()).isEqualTo(time));
        verify(reservationRepository, times(2)).findById(id);
        verify(reservationRepository, times(1)).existsWith(updateDate, time.getId(), reservation.getTheme().getId());
        verify(reservationRepository, times(1)).update(any(Reservation.class));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
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
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT)
                .hasMessage("변경할 날짜 또는 시간이 필요합니다.");

        verify(reservationRepository, times(1)).findById(id);
        verify(reservationRepository, never()).update(any(Reservation.class));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
    }

    @Test
    void 존재하지_않는_예약_변경시_예외_발생() {
        // given
        Long id = 999L;
        when(reservationRepository.findById(id))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.update(id, "브라운", date, 1L))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
                .hasMessage("존재하지 않는 예약입니다.");

        verify(reservationRepository, times(1)).findById(id);
        verify(reservationRepository, never()).update(any(Reservation.class));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
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
        when(reservationTimeRepository.findBy(timeId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.update(id, name, date.plusDays(1), timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
                .hasMessage("존재하지 않는 예약 시간입니다.");

        verify(reservationRepository, times(1)).findById(id);
        verify(reservationTimeRepository, times(1)).findBy(timeId);
        verify(reservationRepository, never()).update(any(Reservation.class));
        verifyNoMoreInteractions(reservationRepository, reservationTimeRepository, themeRepository);
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
