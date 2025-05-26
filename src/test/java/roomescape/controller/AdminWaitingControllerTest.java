package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.createAdminMember;
import static roomescape.TestFixture.createDefaultWaiting_1;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.DBHelper;
import roomescape.DatabaseCleaner;
import roomescape.auth.JwtTokenProvider;
import roomescape.controller.dto.response.BookingResponse;
import roomescape.domain.Member;
import roomescape.domain.Waiting;
import roomescape.domain.repository.WaitingRepository;
import roomescape.service.dto.result.MemberResult;

class AdminWaitingControllerTest extends AbstractRestDocsTest {

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
    @DisplayName("관리자가 대기 예약 목록을 조회한다")
    void getWaitingReservations() {
        // given
        Member admin = createAdminMember();
        dbHelper.insertMember(admin);
        String token = jwtTokenProvider.createToken(MemberResult.from(admin));
        Waiting waiting = createDefaultWaiting_1();
        dbHelper.insertWaiting(waiting);

        // when & then
        List<BookingResponse> responses = givenWithDocs("admin-waiting-get")
                .cookie("token", token)
                .when()
                .get("/admin/waitings")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList(".", BookingResponse.class);

        assertThat(responses).hasSize(1);
    }

    @DisplayName("관리자가 대기 예약을 거절한다.")
    @Test
    void deleteWaiting() {
        // given
        Member admin = createAdminMember();
        dbHelper.insertMember(admin);
        String token = jwtTokenProvider.createToken(MemberResult.from(admin));
        Waiting waiting = createDefaultWaiting_1();
        dbHelper.insertWaiting(waiting);

        // when & then
        givenWithDocs("admin-waiting-delete")
                .cookie("token", token)
                .when()
                .delete("/admin/waitings/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(waitingRepository.findById(1L)).isEmpty();
    }
}
