package roomescape.controller;

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
import roomescape.controller.dto.ReservationCreateRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.controller.dto.ReservationTimeResponse;
import roomescape.controller.dto.ThemeResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.ReservationService;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png");
        Reservation reservation = new Reservation(1L, "브라운", LocalDate.of(2023, 8, 5), time, theme);

        given(reservationService.create(anyString(), any(), anyLong(), anyLong()))
                .willReturn(reservation);

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

        ReservationResponse reservationResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ReservationResponse.class
        );

        assertReservation(reservationResponse, reservation);
        assertTime(reservationResponse, time);
        assertTheme(reservationResponse, theme);

        then(reservationService)
                .should()
                .create(request.name(), request.date(), request.timeId(), request.themeId());
    }

    private static void assertReservation(ReservationResponse reservationResponse, Reservation reservation) {
        assertThat(reservationResponse).extracting(
                ReservationResponse::id,
                ReservationResponse::name,
                ReservationResponse::date
        ).containsExactly(reservation.getId(), reservation.getName(), reservation.getDate().toString());
    }

    private static void assertTime(ReservationResponse reservationResponse, ReservationTime time) {
        assertThat(reservationResponse.time()).extracting(
                ReservationTimeResponse::id,
                ReservationTimeResponse::startAt
        ).containsExactly(time.getId(), time.getStartAt().toString());
    }

    private static void assertTheme(ReservationResponse reservationResponse, Theme theme) {
        assertThat(reservationResponse.theme()).extracting(
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
    public void create_fail1(String name, String date, Long timeId, Long themeId) throws Exception {
        // given
        LocalDate reservationDate = date == null ? null : LocalDate.parse(date);
        ReservationCreateRequest request = new ReservationCreateRequest(name, reservationDate, timeId, themeId);

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
                    "name": "브라운",
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
                .andExpect(status().isBadRequest());
    }

}
