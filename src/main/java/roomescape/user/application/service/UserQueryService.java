package roomescape.user.application.service;

import roomescape.common.domain.Email;
import roomescape.user.domain.User;

import java.util.List;

public interface UserQueryService {

    User getByEmail(Email email);

    List<User> getAll();

    List<User> getAllByIds(List<Long> ids);

    User getById(Long id);
}
