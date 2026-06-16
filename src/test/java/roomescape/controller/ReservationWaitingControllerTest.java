package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.service.ReservationOrderService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationWaitingService;
import org.springframework.http.MediaType;

@WebMvcTest(ReservationRestController.class)
public class ReservationWaitingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ReservationWaitingService reservationWaitingService;

    @MockBean
    private ReservationOrderService reservationOrderService;

    private final static LocalDate tomorrow = LocalDate.now().plusDays(1);
    private final static LocalDateTime nowDateTime = LocalDateTime.now();
    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(2L, "test", "설명", "url");

    @Test
    void 예약_대기열이_정상_생성된다() throws Exception {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);
        ReservationWaitingResponse reservationWaitingResponse = new ReservationWaitingResponse(
                1L, "테스트", tomorrow, ReservationTimeResponse.from(reservationTime), ThemeResponse.from(theme), 1L, nowDateTime);

        given(reservationWaitingService.create(reservationWaitingRequest)).willReturn(reservationWaitingResponse);

        mockMvc.perform(post("/reservations/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationWaitingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("테스트"))
                .andExpect(jsonPath("$.date").value(tomorrow.toString()))
                .andExpect(jsonPath("$.time.id").value(1L))
                .andExpect(jsonPath("$.time.startAt").value("10:00"))
                .andExpect(jsonPath("$.theme.id").value(2L))
                .andExpect(jsonPath("$.theme.name").value("test"))
                .andExpect(jsonPath("$.sequence").value(1L));
    }

    @Test
    void 잘못된_시간_테마_id_생성시_에러_메세지_반환한다() throws Exception {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트", tomorrow, 999L, 2L);

        given(reservationWaitingService.create(reservationWaitingRequest)).willThrow(new ReservationTimeNotFoundException(999L));

        mockMvc.perform(post("/reservations/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationWaitingRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("TIME_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("999번 예약 시간을 찾을 수 없습니다."));
    }

    @Test
    void 과거_날짜로_예약대기열_생성시_에러_메세지_반환한다() throws Exception {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트", LocalDate.now().minusDays(1), 1L, 2L);

        given(reservationWaitingService.create(reservationWaitingRequest)).willThrow(new ExpiredDateTimeException());

        mockMvc.perform(post("/reservations/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationWaitingRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_DATE_OR_TIME"))
                .andExpect(jsonPath("$.message").value("이미 지난 날짜이거나 시간입니다."));
    }

    @Test
    void 존재하지_않는_예약에_대기열_생성시_에러_메세지_반환한다() throws Exception {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        given(reservationWaitingService.create(reservationWaitingRequest)).willThrow(new ReservationAlreadyExistException());

        mockMvc.perform(post("/reservations/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationWaitingRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESERVATION"))
                .andExpect(jsonPath("$.message").value("이미 예약된 시간입니다."));
    }

    @Test
    void 이미_예약한_이름으로_대기열_생성시_에러_메세지_반환한다() throws Exception {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        given(reservationWaitingService.create(reservationWaitingRequest)).willThrow(new InvalidInputException("이미 예약한 사용자는 대기열을 신청할 수 없습니다."));

        mockMvc.perform(post("/reservations/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationWaitingRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("이미 예약한 사용자는 대기열을 신청할 수 없습니다."));
    }

    @Test
    void 이미_대기열이_존재하는_예약에_중복_대기열_생성시_에러_메세지_반환한다() throws Exception {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        given(reservationWaitingService.create(reservationWaitingRequest)).willThrow(new InvalidInputException("이미 해당 예약에 대기열이 존재합니다."));

        mockMvc.perform(post("/reservations/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationWaitingRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("이미 해당 예약에 대기열이 존재합니다."));
    }

    @Test
    void 예약_대기열_정상_삭제된다() throws Exception {
        willDoNothing().given(reservationWaitingService).delete(1L);

        mockMvc.perform(delete("/reservations/waitings/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void 과거_예약_대기열_삭제시_에러_메세지_반환한다() throws Exception {
        willThrow(new ExpiredDateTimeException()).given(reservationWaitingService).delete(1L);

        mockMvc.perform(delete("/reservations/waitings/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_DATE_OR_TIME"))
                .andExpect(jsonPath("$.message").value("이미 지난 날짜이거나 시간입니다."));
    }

    @Test
    void 존재하지_않는_예약_대기열_삭제시_에러_메세지_반환한다() throws Exception {
        willThrow(new ResourceNotFoundException("1번 예약 대기열을 칮을 수 없습니다.")).given(reservationWaitingService).delete(1L);

        mockMvc.perform(delete("/reservations/waitings/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESERVATION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("1번 예약 대기열을 칮을 수 없습니다."));
    }

    @Test
    void 전체_예약_대기열이_정상적으로_조회된다() throws Exception {
        ReservationWaitingResponse reservationWaitingResponse = new ReservationWaitingResponse(
                1L, "테스트", tomorrow, ReservationTimeResponse.from(reservationTime), ThemeResponse.from(theme), 1L, nowDateTime);

        given(reservationWaitingService.readAll()).willReturn(List.of(reservationWaitingResponse));

        mockMvc.perform(get("/reservations/waitings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("테스트"))
                .andExpect(jsonPath("$[0].date").value(tomorrow.toString()))
                .andExpect(jsonPath("$[0].time.id").value(1L))
                .andExpect(jsonPath("$[0].time.startAt").value("10:00"))
                .andExpect(jsonPath("$[0].theme.id").value(2L))
                .andExpect(jsonPath("$[0].theme.name").value("test"))
                .andExpect(jsonPath("$[0].sequence").value(1L));
    }

    @Test
    void 이름으로_예약_대기열_조회가_정상적으로_반환된다() throws Exception {
        ReservationWaitingResponse reservationWaitingResponse = new ReservationWaitingResponse(
                1L, "테스트", tomorrow, ReservationTimeResponse.from(reservationTime), ThemeResponse.from(theme), 1L, nowDateTime);

        given(reservationWaitingService.readByName("테스트")).willReturn(List.of(reservationWaitingResponse));

        mockMvc.perform(get("/reservations/waitings/mine")
                        .param("name", "테스트"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("테스트"))
                .andExpect(jsonPath("$[0].date").value(tomorrow.toString()))
                .andExpect(jsonPath("$[0].time.id").value(1L))
                .andExpect(jsonPath("$[0].time.startAt").value("10:00"))
                .andExpect(jsonPath("$[0].theme.id").value(2L))
                .andExpect(jsonPath("$[0].theme.name").value("test"))
                .andExpect(jsonPath("$[0].sequence").value(1L));
    }
}
