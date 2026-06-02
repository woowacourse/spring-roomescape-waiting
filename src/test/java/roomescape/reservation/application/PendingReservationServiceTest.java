package roomescape.reservation.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.domain.ActiveReservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.TimeSlot;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.dto.ThemeCommand;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.theme.domain.Theme;
import roomescape.time.application.ReservationTimeService;
import roomescape.time.application.dto.ReservationTimeCommand;
import roomescape.time.application.dto.ReservationTimeInfo;
import roomescape.time.domain.ReservationTime;

@SpringBootTest
class PendingReservationServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Clock clock;

    @Autowired
    private PendingReservationService pendingReservationService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private TimeSlotService timeSlotService;

    private ReservationTime time;
    private Theme theme;
    @Autowired
    private ActiveReservationService activeReservationService;

    @BeforeEach
    void setUp() {
        ReservationTimeInfo reservationTimeInfo = reservationTimeService.addReservationTime(
                ReservationTimeCommand.builder()
                        .startAt(LocalTime.now(clock))
                        .build()
        );

        time = ReservationTime.builder()
                .id(reservationTimeInfo.id())
                .startAt(reservationTimeInfo.startAt())
                .build();

        ThemeInfo themeInfo = themeService.addTheme(ThemeCommand.builder()
                .name("추리")
                .description("추리하기")
                .durationTime(LocalTime.now(clock))
                .thumbnailImageUrl("https://~~~")
                .build()
        );

        theme = Theme.builder()
                .id(themeInfo.id())
                .name(themeInfo.name())
                .description(themeInfo.description())
                .durationTime(themeInfo.durationTime())
                .thumbnailImageUrl(themeInfo.thumbnailImageUrl())
                .build();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE reservation");
        jdbcTemplate.execute("TRUNCATE TABLE time_slot");
        jdbcTemplate.execute("TRUNCATE TABLE theme");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test
    @DisplayName("해당 사용자의 대기 중인 예약이 없으면 저장된다.")
    void normalTest() {
        TimeSlot slot = timeSlotService.getTimeSlot(LocalDate.now(clock), time, theme);
        ReservationCreateCommand command = ReservationCreateCommand.builder()
                .name("포비")
                .date(LocalDate.now(clock))
                .timeId(time.getId())
                .themeId(theme.getId())
                .build();
        ReservationInfo saved = pendingReservationService.add(slot, command);
        Assertions.assertThat(saved.name()).isEqualTo(command.name());
        Assertions.assertThat(saved.status()).isEqualTo(Status.PENDING);
    }

    @Test
    @DisplayName("대기 상태인 예약을 다른 날짜의 대기 상태로 변경하면 정상 동작한다.")
    void changeTest() {
        TimeSlot slot = timeSlotService.getTimeSlot(LocalDate.now(clock), time, theme);
        ReservationCreateCommand command = ReservationCreateCommand.builder()
                .name("포비")
                .date(LocalDate.now(clock))
                .timeId(time.getId())
                .themeId(theme.getId())
                .build();
        ReservationInfo saved = pendingReservationService.add(slot, command);
        TimeSlot newSlot = timeSlotService.getTimeSlot(LocalDate.now(clock).plusDays(1), time, theme);
        ReservationInfo reservationInfo = pendingReservationService.change(saved.id(), newSlot, saved.name());
        Assertions.assertThat(reservationInfo.date()).isEqualTo(newSlot.getDate());
    }

    @Test
    @DisplayName("가장 먼저 생성된 예약을 Active로 변경하면 대기 상태에 있던 예약은 사라진다.")
    void popNextPendingTest() {
        TimeSlot slot = timeSlotService.getTimeSlot(LocalDate.now(clock), time, theme);
        ReservationCreateCommand pobiCommand = ReservationCreateCommand.builder()
                .name("포비")
                .date(LocalDate.now(clock))
                .timeId(time.getId())
                .themeId(theme.getId())
                .build();
        ReservationInfo saved = pendingReservationService.add(slot, pobiCommand);
        ReservationCreateCommand lisaCommand = ReservationCreateCommand.builder()
                .name("리사")
                .date(LocalDate.now(clock))
                .timeId(time.getId())
                .themeId(theme.getId())
                .build();
        pendingReservationService.add(slot, lisaCommand);
        Optional<ActiveReservation> activeReservation = pendingReservationService.popNextPendingAndPromote(
                slot.getId());
        Assertions.assertThat(activeReservation).isNotNull();
        Assertions.assertThat(activeReservation.get().getName()).isEqualTo(saved.name());
        Assertions.assertThat(activeReservationService.getReservations()).isEmpty();
    }

    @Test
    @DisplayName("예약을 취소하면 조회 시 빈배열이 반환된다.")
    void cancelTest() {
        TimeSlot slot = timeSlotService.getTimeSlot(LocalDate.now(clock), time, theme);
        ReservationCreateCommand pobiCommand = ReservationCreateCommand.builder()
                .name("포비")
                .date(LocalDate.now(clock))
                .timeId(time.getId())
                .themeId(theme.getId())
                .build();

        ReservationInfo saved = pendingReservationService.add(slot, pobiCommand);
        Assertions.assertThat(pendingReservationService.getReservations()).hasSize(1);

        pendingReservationService.cancel(saved.id(), saved.name());
        Assertions.assertThat(pendingReservationService.getReservations()).isEmpty();
    }
}
