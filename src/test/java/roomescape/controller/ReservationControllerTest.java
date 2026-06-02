package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.RoomEscapeFixture;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void 예약_생성_성공시_201을_반환한다() throws Exception {
        ReservationCreateRequest request = RoomEscapeFixture.reservationCreateRequest();
        given(reservationService.reserve(any(), any())).willReturn(RoomEscapeFixture.reservationResultWithApproved());

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
        ReservationCreateRequest request = RoomEscapeFixture.reservationCreateRequestWithNullName();

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_생성시_날짜가_없으면_400을_반환한다() throws Exception {
        ReservationCreateRequest request = RoomEscapeFixture.reservationCreateRequestWithNullDate();

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_생성시_TimeId가_없으면_400을_반환한다() throws Exception {
        ReservationCreateRequest request = RoomEscapeFixture.reservationCreateRequestWithNullTimeId();

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_생성시_서비스에서_중복_예외_발생시_409를_반환한다() throws Exception {
        ReservationCreateRequest request = RoomEscapeFixture.reservationCreateRequest();
        given(reservationService.reserve(any(), any()))
                .willThrow(new RoomEscapeException(ErrorCode.DUPLICATE_RESERVATION));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void 예약_생성시_과거_날짜면_422를_반환한다() throws Exception {
        ReservationCreateRequest request = RoomEscapeFixture.reservationCreateRequestWithPastDate();
        given(reservationService.reserve(any(), any()))
                .willThrow(new RoomEscapeException(ErrorCode.PAST_RESERVATION_NOT_ALLOWED));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void 예약_전체_목록_조회_성공시_200을_반환한다() throws Exception {
        given(reservationService.findList(null)).willReturn(List.of(
                RoomEscapeFixture.reservationResultWithApproved(),
                RoomEscapeFixture.reservationResultWithWaiting()));

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void 이름으로_예약_목록_조회_성공시_200을_반환한다() throws Exception {
        given(reservationService.findList("zeze")).willReturn(
                List.of(RoomEscapeFixture.reservationResultWithApproved()));

        mockMvc.perform(get("/reservations").param("name", "zeze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("zeze"));
    }

    @Test
    void 예약_단건_조회_성공시_200을_반환한다() throws Exception {
        given(reservationService.find(1L)).willReturn(RoomEscapeFixture.reservationResultWithApproved());

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
        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isOk());
    }

    @Test
    void 예약_수정_성공시_200을_반환한다() throws Exception {
        ReservationUpdateRequest request = RoomEscapeFixture.reservationUpdateRequest();
        given(reservationService.update(any(), anyLong(), any())).willReturn(
                RoomEscapeFixture.reservationResultWithApproved());

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void 예약_수정시_존재하지_않는_예약이면_404를_반환한다() throws Exception {
        ReservationUpdateRequest request = RoomEscapeFixture.reservationUpdateRequest();
        ;
        given(reservationService.update(any(), anyLong(), any()))
                .willThrow(new RoomEscapeException(ErrorCode.RESERVATION_NOT_FOUND));

        mockMvc.perform(put("/reservations/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 예약_수정시_과거_날짜면_422를_반환한다() throws Exception {
        ReservationUpdateRequest request = RoomEscapeFixture.reservationUpdateRequestWithPastDate();
        given(reservationService.update(any(), anyLong(), any()))
                .willThrow(new RoomEscapeException(ErrorCode.PAST_RESERVATION_NOT_ALLOWED));

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void 대기_예약_조회시_상태가_대기로_반환된다() throws Exception {
        given(reservationService.find(2L)).willReturn(RoomEscapeFixture.reservationResultWithWaiting());

        mockMvc.perform(get("/reservations/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("대기"))
                .andExpect(jsonPath("$.rank").value(2));
    }
}
