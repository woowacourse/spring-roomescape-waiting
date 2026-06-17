package roomescape.store;

import java.util.List;
import java.util.Optional;
import roomescape.store.Store;

public interface StoreDao {
    Optional<Store> findById(Long id);

    List<Store> findAll();
}
