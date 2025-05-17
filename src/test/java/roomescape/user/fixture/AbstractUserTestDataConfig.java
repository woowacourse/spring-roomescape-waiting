package roomescape.user.fixture;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.user.domain.User;
import roomescape.user.repository.UserRepository;

public abstract class AbstractUserTestDataConfig {

    @Autowired
    protected UserRepository repository;

    protected Long savedId;
    protected User savedUser;

    @PostConstruct
    public void steUpTestData() {
        User userEntity = createUserEntity();
        savedUser = repository.save(userEntity);
        savedId = savedUser.getId();
    }

    protected abstract User createUserEntity();

    public Long getSavedId() {
        return savedId;
    }

    public User getSavedUser() {
        return savedUser;
    }
}
