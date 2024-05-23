package roomescape.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.Matchers.containsString;
import static roomescape.TestFixture.ADMIN_EMAIL;
import static roomescape.TestFixture.MEMBER_TENNY_EMAIL;

class PageAcceptanceTest extends AcceptanceTest {

    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/reservation", "/admin/time", "/admin/theme", "admin/waiting"})
    @DisplayName("관리자가 관리자 페이지에 접근하면 200을 응답한다.")
    void respondOkWhenAdminAccessAdminPage(final String adminPath) {
        final String accessToken = getAccessToken(ADMIN_EMAIL);

        assertGetResponseWithToken(accessToken, adminPath, 200)
                .body(containsString("<!DOCTYPE html>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/reservation", "/admin/time", "/admin/theme", "admin/waiting"})
    @DisplayName("사용자가 관리자 페이지에 접근하면 403을 응답한다.")
    void respondForbiddenWhenMemberAccessAdminPage(final String adminPath) {
        final String accessToken = getAccessToken(MEMBER_TENNY_EMAIL);

        assertGetResponseWithToken(accessToken, adminPath, 403);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/reservation", "/login", "/reservation-mine"})
    @DisplayName("사용자가 사용자 페이지에 접근하면 200을 응답한다.")
    void respondOkWhenMemberAccessMemberPage(String path) {
        assertGetResponse(path, 200)
                .body(containsString("<!DOCTYPE html>"));
    }
}
