package roomescape.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import roomescape.reservation.controller.dto.ReservationListResponse;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.reservation.service.ReservationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminReservationController.class)
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    @DisplayName("예약 목록을 조회한다.")
    public void getReservationList() throws Exception {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = Theme.of(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png");
        List<Reservation> reservations = List.of(
                Reservation.of(1L, "브라운", LocalDate.of(2023, 8, 5), time, theme),
                Reservation.of(2L, "포비", LocalDate.of(2023, 8, 6), time, theme),
                Reservation.of(3L, "조이", LocalDate.of(2023, 8, 7), time, theme)
        );
        given(reservationService.findAllReservations(1, 20)).willReturn(reservations);

        // when then
        MvcResult result = mockMvc.perform(get("/admin/reservations"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ReservationListResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ReservationListResponse.class
        );

        assertReservationsResponse(response);

        then(reservationService)
                .should()
                .findAllReservations(1, 20);
    }

    @Test
    @DisplayName("예약 목록을 요청한 페이지와 크기로 조회한다.")
    public void getReservationList_withPaging() throws Exception {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = Theme.of(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png");
        List<Reservation> reservations = List.of(
                Reservation.of(3L, "조이", LocalDate.of(2023, 8, 7), time, theme)
        );
        given(reservationService.findAllReservations(2, 2)).willReturn(reservations);

        // when then
        MvcResult result = mockMvc.perform(get("/admin/reservations")
                        .param("page", "2")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ReservationListResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ReservationListResponse.class
        );

        assertThat(response.reservations()).hasSize(1)
                .extracting(
                        ReservationResponse::id,
                        ReservationResponse::guestName,
                        ReservationResponse::date
                )
                .containsExactly(tuple(3L, "조이", "2023-08-07"));

        then(reservationService)
                .should()
                .findAllReservations(2, 2);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 20",
            "1, 0",
            "1, 21"
    })
    @DisplayName("예약 목록을 조회할 때 페이지 요청 검증에 실패하면 에러가 발생한다.")
    public void getReservationList_fail_when_invalid_page_request(
            String page, String size
    ) throws Exception {
        // when then
        mockMvc.perform(get("/admin/reservations")
                        .param("page", page)
                        .param("size", size))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        then(reservationService).should(never())
                .findAllReservations(anyInt(), anyInt());
    }

    private static void assertReservationsResponse(ReservationListResponse response) {
        assertThat(response.reservations()).hasSize(3)
                .extracting(
                        ReservationResponse::id,
                        ReservationResponse::guestName,
                        ReservationResponse::date
                )
                .containsExactly(
                        tuple(1L, "브라운", "2023-08-05"),
                        tuple(2L, "포비", "2023-08-06"),
                        tuple(3L, "조이", "2023-08-07")
                );
    }

    @Test
    @DisplayName("특정 예약을 취소하는 요청을 한다.")
    public void cancel_success() throws Exception {
        // when then
        long id = 1L;
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/admin/reservations/{id}", id))
                .andDo(print())
                .andExpect(status().isNoContent());

        then(reservationService).should().cancel(id);
    }
}
