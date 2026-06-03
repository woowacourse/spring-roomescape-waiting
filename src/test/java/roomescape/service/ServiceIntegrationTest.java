package roomescape.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
abstract class ServiceIntegrationTest {

    protected static final long DEFAULT_STORE_ID = 1L;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    // store 도메인은 요구사항 외 범위이므로, store 도메인 curd api는 존재하지 않는다. 따라서 테스트시에도 편의상 store_id = 1로 고정된 값만을 사용한다.
    protected long insertDefaultStore() {
        jdbcTemplate.update("INSERT INTO store(id, name) VALUES (?, ?)", DEFAULT_STORE_ID, "매장");
        return DEFAULT_STORE_ID;
    }
}
