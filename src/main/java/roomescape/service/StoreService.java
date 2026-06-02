package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.StoreDao;
import roomescape.domain.Store;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StoreService {

    private final StoreDao storeDao;

    public StoreService(StoreDao storeDao) {
        this.storeDao = storeDao;
    }

    public List<Store> getStores() {
        return storeDao.findAllStores();
    }
}
