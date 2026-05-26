package roomescape.reservation.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.global.auth.AdminInterceptor;
import roomescape.global.exception.ReservationErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@WebMvcTest(ReservationController.class)
@Import({AdminInterceptor.class, ReservationControllerTest.TestWebConfig.class})
class ReservationControllerTest {

    @TestConfiguration
    static class TestWebConfig implements WebMvcConfigurer {
        @Autowired
        private AdminInterceptor adminInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(adminInterceptor).addPathPatterns("/**");
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    private Reservation sampleReservation(Long id, String name, LocalDate date, Long timeId, String startAt,
                                          Long themeId, String themeName) {
        ReservationTime time = ReservationTime.createRow(timeId, LocalTime.parse(startAt));
        Theme theme = Theme.createRow(themeId, themeName, "설명", "https://thumbnail.com");
        return Reservation.createRow(id, name, date, time, theme, LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /reservations - 정상 저장 시 201과 응답 본문을 반환한다")
    void createReservation_success() throws Exception {
        // given
        LocalDate date = LocalDate.of(2026, 5, 5);
        Reservation reservation = sampleReservation(1L, "브라운", date, 1L, "10:00", 1L, "테스트-테마");
        ReservationCreateCommand command = new ReservationCreateCommand("브라운", date, 1L, 1L);
        given(reservationService.saveReservation(command)).willReturn(reservation);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-05");
        body.put("timeId", 1);
        body.put("themeId", 1);

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations/1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("브라운"))
                .andExpect(jsonPath("$.date").value("2026-05-05"))
                .andExpect(jsonPath("$.time.id").value(1))
                .andExpect(jsonPath("$.time.startAt").value("10:00"))
                .andExpect(jsonPath("$.theme.id").value(1))
                .andExpect(jsonPath("$.theme.name").value("테스트-테마"));

        then(reservationService).should().saveReservation(command);
    }

    @Test
    @DisplayName("POST /reservations - 예약자 이름이 비어 있으면 에러 응답을 반환한다")
    void createReservation_fail_with_empty_name() throws Exception {
        // given
        Map<String, Object> body = new HashMap<>();
        body.put("name", " ");
        body.put("date", "2026-05-05");
        body.put("timeId", 1);
        body.put("themeId", 1);

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약자 이름을 입력해 주세요."));
    }

    @Test
    @DisplayName("POST /reservations - 예약 날짜 형식이 잘못되면 에러 응답을 반환한다")
    void createReservation_fail_with_invalid_date_format() throws Exception {
        // given
        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "invalid-date");
        body.put("timeId", 1);
        body.put("themeId", 1);

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("date 값의 형식이 올바르지 않습니다. yyyy-MM-dd 형식으로 입력해 주세요."));
    }

    @Test
    @DisplayName("POST /reservations - 테마 ID가 비어 있으면 에러 응답을 반환한다")
    void createReservation_fail_with_empty_theme_id() throws Exception {
        // given
        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-05");
        body.put("timeId", 1);
        body.put("themeId", null);

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약 테마를 선택해 주세요."));
    }

