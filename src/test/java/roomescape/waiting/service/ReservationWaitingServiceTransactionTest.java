package roomescape.waiting.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.ThemeCommand;
import roomescape.theme.service.dto.ThemeResult;
import roomescape.time.service.ReservationTimeService;
import roomescape.time.service.dto.ReservationTimeCommand;
import roomescape.time.service.dto.ReservationTimeResult;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import org.springframework.test.context.ActiveProfiles;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.domain.ReservationWaitingRepository;
import roomescape.waiting.service.dto.ReservationWaitingCommand;
import roomescape.waiting.service.dto.ReservationWaitingResult;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = NONE)
public class ReservationWaitingServiceTransactionTest {

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ThemeService themeService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationWaitingService reservationWaitingService;

    @MockitoSpyBean
    ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    void save_rollback() {
        // given
        ReservationTimeResult reservationTimeResult = saveReservationTime(LocalTime.of(6, 6));
        ThemeResult themeResult = saveTheme();
        ReservationResult reservationResult = saveReservation(reservationTimeResult, themeResult);

        ReservationWaitingCommand command = new ReservationWaitingCommand(
                "임꺽정",
                reservationResult.date(),
                reservationResult.time().id(),
                reservationResult.theme().id()
        );

        doThrow(new RuntimeException("대기 예약 저장 중 에러 발생"))
                .when(reservationWaitingRepository)
                .save(any(ReservationWaiting.class));

        // when
        assertThatThrownBy(() -> reservationWaitingService.save(command, java.time.LocalDateTime.now()))
                .isInstanceOf(RuntimeException.class);

        boolean exists = reservationWaitingRepository.hasWaitingAtSameTime(
                new ReservationWaiting(
                        null,
                        "임꺽정",
                        new ReservationSlot(
                                command.date(),
                                new ReservationTime(reservationTimeResult.id(), reservationTimeResult.startAt()),
                                new Theme(themeResult.id(), themeResult.name(), themeResult.description(), themeResult.thumbnailUrl())
                        ),
                        command.date().atStartOfDay()
                )
        );
        Assertions.assertFalse(exists);
    }

    @Test
    void delete_rollback() {
        // given
        ReservationTimeResult reservationTimeResult = saveReservationTime(LocalTime.of(7, 7));
        ThemeResult themeResult = saveTheme();
        ReservationResult reservationResult = saveReservation(reservationTimeResult, themeResult);

        ReservationWaitingCommand command = new ReservationWaitingCommand(
                "임꺽정",
                reservationResult.date(),
                reservationResult.time().id(),
                reservationResult.theme().id()
        );
        ReservationWaitingResult waitingResult = reservationWaitingService.save(command, java.time.LocalDateTime.now());

        doThrow(new RuntimeException("대기 예약 삭제 중 에러 발생"))
                .when(reservationWaitingRepository)
                .delete(any(ReservationWaiting.class));

        // when
        assertThatThrownBy(() -> reservationWaitingService.deleteOwnedWaitingById(waitingResult.id(), "임꺽정", java.time.LocalDateTime.now()))
                .isInstanceOf(RuntimeException.class);

        // then
        Optional<ReservationWaiting> result = reservationWaitingRepository.findById(waitingResult.id());
        Assertions.assertTrue(result.isPresent());
    }

    private ReservationResult saveReservation(ReservationTimeResult time, ThemeResult theme) {
        ReservationCommand command = new ReservationCommand(
                "홍길동",
                LocalDate.now().plusDays(1),
                time.id(),
                theme.id()
        );
        return reservationService.save(command, java.time.LocalDateTime.now());
    }

    private ThemeResult saveTheme() {
        ThemeCommand testThemeCommand = new ThemeCommand("테스트 테마", "테마 설명", "http://www.thumbnail.kr");
        return themeService.save(testThemeCommand);
    }

    private ReservationTimeResult saveReservationTime(LocalTime startAt) {
        ReservationTimeCommand testReservationTimeCommand = new ReservationTimeCommand(startAt);
        return reservationTimeService.save(testReservationTimeCommand);
    }
}
