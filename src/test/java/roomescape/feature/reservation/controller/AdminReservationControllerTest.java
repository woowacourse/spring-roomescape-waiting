package roomescape.feature.reservation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.feature.reservation.domain.OrderStatus;
import roomescape.feature.reservation.dto.response.ReservationEditableStatus;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.reservation.error.type.ReservationErrorType;
import roomescape.feature.reservation.service.AdminReservationService;
import roomescape.feature.theme.dto.response.ReservationThemeResponseDto;
import roomescape.feature.time.dto.response.ReservationTimeResponseDto;
import roomescape.fixture.ThemeFixture;
import roomescape.fixture.TimeFixture;
import roomescape.global.error.exception.GeneralException;

@WebMvcTest(AdminReservationController.class)
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminReservationService reservationService;

    private ReservationResponseDto sampleResponse() {
        return new ReservationResponseDto(
            1L, "예약자", LocalDate.of(2099, 5, 1),
            new ReservationTimeResponseDto(1L, TimeFixture.VALID_10_00.getStartAt(), false),
            new ReservationThemeResponseDto(1L, ThemeFixture.VALID.getName(),
                ThemeFixture.VALID.getDescription(), ThemeFixture.VALID.getImageUrl(), false),
            ReservationEditableStatus.EDITABLE, "", null, OrderStatus.CONFIRMED
        );
    }

    @Nested
    class 전체_예약_조회 {

        @Test
        void 전체_예약_목록을_조회한다() throws Exception {
            when(reservationService.getReservations()).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo("예약자")));
        }

        @Test
        void 예약이_없으면_빈_목록을_반환한다() throws Exception {
            when(reservationService.getReservations()).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    class 예약_삭제 {

        @Test
        void 예약을_삭제한다() throws Exception {
            doNothing().when(reservationService).deleteReservationById(1L);

            mockMvc.perform(delete("/api/admin/reservations/1"))
                .andExpect(status().isNoContent());
        }

        @Test
        void 존재하지_않는_예약_ID이면_4xx를_반환한다() throws Exception {
            doThrow(new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND))
                .when(reservationService).deleteReservationById(999L);

            mockMvc.perform(delete("/api/admin/reservations/999"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 음수_ID이면_4xx를_반환한다() throws Exception {
            mockMvc.perform(delete("/api/admin/reservations/-1"))
                .andExpect(status().is4xxClientError());
        }
    }
}
