package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.acceptance.config.AcceptanceTest;
import roomescape.controller.api.dto.request.ReservationTimeRequest;
import roomescape.controller.api.dto.response.AvailReservationTimeResponse;
import roomescape.controller.api.dto.response.AvailableReservationTimesResponse;
import roomescape.controller.api.dto.response.ReservationTimeResponse;
import roomescape.domain.reservation.ReservationTime;
import roomescape.fixture.ReservationTimeFixture;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.acceptance.step.MemberStep.멤버_로그인;
import static roomescape.acceptance.step.ReservationStep.예약_생성;
import static roomescape.acceptance.step.ReservationTimeStep.예약_시간_생성;
import static roomescape.acceptance.step.ThemeStep.테마_생성;

@AcceptanceTest
public class ReservationTimeAcceptanceTest {
    private final ReservationTime reservationTime = ReservationTimeFixture.getDomain();

    @Nested
    @DisplayName("예약 가능한 시간 생성할때")
    class DescribeCreateReservation {
        @Nested
        @DisplayName("시간을(EX- 10:00) 알맞게 작성한 경우")
        class ContextWithValidRequest {
            @Test
            @DisplayName("201과 결과를 반환한다.")
            void it_returns_201_and_response() {
                final ReservationTimeRequest request = new ReservationTimeRequest(
                        reservationTime.getStartAtAsString()
                );
                //@formatter:off
                final ReservationTimeResponse response =
                        RestAssured.given().body(request).contentType(ContentType.JSON)
                                .when().post("/times")
                                .then().assertThat().statusCode(201).extract().as(ReservationTimeResponse.class);
                //@formatter:on

                assertThat(response.id()).isNotZero()
                        .isNotNull();
                assertThat(response.startAt()).isEqualTo(reservationTime.getStartAtAsString());
            }
        }

        @Nested
        @DisplayName("시간을 알맞지 않게 작성한 경우")
        class ContextWithInvalidRequest {
            @ParameterizedTest
            @ValueSource(strings = {"10.00", "23시59분", "24:50"})
            @DisplayName("정해진 형식과 다를시 400을 반환한다.")
            void it_returns_400(final String invalidValue) {
                final ReservationTimeRequest request = new ReservationTimeRequest(
                        invalidValue
                );

                //@formatter:off
                RestAssured.given().body(request).contentType(ContentType.JSON)
                        .when().post("/times")
                        .then().assertThat().statusCode(400);
                //@formatter:on
            }
        }

        @Nested
        @DisplayName("존재하는 시간을 작성한 경우")
        class ContextWithExistTime {
            @Test
            @DisplayName("409를 반환한다.")
            void it_returns_409() {
                final ReservationTimeResponse createResponse = 예약_시간_생성();
                final ReservationTimeRequest request = new ReservationTimeRequest(
                        createResponse.startAt()
                );

                //@formatter:off
                RestAssured.given().body(request).contentType(ContentType.JSON)
                        .when().post("/times")
                        .then().assertThat().statusCode(409);
                //@formatter:on
            }
        }
    }

    @Nested
    @DisplayName("예약을 삭제할때")
    class DescribeDeleteReservation {
        @Nested
        @DisplayName("존재하는 식별자를 가지고 삭제하는 경우")
        class ContextWithExistId {
            @Test
            @DisplayName("204를 반환한다.")
            void it_returns_204() {
                final ReservationTimeResponse createResponse = 예약_시간_생성();

                //@formatter:off
                RestAssured.given().contentType(ContentType.JSON)
                        .when().delete("/times/"+createResponse.id())
                        .then().assertThat().statusCode(204);
                //@formatter:on
            }
        }

        @Nested
        @DisplayName("존재하지 않는 식별자를 가지고 삭제하는 경우")
        class ContextWithNotExistId {
            @Test
            @DisplayName("404를 반환한다.")
            void it_returns_404() {
                //@formatter:off
                RestAssured.given().contentType(ContentType.JSON)
                        .when().delete("/times/"+"-1")
                        .then().assertThat().statusCode(404);
                //@formatter:on
            }
        }
    }

    @Nested
    @DisplayName("예약 가능한 시간을 가져올때")
    class DescribeGetAvailableTimes {
        @Test
        @DisplayName("200과 결과를 반환한다.")
        void it_returns_200_and_response() {
            final String token = 멤버_로그인();
            final var theme = 테마_생성();
            final var reservationTime1 = 예약_시간_생성("10:00");
            final var reservationTime2 = 예약_시간_생성("11:00");
            예약_생성("2024-10-03", theme.id(), reservationTime1.id(), token);

            final Map<String, Object> params = new HashMap<>();
            params.put("date","2024-10-03");
            params.put("themeId", theme.id());

            //@formatter:off
            final var response = RestAssured.given().params(params).contentType(ContentType.JSON)
                    .when().get("/times/available")
                    .then().assertThat().statusCode(200).extract().as(AvailableReservationTimesResponse.class);
            //@formatter:on

            assertThat(response.data()).containsExactly(
                    new AvailReservationTimeResponse(reservationTime1.id(), reservationTime1.startAt(), true),
                    new AvailReservationTimeResponse(reservationTime2.id(), reservationTime2.startAt(), false)
            );
        }
    }
}
