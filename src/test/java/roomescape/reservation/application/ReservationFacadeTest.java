package roomescape.reservation.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.application.dto.ReservationCancelCommand;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationPendingInfo;
import roomescape.reservation.domain.Status;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.dto.ThemeCommand;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.theme.domain.Theme;
import roomescape.time.application.ReservationTimeService;
import roomescape.time.application.dto.ReservationTimeCommand;
import roomescape.time.application.dto.ReservationTimeInfo;
import roomescape.time.domain.ReservationTime;

@SpringBootTest
class ReservationFacadeTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Clock clock;

    @Autowired
    private ReservationFacade facade;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;

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
    @DisplayName("예약이 없으면 확정 예약으로 등록된다.")
    void addActiveReservationTest() {
        ReservationInfo reservationInfo = facade.addReservation(createCommand("포비", time.getId()));
        Assertions.assertThat(reservationInfo.status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("예약이 있으면 대기 예약으로 등록된다.")
    void addPendingReservationTest() {
        facade.addReservation(createCommand("포비", time.getId()));
        ReservationInfo reservationInfo = facade.addReservation(createCommand("리사", time.getId()));
        Assertions.assertThat(reservationInfo.status()).isEqualTo(Status.PENDING);
    }

    @Test
    @DisplayName("대기 중인 예약을 빈 시간대로 변경하면 확정 예약으로 승격된다.")
    void changePendingToEmptySlotTest() {
        facade.addReservation(createCommand("포비", time.getId()));
        ReservationInfo lisaInfo = facade.addReservation(createCommand("리사", time.getId()));

        ReservationTime newTime = createNewTime();

        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .name("리사")
                .date(LocalDate.now(clock))
                .timeId(newTime.getId())
                .themeId(theme.getId())
                .status(Status.PENDING)
                .build();
        ReservationInfo changedInfo = facade.changeReservation(lisaInfo.id(), changeCommand);

        Assertions.assertThat(changedInfo.status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("대기 중인 예약을 꽉 찬 시간대로 변경하면 여전히 대기 예약으로 남는다.")
    void changePendingToFullSlotTest() {
        facade.addReservation(createCommand("포비", time.getId()));
        ReservationInfo lisaInfo = facade.addReservation(createCommand("리사", time.getId()));

        ReservationTime newTime = createNewTime();
        facade.addReservation(createCommand("브라운", newTime.getId()));

        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .name("리사")
                .date(LocalDate.now(clock))
                .timeId(newTime.getId())
                .themeId(theme.getId())
                .status(Status.PENDING)
                .build();
        ReservationInfo changedInfo = facade.changeReservation(lisaInfo.id(), changeCommand);

        Assertions.assertThat(changedInfo.status()).isEqualTo(Status.PENDING);
    }

    @Test
    @DisplayName("확정 예약을 다른 빈 시간대로 변경하면, 원래 자리의 대기자가 확정으로 승격된다.")
    void changeActiveAndPromotePendingTest() {
        ReservationInfo pobiInfo = facade.addReservation(createCommand("포비", time.getId()));
        facade.addReservation(createCommand("리사", time.getId()));

        ReservationTime newTime = createNewTime();

        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .name("포비")
                .date(LocalDate.now(clock))
                .timeId(newTime.getId())
                .themeId(theme.getId())
                .status(Status.ACTIVE)
                .build();
        ReservationInfo changedInfo = facade.changeReservation(pobiInfo.id(), changeCommand);

        Assertions.assertThat(changedInfo.status()).isEqualTo(Status.ACTIVE);

        List<ReservationPendingInfo> lisaReservations = facade.getReservationsByName("리사");
        Assertions.assertThat(lisaReservations.get(0).status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("확정 예약을 취소하면 대기자가 확정 예약으로 승격된다.")
    void cancelActiveAndPromotePendingTest() {
        ReservationInfo pobiInfo = facade.addReservation(createCommand("포비", time.getId()));
        facade.addReservation(createCommand("리사", time.getId()));

        ReservationCancelCommand cancelCommand = ReservationCancelCommand.builder()
                .name("포비")
                .status(Status.ACTIVE)
                .build();
        facade.cancelReservation(pobiInfo.id(), cancelCommand);

        List<ReservationPendingInfo> lisaReservations = facade.getReservationsByName("리사");
        Assertions.assertThat(lisaReservations.get(0).status()).isEqualTo(Status.ACTIVE);
    }

    private ReservationCreateCommand createCommand(String name, Long timeId) {
        return ReservationCreateCommand.builder()
                .name(name)
                .date(LocalDate.now(clock))
                .timeId(timeId)
                .themeId(theme.getId())
                .build();
    }

    private ReservationTime createNewTime() {
        ReservationTimeInfo info = reservationTimeService.addReservationTime(
                ReservationTimeCommand.builder()
                        .startAt(LocalTime.now(clock).plusHours(1)) // 기존 시간과 다르게 +1시간
                        .build()
        );
        return ReservationTime.builder()
                .id(info.id())
                .startAt(info.startAt())
                .build();
    }
}
