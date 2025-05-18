package roomescape.presentation.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.TestFixtures.anyReservationWithId;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import roomescape.application.UserService;
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.domain.user.Email;
import roomescape.domain.user.Password;
import roomescape.domain.user.User;
import roomescape.domain.user.UserName;
import roomescape.domain.user.UserRole;
import roomescape.presentation.StubAuthInfoArgumentResolver;

class UserControllerTest {

    private final UserService userService = Mockito.mock(UserService.class);
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new UserController(userService))
        .setCustomArgumentResolvers(new StubAuthInfoArgumentResolver(new AuthenticationInfo(1L, UserRole.USER)))
        .build();

    @Test
    @DisplayName("멤버 추가 요청시, id를 포함한 멤버와 CREATED를 응답한다.")
    void register() throws Exception {
        var razel = new User(1L, new UserName("razel"), UserRole.USER, new Email("razel@email.com"), new Password("razelpassword"));
        Mockito.when(userService.register("razel@email.com", "razelpassword", "razel")).thenReturn(razel);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email" : "razel@email.com",
                            "password" : "razelpassword",
                            "name" : "razel"
                        }
                    """))
            .andExpect(content().json("""
                {
                  "id": 1,
                  "name" : "razel"
                }
                """))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("예약 조회 요청시, 존재하는 모든 예약과 OK를 응답한다.")
    void getAllReservationsByUser() throws Exception {
        Mockito.when(userService.getReservations(Mockito.anyLong())).thenReturn(List.of(anyReservationWithId()));

        mockMvc.perform(get("/users/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "id": 1,
                        "role": "admin"
                    }
                    """))
            .andExpect(status().isOk())
            .andDo(MockMvcResultHandlers.print());
    }
}
