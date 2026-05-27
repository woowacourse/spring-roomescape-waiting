package roomescape.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Store;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JdbcTest
@ActiveProfiles("test")
@Import(StoreDao.class)
public class StoreDaoTest {

    private static final String INSERT_THREE_STORES_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점'),
                   (2, '홍대점'),
                   (3, '판교점');
            """;

    @Autowired
    private StoreDao storeDao;

    @Test
    @Sql(statements = INSERT_THREE_STORES_SQL)
    void 모든_매장을_조회한다() {
        List<Store> stores = storeDao.findAllStores();

        assertThat(stores).hasSize(3);
        assertThat(stores)
                .extracting(Store::getId, Store::getName)
                .containsExactlyInAnyOrder(
                        tuple(1L, "강남점"),
                        tuple(2L, "홍대점"),
                        tuple(3L, "판교점")
                );
    }

    @Test
    @Sql(statements = INSERT_THREE_STORES_SQL)
    void ID로_매장을_조회한다() {
        Store store = storeDao.findById(2L);

        assertThat(store.getId()).isEqualTo(2L);
        assertThat(store.getName()).isEqualTo("홍대점");
    }

    @Test
    void 매장이_없으면_빈_리스트를_반환한다() {
        List<Store> stores = storeDao.findAllStores();

        assertThat(stores).isEmpty();
    }
}
