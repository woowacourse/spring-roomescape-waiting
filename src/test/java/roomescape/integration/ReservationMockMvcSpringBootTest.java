package roomescape.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@Sql(scripts = {"/schema.sql", "/test-data.sql"})
public class ReservationMockMvcSpringBootTest {

    @Autowired
    MockMvc mockMvc;

    @DisplayName("조건에 따른 예약 목록을 조회할 수 있다")
    @MethodSource("returnParametersAndExpectedSize")
    @ParameterizedTest
    void aa(Map<String, String> params, int expected) throws Exception {
        // given
        String token = getAdminToken();
        Cookie cookie = new Cookie("token", token);
        MockHttpServletRequestBuilder requestBuilder = get("/reservations")
                .contentType("application/json")
                .cookie(cookie);
        params.entrySet()
                .stream()
                .filter(e -> e.getValue() != null)
                .forEach(e -> requestBuilder.queryParam(e.getKey(), e.getValue()));

        // when
        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expected));
    }

    private static Stream<Arguments> returnParametersAndExpectedSize() {
        return Stream.of(
                Arguments.arguments(createParamMap(null, null, null, null), 13), // 전체 조회
                Arguments.arguments(createParamMap("2", null, null, null), 0),
                Arguments.arguments(createParamMap("1", "11", null, null), 3),
                Arguments.arguments(createParamMap(null, null, "2025-04-28", "2025-04-30"), 6),
                Arguments.arguments(createParamMap("1", null, null, "2025-04-29"), 11),
                Arguments.arguments(createParamMap("1", null, "2025-04-28", null), 6),
                Arguments.arguments(createParamMap("1", "9", "2025-04-28", "2025-04-29"), 2)
        );
    }

    private static Map<String, String> createParamMap(String memberId, String themeId, String dateFrom, String dateTo) {
        HashMap<String, String> params = new HashMap<>();
        if (memberId != null) {
            params.put("memberId", memberId);
        }
        if (themeId != null) {
            params.put("themeId", themeId);
        }
        if (dateFrom != null) {
            params.put("dateFrom", dateFrom);
        }
        if (dateTo != null) {
            params.put("dateTo", dateTo);
        }
        return params;
    }

    private String getAdminToken() throws Exception {
        return mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content("""
                                {
                                    "email": "admin@gmail.com",
                                    "password": "qwer!"
                                }
                                """)
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("token")
                .getValue();
    }
}
