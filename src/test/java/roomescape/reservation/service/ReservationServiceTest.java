package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.fake.FakeAvailableReservationTimeRepository;
import roomescape.fake.FakeReservationQueryRepository;
import roomescape.fake.FakeReservationRepository;
import roomescape.fake.FakeReservationTimeRepository;
import roomescape.fake.FakeThemeRepository;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationQueryResult;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservationtime.application.dto.ReservationTimeCreateCommand;
import roomescape.reservationtime.application.dto.ReservationTimeQueryResult;
import roomescape.reservationtime.application.service.ReservationTimeService;
import roomescape.theme.application.dto.ThemeCreateCommand;
import roomescape.theme.application.dto.ThemeQueryResult;
import roomescape.theme.application.service.ThemeService;

class ReservationServiceTest {

    private ReservationQueryService reservationQueryService;
    private ThemeService themeService;
    private ReservationTimeService timeService;
    private ReservationService reservationService;
    private FakeReservationRepository reservationRepository;
    private FakeReservationTimeRepository timeRepository;
    private FakeThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        themeRepository = new FakeThemeRepository();
        timeRepository = new FakeReservationTimeRepository();
        reservationRepository = new FakeReservationRepository(themeRepository, timeRepository);
        reservationQueryService = new ReservationQueryService(new FakeReservationQueryRepository(reservationRepository));
        themeService = new ThemeService(themeRepository, reservationQueryService);
        FakeAvailableReservationTimeRepository availableReservationTimeRepository =
                new FakeAvailableReservationTimeRepository(timeRepository, reservationRepository);
        timeService = new ReservationTimeService(timeRepository, availableReservationTimeRepository,
                reservationQueryService);
        reservationService = new ReservationService(reservationRepository, themeService, timeService);
    }

    @DisplayName("사용자의 방탈출 예약 시간 추가를 테스트합니다.")
    @Test
    void save_user_reservation_successfully() {
        themeService.save(new ThemeCreateCommand("theme name", "theme description", "theme img url"));
        timeService.save(new ReservationTimeCreateCommand(LocalTime.of(10, 0)));

        ReservationCreateCommand request = new ReservationCreateCommand("스타크", LocalDate.of(2026, 5, 6), 1L, 1L);
        ReservationQueryResult reservationQueryResult = reservationService.save(request,
                LocalDateTime.of(2000, 1, 1, 0, 0));

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(reservationQueryResult.id()).isEqualTo(1L);
            assertSoftly.assertThat(reservationQueryResult.name()).isEqualTo("스타크");
            assertSoftly.assertThat(reservationQueryResult.date()).isEqualTo("2026-05-06");
            assertSoftly.assertThat(reservationQueryResult.time())
                    .isEqualTo(new ReservationTimeQueryResult(1L, LocalTime.of(10, 0)));
            assertSoftly.assertThat(reservationQueryResult.theme())
                    .isEqualTo(new ThemeQueryResult(1L, "theme name", "theme description", "theme img url"));
        });
    }

    @DisplayName("중복된 시간과 테마에 예약 추가 시 예외 발생을 테스트합니다.")
    @Test
    void validate_duplicated_reservation() {
        themeService.save(new ThemeCreateCommand("theme name", "theme description", "theme img url"));
        timeService.save(new ReservationTimeCreateCommand(LocalTime.of(10, 0)));

        ReservationCreateCommand firstRequest = new ReservationCreateCommand("스타크", LocalDate.of(2026, 5, 6), 1L, 1L);
        reservationService.save(firstRequest, LocalDateTime.of(2000, 1, 1, 0, 0));

        ReservationCreateCommand secondRequest = new ReservationCreateCommand("카야", LocalDate.of(2026, 5, 6), 1L, 1L);

        Assertions.assertThatThrownBy(() -> reservationService.save(secondRequest, LocalDateTime.of(2000, 1, 1, 0, 0)))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 해당 날짜와 시간에 예약이 존재합니다.");
    }

    @DisplayName("오늘보다 이전 날짜 혹은 시간 예약 시도 시 예외 발생을 테스트합니다.")
    @Test
    void validate_throw_exception_when_reserving_past_date_or_time() {
        themeService.save(new ThemeCreateCommand("theme name", "theme description", "theme img url"));
        timeService.save(new ReservationTimeCreateCommand(LocalTime.of(10, 0)));

        ReservationCreateCommand request = new ReservationCreateCommand("스타크", LocalDate.of(2026, 5, 6), 1L, 1L);

        Assertions.assertThatThrownBy(() -> reservationService.save(request, LocalDateTime.of(2026, 5, 6, 11, 0)))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("현재 시간보다 이전 시간으로 예약을 할 수 없습니다.");
    }

    @DisplayName("이름으로 본인 예약 목록 조회를 테스트합니다.")
    @Test
    void find_reservations_by_name() {
        themeService.save(new ThemeCreateCommand("theme name", "theme description", "theme img url"));
        timeService.save(new ReservationTimeCreateCommand(LocalTime.of(10, 0)));
        timeService.save(new ReservationTimeCreateCommand(LocalTime.of(11, 0)));

        reservationService.save(
                new ReservationCreateCommand("스타크", LocalDate.of(2026, 5, 6), 1L, 1L),
                LocalDateTime.of(2000, 1, 1, 0, 0)
        );
        reservationService.save(
                new ReservationCreateCommand("스타크", LocalDate.of(2026, 5, 7), 1L, 2L),
                LocalDateTime.of(2000, 1, 1, 0, 0)
        );
        reservationService.save(
                new ReservationCreateCommand("카야", LocalDate.of(2026, 5, 8), 1L, 2L),
                LocalDateTime.of(2000, 1, 1, 0, 0)
        );

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(reservationService.findAllByName("스타크")).hasSize(2);
            assertSoftly.assertThat(reservationService.findAllByName("카야")).hasSize(1);
        });
    }

    @DisplayName("본인 예약의 날짜와 시간을 변경합니다.")
    @Test
    void update_reservation() {
        themeService.save(new ThemeCreateCommand("theme name", "theme description", "theme img url"));
        timeService.save(new ReservationTimeCreateCommand(LocalTime.of(10, 0)));
        timeService.save(new ReservationTimeCreateCommand(LocalTime.of(11, 0)));
        reservationService.save(
                new ReservationCreateCommand("스타크", LocalDate.of(2026, 5, 6), 1L, 1L),
                LocalDateTime.of(2000, 1, 1, 0, 0)
        );

        ReservationQueryResult updatedReservation = reservationService.update(
                new ReservationUpdateCommand(1L, "스타크", LocalDate.of(2026, 5, 7), 2L),
                LocalDateTime.of(2000, 1, 1, 0, 0)
        );

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(updatedReservation.date()).isEqualTo(LocalDate.of(2026, 5, 7));
            assertSoftly.assertThat(updatedReservation.time())
                    .isEqualTo(new ReservationTimeQueryResult(2L, LocalTime.of(11, 0)));
        });
    }

    @DisplayName("본인 이름이 아닌 경우 예약 변경 시 예외가 발생합니다.")
    @Test
    void update_other_users_reservation() {
        themeService.save(new ThemeCreateCommand("theme name", "theme description", "theme img url"));
        timeService.save(new ReservationTimeCreateCommand(LocalTime.of(10, 0)));
        reservationService.save(
                new ReservationCreateCommand("스타크", LocalDate.of(2026, 5, 6), 1L, 1L),
                LocalDateTime.of(2000, 1, 1, 0, 0)
        );

        Assertions.assertThatThrownBy(() -> reservationService.update(
                        new ReservationUpdateCommand(1L, "카야", LocalDate.of(2026, 5, 7), 1L),
                        LocalDateTime.of(2000, 1, 1, 0, 0)
                ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("본인의 예약만 변경하거나 취소할 수 있습니다.");
    }

    @DisplayName("지난 예약은 취소할 수 없습니다.")
    @Test
    void cancel_past_reservation() {
        themeService.save(new ThemeCreateCommand("theme name", "theme description", "theme img url"));
        timeService.save(new ReservationTimeCreateCommand(LocalTime.of(10, 0)));
        reservationService.save(
                new ReservationCreateCommand("스타크", LocalDate.of(2026, 5, 6), 1L, 1L),
                LocalDateTime.of(2000, 1, 1, 0, 0)
        );

        Assertions.assertThatThrownBy(() -> reservationService.delete(
                        1L,
                        "스타크",
                        LocalDateTime.of(2026, 5, 6, 11, 0)
                ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("지난 예약은 변경하거나 취소할 수 없습니다.");
    }
}
