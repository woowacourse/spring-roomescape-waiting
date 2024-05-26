package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MemberRoleTest {

    @DisplayName("관리자인지 판단한다.")
    @CsvSource(value = {"ADMIN,true", "USER,false"})
    @ParameterizedTest
    void isAdmin(MemberRole role, boolean expected) {
        boolean result = role.isAdmin();

        assertThat(result).isEqualTo(expected);
    }
}
