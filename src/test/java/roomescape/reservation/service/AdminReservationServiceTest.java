package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.controller.dto.response.AdminReservationResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationServiceTest {
    @Autowired
    private AdminReservationService adminReservationService;

    @DisplayName("날짜의 시작과 끝을 기준으로 예약을 조회한다.")
    @Test
    void findByDateFromAndDateTo() {
        // given
        LocalDate dateFrom = LocalDate.parse("2024-12-11");
        LocalDate dateTo = LocalDate.parse("2024-12-24");

        // when
        List<AdminReservationResponse> results = adminReservationService.getByFilter(null, null, dateFrom, dateTo);

        // then
        assertEquals(2, results.size());
        assertThat(results.get(0).date()).isEqualTo(LocalDate.parse("2024-12-12"));
        assertThat(results.get(1).date()).isEqualTo(LocalDate.parse("2024-12-23"));
    }

    @DisplayName("예약자 번호와 테마 번호를 기준으로 예약을 조회한다.")
    @Test
    void findByMemberIdAndThemeId() {
        // given
        Long memberId = 1L;
        Long themeId = 1L;

        // when
        List<AdminReservationResponse> results = adminReservationService.getByFilter(memberId, themeId, null, null);

        // then
        assertEquals(1, results.size());
        assertThat(results.get(0).date()).isEqualTo(LocalDate.parse("2024-12-12"));
    }

    @DisplayName("예약자 번호를 기준으로 예약을 조회한다.")
    @Test
    void findByMemberId() {
        // given
        Long memberId = 1L;

        // when
        List<AdminReservationResponse> results = adminReservationService.getByFilter(memberId, null, null, null);

        // then
        assertEquals(2, results.size());
        assertThat(results.get(0).date()).isEqualTo(LocalDate.parse("2024-12-12"));
        assertThat(results.get(1).date()).isEqualTo(LocalDate.parse("2024-12-23"));
    }

    @DisplayName("테마 번호를 기준으로 예약을 조회한다.")
    @Test
    void findByThemeId() {
        // given
        Long themeId = 1L;

        // when
        List<AdminReservationResponse> results = adminReservationService.getByFilter(null, themeId, null, null);

        // then
        assertEquals(1, results.size());
        assertThat(results.get(0).date()).isEqualTo(LocalDate.parse("2024-12-12"));
    }

    @DisplayName("검색 시 시작일 기준으로 예약을 조회한다.")
    @Test
    void findByDateFrom() {
        // given
        LocalDate dateFrom = LocalDate.parse("2024-12-24");

        // when
        List<AdminReservationResponse> results = adminReservationService.getByFilter(null, null, dateFrom, null);

        // then
        assertEquals(1, results.size());
        assertThat(results.get(0).date()).isEqualTo(LocalDate.parse("2024-12-25"));
    }

    @DisplayName("검색 시 종료일 기준으로 예약을 조회한다.")
    @Test
    void findByDateTo() {
        // given
        LocalDate dateTo = LocalDate.parse("2024-12-24");

        // when
        List<AdminReservationResponse> results = adminReservationService.getByFilter(null, null, null, dateTo);

        // then
        assertEquals(2, results.size());
        assertThat(results.get(0).date()).isEqualTo(LocalDate.parse("2024-12-12"));
        assertThat(results.get(1).date()).isEqualTo(LocalDate.parse("2024-12-23"));
    }
}
