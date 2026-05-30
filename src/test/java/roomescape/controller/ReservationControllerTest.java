package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.domain.reservation.Rank;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationResult;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    private ReservationResult approvedResult() {
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
        Theme theme = Theme.load(1L, new ThemeName("공포"), "무서워요", new ThumbnailUrl("https://zeze.com"));
        Reservation reservation = Reservation.load(1L,
                new ReservationName("zeze"),
                new ReservationDate(LocalDate.of(2099, 1, 1)),
                time, theme, LocalDateTime.now());
        return new ReservationResult(new Rank(1), reservation);
    }

    private ReservationResult waitingResult() {
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
        Theme theme = Theme.load(1L, new ThemeName("공포"), "무서워요", new ThumbnailUrl("https://zeze.com"));
        Reservation reservation = Reservation.load(2L,
                new ReservationName("mingu"),
                new ReservationDate(LocalDate.of(2099, 1, 1)),
                time, theme, LocalDateTime.now());
        return new ReservationResult(new Rank(2), reservation);
    }

    @Test
    void 예약_생성_성공시_201을_반환한다() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.of(2099, 1, 1), 1L, 1L);
        given(reservationService.reserve(any(), any())).willReturn(approvedResult());

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("zeze"))
                .andExpect(jsonPath("$.state").value("승인"))
                .andExpect(jsonPath("$.rank").value(1));
    }

    @Test
    void 예약_생성시_이름이_없으면_400을_반환한다() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest(null, LocalDate.of(2099, 1, 1), 1L, 1L);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_생성시_날짜가_없으면_400을_반환한다() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest("zeze", null, 1L, 1L);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_생성시_TimeId가_없으면_400을_반환한다() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.of(2099, 1, 1), null, 1L);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_생성시_서비스에서_중복_예외_발생시_409를_반환한다() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.of(2099, 1, 1), 1L, 1L);
        given(reservationService.reserve(any(), any()))
                .willThrow(new RoomEscapeException(ErrorCode.DUPLICATE_RESERVATION));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void 예약_생성시_과거_날짜면_422를_반환한다() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.of(2000, 1, 1), 1L, 1L);
        given(reservationService.reserve(any(), any()))
                .willThrow(new RoomEscapeException(ErrorCode.PAST_RESERVATION_NOT_ALLOWED));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void 예약_전체_목록_조회_성공시_200을_반환한다() throws Exception {
        given(reservationService.findList(null)).willReturn(List.of(approvedResult(), waitingResult()));

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void 이름으로_예약_목록_조회_성공시_200을_반환한다() throws Exception {
        given(reservationService.findList("zeze")).willReturn(List.of(approvedResult()));

        mockMvc.perform(get("/reservations").param("name", "zeze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("zeze"));
    }

    @Test
    void 예약_단건_조회_성공시_200을_반환한다() throws Exception {
        given(reservationService.find(1L)).willReturn(approvedResult());

        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void 없는_예약_단건_조회시_404를_반환한다() throws Exception {
        given(reservationService.find(999L))
                .willThrow(new RoomEscapeException(ErrorCode.RESERVATION_NOT_FOUND));

        mockMvc.perform(get("/reservations/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 예약_삭제_성공시_200을_반환한다() throws Exception {


        mockMvc.perform(delete("/reservations/1")
                        .param("name", "홍길동"))
                .andExpect(status().isNoContent());
    }

    @Test
    void 예약_삭제시_이름이_다르면_401을_반환한다() throws Exception {
        willThrow(new RoomEscapeException(ErrorCode.UNAUTHORIZED_SAME_NAME))
                .given(reservationService).cancel(anyLong(), anyString(), any());

        mockMvc.perform(delete("/reservations/1")
                        .param("name","other"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 예약_삭제시_이름이_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_수정_성공시_200을_반환한다() throws Exception {
        ReservationUpdateRequest request = new ReservationUpdateRequest("zeze", LocalDate.of(2099, 6, 1), 1L, 1L);
        given(reservationService.update(any(), anyLong(), any())).willReturn(approvedResult());

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void 예약_수정시_존재하지_않는_예약이면_404를_반환한다() throws Exception {
        ReservationUpdateRequest request = new ReservationUpdateRequest("zeze", LocalDate.of(2099, 6, 1), 1L, 1L);
        given(reservationService.update(any(), anyLong(), any()))
                .willThrow(new RoomEscapeException(ErrorCode.RESERVATION_NOT_FOUND));

        mockMvc.perform(put("/reservations/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 예약_수정시_과거_날짜면_422를_반환한다() throws Exception {
        ReservationUpdateRequest request = new ReservationUpdateRequest("zeze", LocalDate.of(2000, 1, 1), 1L, 1L);
        given(reservationService.update(any(), anyLong(), any()))
                .willThrow(new RoomEscapeException(ErrorCode.PAST_RESERVATION_NOT_ALLOWED));

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void 대기_예약_조회시_상태가_대기로_반환된다() throws Exception {
        given(reservationService.find(2L)).willReturn(waitingResult());

        mockMvc.perform(get("/reservations/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("대기"))
                .andExpect(jsonPath("$.rank").value(2));
    }
}
