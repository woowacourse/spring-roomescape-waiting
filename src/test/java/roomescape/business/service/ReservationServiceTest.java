package roomescape.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.business.dto.ReservationDto;
import roomescape.business.dto.ReservationTimeDto;
import roomescape.business.dto.ThemeDto;
import roomescape.business.dto.UserDto;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.ReservationRepository;
import roomescape.business.model.repository.ReservationTimeRepository;
import roomescape.business.model.repository.ThemeRepository;
import roomescape.business.model.repository.UserRepository;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.business.DuplicatedException;
import roomescape.exception.business.NotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        when(reservationRepository.isDuplicateDateAndTimeAndTheme(eq(date), eq(LocalTime.of(10, 0)), eq(theme.getId())))
                .thenReturn(false);

        // when
        ReservationDto result = sut.addAndGet(date, timeIdValue, themeIdValue, userIdValue);

        // then
        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(reservationTimeRepository).findById(timeId);
        verify(themeRepository).findById(themeId);
        verify(reservationRepository).isDuplicateDateAndTimeAndTheme(eq(date), any(LocalTime.class), eq(theme.getId()));
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_사용자_ID로_예약_시_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        String timeId = "time-id";
        String themeId = "theme-id";
        String userIdValue = "nonexistent-id";
        Id userId = Id.create(userIdValue);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> sut.addAndGet(date, timeId, themeId, userIdValue))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoInteractions(reservationTimeRepository);
        verifyNoInteractions(themeRepository);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void 존재하지_않는_예약_시간_ID로_예약_시_예외가_발생한다() {
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
    void 이미_예약된_날짜_시간_테마로_예약_시_예외가_발생한다() {
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
    void 필터를_적용하여_모든_예약을_조회할_수_있다() {
        // given
        String themeIdValue = "theme-id";
        String userIdValue = "user-id";
        Id themeId = Id.create(themeIdValue);
        Id userId = Id.create(userIdValue);

        LocalDate dateFrom = LocalDate.now();
        LocalDate dateTo = LocalDate.now().plusDays(7);

        User user1 = User.restore("user-id-1", "USER", "User One", "user1@example.com", "password1");
        User user2 = User.restore("user-id-2", "USER", "User Two", "user2@example.com", "password2");
        ReservationTime time1 = ReservationTime.restore("time-id-1", LocalTime.of(10, 0));
        ReservationTime time2 = ReservationTime.restore("time-id-2", LocalTime.of(14, 0));
        Theme theme1 = Theme.restore("theme-id-1", "Theme One", "Description One", "thumbnail1.jpg");
        Theme theme2 = Theme.restore("theme-id-2", "Theme Two", "Description Two", "thumbnail2.jpg");

        List<Reservation> reservationData = Arrays.asList(
                Reservation.restore("reservation-id-1", user1, dateFrom, time1, theme1),
                Reservation.restore("reservation-id-2", user2, dateFrom.plusDays(1), time2, theme2)
        );
        List<ReservationDto> expectedReservations = Arrays.asList(
                new ReservationDto(Id.create("reservation-id-1"), UserDto.fromEntity(user1), new ReservationDate(dateFrom), ReservationTimeDto.fromEntity(time1), ThemeDto.fromEntity(theme1)),
                new ReservationDto(Id.create("reservation-id-2"), UserDto.fromEntity(user2), new ReservationDate(dateFrom.plusDays(1)), ReservationTimeDto.fromEntity(time2), ThemeDto.fromEntity(theme2))
        );

        when(reservationRepository.findAllWithFilter(themeId, userId, dateFrom, dateTo))
                .thenReturn(reservationData);

        // when
        List<ReservationDto> result = sut.getAll(themeIdValue, userIdValue, dateFrom, dateTo);

        // then
        assertThat(result).isEqualTo(expectedReservations);
        verify(reservationRepository).findAllWithFilter(themeId, userId, dateFrom, dateTo);
    }

    @Test
    void 예약을_삭제할_수_있다() {
        // given
        final User user = User.create("dompoo", "dompoo@email.com", "password");
        final Reservation reservation = Reservation.create(
                user,
                LocalDate.now().plusDays(5),
                ReservationTime.create(LocalTime.of(10, 0)),
                Theme.create("theme", "", "")
        );
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        // when
        sut.delete(reservation.getId().value(), user.getId().value());

        // then
        verify(reservationRepository).findById(reservation.getId());
        verify(reservationRepository).deleteById(reservation.getId());
    }

    @Test
    void 존재하지_않는_예약_삭제_시_예외가_발생한다() {
        // given
        String reservationIdValue = "nonexistent-id";
        Id reservatinId = Id.create(reservationIdValue);

        final User user = User.create("dompoo", "dompoo@email.com", "password");
        when(reservationRepository.findById(reservatinId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> sut.delete(reservationIdValue, user.getId().value()))
                .isInstanceOf(NotFoundException.class);

        verify(reservationRepository).findById(reservatinId);
        verify(reservationRepository, never()).deleteById(reservatinId);
    }

    @Test
    void 예약자가_아닌_유저가_삭제_시도시_예외가_발생한다() {
        // given
        final User user1 = User.create("dompoo", "dompoo@email.com", "password");
        final User user2 = User.create("lemon", "lemon@email.com", "password");
        final Reservation reservation = Reservation.create(
                user1,
                LocalDate.now().plusDays(5),
                ReservationTime.create(LocalTime.of(10, 0)),
                Theme.create("theme", "", "")
        );

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        // when, then
        assertThatThrownBy(() -> sut.delete(reservation.getId().value(), user2.getId().value()))
                .isInstanceOf(AuthorizationException.class);

        verify(reservationRepository).findById(reservation.getId());
        verify(reservationRepository, never()).deleteById(any());
    }
}
