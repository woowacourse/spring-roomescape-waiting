package roomescape.reservation.domain;

import org.springframework.stereotype.Repository;

@Repository
public class CustomRepository {

    private final CustomDao dao;

    public CustomRepository() {
        dao = null;
    }

    public CustomRepository(CustomDao dao) {
        this.dao = dao;
    }

    public Object method() {
        return null;
    }
}
