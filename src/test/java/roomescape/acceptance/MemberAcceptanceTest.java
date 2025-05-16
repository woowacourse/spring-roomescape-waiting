package roomescape.acceptance;

import io.restassured.RestAssured;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.response.MemberResponseDto;
import roomescape.model.Role;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MemberAcceptanceTest {

    @Test
    @DisplayName("저장된 멤버 전체를 조회한다")
    void test1() {
        // when
        List<MemberResponseDto> responseDtos = RestAssured.given().log().all()
                .when().get("/members")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", MemberResponseDto.class);

        // then
        assertAll(
                () -> assertThat(responseDtos.size()).isEqualTo(1),
                () -> assertThat(responseDtos.getFirst().name()).isEqualTo("히로"),
                () -> assertThat(responseDtos.getFirst().email()).isEqualTo("example@gmail.com"),
                () -> assertThat(responseDtos.getFirst().role()).isEqualTo(Role.ADMIN)
        );
    }
}
