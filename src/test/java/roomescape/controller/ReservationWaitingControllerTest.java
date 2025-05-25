package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.AuthInterceptor;
import roomescape.service.ReservationWaitingService;
import roomescape.service.dto.ReservationWaitingRequest;
import roomescape.service.dto.ReservationWaitingResponse;

@WebMvcTest(value = ReservationWaitingController.class)
class ReservationWaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationWaitingService reservationWaitingService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    void addReservationWaitingTest() {
        //given
        final ReservationWaitingRequest request = new ReservationWaitingRequest(
                LocalDate.of(2025, 5, 30), 1L, 1L);
        given(reservationWaitingService.addReservationWaiting(any(ReservationWaitingRequest.class),
                anyLong())).willReturn(new ReservationWaitingResponse(1L, LocalDate.of(2025, 5, 30), null, null));

        // should
        RestAssuredMockMvc.given().log().all()
                .contentType(ContentType.JSON)
                .sessionAttr("id", "1")
                .body(request)
                .when().post("/reservations-waiting")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void removeReservationWaitingTest() {
        //given
        doNothing().when(reservationWaitingService).removeReservationWaiting(anyLong());

        //should
        RestAssuredMockMvc.given().log().all()
                .contentType(ContentType.JSON)
                .sessionAttr("id", "1")
                .when().delete("/reservations-waiting/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
