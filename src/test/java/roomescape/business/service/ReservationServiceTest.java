package roomescape.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.business.dto.ReservationDto;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.ReservationTimes;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.repository.Themes;
import roomescape.business.model.repository.Users;
import roomescape.business.model.vo.Id;
import roomescape.exception.business.DuplicatedException;
import roomescape.exception.business.NotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private Users users;

    @Mock
    private Reservations reservations;

    @Mock
    private ReservationTimes reservationTimes;

    @Mock
    private Themes themes;

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

        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(reservationTimes.findById(Id.create(timeId))).thenReturn(Optional.of(reservationTime));
        when(themes.findById(Id.create(themeId))).thenReturn(Optional.of(theme));
        when(reservations.isDuplicateDateAndTimeAndTheme(eq(date), eq(LocalTime.of(10, 0)), eq(theme.getId())))
                .thenReturn(false);

        // when
        ReservationDto result = sut.addAndGet(date, timeId, themeId, userIdValue);

        // then
        assertThat(result).isNotNull();
        verify(users).findById(userId);
        verify(reservationTimes).findById(Id.create(timeId));
        verify(themes).findById(Id.create(themeId));
        verify(reservations).isDuplicateDateAndTimeAndTheme(eq(date), any(LocalTime.class), eq(theme.getId()));
        verify(reservations).save(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_시간_ID로_예약_시_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        String timeId = "nonexistent-time-id";
        String themeId = "theme-id";
        String userId = "user-id";

        User user = User.restore(userId, "USER", "Test User", "test@example.com", "password");

        when(users.findById(Id.create(userId))).thenReturn(Optional.of(user));
        when(reservationTimes.findById(Id.create(timeId))).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> sut.addAndGet(date, timeId, themeId, userId))
                .isInstanceOf(NotFoundException.class);

        verify(users).findById(Id.create(userId));
        verify(reservationTimes).findById(Id.create(timeId));
        verifyNoInteractions(themes);
        verifyNoInteractions(reservations);
    }

    @Test
    void 존재하지_않는_테마_ID로_예약_시_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        String timeIdValue = "time-id";
        String themeIdValue = "nonexistent-theme-id";
        String userIdValue = "user-id";
        Id timeId = Id.create(timeIdValue);
        Id themeId = Id.create(themeIdValue);
        Id userId = Id.create(userIdValue);

        User user = User.restore(userIdValue, "USER", "Test User", "test@example.com", "password");
        ReservationTime reservationTime = ReservationTime.restore(timeIdValue, LocalTime.of(10, 0));

        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(reservationTimes.findById(timeId)).thenReturn(Optional.of(reservationTime));
        when(themes.findById(themeId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> sut.addAndGet(date, timeIdValue, themeIdValue, userIdValue))
                .isInstanceOf(NotFoundException.class);

        verify(users).findById(userId);
        verify(reservationTimes).findById(timeId);
        verify(themes).findById(themeId);
        verifyNoInteractions(reservations);
    }

    @Test
    void 중복된_예약_시_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        String timeIdValue = "time-id";
        String themeIdValue = "theme-id";
        String userIdValue = "user-id";
        Id timeId = Id.create(timeIdValue);
        Id themeId = Id.create(themeIdValue);
        Id userId = Id.create(userIdValue);

        User user = User.restore(userIdValue, "USER", "Test User", "test@example.com", "password");
        ReservationTime reservationTime = ReservationTime.restore(timeIdValue, LocalTime.of(10, 0));
        Theme theme = Theme.restore(themeIdValue, "Test Theme", "Description", "thumbnail.jpg");

        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(reservationTimes.findById(timeId)).thenReturn(Optional.of(reservationTime));
        when(themes.findById(themeId)).thenReturn(Optional.of(theme));
        when(reservations.isDuplicateDateAndTimeAndTheme(eq(date), any(LocalTime.class), eq(theme.getId())))
                .thenReturn(true);

        // when, then
        assertThatThrownBy(() -> sut.addAndGet(date, timeIdValue, themeIdValue, userIdValue))
                .isInstanceOf(DuplicatedException.class);

        verify(users).findById(userId);
        verify(reservationTimes).findById(timeId);
        verify(themes).findById(themeId);
        verify(reservations).isDuplicateDateAndTimeAndTheme(eq(date), any(LocalTime.class), eq(theme.getId()));
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
                LocalDate.now().plusDays(1),
                ReservationTime.restore("time-id", LocalTime.of(10, 0)),
                Theme.restore("theme-id", "Test Theme", "Description", "thumbnail.jpg")
        );

        when(reservations.findById(Id.create(reservationId))).thenReturn(Optional.of(reservation));

        // when
        sut.delete(reservationId, userId);

        // then
        verify(reservations).deleteById(Id.create(reservationId));
    }
}
