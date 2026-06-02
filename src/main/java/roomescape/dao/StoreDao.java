package roomescape.dao;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Store;

public interface StoreDao {
    Optional<Store> findById(Long id);

    List<Store> findAll();
}
