package roomescape.presentation;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecBuilder;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.springframework.web.context.WebApplicationContext;

public abstract class BaseControllerUnitTest {

    protected void mockMvcSetting(WebApplicationContext webApplicationContext) {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }

    protected MockMvcRequestSpecification defaultSpec() {
        return new MockMvcRequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }
}
