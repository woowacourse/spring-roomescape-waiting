package roomescape.service;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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

    // 공유 인메모리 DB(jdbc:h2:mem:testdatabase)에 다른(비트랜잭션) 테스트가 커밋한 데이터가 남아
    // 실행 순서에 따라 격리가 깨지는 것을 방지한다. 트랜잭션 롤백을 깨지 않도록 DDL(TRUNCATE) 대신
    // DML(DELETE)로 비우고, FK 순서를 신경 쓰지 않도록 참조 무결성만 잠시 해제한다.
    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables "
                        + "WHERE table_schema = 'PUBLIC' AND table_type = 'BASE TABLE'",
                String.class);
        for (String table : tables) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    // store 도메인은 요구사항 외 범위이므로, store 도메인 curd api는 존재하지 않는다. 따라서 테스트시에도 편의상 store_id = 1로 고정된 값만을 사용한다.
    protected long insertDefaultStore() {
        jdbcTemplate.update("INSERT INTO store(id, name) VALUES (?, ?)", DEFAULT_STORE_ID, "매장");
        return DEFAULT_STORE_ID;
    }
}
