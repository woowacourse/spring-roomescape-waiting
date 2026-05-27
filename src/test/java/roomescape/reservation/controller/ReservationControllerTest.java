package roomescape.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import roomescape.reservation.controller.dto.*;
import roomescape.reservation.domain.Status;
import roomescape.reservation.service.dto.ReservationWaitingResult;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.reservation.service.ReservationService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.common.auth.UserArgumentResolver.GUEST_NAME_HEADER;
import static roomescape.common.exception.GlobalErrorCode.INVALID_GUEST_NAME_HEADER;
import static roomescape.common.exception.GlobalErrorCode.VALIDATION_ERROR;

@WebMvcTest(controllers = ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    @DisplayName("예약을 생성하는 요청을 하면 생성된 예약 정보가 응답으로 반환된다.")
    public void create_success() throws Exception {
        // given
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
        Theme theme = Theme.of(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png");
        ReservationWaitingResult reservationWaitingResult =
                new ReservationWaitingResult(1L, "브라운", LocalDate.of(2023, 8, 5), time, theme, Status.CONFIRMED, 0);

        given(reservationService.create(anyString(), any(), anyLong(), anyLong()))
                .willReturn(reservationWaitingResult);

        ReservationCreateRequest request = new ReservationCreateRequest(
                "브라운",
                LocalDate.of(2023, 8, 5),
                1L,
                1L
        );

        // when then
        MvcResult result = mockMvc.perform(
                        post("/reservations")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ReservationWaitingResponse reservationWaitingResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ReservationWaitingResponse.class
        );

        assertReservation(reservationWaitingResponse, reservationWaitingResult);
        assertTime(reservationWaitingResponse.time(), time);
        assertTheme(reservationWaitingResponse.theme(), theme);

        then(reservationService)
                .should()
                .create(request.guestName(), request.date(), request.timeId(), request.themeId());
    }

    private static void assertReservation(ReservationWaitingResponse reservationWaitingResponse, ReservationWaitingResult reservationWaitingResult) {
        assertThat(reservationWaitingResponse).extracting(
                ReservationWaitingResponse::id,
                ReservationWaitingResponse::guestName,
                ReservationWaitingResponse::date,
                ReservationWaitingResponse::status,
                ReservationWaitingResponse::waitNumber
        ).containsExactly(
                reservationWaitingResult.id(),
                reservationWaitingResult.guestName(),
                reservationWaitingResult.date().toString(),
                reservationWaitingResult.status().toString(),
                reservationWaitingResult.waitNumber()
        );
    }

    private static void assertTime(ReservationTimeResponse reservationTimeResponse, ReservationTime time) {
        assertThat(reservationTimeResponse).extracting(
                ReservationTimeResponse::id,
                ReservationTimeResponse::startAt
        ).containsExactly(time.getId(), time.getStartAt().toString());
    }

    private static void assertTheme(ThemeResponse themeResponse, Theme theme) {
        assertThat(themeResponse).extracting(
                ThemeResponse::id,
                ThemeResponse::name,
                ThemeResponse::description,
                ThemeResponse::thumbnail
        ).containsExactly(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @ParameterizedTest
    @CsvSource(value = {
            ",2023-08-05,1,1",
            "브라운,,1,1",
            "브라운,2023-08-05,,1",
            "브라운,2023-08-05,1,",
    })
    @DisplayName("예약을 생성하는 요청을 할 때 특정 요청값이 비어있으면 에러가 발생한다.")
    public void create_fail1(String guestName, String date, Long timeId, Long themeId) throws Exception {
        // given
        LocalDate reservationDate = date == null ? null : LocalDate.parse(date);
        ReservationCreateRequest request = new ReservationCreateRequest(guestName, reservationDate, timeId, themeId);

        // when then
        mockMvc.perform(
                        post("/reservations")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("예약을 생성하는 요청을 할 때 날짜 형식이 올바르지 않으면 에러가 발생한다.")
    public void create_fail2() throws Exception {
        // given
        String request = """
                {
                    "guestName": "브라운",
                    "date": "2023/08/05",
                    "timeId": 1,
                    "themeId": 1
                }
                """;

        // when then
        mockMvc.perform(
                        post("/reservations")
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_ERROR.code()));
    }

    @Test
    @DisplayName("예약자 이름 헤더가 없으면 에러 응답을 반환한다.")
    public void getListByGuestName_fail1() throws Exception {
        // when then
        mockMvc.perform(get("/reservations/me"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_GUEST_NAME_HEADER.code()))
                .andExpect(jsonPath("$.message").value(INVALID_GUEST_NAME_HEADER.message()));
    }

    @Test
    @DisplayName("예약자 이름으로 된 예약을 조회하는 요청을 하면 특정 사용자의 예약 정보가 응답으로 반환된다.")
    public void getListByGuestName_success() throws Exception {
        // given
        String guestName = "브라운";

        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
        Theme theme = Theme.of(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png");
        ReservationWaitingResult reservationWaitingResult =
                new ReservationWaitingResult(1L, guestName, LocalDate.of(2023, 8, 5), time, theme, Status.CONFIRMED, 0);
        given(reservationService.findByGuestName(guestName))
                .willReturn(List.of(reservationWaitingResult));

        // when then
        MvcResult result = mockMvc.perform(
                        get("/reservations/me")
                                .header(GUEST_NAME_HEADER, guestName))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ReservationWaitingListResponse reservationWaitingListResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ReservationWaitingListResponse.class
        );

        List<ReservationWaitingResponse> responses = reservationWaitingListResponse.reservations();
        assertThat(responses).hasSize(1);
        ReservationWaitingResponse response = responses.getFirst();
        assertReservation(response, reservationWaitingResult);
        assertTime(response.time(), time);
        assertTheme(response.theme(), theme);

        then(reservationService)
                .should()
                .findByGuestName(guestName);
    }

    @Test
    @DisplayName("예약의 날짜 및 시간을 수정하는 요청을 하면 200 응답 코드가 반환된다.")
    public void editDateTime_success() throws Exception {
        // given
        Long reservationId = 1L;
        ReservationTime time = ReservationTime.of(2L, LocalTime.of(12, 0));
        Theme theme = Theme.of(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png");


        ReservationEditRequest request = new ReservationEditRequest(
                LocalDate.of(2023, 8, 10),
                2L
        );

        // when then
        String guestNameHeader = URLEncoder.encode("브라운", StandardCharsets.UTF_8);
        mockMvc.perform(
                        patch("/reservations/{id}", reservationId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(GUEST_NAME_HEADER, guestNameHeader)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        then(reservationService)
                .should()
                .editDateTime(reservationId, request.date(), request.timeId(), "브라운");
    }

    @ParameterizedTest
    @CsvSource(value = {
            ",1",
            "2023-08-05,"
    })
    @DisplayName("예약의 날짜 및 시간을 수정하는 요청을 할 때 특정 요청값이 비어있으면 에러가 발생한다.")
    public void editDateTime_fail1(String date, Long timeId) throws Exception {
        // given
        Long reservationId = 1L;
        LocalDate reservationDate = date == null ? null : LocalDate.parse(date);
        ReservationEditRequest request = new ReservationEditRequest(reservationDate, timeId);

        // when then
        mockMvc.perform(
                        patch("/reservations/{id}", reservationId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(GUEST_NAME_HEADER, "브라운"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예약의 날짜 및 시간을 수정하는 요청을 할 때 날짜 형식이 올바르지 않으면 에러가 발생한다.")
    public void editDateTime_fail2() throws Exception {
        // given
        Long reservationId = 1L;
        String request = """
                {
                    "date": "2023/08/05",
                    "timeId": 1
                }
                """;

        // when then
        mockMvc.perform(
                        patch("/reservations/{id}", reservationId)
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(GUEST_NAME_HEADER, "브라운"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_ERROR.code()));
    }

    @Test
    @DisplayName("예약 id 타입이 올바르지 않으면 에러 응답을 반환한다.")
    public void editDateTime_fail3() throws Exception {
        // given
        ReservationEditRequest request = new ReservationEditRequest(LocalDate.of(2023, 8, 10), 1L);

        // when then
        mockMvc.perform(
                        patch("/reservations/{id}", "abc")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(GUEST_NAME_HEADER, "브라운"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_ERROR.code()));
    }

    @Test
    @DisplayName("본인의 예약을 삭제하는 요청을 하면 응답으로 204 상태코드를 반환한다.")
    public void delete_success() throws Exception {
        // given
        Long reservationId = 1L;
        String guestNameHeader = "브라운";

        // when then
        mockMvc.perform(
                        delete("/reservations/{id}", reservationId)
                                .header(GUEST_NAME_HEADER, guestNameHeader))
                .andDo(print())
                .andExpect(status().isNoContent());

        then(reservationService)
                .should()
                .cancelMine(reservationId, "브라운");
    }

}