    @Test
    @DisplayName("POST /reservations - 서비스 정책 위반 시 에러 응답을 반환한다")
    void createReservation_fail_with_business_exception() throws Exception {
        // given
        LocalDate date = LocalDate.of(2026, 5, 5);
        ReservationCreateCommand command = new ReservationCreateCommand("브라운", date, 1L, 1L);
        willThrow(new BusinessException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS))
                .given(reservationService)
                .saveReservation(command);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-05");
        body.put("timeId", 1);
        body.put("themeId", 1);

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 예약된 시간입니다. 다른 시간을 선택해 주세요."));
    }

    @Test
    @DisplayName("GET /reservations - 필터 없이 호출하면 전체 목록을 반환한다")
    void readReservations_no_filter() throws Exception {
        // given
        List<Reservation> reservations = List.of(
                sampleReservation(1L, "브라운", LocalDate.of(2026, 5, 5), 1L, "10:00", 1L, "테마A"),
                sampleReservation(2L, "리오", LocalDate.of(2026, 5, 6), 2L, "11:00", 2L, "테마B")
        );
        given(reservationService.getReservations()).willReturn(reservations);

        // when & then
        mockMvc.perform(get("/reservations")
                        .header("Authorization", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("브라운"))
                .andExpect(jsonPath("$[1].name").value("리오"));

        then(reservationService).should().getReservations();
    }

    @Test
    @DisplayName("GET /reservations?date=...&themeId=... - 예약 응답 형식을 유지하면서 필터링한다")
    void readReservations_filter_by_date_and_theme() throws Exception {
        // given
        LocalDate date = LocalDate.of(2026, 5, 5);
        List<Reservation> reservations = List.of(
                sampleReservation(1L, "브라운", date, 1L, "10:00", 1L, "테마A"),
                sampleReservation(2L, "리오", date, 2L, "11:00", 1L, "테마A")
        );
        given(reservationService.getReservationsByDateAndTheme(date, 1L)).willReturn(reservations);

        // when & then
        mockMvc.perform(get("/reservations")
                        .header("Authorization", "ADMIN")
                        .param("date", "2026-05-05")
                        .param("themeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("브라운"))
                .andExpect(jsonPath("$[0].time.id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("리오"))
                .andExpect(jsonPath("$[1].time.id").value(2));

        then(reservationService).should().getReservationsByDateAndTheme(date, 1L);
    }

    @Test
    @DisplayName("GET /reservations/me - 이름을 기반으로 본인 예약 목록을 반환한다")
    void readMyReservations_success() throws Exception {
        // given
        Reservation reservation = sampleReservation(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 5),
                1L,
                "10:00",
                1L,
                "테마A"
        );
        given(reservationService.getReservationsByName("브라운")).willReturn(List.of(reservation));

        // when & then
        mockMvc.perform(get("/reservations/me")
                        .param("name", "브라운"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("브라운"))
                .andExpect(jsonPath("$[0].time.startAt").value("10:00"))
                .andExpect(jsonPath("$[0].theme.name").value("테마A"));

        then(reservationService).should().getReservationsByName("브라운");
    }

    @Test
    @DisplayName("PATCH /reservations/me/{id} - 본인 예약 변경 요청을 서비스에 전달하고 204 응답을 반환한다")
    void updateMyReservation_success() throws Exception {
        // given
        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-05");
        body.put("timeId", 2);
        ReservationUpdateCommand command = new ReservationUpdateCommand(
                1L,
                LocalDate.of(2026, 5, 5),
                2L,
                "브라운"
        );

        // when & then
        mockMvc.perform(patch("/reservations/me/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        then(reservationService).should()
                .updateReservationSchedule(command);
    }

    @Test
    @DisplayName("PATCH /reservations/me/{id} - 이미 예약된 시간으로 변경하면 에러 응답을 반환한다")
    void updateMyReservation_fail_with_duplicate_reservation() throws Exception {
        // given
        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-05");
        body.put("timeId", 2);
        ReservationUpdateCommand command = new ReservationUpdateCommand(
                1L,
                LocalDate.of(2026, 5, 5),
                2L,
                "브라운"
        );
        willThrow(new BusinessException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS))
                .given(reservationService)
                .updateReservationSchedule(command);

        // when & then
        mockMvc.perform(patch("/reservations/me/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 예약된 시간입니다. 다른 시간을 선택해 주세요."));
    }

    @Test
    @DisplayName("DELETE /reservations/{id} - 정상 삭제 시 204와 빈 본문을 반환한다")
    void deleteReservation_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/reservations/1")
                        .header("Authorization", "ADMIN"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        then(reservationService).should().deleteReservation(1L);
    }

    @Test
    @DisplayName("DELETE /reservations/me/{id} - 본인 예약 취소 요청을 서비스에 전달하고 204 응답을 반환한다")
    void deleteMyReservation_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/reservations/me/1")
                        .param("name", "브라운"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        then(reservationService).should().cancelReservation(1L, "브라운");
    }

    @Test
    @DisplayName("DELETE /reservations/me/{id} - 존재하지 않는 예약이면 에러 응답을 반환한다")
    void deleteMyReservation_fail_with_not_found_reservation() throws Exception {
        // given
        willThrow(new EntityNotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND))
                .given(reservationService)
                .cancelReservation(1L, "브라운");

        // when & then
        mockMvc.perform(delete("/reservations/me/1")
                        .param("name", "브라운"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("예약을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("DELETE /reservations/me/{id} - 본인 예약이 아니면 에러 응답을 반환한다")
    void deleteMyReservation_fail_with_invalid_owner() throws Exception {
        // given
        willThrow(new BusinessException(ReservationErrorCode.RESERVATION_OWNER_MISMATCH))
                .given(reservationService)
                .cancelReservation(1L, "브라운");

        // when & then
        mockMvc.perform(delete("/reservations/me/1")
                        .param("name", "브라운"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("수정할 수 있는 권한이 없습니다."));
    }

    @Test
    @DisplayName("DELETE /reservations/me/{id} - 지난 예약이면 취소할 수 없다는 에러 응답을 반환한다")
    void deleteMyReservation_fail_with_past_reservation() throws Exception {
        // given
        willThrow(new BusinessException(ReservationErrorCode.RESERVATION_ALREADY_PAST))
                .given(reservationService)
                .cancelReservation(1L, "브라운");

        // when & then
        mockMvc.perform(delete("/reservations/me/1")
                        .param("name", "브라운"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 지난 예약은 취소할 수 없습니다."));
    }
}
