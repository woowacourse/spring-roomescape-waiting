package roomescape.controller.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.*;
import roomescape.service.ReservationWaitingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationWaitingController.class)
class ReservationWaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationWaitingService reservationWaitingService;

    @Test
    void 예약_대기를_생성한다() throws Exception {
        // given
        given(reservationWaitingService.create(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L),
                any(LocalDateTime.class)))
                .willReturn(waitingResult());

        // when & then
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/waitings/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("브라운"))
                .andExpect(jsonPath("$.date").value("2099-01-01"))
                .andExpect(jsonPath("$.time.id").value(1))
                .andExpect(jsonPath("$.time.startAt").value("10:00:00"))
                .andExpect(jsonPath("$.theme.id").value(1))
                .andExpect(jsonPath("$.theme.name").value("테마"))
                .andExpect(jsonPath("$.turn").value(2));

        verify(reservationWaitingService, times(1)).create(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L),
                any(LocalDateTime.class));
        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 이름으로_예약_대기_목록을_조회한다() throws Exception {
        // given
        given(reservationWaitingService.findByName(eq("브라운")))
                .willReturn(List.of(waitingResult()));

        // when & then
        mockMvc.perform(get("/waitings")
                        .param("name", "브라운"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("브라운"))
                .andExpect(jsonPath("$[0].date").value("2099-01-01"))
                .andExpect(jsonPath("$[0].time.id").value(1))
                .andExpect(jsonPath("$[0].theme.id").value(1))
                .andExpect(jsonPath("$[0].theme.name").value("테마"))
                .andExpect(jsonPath("$[0].turn").value(2));

        verify(reservationWaitingService, times(1)).findByName("브라운");
        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기를_취소한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/waitings/1")
                        .param("name", "브라운"))
                .andExpect(status().isNoContent());

        verify(reservationWaitingService, times(1)).delete(eq(1L), eq("브라운"), any(LocalDateTime.class));
        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_생성시_유효하지_않은_입력값이면_에러_응답() throws Exception {
        // given
        String request = """
                {
                  "name": "",
                  "date": "2099-01-01",
                  "timeId": 1,
                  "themeId": 1
                }
                """;

        // when & then
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name은 비어 있을 수 없습니다."));

        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_생성시_요청_본문_형식이_올바르지_않으면_에러_응답() throws Exception {
        // given
        String request = """
                {
                  "name": "브라운",
                  "date": "2099-01-01",
                  "timeId": "abc",
                  "themeId": 1
                }
                """;

        // when & then
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("요청 본문 형식이 올바르지 않습니다."));

        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_조회시_이름이_비어있으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(get("/waitings")
                        .param("name", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name은 비어 있을 수 없습니다."));

        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_취소시_id가_양수가_아니면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(delete("/waitings/0")
                        .param("name", "브라운"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("id는 양수이어야 합니다."));

        verifyNoMoreInteractions(reservationWaitingService);
    }

    private String validRequest() {
        return """
                {
                  "name": "브라운",
                  "date": "2099-01-01",
                  "timeId": 1,
                  "themeId": 1
                }
                """;
    }

    private WaitingWithTurn waitingResult() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        return new WaitingWithTurn(
                new ReservationWaiting(1L, "브라운", new ReservationSlot(LocalDate.of(2099, 1, 1), time, theme)),
                2L);
    }
}
