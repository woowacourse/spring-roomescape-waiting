package roomescape.presentation.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import roomescape.TestFixtures;
import roomescape.application.ReservationService;
import roomescape.application.UserService;
import roomescape.domain.user.User;

class AdminControllerTest {

    private final ReservationService reservationService = Mockito.mock(ReservationService.class);
    private final UserService userService = Mockito.mock(UserService.class);
    private final User admin = TestFixtures.anyAdminWithId();
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new AdminController(reservationService, userService))
        .build();

    @Test
    @DisplayName("어드민 페이지에서 예약 추가 요청시, id를 포함한 예약 내용과 CREATED를 응답한다.")
    void reserve() throws Exception {
        var reservation = TestFixtures.anyReservationWithId();
        Mockito.when(reservationService.reserve(anyLong(), any(), anyLong(), anyLong())).thenReturn(reservation);

        mockMvc.perform(post("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "date": "3000-03-17",
                        "timeId": "1",
                        "themeId": "1",
                        "userId": "1"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$..['id','user','date','time','theme']").exists());
    }

    @Test
    @DisplayName("어드민 페이지에서 유저 조회 요청 시, 존재하는 유저들과 OK를 응답한다.]")
    void getAllUsers() throws Exception {
        Mockito.when(userService.findAllUsers()).thenReturn(List.of(admin));

        mockMvc.perform(get("/admin/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$..['id', 'name']").exists());
    }
}
