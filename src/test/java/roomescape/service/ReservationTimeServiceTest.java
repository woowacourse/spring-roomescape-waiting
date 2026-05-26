package roomescape.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static roomescape.exception.ErrorCode.DUPLICATED_RESERVATION_TIME;
import static roomescape.exception.ErrorCode.NOT_FOUND_THEME;
import static roomescape.exception.ErrorCode.PAST_RESERVATION_TIME_READ;
import static roomescape.exception.ErrorCode.REFERENCED_TIME;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.repository.FakeDatabase;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeReservationTimeRepository;
import roomescape.repository.FakeThemeRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ServiceReservationTimeCreateRequest;
import roomescape.service.dto.response.ServiceReservationTimeAvailabilityResponse;
import roomescape.service.dto.response.ServiceReservationTimeResponse;

public class ReservationTimeServiceTest {

    private ReservationTimeService reservationTimeService;

    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private Clock clock;

    @BeforeEach
    void beforeEach() {
        FakeDatabase fakeDatabase = new FakeDatabase();

        reservationRepository = new FakeReservationRepository(fakeDatabase);
        reservationTimeRepository = new FakeReservationTimeRepository(fakeDatabase);
        themeRepository = new FakeThemeRepository(fakeDatabase);
        clock = Clock.fixed(Instant.parse("2026-05-02T00:00:00Z"), ZoneId.of("Asia/Seoul"));

        reservationTimeService = new ReservationTimeService(reservationTimeRepository, themeRepository,
                reservationRepository, clock);
    }

    @Test
    void createDuplicatedReservationTimeExceptionTest() {
        ServiceReservationTimeCreateRequest request = new ServiceReservationTimeCreateRequest(LocalTime.of(10, 0));
        reservationTimeService.create(request);

        assertThatThrownBy(() -> reservationTimeService.create(request))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(DUPLICATED_RESERVATION_TIME.getMessage());
    }

    @Test
    void readAvailabilityNotFoundThemeExceptionTest() {
        assertThatThrownBy(() -> reservationTimeService.readAvailabilityByDateAndTheme(LocalDate.of(2026, 5, 3), 1L))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(NOT_FOUND_THEME.getMessage());
    }

    @Test
    void readAvailabilityPastDateExceptionTest() {
        reservationTimeRepository.create(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.create(new Theme("방탈출1", "방탈출1 설명", "url.jpg"));

        LocalDate beforeDate = LocalDate.of(2026, 5, 1);

        assertThatThrownBy(() -> reservationTimeService.readAvailabilityByDateAndTheme(beforeDate, theme.getId()))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(PAST_RESERVATION_TIME_READ.getMessage());
    }

    @Test
    void deleteReferencedReservationTimeExceptionTest() {
        ReservationTime reservationTime = reservationTimeRepository.create(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.create(new Theme("방탈출1", "방탈출1 설명", "url.jpg"));

        reservationRepository.create(new Reservation("fizz", LocalDate.of(2026, 5, 3), reservationTime, theme));

        assertThatThrownBy(() -> reservationTimeService.delete(1L))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(REFERENCED_TIME.getMessage());
    }

    @Test
    void createTest() {
        ServiceReservationTimeResponse response = reservationTimeService.create(
                new ServiceReservationTimeCreateRequest(LocalTime.of(10, 0)));

        assertThat(response).isEqualTo(new ServiceReservationTimeResponse(1L, LocalTime.of(10, 0)));
    }

    @Test
    void readAllTest() {
        reservationTimeRepository.create(new ReservationTime(LocalTime.of(10, 0)));
        reservationTimeRepository.create(new ReservationTime(LocalTime.of(11, 0)));

        List<ServiceReservationTimeResponse> responses = reservationTimeService.readAll();

        assertThat(responses.getFirst()).isEqualTo(new ServiceReservationTimeResponse(1L, LocalTime.of(10, 0)));
        assertThat(responses.get(1)).isEqualTo(new ServiceReservationTimeResponse(2L, LocalTime.of(11, 0)));
    }

    @Test
    void readAvailabilityByDateAndThemeTest() {
        ReservationTime reservationTime = reservationTimeRepository.create(new ReservationTime(LocalTime.of(10, 0)));
        reservationTimeRepository.create(reservationTime);
        Theme theme = themeRepository.create(new Theme("방탈출1", "방탈출1 설명", "url.jpg"));

        reservationRepository.create(new Reservation("fizz", LocalDate.now(clock), reservationTime, theme));

        List<ServiceReservationTimeAvailabilityResponse> responses = reservationTimeService.readAvailabilityByDateAndTheme(
                LocalDate.now(clock), theme.getId());

        assertThat(responses.get(0).available()).isFalse();
        assertThat(responses.get(1).available()).isTrue();
    }

    @Test
    void deleteTest() {
        reservationTimeRepository.create(new ReservationTime(LocalTime.of(10, 0)));
        reservationTimeService.delete(1L);

        List<ReservationTime> reservationTimes = reservationTimeRepository.readAll();

        assertThat(reservationTimes.size()).isEqualTo(0);
    }
}
