package roomescape.admin.fixture;

import org.springframework.boot.test.context.TestConfiguration;
import roomescape.user.domain.Role;
import roomescape.user.domain.User;
import roomescape.user.fixture.AbstractUserTestDataConfig;
import roomescape.user.fixture.UserFixture;

@TestConfiguration
public class AdminTestDataConfig extends AbstractUserTestDataConfig {

    private static final Role ROLE_FIELD = Role.ROLE_ADMIN;
    private static final String NAME_FIELD = "admin_dummyName";
    private static final String EMAIL_FIELD = "admin_dummyEmail";
    private static final String PASSWORD_FILED = "admin_dummyPassword";

    @Override
    protected User createUserEntity() {
        return UserFixture.create(ROLE_FIELD, NAME_FIELD, EMAIL_FIELD, PASSWORD_FILED);
    }
}
