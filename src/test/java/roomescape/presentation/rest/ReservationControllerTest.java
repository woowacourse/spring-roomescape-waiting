package roomescape.presentation.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.TestFixtures.anyReservationWithNewId;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import roomescape.application.ReservationService;
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.domain.user.UserRole;
import roomescape.presentation.GlobalExceptionHandler;
import roomescape.presentation.StubAuthenticationInfoArgumentResolver;

class ReservationControllerTest {

    private final ReservationService reservationService = Mockito.mock(ReservationService.class);
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new ReservationController(reservationService))
        .setCustomArgumentResolvers(new StubAuthenticationInfoArgumentResolver(new AuthenticationInfo(99L, UserRole.USER)))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();

    @Test
    @DisplayName("예약 추가 요청시, id를 포함한 예약 내용과 CREATED를 응답한다.")
    void reserve() throws Exception {
        Mockito.when(reservationService.reserve(anyLong(), any(), anyLong(), anyLong()))
            .thenReturn(anyReservationWithNewId());

        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "date": "3000-03-17",
                        "timeId": "1",
                        "themeId": "1"
                    }
                    """))
            .andExpect(jsonPath("$..['id','user','date','time','theme']").exists())
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("예약 대기 요청시, id를 포함한 예약 내용과 CREATED를 응답한다.")
    void waitFor() throws Exception {
        Mockito.when(reservationService.waitFor(anyLong(), any(), anyLong(), anyLong()))
            .thenReturn(anyReservationWithNewId());

        mockMvc.perform(post("/reservations/wait")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "date": "3000-03-17",
                        "timeId": "1",
                        "themeId": "1"
                    }
                    """))
            .andExpect(jsonPath("$..['id','user','date','time','theme']").exists())
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("예약 조회 요청시, 조건에 맞는 모든 예약과 OK를 응답한다.")
    void getAllReservations() throws Exception {
        var expectedReservations = List.of(anyReservationWithNewId(), anyReservationWithNewId(), anyReservationWithNewId());
        Mockito.when(reservationService.findAllReservations(any())).thenReturn(expectedReservations);

        mockMvc.perform(get("/reservations"))
            .andExpect(jsonPath("$..['id','user','date','time','theme']").exists())
            .andExpect(jsonPath("$", hasSize(expectedReservations.size())))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("일반 유저가 예약 삭제 요청시, FORBIDDEN을 응답한다.")
    void deleteUnauthorized() throws Exception {
        mockMvc.perform(delete("/reservations/1"))
            .andExpect(status().isForbidden());

        Mockito.verify(reservationService, never()).removeByIdForce(1L);
    }
}
