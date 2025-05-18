package roomescape.presentation.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import roomescape.TestFixtures;
import roomescape.application.UserService;
import roomescape.domain.user.Email;
import roomescape.domain.user.Password;
import roomescape.domain.user.User;
import roomescape.domain.user.UserName;
import roomescape.domain.user.UserRole;
import roomescape.presentation.StubUserArgumentResolver;

class UserControllerTest {

    private static final String USER_JSON = """
        {
            "email" : "razel@email.com",
            "password" : "razelpassword",
            "name" : "razel"
        }
        """;

    private final UserService userService = Mockito.mock(UserService.class);
    private final User razel = new User(1L, new UserName("razel"), UserRole.USER, new Email("razel@email.com"), new Password("razelpassword"));
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new UserController(userService))
        .setCustomArgumentResolvers(new StubUserArgumentResolver(razel))
        .build();

    @Test
    @DisplayName("멤버 추가 요청시, id를 포함한 멤버와 CREATED를 응답한다.")
    void register() throws Exception {
        Mockito.when(userService.register("razel@email.com", "razelpassword", "razel")).thenReturn(razel);

        var expectedJson = """
            {
              "id": 1,
              "name" : "razel"
            }
            """;
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(USER_JSON))
            .andExpect(content().json(expectedJson))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("예약 조회 요청시, 존재하는 모든 예약과 OK를 응답한다.")
    void getAllReservationsByUser() throws Exception {
        Mockito.when(userService.getReservations(Mockito.anyLong())).thenReturn(List.of(TestFixtures.anyReservationWithId()));

        mockMvc.perform(get("/users/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(USER_JSON))
            .andExpect(status().isOk());
    }
}
