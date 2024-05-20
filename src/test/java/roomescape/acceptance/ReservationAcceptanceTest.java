package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.acceptance.config.AcceptanceTest;
import roomescape.controller.api.dto.request.ReservationRequest;
import roomescape.controller.api.dto.response.ReservationResponse;
import roomescape.controller.api.dto.response.ReservationTimeResponse;
import roomescape.controller.api.dto.response.ThemeResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.acceptance.step.MemberStep.멤버_로그인;
import static roomescape.acceptance.step.ReservationStep.예약_생성;
import static roomescape.acceptance.step.ReservationTimeStep.예약_시간_생성;
import static roomescape.acceptance.step.ThemeStep.테마_생성;

@AcceptanceTest
public class ReservationAcceptanceTest {
    @Nested
    @DisplayName("예약을 생성할때")
    class DescribeCreateReservation {
        @Nested
        @DisplayName("DB내 존재하는 테마,예약시간 Id, 쿠키에 로그인한 토큰을 담은 경우")
        class ContextWithValidRequest {
            // 토큰을 빼는게 맞을까??
            private final String token = 멤버_로그인();
            @Test
            @DisplayName("201과 결과를 반환한다.")
            void it_returns_201_and_response() {
                final ThemeResponse themeResponse = 테마_생성();
                final ReservationTimeResponse reservationTimeResponse = 예약_시간_생성();
                final ReservationRequest request = new ReservationRequest(
                        "2024-10-03",
                        reservationTimeResponse.id(),
                        themeResponse.id()
                );

                //@formatter:off
                final ReservationResponse response =
                        RestAssured.given().cookie(token).body(request).contentType(ContentType.JSON)
                            .when().post("/reservations")
                            .then().assertThat().statusCode(201).extract().as(ReservationResponse.class);
                //@formatter:on

                assertAll(() -> {
                    assertThat(response.date()).isEqualTo("2024-10-03");
                    assertThat(response.time()).isEqualTo(reservationTimeResponse);
                    assertThat(response.theme()).isEqualTo(themeResponse);
                });
            }
            @Test
            @DisplayName("날짜,시간,테마가 똑같은 예약이 있다면 409를 반환한다.")
            void it_returns_409_with_duplicate_reservation_reservationTime_date_theme(){
                final ThemeResponse themeResponse = 테마_생성();
                final ReservationTimeResponse reservationTimeResponse = 예약_시간_생성();
                예약_생성("2024-10-03",themeResponse.id(),reservationTimeResponse.id(),token);

                final ReservationRequest request = new ReservationRequest(
                        "2024-10-03",
                        reservationTimeResponse.id(),
                        themeResponse.id()
                );

                //@formatter:off
                RestAssured.given().cookie(token).body(request).contentType(ContentType.JSON)
                        .when().post("/reservations")
                        .then().assertThat().statusCode(409);
                //@formatter:on
            }
        }

        @Nested
        @DisplayName("쿠키에 로그인한 토큰을 담지 않은 경우")
        class ContextWithoutToken {
            @Test
            @DisplayName("401을 반환한다.")
            void it_returns_401() {
                final ThemeResponse themeResponse = 테마_생성();
                final ReservationTimeResponse reservationTimeResponse = 예약_시간_생성();
                final ReservationRequest request = new ReservationRequest(
                        "2024-10-03",
                        reservationTimeResponse.id(),
                        themeResponse.id()
                );

                //@formatter:off
                RestAssured.given().body(request).contentType(ContentType.JSON)
                        .when().post("/reservations")
                        .then().assertThat().statusCode(401);
                //@formatter:on
            }
        }
        @Nested
        @DisplayName("예약 정보 및 Id에 대한 값이 타당하지 않은 경우")
        class ContextWithInvalidRequest{
            private final String token = 멤버_로그인();
            @Test
            @DisplayName("존재하지 않는 id를 통한 생성은 404를 반환한다.")
            void it_returns_404_with_not_exist_id(){
                final ReservationRequest request = new ReservationRequest(
                        "2024-05-20",
                        -1L,
                        -1L
                );

                RestAssured.given().body(request).cookie(token).contentType(ContentType.JSON)
                        .when().post("/reservations")
                        .then().assertThat().statusCode(404);
            }

            /**
             * {@link roomescape.util.OldDateTimeFormatter} 에 기반하여
             * 테스트에서는 현재 시간이 아닌, 2000년도 01-01 10:00 기준으로 동작한다.
             */
            @Test
            @DisplayName("현재보다 이전 날짜&시간 에 대한 생성은 400을 반환한다.")
            void it_returns_400_with_past_date_and_time(){
                final ThemeResponse themeResponse = 테마_생성();
                final ReservationTimeResponse reservationTimeResponse = 예약_시간_생성();
                final ReservationRequest request = new ReservationRequest(
                        "1999-12-31",
                        reservationTimeResponse.id(),
                        themeResponse.id()
                );

                RestAssured.given().body(request).cookie(token).contentType(ContentType.JSON)
                        .when().post("/reservations")
                        .then().assertThat().statusCode(400);
            }
        }
    }
}
