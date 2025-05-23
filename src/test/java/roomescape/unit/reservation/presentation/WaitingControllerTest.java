package roomescape.unit.reservation.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.auth.presentation.AuthorizationExtractor;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.WaitingRequest;
import roomescape.reservation.dto.response.TimeSlotResponse;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.dto.response.WaitingWithRankResponse;
import roomescape.reservation.presentation.WaitingController;
import roomescape.reservation.service.WaitingService;

@WebMvcTest(value = {WaitingController.class, AuthorizationExtractor.class})
public class WaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private WaitingService waitingService;

    @Test
    void 대기_생성에_성공한다() throws Exception {
        // given
        TimeSlotResponse timeResponse = new TimeSlotResponse(1L, LocalTime.of(9, 0));
        WaitingRequest request = new WaitingRequest(LocalDate.of(2025, 1, 1), 1L, 1L);
        WaitingResponse response = new WaitingResponse(1L, "member1", LocalDate.of(2025, 1, 1), timeResponse, "theme1");
        given(tokenProvider.extractSubject("accessToken")).willReturn("1");
        given(waitingService.createWaiting(1L, request)).willReturn(response);
        // when & then
        mockMvc.perform(post("/api/waiting")
                        .cookie(new Cookie("token", "accessToken"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/waiting/1"))
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    void 사용자의_대기목록을_조회한다() throws Exception {
        // given
        Waiting waiting = Waiting.builder()
                .date(LocalDate.of(2025, 1, 1))
                .timeSlot(TimeSlot.builder().startAt(LocalTime.of(9, 0)).build())
                .member(
                        Member.builder()
                                .id(1L)
                                .name("name1").build()
                ).theme(
                        Theme.builder()
                                .name("theme1")
                                .description("desc")
                                .thumbnail("thumb").build()
                ).build();
        WaitingWithRankResponse waitingWithRankResponse = new WaitingWithRankResponse(waiting, 1L);
        List<WaitingWithRankResponse> response = List.of(waitingWithRankResponse);
        given(tokenProvider.extractSubject("accessToken")).willReturn("1");
        given(waitingService.findWaitingByMemberId(1L)).willReturn(response);
        // when & then
        mockMvc.perform(get("/api/waiting/my")
                        .cookie(new Cookie("token", "accessToken"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    void 자신_대기를_취소한다() throws Exception {
        // given
        given(tokenProvider.extractSubject("accessToken")).willReturn("1");
        // when & then
        mockMvc.perform(delete("/api/waiting/{waitingId}", 1L)
                        .cookie(new Cookie("token", "accessToken"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
