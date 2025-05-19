package roomescape.user.application.service;

import roomescape.common.domain.Email;
import roomescape.user.domain.User;
import roomescape.user.domain.UserId;

import java.util.List;

public interface UserQueryService {

    User getByEmail(Email email);

    List<User> getAll();

    List<User> getAllByIds(List<UserId> ids);

    User getById(UserId id);
}
