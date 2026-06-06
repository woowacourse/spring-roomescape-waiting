package roomescape.presentation.reservation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.application.reservation.ReservationTimeService;
import roomescape.common.auth.AdminRequestValidator;
import roomescape.domain.reservation.ReservationTime;
import roomescape.presentation.error.GlobalExceptionHandler;
import roomescape.presentation.reservation.response.ReservationTimesResponse;
import roomescape.presentation.reservation.response.TimeCreateResponse;

@DisplayName("관리자 예약 시간 컨트롤러")
@WebMvcTest(controllers = AdminReservationTimeController.class)
@Import(GlobalExceptionHandler.class)
class AdminReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @MockitoBean
    private AdminRequestValidator validator;

    @Test
    @DisplayName("관리자는 예약 시간을 조회할 수 있다")
    void getAllReservationTimes() throws Exception {
        // given
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
        given(validator.isUnauthorized(any())).willReturn(false);
        given(reservationTimeService.getAllReservationTime()).willReturn(ReservationTimesResponse.from(List.of(time)));

        // when & then
        mockMvc.perform(get("/admin/times"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.times[0].id").value(1))
                .andExpect(jsonPath("$.times[0].startAt").value("10:00"));

        verify(validator, times(1)).isUnauthorized(any());
        verify(reservationTimeService, times(1)).getAllReservationTime();
    }

    @Test
    @DisplayName("권한이 없으면 예약 시간을 조회할 수 없다")
    void getAllReservationTimesWhenUnauthorized() throws Exception {
        // given
        given(validator.isUnauthorized(any())).willReturn(true);

        // when & then
        mockMvc.perform(get("/admin/times"))
                .andExpect(status().isUnauthorized());

        verify(validator, times(1)).isUnauthorized(any());
        verifyNoInteractions(reservationTimeService);
    }

    @Test
    @DisplayName("관리자는 예약 시간을 생성할 수 있다")
    void createReservationTime() throws Exception {
        // given
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(18, 30));
        given(validator.isUnauthorized(any())).willReturn(false);
        given(reservationTimeService.createReservationTime(any())).willReturn(TimeCreateResponse.from(time));

        // when & then
        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startAt": "18:30"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/times/1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startAt").value("18:30"));

        verify(validator, times(1)).isUnauthorized(any());
        verify(reservationTimeService, times(1)).createReservationTime(any());
    }

    @Test
    @DisplayName("관리자는 예약 시간을 삭제할 수 있다")
    void deleteReservationTime() throws Exception {
        // given
        given(validator.isUnauthorized(any())).willReturn(false);

        // when & then
        mockMvc.perform(delete("/admin/times/{timeId}", 1L))
                .andExpect(status().isNoContent());

        verify(validator, times(1)).isUnauthorized(any());
        verify(reservationTimeService, times(1)).deleteReservationTime(1L);
    }
}
