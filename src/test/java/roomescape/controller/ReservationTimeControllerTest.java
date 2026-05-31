package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.controller.dto.ReservationTimeResponse;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.global.DomainErrorHttpMapper;
import roomescape.service.ReservationTimeService;

@WebMvcTest(AdminReservationTimeController.class)
@Import(DomainErrorHttpMapper.class)
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @DisplayName("관리자는 예약 시간을 조회한다.")
    @Test
    void findAll() throws Exception {
        given(reservationTimeService.findAll()).willReturn(List.of(
                new ReservationTimeResponse(1L, LocalTime.of(10, 0))
        ));

        mockMvc.perform(get("/admin/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].startAt").value("10:00"));
    }

    @DisplayName("관리자는 예약 시간을 생성한다.")
    @Test
    void create() throws Exception {
        given(reservationTimeService.saveReservationTime(any())).willReturn(1L);

        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startAt": "10:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/times/1"));
    }

    @DisplayName("예약 시간 생성 요청 값이 올바르지 않으면 400을 반환한다.")
    @Test
    void createInvalidRequest() throws Exception {
        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startAt": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @DisplayName("관리자는 예약 시간을 삭제한다.")
    @Test
    void deleteReservationTime() throws Exception {
        mockMvc.perform(delete("/admin/times/1"))
                .andExpect(status().isNoContent());

        verify(reservationTimeService).deleteReservationTime(1L);
    }

    @DisplayName("참조 중인 예약 시간 삭제는 422를 반환한다.")
    @Test
    void deleteReferencedReservationTime() throws Exception {
        org.mockito.Mockito.doThrow(new RoomescapeException(
                        DomainErrorCode.REFERENTIAL_INTEGRITY,
                        "이 시간을 참조하는 예약이 있어 삭제할 수 없습니다."
                ))
                .when(reservationTimeService)
                .deleteReservationTime(1L);

        mockMvc.perform(delete("/admin/times/1"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("REFERENTIAL_INTEGRITY"));
    }
}
