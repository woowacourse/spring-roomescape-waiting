package roomescape.member.role;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.model.RoomEscapeException;
import roomescape.member.exception.MemberExceptionCode;

class MemberRoleTest {

    @Test
    @DisplayName("일치하는 권한이 없는 경우 에러를 던진다.")
    void findMemberRole() {
        Throwable noExistRole = assertThrows(RoomEscapeException.class, () -> MemberRole.findMemberRole("polla"));

        assertEquals(noExistRole.getMessage(), MemberExceptionCode.MEMBER_ROLE_NOT_EXIST_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("같은 권한인지 확인한다.")
    void isSameRole() {
        MemberRole[] memberRoles = new MemberRole[]{MemberRole.MEMBER};

        assertAll(
                () -> assertTrue(MemberRole.MEMBER.hasSameRoleFrom(memberRoles)),
                () -> assertFalse(MemberRole.ADMIN.hasSameRoleFrom(memberRoles))
        );
    }
}
