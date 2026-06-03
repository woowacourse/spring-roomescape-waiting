package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.DatabaseHelper;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.ReservationServiceRollbackTest.ServiceIntegrationTestConfig;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservationWaiting.exception.ReservationWaitingNotFoundException;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;

@Transactional(propagation =  Propagation.NOT_SUPPORTED)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(ServiceIntegrationTestConfig.class)
public class ReservationServiceRollbackTest {

    private static final long TIME_ID = 1L;
    private static final long RESERVATION_ID = 1L;
    private static final long WAITING_ID = 1L;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ReservationService reservationService;

    @MockitoSpyBean
    ReservationRepository reservationRepository;

    @MockitoSpyBean
    ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void cleanUpDatabase() {
        databaseHelper.clear();
    }

    @DisplayName("예약 변경 후 기존 슬롯의 대기 삭제가 실패하면 예약 변경이 롤백된다.")
    @Test
    void updateReservationTest_roll_back_when_waiting_delete_fails() {
        //given
        jdbcTemplate.update("""
                INSERT INTO reservation_time (start_at)
                VALUES ('10:00:00')
                """);
        jdbcTemplate.update("""
                INSERT INTO theme (name, description, thumbnail_url)
                VALUES ('우주선 탈출', '고장 난 우주선에서 제한 시간 안에 탈출하세요.', 'https://example.com/themes/space-escape.jpg')
                """);
        jdbcTemplate.update("""
                INSERT INTO reservation (name, reservation_date, time_id, theme_id)
                VALUES ('brown', '2026-05-05', 1, 1)
                """);
        jdbcTemplate.update("""
                INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id)
                VALUES ('pobi', '2026-05-05', 1, 1)
                """);

        doReturn(0)
                .when(reservationWaitingRepository)
                .deleteById(anyLong());

        //when & then
        assertThatThrownBy(() -> reservationService.updateReservation(
                new ReservationUpdateCommand(LocalDate.of(2026, 5, 6), TIME_ID),
                RESERVATION_ID,
                "brown"
        )).isInstanceOf(ReservationWaitingNotFoundException.class);

        assertReservationDate(RESERVATION_ID, LocalDate.of(2026, 5, 5));
        assertWaitingDate(WAITING_ID, LocalDate.of(2026, 5, 5));
    }

