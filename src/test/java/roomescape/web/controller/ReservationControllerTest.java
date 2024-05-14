package roomescape.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import roomescape.domain.Member;
import roomescape.service.ReservationService;
import roomescape.service.security.JwtProvider;
import roomescape.web.dto.request.reservation.ReservationRequest;
import roomescape.web.dto.request.reservation.UserReservationRequest;
import roomescape.web.dto.response.member.MemberResponse;
import roomescape.web.dto.response.reservation.ReservationResponse;
import roomescape.web.dto.response.theme.ThemeResponse;
import roomescape.web.dto.response.time.ReservationTimeResponse;

@WebMvcTest(controllers = ReservationController.class)
class ReservationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ReservationService reservationService;
    private final JwtProvider jwtProvider = new JwtProvider();

    @Test
    @DisplayName("예약 저장 시 모든 필드가 유효한 값이라면 201Created를 반환한다.")
    void saveReservation_ShouldReturn201StatusCode_WhenInsertAllValidateField() throws Exception {
        // given
        Member member = new Member(1L, "name", "email", "password");
        String token = jwtProvider.encode(member);
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L, 1L);
        Mockito.when(reservationService.saveReservation(any(ReservationRequest.class)))
                .thenReturn(
                        new ReservationResponse(1L, LocalDate.now(), new ReservationTimeResponse(1L, LocalTime.now()),
                                new ThemeResponse(1L, "n", "d", "t"),
                                new MemberResponse(member.getId(), member.getName())
                        )
                );

        // when & then
        mockMvc.perform(
                        post("/reservations")
                                .content(objectMapper.writeValueAsString(request))
                                .cookie(new Cookie("token", token))
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("예약 저장 시 모든 필드가 유효한 값이라면 location 헤더가 추가된다.")
    void saveReservation_ShouldRedirect_WhenInsertAllValidateField() throws Exception {
        // given
        String token = jwtProvider.encode(new Member(1L, "a", "b", "c"));
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L, 1L);
        Mockito.when(reservationService.saveReservation(any(ReservationRequest.class)))
                .thenReturn(
                        new ReservationResponse(1L, LocalDate.now(),
                                new ReservationTimeResponse(1L, LocalTime.now()),
                                new ThemeResponse(1L, "n", "d", "t"),
                                new MemberResponse(1L, "aa"))
                );

        // when & then
        mockMvc.perform(
                        post("/reservations")
                                .cookie(new Cookie("token", token))
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(header().string(LOCATION, "/reservations/1"));
    }

    @Test
    @DisplayName("예약 저장 시 날짜에 빈값이면 400 BadRequest를 반환한다.")
    void saveReservation_ShouldReturn400StatusCode_WhenInsertNullDate() throws Exception {
        // given
        UserReservationRequest request = new UserReservationRequest(null, 1L, 1L);

        // when & then
        mockMvc.perform(
                        post("/reservations")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(content().string(containsString("날짜에 빈값은 허용하지 않습니다.")));
    }

    @Test
    @DisplayName("예약 저장 시 타임아이디가 0이하의 값이면 400 BadRequest를 반환한다.")
    void saveReservation_ShouldReturn400StatusCode_WhenInsertLessThen0TimeId() throws Exception {
        // given
        UserReservationRequest request = new UserReservationRequest(LocalDate.now().plusDays(1), 0L, 1L);

        // when & then
        mockMvc.perform(
                        post("/reservations")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(content().string(containsString("타임 아이디는 1이상의 정수만 허용합니다.")));
    }

    @Test
    @DisplayName("예약 저장 시 테마아이디가 0이하의 값이면 400 BadRequest를 반환한다.")
    void saveReservation_ShouldReturn400StatusCode_WhenInsertLessThen0ThemeId() throws Exception {
        // given
        UserReservationRequest request = new UserReservationRequest(LocalDate.now().plusDays(1), 1L, 0L);

        // when & then
        mockMvc.perform(
                        post("/reservations")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(content().string(containsString("테마 아이디는 1이상의 정수만 허용합니다.")));
    }

}
