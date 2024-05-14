package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exceptions.NotFoundException;

class RoleTest {

    @Test
    @DisplayName("dbValue와 일치하는 Role을 찾을 수 없는 경우 예외가 발생한다.")
    void throwExceptionIfNotFoundByDbValue() {
        assertThatThrownBy(() -> Role.getByDbValue("invalid dbValue"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("dbValue와 일치하는 Role을 찾는다.")
    void findRoleByDbValue() {
        assertAll(
                () -> assertThat(Role.getByDbValue(Role.ADMIN.getDbValue())).isEqualTo(Role.ADMIN),
                () -> assertThat(Role.getByDbValue(Role.USER.getDbValue())).isEqualTo(Role.USER)
        );
    }
}
