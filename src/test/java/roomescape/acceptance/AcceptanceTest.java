package roomescape.acceptance;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(
        scripts = "/cleanup.sql",
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD
)
@SqlMergeMode(MergeMode.MERGE)
public abstract class AcceptanceTest {
}
