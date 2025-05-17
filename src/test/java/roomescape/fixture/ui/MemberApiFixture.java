package roomescape.fixture.ui;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class MemberApiFixture {

    public static final List<Map<String, String>> SIGN_UP_PARAMS_LIST = List.of(
            Map.of("email", "one@one.com",
                    "password", "pwd1",
                    "name", "헤일러"),
            Map.of("email", "two@two.com",
                    "password", "pwd2",
                    "name", "머피"),
            Map.of("email", "three@three.com",
                    "password", "pwd3",
                    "name", "매트"),
            Map.of("email", "four@four.com",
                    "password", "pwd4",
                    "name", "피케이")
    );

    private MemberApiFixture() {
    }

    public static Map<String, String> signUpParams1() {
        if (SIGN_UP_PARAMS_LIST.isEmpty()) {
            throw new IllegalStateException("회원 픽스처 개수가 부족합니다.");
        }
        return SIGN_UP_PARAMS_LIST.get(0);
    }

    public static Map<String, String> signUpParams2() {
        if (SIGN_UP_PARAMS_LIST.size() < 2) {
            throw new IllegalStateException("회원 픽스처 개수가 부족합니다.");
        }
        return SIGN_UP_PARAMS_LIST.get(1);
    }

    public static void signUp(final Map<String, String> signUpParams) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signUpParams)
                .when().post("/members")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    public static List<ValidatableResponse> signUpMembers(final int count) {
        if (SIGN_UP_PARAMS_LIST.size() < count) {
            throw new IllegalStateException("회원 픽스처 개수가 부족합니다.");
        }

        return SIGN_UP_PARAMS_LIST.stream()
                .limit(count)
                .map(member -> RestAssured.given().log().all()
                        .contentType(ContentType.JSON)
                        .body(member)
                        .when().post("/members")
                        .then().log().all()
                        .statusCode(HttpStatus.CREATED.value()))
                .toList();
    }
}
