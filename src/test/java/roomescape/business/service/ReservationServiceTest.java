package roomescape.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.business.dto.ReservationDto;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.repository.Users;
import roomescape.business.reader.ReservationSlotReader;
import roomescape.exception.business.DuplicatedException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private Users users;
    @Mock
    private Reservations reservations;
    @Mock
    private ReservationSlotReader slotReader;
    @InjectMocks
    private ReservationService sut;

    @Test
    void 사용자_ID로_예약을_추가하고_반환한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);

        User user = User.member("Test User", "test@example.com", "password");
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        Theme theme = new Theme("Test Theme", "Description", "thumbnail.jpg");
        ReservationSlot slot = new ReservationSlot(time, date, theme);

        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(slotReader.findByDateAndTimeIdAndThemeId(date, time.getId().value(), theme.getId().value())).thenReturn(Optional.of(slot));
        when(reservations.isSlotFreeFor(slot, user)).thenReturn(true);

        // when
        ReservationDto result = sut.addAndGet(date, time.getId().value(), theme.getId().value(), user.getId().value());

        // then
        assertThat(result).isNotNull();
        verify(users).findById(user.getId());
        verify(slotReader).findByDateAndTimeIdAndThemeId(date, time.getId().value(), theme.getId().value());
        verify(reservations).isSlotFreeFor(slot, user);
        verify(reservations).save(any(Reservation.class));
    }

    @Test
    void 동일한_유저가_동일한_슬롯에_예약_시_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);

        User user = User.member("Test User", "test@example.com", "password");
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        Theme theme = new Theme("Test Theme", "Description", "thumbnail.jpg");
        ReservationSlot slot = new ReservationSlot(time, date, theme);

        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(slotReader.findByDateAndTimeIdAndThemeId(date, time.getId().value(), theme.getId().value())).thenReturn(Optional.of(slot));
        when(reservations.isSlotFreeFor(slot, user)).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> sut.addAndGet(date, time.getId().value(), theme.getId().value(), user.getId().value()))
                .isInstanceOf(DuplicatedException.class);

        verify(users).findById(user.getId());
        verify(reservations).isSlotFreeFor(slot, user);
        verify(reservations, never()).save(any(Reservation.class));
    }

    @Test
    void 예약_삭제_성공() {
        // given
        User user = User.member("Test User", "test@example.com", "password");
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        Theme theme = new Theme("Test Theme", "Description", "thumbnail.jpg");
        ReservationSlot slot = new ReservationSlot(time, LocalDate.now().plusDays(1), theme);
        Reservation reservation = new Reservation(user, slot);

        when(reservations.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        // when
        sut.delete(reservation.getId().value(), user.getId().value());

        // then
        verify(reservations).deleteById(reservation.getId());
    }
}
