package roomescape.controller;

import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultTheme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.DBHelper;
import roomescape.DatabaseCleaner;
import roomescape.auth.JwtTokenProvider;
import roomescape.controller.dto.request.CreateBookingRequest;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.dto.result.MemberResult;

class ReservationControllerTest extends AbstractRestDocsTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private DBHelper dbHelper;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @Test
    @DisplayName("예약을 생성한다")
    void createReservation() {
        // given
        Member member = createDefaultMember();
        ReservationTime reservationTime = createDefaultReservationTime();
        Theme theme = createDefaultTheme();
        dbHelper.prepareForBooking(member, reservationTime, theme);

        String token = jwtTokenProvider.createToken(MemberResult.from(member));

        CreateBookingRequest request = new CreateBookingRequest(
                DEFAULT_DATE, reservationTime.getId(), theme.getId()
        );

        // when & then
        givenWithDocs("reservation-create")
                .cookie("token", token)
                .contentType("application/json")
                .body(request)
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }
} 
