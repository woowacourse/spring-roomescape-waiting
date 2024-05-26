package roomescape.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql("/truncate.sql")
public abstract class ServiceTestBase {
}
