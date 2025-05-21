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
import roomescape.business.model.vo.Id;
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
    private ReservationSlotService slotService;

    @InjectMocks
    private ReservationService sut;

    @Test
    void 사용자_ID로_예약을_추가하고_반환한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        String timeId = "time-id";
        String themeId = "theme-id";
        String userIdValue = "user-id";
        Id userId = Id.create(userIdValue);

        User user = User.restore(userIdValue, "USER", "Test User", "test@example.com", "password");
        ReservationTime reservationTime = ReservationTime.restore(timeId, LocalTime.of(10, 0));
        Theme theme = Theme.restore(themeId, "Test Theme", "Description", "thumbnail.jpg");
        ReservationSlot slot = ReservationSlot.restore("slot-id", reservationTime, date, theme);

        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(slotService.findByDateAndTimeIdAndThemeIdOrElseCreate(date, timeId, themeId)).thenReturn(slot);
        when(reservations.isSlotFreeFor(slot, user)).thenReturn(true);

        // when
        ReservationDto result = sut.addAndGet(date, timeId, themeId, userIdValue);

        // then
        assertThat(result).isNotNull();
        verify(users).findById(userId);
        verify(slotService).findByDateAndTimeIdAndThemeIdOrElseCreate(date, timeId, themeId);
        verify(reservations).isSlotFreeFor(slot, user);
        verify(reservations).save(any(Reservation.class));
    }

    @Test
    void 동일한_유저가_동일한_슬롯에_예약_시_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        String timeIdValue = "time-id";
        String themeIdValue = "theme-id";
        String userIdValue = "user-id";
        String slotIdValue = "slot-id";
        Id userId = Id.create(userIdValue);

        User user = User.restore(userIdValue, "USER", "Test User", "test@example.com", "password");
        ReservationTime reservationTime = ReservationTime.restore(timeIdValue, LocalTime.of(10, 0));
        Theme theme = Theme.restore(themeIdValue, "Test Theme", "Description", "thumbnail.jpg");
        ReservationSlot slot = ReservationSlot.restore(slotIdValue, reservationTime, date, theme);

        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(slotService.findByDateAndTimeIdAndThemeIdOrElseCreate(date, timeIdValue, themeIdValue)).thenReturn(slot);
        when(reservations.isSlotFreeFor(slot, user)).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> sut.addAndGet(date, timeIdValue, themeIdValue, userIdValue))
                .isInstanceOf(DuplicatedException.class);

        verify(users).findById(userId);
        verify(reservations).isSlotFreeFor(slot, user);
        verify(reservations, never()).save(any(Reservation.class));
    }

    @Test
    void 예약_삭제_성공() {
        // given
        String reservationId = "reservation-id";
        String userId = "user-id";

        Reservation reservation = Reservation.restore(
                reservationId,
                User.restore(userId, "USER", "Test User", "test@example.com", "password"),
                ReservationSlot.restore(
                        "slot-id",
                        ReservationTime.restore("time-id", LocalTime.of(10, 0)),
                        LocalDate.now().plusDays(1),
                        Theme.restore("theme-id", "Test Theme", "Description", "thumbnail.jpg")
                )
        );

        when(reservations.findById(Id.create(reservationId))).thenReturn(Optional.of(reservation));

        // when
        sut.delete(reservationId, userId);

        // then
        verify(reservations).deleteById(Id.create(reservationId));
    }
}
