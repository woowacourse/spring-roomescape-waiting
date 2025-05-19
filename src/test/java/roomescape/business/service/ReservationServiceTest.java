package roomescape.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.business.dto.ReservationDto;
import roomescape.business.model.vo.Id;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.ReservationRepository;
import roomescape.business.model.repository.ReservationTimeRepository;
import roomescape.business.model.repository.ThemeRepository;
import roomescape.business.model.repository.UserRepository;
import roomescape.exception.business.NotFoundException;
import roomescape.exception.business.DuplicatedException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

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

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reservationTimeRepository.findById(Id.create(timeId))).thenReturn(Optional.of(reservationTime));
        when(themeRepository.findById(Id.create(themeId))).thenReturn(Optional.of(theme));
        when(reservationRepository.isDuplicateDateAndTimeAndTheme(eq(date), eq(LocalTime.of(10, 0)), eq(theme.getId())))
                .thenReturn(false);

        // when
        ReservationDto result = sut.addAndGet(date, timeId, themeId, userIdValue);

        // then
        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(reservationTimeRepository).findById(Id.create(timeId));
        verify(themeRepository).findById(Id.create(themeId));
        verify(reservationRepository).isDuplicateDateAndTimeAndTheme(eq(date), any(LocalTime.class), eq(theme.getId()));
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_시간_ID로_예약_시_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        String timeId = "nonexistent-time-id";
        String themeId = "theme-id";
        String userId = "user-id";

        User user = User.restore(userId, "USER", "Test User", "test@example.com", "password");

        when(userRepository.findById(Id.create(userId))).thenReturn(Optional.of(user));
        when(reservationTimeRepository.findById(Id.create(timeId))).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> sut.addAndGet(date, timeId, themeId, userId))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).findById(Id.create(userId));
        verify(reservationTimeRepository).findById(Id.create(timeId));
        verifyNoInteractions(themeRepository);
        verifyNoInteractions(reservationRepository);
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

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reservationTimeRepository.findById(timeId)).thenReturn(Optional.of(reservationTime));
        when(themeRepository.findById(themeId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> sut.addAndGet(date, timeIdValue, themeIdValue, userIdValue))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).findById(userId);
        verify(reservationTimeRepository).findById(timeId);
        verify(themeRepository).findById(themeId);
        verifyNoInteractions(reservationRepository);
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

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reservationTimeRepository.findById(timeId)).thenReturn(Optional.of(reservationTime));
        when(themeRepository.findById(themeId)).thenReturn(Optional.of(theme));
        when(reservationRepository.isDuplicateDateAndTimeAndTheme(eq(date), any(LocalTime.class), eq(theme.getId())))
                .thenReturn(true);

        // when, then
        assertThatThrownBy(() -> sut.addAndGet(date, timeIdValue, themeIdValue, userIdValue))
                .isInstanceOf(DuplicatedException.class);

        verify(userRepository).findById(userId);
        verify(reservationTimeRepository).findById(timeId);
        verify(themeRepository).findById(themeId);
        verify(reservationRepository).isDuplicateDateAndTimeAndTheme(eq(date), any(LocalTime.class), eq(theme.getId()));
        verify(reservationRepository, never()).save(any(Reservation.class));
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

        when(reservationRepository.findById(Id.create(reservationId))).thenReturn(Optional.of(reservation));

        // when
        sut.delete(reservationId, userId);

        // then
        verify(reservationRepository).deleteById(Id.create(reservationId));
    }
}