    private void assertReservationDate(Long id, LocalDate date) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT reservation_date FROM reservation WHERE id = ?",
                Date.class,
                id
        ).toLocalDate()).isEqualTo(date);
    }

    private void assertWaitingDate(Long id, LocalDate date) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT reservation_date FROM reservation_waiting WHERE id = ?",
                Date.class,
                id
        ).toLocalDate()).isEqualTo(date);
    }


    @DisplayName("예약 변경 후 기존 슬롯의 대기 승격 저장이 실패하면 예약 변경과 대기 삭제가 롤백된다.")
    @Test
    void updateReservationTest_rolls_back_when_promotion_save_Fails() {
        //given
        jdbcTemplate.update("""
                INSERT INTO reservation_time (start_at)
                VALUES ('10:00:00')
                """);
        jdbcTemplate.update("""
                INSERT INTO theme (name, description, thumbnail_url)
                VALUES ('우주선 탈출', '고장 난 우주선에서 제한 시간 안에 탈출하세요.', 'https://example.com/themes/space-escape.jpg')
                """);
        jdbcTemplate.update("""
                INSERT INTO reservation (name, reservation_date, time_id, theme_id)
                VALUES ('brown', '2026-05-05', 1, 1)
                """);
        jdbcTemplate.update("""
                INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id)
                VALUES ('pobi', '2026-05-05', 1, 1)
                """);

        doThrow(new DuplicateKeyException("duplicate"))
                .when(reservationRepository)
                .save(any(Reservation.class));

        //when & then
        assertThatThrownBy(() -> reservationService.updateReservation(
                new ReservationUpdateCommand(LocalDate.of(2026, 5, 6), TIME_ID),
                RESERVATION_ID,
                "brown"
        )).isInstanceOf(DuplicateReservationException.class);

        assertAll(
                () -> assertReservationDate(RESERVATION_ID, LocalDate.of(2026, 5, 5)),
                () -> assertWaitingCount(WAITING_ID, 1)
        );
    }

    private void assertWaitingCount(Long id, int expectedCount) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_waiting WHERE id = ?", Integer.class, id
        )).isEqualTo(expectedCount);
    }

    @DisplayName("예약 삭제 후 기존 슬롯의 대기 삭제가 실패하면 예약 삭제가 롤백된다.")
    @Test
    void deleteReservationByIdTest_rolls_back_when_waiting_delete_fails() {
        //given
        jdbcTemplate.update("""
                INSERT INTO reservation_time (start_at)
                VALUES ('10:00:00')
                """);
        jdbcTemplate.update("""
                INSERT INTO theme (name, description, thumbnail_url)
                VALUES ('우주선 탈출', '고장 난 우주선에서 제한 시간 안에 탈출하세요.', 'https://example.com/themes/space-escape.jpg')
                """);
        jdbcTemplate.update("""
                INSERT INTO reservation (name, reservation_date, time_id, theme_id)
                VALUES ('brown', '2026-05-05', 1, 1)
                """);
        jdbcTemplate.update("""
                INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id)
                VALUES ('pobi', '2026-05-05', 1, 1)
                """);

        doReturn(0)
                .when(reservationWaitingRepository)
                .deleteById(anyLong());

        //when & then
        assertThatThrownBy(() -> reservationService.deleteReservationById(RESERVATION_ID))
                .isInstanceOf(ReservationWaitingNotFoundException.class);

        assertAll(
                () -> assertReservationCount(RESERVATION_ID, 1),
                () -> assertWaitingCount(WAITING_ID, 1)
        );
    }

    private void assertReservationCount(Long id, int expectedCount) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?", Integer.class, id
        )).isEqualTo(expectedCount);
    }

    @DisplayName("예약 삭제 후 기존 슬롯의 대기 승격 저장이 실패하면 예약 삭제와 대기 삭제가 롤백된다.")
    @Test
    void deleteReservationByIdTest_rolls_back_when_promotion_save_fails() {
        //given
        jdbcTemplate.update("""
                INSERT INTO reservation_time (start_at)
                VALUES ('10:00:00')
                """);
        jdbcTemplate.update("""
                INSERT INTO theme (name, description, thumbnail_url)
                VALUES ('우주선 탈출', '고장 난 우주선에서 제한 시간 안에 탈출하세요.', 'https://example.com/themes/space-escape.jpg')
                """);
        jdbcTemplate.update("""
                INSERT INTO reservation (name, reservation_date, time_id, theme_id)
                VALUES ('brown', '2026-05-05', 1, 1)
                """);
        jdbcTemplate.update("""
                INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id)
                VALUES ('pobi', '2026-05-05', 1, 1)
                """);

        doThrow(new DuplicateKeyException("duplicate"))
                .when(reservationRepository)
                .save(any(Reservation.class));

        //when & then
        assertThatThrownBy(() -> reservationService.deleteReservationById(RESERVATION_ID))
                .isInstanceOf(DuplicateReservationException.class);

        assertAll(
                () -> assertReservationCount(RESERVATION_ID, 1),
                () -> assertWaitingCount(WAITING_ID, 1)
        );
    }

    @TestConfiguration
    static class ServiceIntegrationTestConfig {

        @Bean
        public Clock clock() {
            return Clock.fixed(
                    Instant.parse("2026-05-01T09:00:00+09:00"),
                    ZoneId.of("Asia/Seoul")
            );
        }

        @Bean
        public DatabaseHelper databaseHelper(JdbcTemplate jdbcTemplate) {
            return new DatabaseHelper(jdbcTemplate);
        }
    }
}
