package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.controller.dto.request.ReservationUpdateRequest;
import roomescape.reservation.domain.exception.ReservationSlotDuplicateException;

@Sql("/clear.sql")
@SpringBootTest
class ReservationTransactionIntegrationTest {

    private static final String DATE = "2026-08-01";
    private static final long TIME_ID = 1L;
    private static final long THEME_ID = 1L;

    @Autowired
    ReservationApplicationService reservationApplicationService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    ReservationService reservationService;

    @AfterEach
    void resetSpy() {
        Mockito.reset(reservationService);
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(
                LocalDate.of(2026, 6, 8)
                    .atStartOfDay(ZoneId.of("Asia/Seoul"))
                    .toInstant(),
                ZoneId.of("Asia/Seoul")
            );
        }
    }

    @Nested
    @DisplayName("예약을 취소하면 대기를 예약으로 승격한다")
    class CancelAndPromote {

        @Test
        void 취소_시_가장_빠른_대기자가_예약으로_승격된다() {
            // given
            insertReservationTime("12:00");
            insertTheme("테마", "설명", "url");
            insertReservation("customer", DATE, TIME_ID, THEME_ID);
            insertWaiting("코로구", DATE, TIME_ID, THEME_ID);
            insertWaiting("재키", DATE, TIME_ID, THEME_ID);

            // when
            reservationApplicationService.cancelReservationByIdAndPromoteWaiting(1L);

            // then
            assertThat(countReservations()).isEqualTo(1);
            assertThat(findReservationName(DATE, TIME_ID, THEME_ID)).isEqualTo("코로구");
            assertThat(countWaitings()).isEqualTo(1);
        }

        @Test
        void 대기_승격_실패_시_예약_취소는_커밋되고_대기는_복구된다() {
            // given
            insertReservationTime("12:00");
            insertTheme("테마", "설명", "url");
            insertReservation("customer", DATE, TIME_ID, THEME_ID);
            insertWaiting("코로구", DATE, TIME_ID, THEME_ID);

            doThrow(new ReservationSlotDuplicateException())
                .when(reservationService).promote(any(), any(), any(), any());

            // when
            reservationApplicationService.cancelReservationByIdAndPromoteWaiting(1L);

            // then
            assertThat(countReservations()).isZero();
            assertThat(countWaitings()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("관리자가 예약을 삭제하면 대기를 예약으로 승격한다")
    class DeleteAndPromote {

        @Test
        void 예약_삭제_시_가장_빠른_대기가_예약으로_승격된다() {
            // given
            insertReservationTime("12:00");
            insertTheme("테마", "설명", "url");
            insertReservation("customer", DATE, TIME_ID, THEME_ID);
            insertWaiting("코로구", DATE, TIME_ID, THEME_ID);
            insertWaiting("재키", DATE, TIME_ID, THEME_ID);

            // when
            reservationApplicationService.deleteReservationById(1L);

            // then
            assertThat(countReservations()).isEqualTo(1);
            assertThat(findReservationName(DATE, TIME_ID, THEME_ID)).isEqualTo("코로구");
            assertThat(countWaitings()).isEqualTo(1);
        }

        @Test
        void 대기_승격_실패_시_예약_삭제는_커밋되고_대기는_복구된다() {
            // given
            insertReservationTime("12:00");
            insertTheme("테마", "설명", "url");
            insertReservation("customer", DATE, TIME_ID, THEME_ID);
            insertWaiting("코로구", DATE, TIME_ID, THEME_ID);

            doThrow(new ReservationSlotDuplicateException())
                .when(reservationService).promote(any(), any(), any(), any());

            // when
            reservationApplicationService.deleteReservationById(1L);

            // then
            assertThat(countReservations()).isZero();
            assertThat(countWaitings()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("예약 슬롯을 변경하면 대기를 예약으로 승격한다")
    class UpdateAndPromote {

        private static final long NEW_TIME_ID = 2L;

        @Test
        void 사용자가_예약_슬롯_변경_시_가장_빠른_대기가_예약으로_승격된다() {
            // given
            insertReservationTime("12:00");
            insertReservationTime("13:00");
            insertTheme("테마", "설명", "url");
            insertReservation("customer", DATE, TIME_ID, THEME_ID);
            insertWaiting("코로구", DATE, TIME_ID, THEME_ID);

            // when
            reservationApplicationService.updateByCustomer(
                1L, new ReservationUpdateRequest(LocalDate.parse(DATE), NEW_TIME_ID));

            // then
            assertThat(countReservations()).isEqualTo(2);
            assertThat(findReservationName(DATE, TIME_ID, THEME_ID)).isEqualTo("코로구");
            assertThat(findReservationName(DATE, NEW_TIME_ID, THEME_ID)).isEqualTo("customer");
            assertThat(countWaitings()).isZero();
        }

        @Test
        void 관리자가_예약_슬롯_변경_시_가장_빠른_대기가_예약으로_승격된다() {
            // given
            insertReservationTime("12:00");
            insertReservationTime("13:00");
            insertTheme("테마", "설명", "url");
            insertReservation("customer", DATE, TIME_ID, THEME_ID);
            insertWaiting("코로구", DATE, TIME_ID, THEME_ID);

            // when
            reservationApplicationService.updateByAdmin(
                1L, new ReservationUpdateRequest(LocalDate.parse(DATE), NEW_TIME_ID));

            // then
            assertThat(countReservations()).isEqualTo(2);
            assertThat(findReservationName(DATE, TIME_ID, THEME_ID)).isEqualTo("코로구");
            assertThat(findReservationName(DATE, NEW_TIME_ID, THEME_ID)).isEqualTo("customer");
            assertThat(countWaitings()).isZero();
        }

        @Test
        void 사용자가_예약_슬롯_변경_실패_시_슬롯_변경은_커밋되고_대기는_복구된다() {
            // given
            insertReservationTime("12:00");
            insertReservationTime("13:00");
            insertTheme("테마", "설명", "url");
            insertReservation("customer", DATE, TIME_ID, THEME_ID);
            insertWaiting("코로구", DATE, TIME_ID, THEME_ID);

            doThrow(new ReservationSlotDuplicateException())
                .when(reservationService).promote(any(), any(), any(), any());

            // when
            reservationApplicationService.updateByCustomer(
                1L, new ReservationUpdateRequest(LocalDate.parse(DATE), NEW_TIME_ID));

            // then
            assertThat(countReservations()).isEqualTo(1);
            assertThat(findReservationName(DATE, NEW_TIME_ID, THEME_ID)).isEqualTo("customer");
            assertThat(countWaitings()).isEqualTo(1);
        }

        @Test
        void 관리자가_예약_슬롯_변경_실패_시_슬롯_변경은_커밋되고_대기는_복구된다() {
            // given
            insertReservationTime("12:00");
            insertReservationTime("13:00");
            insertTheme("테마", "설명", "url");
            insertReservation("customer", DATE, TIME_ID, THEME_ID);
            insertWaiting("코로구", DATE, TIME_ID, THEME_ID);

            doThrow(new ReservationSlotDuplicateException())
                .when(reservationService).promote(any(), any(), any(), any());

            // when
            reservationApplicationService.updateByAdmin(
                1L, new ReservationUpdateRequest(LocalDate.parse(DATE), NEW_TIME_ID));

            // then
            assertThat(countReservations()).isEqualTo(1);
            assertThat(findReservationName(DATE, NEW_TIME_ID, THEME_ID)).isEqualTo("customer");
            assertThat(countWaitings()).isEqualTo(1);
        }
    }

    private String findReservationName(final String date, final long timeId, final long themeId) {
        return jdbcTemplate.queryForObject("""
                SELECT name FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?
                """,
            String.class,
            date,
            timeId,
            themeId
        );
    }

    private int countReservations() {
        return jdbcTemplate.queryForObject("SELECT count(1) FROM reservation", Integer.class);
    }

    private int countWaitings() {
        return jdbcTemplate.queryForObject("SELECT count(1) FROM waiting", Integer.class);
    }

    private void insertReservationTime(final String startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", startAt);
    }

    private void insertTheme(final String name, final String description, final String url) {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
            name, description, url);
    }

    private void insertReservation(final String name, final String date, final long timeId, final long themeId) {
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
            name, date, timeId, themeId);
    }

    private void insertWaiting(final String customerName, final String date, final long timeId, final long themeId) {
        jdbcTemplate.update(
            "INSERT INTO waiting (customer_name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
            customerName, date, timeId, themeId);
    }
}
