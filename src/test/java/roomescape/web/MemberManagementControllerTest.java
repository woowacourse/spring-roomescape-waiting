package roomescape.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

class MemberManagementControllerTest extends ExcludeInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("모든 사용자를 조회한다")
    @Sql("/test-data/members.sql")
    @Test
    void when_findAllMembers_then_returnMembers() throws Exception {
        // when, then
        mockMvc.perform(get("/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }
}
