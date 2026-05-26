package roomescape.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static roomescape.exception.ErrorCode.DUPLICATED_RESERVATION;
import static roomescape.exception.ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_CREATE;
import static roomescape.exception.ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_DELETE;
import static roomescape.exception.ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_UPDATE;
import static roomescape.exception.ErrorCode.NOT_FOUND_RESERVATION;
import static roomescape.exception.ErrorCode.NOT_FOUND_RESERVATION_TIME;
import static roomescape.exception.ErrorCode.NOT_FOUND_THEME;

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
import roomescape.service.dto.request.ServiceReservationCreateRequest;
import roomescape.service.dto.request.ServiceReservationUpdateRequest;
import roomescape.service.dto.response.ServiceReservationResponse;
import roomescape.service.dto.response.ServiceReservationTimeResponse;
import roomescape.service.dto.response.ServiceThemeResponse;

public class ReservationServiceTest {

    private ReservationService reservationService;

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

        reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeRepository,
                clock);
    }

    @Test
    void createNotFoundReservationTimeExceptionTest() {
        themeRepository.create(new Theme("피즈의 모험", "설명", "url.jpg"));
        ServiceReservationCreateRequest serviceReservationCreateRequest = new ServiceReservationCreateRequest("fizz",
                LocalDate.of(2026, 5, 3), 1L, 1L);

        assertThatThrownBy(() -> reservationService.create(serviceReservationCreateRequest))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(NOT_FOUND_RESERVATION_TIME.getMessage());
    }

    @Test
    void createNotFoundThemeTimeExceptionTest() {
        reservationTimeRepository.create(new ReservationTime(LocalTime.of(10, 0)));
        ServiceReservationCreateRequest serviceReservationCreateRequest = new ServiceReservationCreateRequest("fizz",
                LocalDate.of(2026, 5, 3), 1L, 1L);

        assertThatThrownBy(() -> reservationService.create(serviceReservationCreateRequest))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(NOT_FOUND_THEME.getMessage());
    }

    @Test
    void createPastReservationExceptionTest() {
        reservationTimeRepository.create(new ReservationTime(1L, LocalTime.of(10, 0)));
        themeRepository.create(new Theme("피즈의 모험", "설명", "url.jpg"));

        ServiceReservationCreateRequest serviceReservationCreateRequest = new ServiceReservationCreateRequest("fizz",
                LocalDate.of(2026, 5, 1), 1L, 1L);

        assertThatThrownBy(() -> reservationService.create(serviceReservationCreateRequest))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(NOT_ALLOW_PAST_TIME_RESERVATION_CREATE.getMessage());
    }

    @Test
    void createDuplicatedReservationExceptionTest() {
        reservationTimeRepository.create(new ReservationTime(LocalTime.of(10, 0)));
        themeRepository.create(new Theme("피즈의 모험", "모험 이야기", "url.jpg"));

        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest("fizz",
                LocalDate.of(2026, 5, 3), 1L,
                1L);
        reservationService.create(request);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(DUPLICATED_RESERVATION.getMessage());
    }

    @Test
    void updateNotFoundReservationExceptionTest() {
        ReservationTime reservationTime = reservationTimeRepository.create(
                new ReservationTime(LocalTime.of(10, 0)));
        ServiceReservationUpdateRequest request = new ServiceReservationUpdateRequest(LocalDate.of(2026, 5, 3),
                reservationTime.getId());

        assertThatThrownBy(() -> reservationService.update(1L, request))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(NOT_FOUND_RESERVATION.getMessage());
    }

    @Test
    void updateNotFoundReservationTimeExceptionTest() {
        ReservationTime reservationTime = reservationTimeRepository.create(
                new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.create(new Theme("피즈의 모험", "모험 이야기", "url.jpg"));
        reservationRepository.create(new Reservation("fizz", LocalDate.of(2026, 5, 3), reservationTime, theme));

        ServiceReservationUpdateRequest request = new ServiceReservationUpdateRequest(LocalDate.of(2026, 5, 3), 2L);

        assertThatThrownBy(() -> reservationService.update(1L, request))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(NOT_FOUND_RESERVATION_TIME.getMessage());
    }

    @Test
    void updatePastReservationCreateExceptionTest() {
        ReservationTime reservationTime = reservationTimeRepository.create(
                new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.create(new Theme("피즈의 모험", "모험 이야기", "url.jpg"));
        reservationRepository.create(new Reservation("fizz", LocalDate.of(2026, 5, 3), reservationTime, theme));

        ServiceReservationUpdateRequest request = new ServiceReservationUpdateRequest(LocalDate.of(2026, 5, 1),
                reservationTime.getId());

        assertThatThrownBy(() -> reservationService.update(1L, request))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(NOT_ALLOW_PAST_TIME_RESERVATION_CREATE.getMessage());
    }

    @Test
    void updatePastReservationUpdateExceptionTest() {
        ReservationTime reservationTime = reservationTimeRepository.create(
                new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.create(new Theme("피즈의 모험", "모험 이야기", "url.jpg"));
        reservationRepository.create(new Reservation("fizz", LocalDate.of(2026, 5, 1), reservationTime, theme));

        ServiceReservationUpdateRequest request = new ServiceReservationUpdateRequest(LocalDate.of(2026, 5, 3),
                reservationTime.getId());

        assertThatThrownBy(() -> reservationService.update(1L, request))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(NOT_ALLOW_PAST_TIME_RESERVATION_UPDATE.getMessage());
    }

    @Test
    void updateDuplicatedReservationExceptionTest() {
        ReservationTime reservationTime = reservationTimeRepository.create(
                new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.create(new Theme("피즈의 모험", "모험 이야기", "url.jpg"));

        reservationRepository.create(new Reservation("fizz", LocalDate.of(2026, 5, 3), reservationTime, theme));
        reservationRepository.create(
                new Reservation("fizz2", LocalDate.of(2026, 5, 4), reservationTime, theme));

        ServiceReservationUpdateRequest request = new ServiceReservationUpdateRequest(
                LocalDate.of(2026, 5, 4),
                reservationTime.getId());

        assertThatThrownBy(() -> reservationService.update(1L, request))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(DUPLICATED_RESERVATION.getMessage());
    }

    @Test
    void deleteNotFoundReservationExceptionTest() {
        assertThatThrownBy(() -> reservationService.delete(1L))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(NOT_FOUND_RESERVATION.getMessage());
    }

    @Test
    void deletePastReservationExceptionTest() {
        ReservationTime reservationTime = reservationTimeRepository.create(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.create(new Theme("피즈의 모험", "모험 이야기", "url.jpg"));

        reservationRepository.create(new Reservation("fizz", LocalDate.of(2026, 5, 1), reservationTime, theme));

        assertThatThrownBy(() -> reservationService.delete(1L))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(NOT_ALLOW_PAST_TIME_RESERVATION_DELETE.getMessage());
    }

    @Test
    void createTest() {
        ReservationTime reservationTime = reservationTimeRepository.create(
                new ReservationTime(LocalTime.of(10, 0)));
        ServiceReservationTimeResponse serviceReservationTimeResponse = ServiceReservationTimeResponse.from(
                reservationTime);
        Theme theme = themeRepository.create(new Theme("피즈의 모험", "모험 이야기", "url.jpg"));
        ServiceThemeResponse serviceThemeResponse = ServiceThemeResponse.from(theme);

        ServiceReservationResponse responseDto = reservationService.create(
                new ServiceReservationCreateRequest("fizz", LocalDate.of(2026, 5, 3), 1L, 1L));

        assertThat(responseDto).isEqualTo(
                new ServiceReservationResponse(1L, "fizz", LocalDate.of(2026, 5, 3),
                        serviceReservationTimeResponse,
                        serviceThemeResponse));
    }

    @Test
    void readAllTest() {
        ReservationTime reservationTime = reservationTimeRepository.create(
                new ReservationTime(LocalTime.of(10, 0)));
        ServiceReservationTimeResponse serviceReservationTimeResponse = ServiceReservationTimeResponse.from(
                reservationTime);
        Theme theme = themeRepository.create(new Theme("피즈의 모험", "모험 이야기", "url.jpg"));
        ServiceThemeResponse serviceThemeResponse = ServiceThemeResponse.from(theme);

        reservationService.create(new ServiceReservationCreateRequest("fizz", LocalDate.of(2026, 5, 3), 1L, 1L));
        reservationService.create(
                new ServiceReservationCreateRequest("fizz2", LocalDate.of(2026, 5, 3).plusDays(1), 1L, 1L));

        List<ServiceReservationResponse> response = reservationService.readAll();

        assertThat(response.getFirst()).isEqualTo(
                new ServiceReservationResponse(response.getFirst().id(), "fizz", LocalDate.of(2026, 5, 3),
                        serviceReservationTimeResponse,
                        serviceThemeResponse));
        assertThat(response.get(1)).isEqualTo(
                new ServiceReservationResponse(response.get(1).id(), "fizz2",
                        LocalDate.of(2026, 5, 3).plusDays(1),
                        serviceReservationTimeResponse,
                        serviceThemeResponse));
    }

    @Test
    void deleteTest() {
        ReservationTime reservationTime = reservationTimeRepository.create(
                new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.create(new Theme("피즈의 모험", "모험 이야기", "url.jpg"));

        reservationRepository.create(new Reservation("fizz", LocalDate.of(2026, 5, 3), reservationTime, theme));
        reservationService.delete(1L);

        List<ServiceReservationResponse> response = reservationService.readAll();

        assertThat(response.size()).isEqualTo(0);
    }
}
