package roomescape.user;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import roomescape.user.domain.Role;
import roomescape.user.domain.User;
import roomescape.user.fixture.UserFixture;
import roomescape.user.repository.UserRepository;

@TestConfiguration
public class AdminTestDataConfig {

    private static final Role ROLE_FIELD = Role.ROLE_ADMIN;
    private static final String NAME_FIELD = "admin_dummyName";
    private static final String EMAIL_FIELD = "admin_dummyEmail";
    private static final String PASSWORD_FILED = "admin_dummyPassword";

    @Autowired
    private UserRepository repository;

    private Long savedId;
    private User savedAdmin;

    @PostConstruct
    public void setUpTestData() {
        User admin = UserFixture.create(ROLE_FIELD, NAME_FIELD, EMAIL_FIELD, PASSWORD_FILED);
        savedAdmin = repository.save(admin);
        savedId = savedAdmin.getId();
    }

    public Long getSavedId() {
        return savedId;
    }

    public User getSavedAdmin() {
        return savedAdmin;
    }
}

