package roomescape.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.CookieProvider;
import roomescape.auth.JwtTokenProvider;
import roomescape.service.MemberService;
import roomescape.service.ReservationTimeService;
import roomescape.service.param.CreateReservationTimeParam;
import roomescape.service.result.ReservationTimeResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.TestFixture.TEST_TIME;

@WebMvcTest(ReservationTimeController.class)
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CookieProvider cookieProvider;

    @Test
    @DisplayName("예약 시간을 생성할 수 있다.")
    void createReservationTime() throws Exception {
        // given
        String timeJson = String.format("""
                {
                    "startAt": "%s"
                }
                """, TEST_TIME);

        ReservationTimeResult timeResult = new ReservationTimeResult(1L, TEST_TIME);
        when(reservationTimeService.create(any(CreateReservationTimeParam.class))).thenReturn(timeResult);

        // when & then
        mockMvc.perform(post("/times")
                .contentType(MediaType.APPLICATION_JSON)
                .content(timeJson)
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(timeResult.id()));
    }

    @Test
    @DisplayName("예약 시간 목록을 조회할 수 있다.")
    void getReservationTimes() throws Exception {
        // given
        ReservationTimeResult timeResult = new ReservationTimeResult(1L, TEST_TIME);
        when(reservationTimeService.findAll()).thenReturn(List.of(timeResult));

        // when & then
        mockMvc.perform(get("/times"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(timeResult.id()));
    }

    @Test
    @DisplayName("예약 시간을 삭제할 수 있다.")
    void deleteReservationTime() throws Exception {
        mockMvc.perform(delete("/times/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
} 