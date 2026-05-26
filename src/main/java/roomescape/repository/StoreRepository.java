package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Store;

public interface StoreRepository {

    Optional<Store> findById(Long id);

    boolean existsByStoreIdAndUserId(Long storeId, Long userId);

    List<Long> findStoreIdsByUserId(Long userId);
}