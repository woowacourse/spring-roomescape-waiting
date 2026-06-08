package roomescape.dao;

import java.util.List;
import java.util.Optional;
import roomescape.domain.store.Store;

public interface StoreDao {
    Optional<Store> findById(Long id);

    List<Store> findAll();
}
