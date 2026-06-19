package roomescape.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Reservations;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.service.ReservationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ReservationService reservationService;

    private Reservation approvedReservation() {
        ReservationTime time = ReservationTime.load(1L, LocalTime.of(10, 0));
        Theme theme = Theme.load(1L, "공포", "무서워요", "https://zeze.com");
        Slot slot = Slot.load(1L, LocalDate.of(2099, 1, 1), time, theme);
        return new Reservation(1L, new Member(1L, "zeze"), Status.APPROVED, slot);
    }

    private Reservation waitingReservation() {
        ReservationTime time = ReservationTime.load(1L, LocalTime.of(10, 0));
        Theme theme = Theme.load(1L, "공포", "무서워요", "https://zeze.com");
        Slot slot = Slot.load(1L, LocalDate.of(2099, 1, 1), time, theme);
        return new Reservation(2L, new Member(2L, "mingu"), Status.WAITING, slot);
    }

    @Test
    void 예약_생성_성공시_201을_반환한다() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest(1L, LocalDate.of(2099, 1, 1), 1L, 1L);
        given(reservationService.reserve(any())).willReturn(approvedReservation());

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("zeze"))
                .andExpect(jsonPath("$.state").value("승인"));
    }

    @Test
    void 예약_생성시_memberId가_없으면_400을_반환한다() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest(null, LocalDate.of(2099, 1, 1), 1L, 1L);
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_생성시_날짜가_없으면_400을_반환한다() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest(1L, null, 1L, 1L);
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_생성시_TimeId가_없으면_400을_반환한다() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest(1L, LocalDate.of(2099, 1, 1), null, 1L);
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_생성시_서비스에서_중복_예외_발생시_409를_반환한다() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest(1L, LocalDate.of(2099, 1, 1), 1L, 1L);
        given(reservationService.reserve(any()))
                .willThrow(new RoomEscapeException(DomainErrorCode.ALREADY_EXISTS, "test"));
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void 예약_생성시_과거_날짜면_422를_반환한다() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest(1L, LocalDate.of(2000, 1, 1), 1L, 1L);
        given(reservationService.reserve(any()))
                .willThrow(new RoomEscapeException(DomainErrorCode.PAST_DATE, "test"));
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void 예약_전체_목록_조회_성공시_200을_반환한다() throws Exception {
        given(reservationService.findAll(any()))
                .willReturn(new Reservations(List.of(approvedReservation(), waitingReservation())));
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.length()").value(2));
    }

    @Test
    void memberId로_예약_목록_조회_성공시_200을_반환한다() throws Exception {
        given(reservationService.findAll(any()))
                .willReturn(new Reservations(List.of(approvedReservation())));
        mockMvc.perform(get("/reservations").param("memberId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.length()").value(1))
                .andExpect(jsonPath("$.reservations[0].name").value("zeze"));
    }

    @Test
    void 예약_단건_조회_성공시_200을_반환한다() throws Exception {
        given(reservationService.find(1L)).willReturn(approvedReservation());
        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void 없는_예약_단건_조회시_404를_반환한다() throws Exception {
        given(reservationService.find(999L))
                .willThrow(new RoomEscapeException(DomainErrorCode.RESOURCE_NOT_FOUND, "test"));
        mockMvc.perform(get("/reservations/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 예약_삭제_성공시_204를_반환한다() throws Exception {
        mockMvc.perform(delete("/reservations/1").param("memberId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void 예약_삭제시_회원ID가_다르면_403을_반환한다() throws Exception {
        willThrow(new RoomEscapeException(DomainErrorCode.FORBIDDEN, "test"))
                .given(reservationService).cancel(anyLong(), anyLong());
        mockMvc.perform(delete("/reservations/1").param("memberId", "999"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 예약_삭제시_memberId가_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_수정_성공시_200을_반환한다() throws Exception {
        ReservationUpdateRequest request = new ReservationUpdateRequest(1L, LocalDate.of(2099, 6, 1), 1L, 1L);
        given(reservationService.update(any(), anyLong())).willReturn(approvedReservation());
        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void 예약_수정시_존재하지_않는_예약이면_404를_반환한다() throws Exception {
        ReservationUpdateRequest request = new ReservationUpdateRequest(1L, LocalDate.of(2099, 6, 1), 1L, 1L);
        given(reservationService.update(any(), anyLong()))
                .willThrow(new RoomEscapeException(DomainErrorCode.RESOURCE_NOT_FOUND, "test"));
        mockMvc.perform(put("/reservations/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 예약_수정시_과거_날짜면_422를_반환한다() throws Exception {
        ReservationUpdateRequest request = new ReservationUpdateRequest(1L, LocalDate.of(2000, 1, 1), 1L, 1L);
        given(reservationService.update(any(), anyLong()))
                .willThrow(new RoomEscapeException(DomainErrorCode.PAST_DATE, "test"));
        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void 대기_예약_조회시_상태가_대기로_반환된다() throws Exception {
        given(reservationService.find(2L)).willReturn(waitingReservation());
        mockMvc.perform(get("/reservations/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("대기"));
    }
}
