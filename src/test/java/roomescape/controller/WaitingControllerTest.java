package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultTheme;

import java.util.List;
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
import roomescape.domain.Waiting;
import roomescape.domain.repository.WaitingRepository;
import roomescape.service.dto.result.MemberResult;

class WaitingControllerTest extends AbstractRestDocsTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private DBHelper dbHelper;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @Test
    @DisplayName("대기 예약을 생성한다")
    void createWaitingReservation() {
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
        givenWithDocs("waiting-create")
                .cookie("token", token)
                .contentType("application/json")
                .body(request)
                .when()
                .post("/waitings")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        List<Waiting> waitings = waitingRepository.findAll();

        assertAll(
                () -> assertThat(waitings).hasSize(1),
                () -> assertThat(waitings.get(0).getMember().getEmail()).isEqualTo(member.getEmail()),
                () -> assertThat(waitings.get(0).getTime().getStartAt()).isEqualTo(reservationTime.getStartAt()),
                () -> assertThat(waitings.get(0).getTheme().getName()).isEqualTo(theme.getName())
        );
    }
}
