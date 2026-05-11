package roomescape.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import roomescape.controller.dto.ReservationListResponse;
import roomescape.controller.dto.ReservationResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.ReservationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
        Theme theme = new Theme(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png");
        List<Reservation> reservations = List.of(
                new Reservation(1L, "브라운", LocalDate.of(2023, 8, 5), time, theme),
                new Reservation(2L, "포비", LocalDate.of(2023, 8, 6), time, theme),
                new Reservation(3L, "조이", LocalDate.of(2023, 8, 7), time, theme)
        );
        given(reservationService.findAllReservations()).willReturn(reservations);

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
                .findAllReservations();
    }

    private static void assertReservationsResponse(ReservationListResponse response) {
        assertThat(response.reservations()).hasSize(3)
                .extracting(
                        ReservationResponse::id,
                        ReservationResponse::name,
                        ReservationResponse::date
                )
                .containsExactly(
                        tuple(1L, "브라운", "2023-08-05"),
                        tuple(2L, "포비", "2023-08-06"),
                        tuple(3L, "조이", "2023-08-07")
                );
    }

    @Test
    @DisplayName("특정 예약을 삭제하는 요청을 한다.")
    public void delete() throws Exception {
        // when then
        long id = 1L;
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/admin/reservations/{id}", id))
                .andDo(print())
                .andExpect(status().isNoContent());

        then(reservationService).should().delete(id);
    }
}
