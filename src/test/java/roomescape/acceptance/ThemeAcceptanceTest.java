package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.acceptance.config.AcceptanceTest;
import roomescape.controller.api.dto.request.ThemeCreateRequest;
import roomescape.controller.api.dto.response.ReservationTimeResponse;
import roomescape.controller.api.dto.response.ThemeResponse;
import roomescape.domain.reservation.Theme;
import roomescape.fixture.ThemeFixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.acceptance.step.MemberStep.멤버_로그인;
import static roomescape.acceptance.step.ReservationStep.예약_생성;
import static roomescape.acceptance.step.ReservationTimeStep.예약_시간_생성;
import static roomescape.acceptance.step.ThemeStep.테마_생성;

@AcceptanceTest
public class ThemeAcceptanceTest {
    final Theme theme = ThemeFixture.getDomain();

    @Nested
    @DisplayName("방탈출 테마를 생성할때")
    class DescribeCreateTheme {
        @Nested
        @DisplayName("테마에 대한 정보를 알맞게 작성할 때")
        class ContextWithValidRequest {
            @Test
            @DisplayName("201과 결과를 반환한다.")
            void it_returns_201_and_response() {
                final ThemeCreateRequest request = new ThemeCreateRequest(
                        theme.getName(),
                        theme.getDescription(),
                        theme.getThumbnailAsString()
                );

                //@formatter:off
                final ThemeResponse response = RestAssured.given().body(request).contentType(ContentType.JSON)
                        .when().post("/themes")
                        .then().assertThat().statusCode(201).extract().as(ThemeResponse.class);
                //@formatter:on

                assertThat(response.id()).isNotZero()
                        .isNotNull();
                assertAll((() -> {
                    assertThat(response.name()).isEqualTo(theme.getName());
                    assertThat(response.description()).isEqualTo(theme.getDescription());
                    assertThat(response.thumbnail()).isEqualTo(theme.getThumbnailAsString());
                }));
            }
        }

        @Nested
        @DisplayName("테마 정보를 알맞지 않게 작성한 경우")
        class ContextWithInvalidRequest {
            @Test
            @DisplayName("빈 문자열은 400을 반환한다.")
            void it_returns_400() {
                final ThemeCreateRequest request = new ThemeCreateRequest(
                        theme.getName(),
                        "",
                        theme.getThumbnailAsString()
                );

                //@formatter:off
                RestAssured.given().body(request).contentType(ContentType.JSON)
                        .when().post("/themes")
                        .then().assertThat().statusCode(400);
                //@formatter:on
            }
        }
    }
    @Nested
    @DisplayName("테마를 삭제하려 할때")
    class DescribeDeleteTheme{
        @Nested
        @DisplayName("존재하는 식별자를 가지고 삭제하는 경우")
        class ContextWithExistsId{
            @Test
            @DisplayName("204를 반환한다.")
            void it_returns_204() {
                final ThemeResponse createResponse = 테마_생성();

                //@formatter:off
                RestAssured.given().contentType(ContentType.JSON)
                        .when().delete("/themes/"+createResponse.id())
                        .then().assertThat().statusCode(204);
                //@formatter:on
            }
        }
        @Nested
        @DisplayName("존재하지 않는 식별자를 가지고 삭제할 경우")
        class ContextWithNotExistId {
            @Test
            @DisplayName("404를 반환한다.")
            void it_returns_404() {
                //@formatter:off
                RestAssured.given().contentType(ContentType.JSON)
                        .when().delete("/themes/"+"-1")
                        .then().assertThat().statusCode(404);
                //@formatter:on
            }
        }
        @Nested
        @DisplayName("식별자에 대한 예약이 존재하는 경우")
        class ContextWithExistReservation{
            @Test
            @DisplayName("400을 반환한다.")
            void it_returns_(){
                final ThemeResponse themeResponse = 테마_생성();
                final ReservationTimeResponse reservationTimeResponse = 예약_시간_생성();
                final String token = 멤버_로그인();
                예약_생성("2024-10-03",themeResponse.id(),reservationTimeResponse.id(),token);

                //@formatter:off
                RestAssured.given().contentType(ContentType.JSON)
                        .when().delete("/themes/"+themeResponse.id())
                        .then().assertThat().statusCode(400);
                //@formatter:on
            }
        }
    }
}
