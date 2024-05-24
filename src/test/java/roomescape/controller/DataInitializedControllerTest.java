package roomescape.controller;

import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql("/truncate-with-admin-and-guest.sql")
public abstract class DataInitializedControllerTest extends ControllerTestBase {
}
