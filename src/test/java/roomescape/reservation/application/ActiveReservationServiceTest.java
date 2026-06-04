package roomescape.reservation.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
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
import roomescape.reservation.application.exception.ReservationInUseException;
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
class ActiveReservationServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ActiveReservationService activeReservationService;

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private Clock clock;

    private ReservationTime time;
    private Theme theme;

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
    @DisplayName("확정 예약이 없는 경우 새 예약은 확정 예약으로 저장된다.")
    void normalTest() {
        TimeSlot slot = timeSlotService.getTimeSlot(LocalDate.now(clock), time, theme);
        ReservationInfo saved = activeReservationService.add(slot, createCommand("포비", time.getId()));
        Assertions.assertThat(saved.status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("확정 예약이 있는 경우 예외가 발생한다.")
    void pendingNormalTest() {
        TimeSlot slot = timeSlotService.getTimeSlot(LocalDate.now(clock), time, theme);
        activeReservationService.add(slot, createCommand("포비", time.getId()));
        Assertions.assertThatThrownBy(() -> activeReservationService.add(slot, createCommand("리사", time.getId())))
                .isInstanceOf(ReservationInUseException.class);
    }

    @Test
    @DisplayName("변경하려는 시간에 예약이 없으면, 시간이 변경된다.")
    void changeTest() {
        TimeSlot slot = timeSlotService.getTimeSlot(LocalDate.now(clock), time, theme);
        ReservationCreateCommand command = createCommand("포비", time.getId());
        ReservationInfo saved = activeReservationService.add(slot, command);
        TimeSlot newSlot = timeSlotService.getTimeSlot(LocalDate.now(clock).plusDays(1), time, theme);
        ReservationInfo reservationInfo = activeReservationService.change(saved.id(), newSlot, command.name());
        Assertions.assertThat(reservationInfo.date()).isEqualTo(newSlot.getDate());
    }

    @Test
    @DisplayName("예약을 취소하면, 해당 시간 슬롯의 예약 조회시 false를 반환한다.")
    void cancelTest() {
        TimeSlot slot = timeSlotService.getTimeSlot(LocalDate.now(clock), time, theme);
        ReservationInfo saved = activeReservationService.add(slot, createCommand("포비", time.getId()));
        activeReservationService.cancel(saved.id(), saved.name());
        Assertions.assertThat(activeReservationService.existsBySlotId(slot.getId())).isFalse();
    }

    private ReservationCreateCommand createCommand(String name, Long timeId) {
        return ReservationCreateCommand.builder()
                .name(name)
                .date(LocalDate.now(clock))
                .timeId(timeId)
                .themeId(theme.getId())
                .build();
    }
}
