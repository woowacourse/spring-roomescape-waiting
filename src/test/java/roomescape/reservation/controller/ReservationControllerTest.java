package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
import roomescape.global.config.WebMvcConfig;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import roomescape.reservation.domain.ReservationSlot;

@WebMvcTest(ReservationController.class)
@Import(WebMvcConfig.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("예약을 성공적으로 생성한다.")
    void create_Success() throws Exception {
        // given
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.of(2026, 5, 5), 1L, 1L);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation reservation = new Reservation(1L, "브라운", new ReservationSlot(LocalDate.of(2026, 5, 5), time, theme));

        given(reservationService.save(any())).willReturn(ReservationResult.from(reservation));

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("브라운"));
    }

    @Test
    @DisplayName("예약 생성 시 필수 필드가 누락되면 400 에러를 반환한다.")
    void create_MissingFields_BadRequest() throws Exception {
        // given
        String requestBody = "{\"name\":\"\", \"date\":\"2026-05-05\", \"timeId\":1, \"themeId\":1}";

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다. 안내된 양식에 맞춰 다시 입력해 주세요."));
    }

    @Test
    @DisplayName("이름으로 모든 예약을 성공적으로 조회한다.")
    void readAllByName_Success() throws Exception {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        List<ReservationWithStatusResult> results = List.of(
                new ReservationWithStatusResult(1L, "브라운", LocalDate.of(2026, 5, 5), time, theme, "reserved", 0L)
        );

        given(reservationService.findAllByName(anyString())).willReturn(results);

        // when & then
        mockMvc.perform(get("/reservations")
                        .param("name", "브라운"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("브라운"))
                .andExpect(jsonPath("$[0].status").value("reserved"));
    }

    @Test
    @DisplayName("이름으로 조회 시 파라미터가 누락되면 400 에러를 반환한다.")
    void readAllByName_MissingName_BadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("필수 요청 파라미터가 누락되었습니다. 입력 값을 다시 확인해 주세요."));
    }
}
