package roomescape.util;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.global.config.JpaAuditingConfig;

@DataJpaTest
@Import(JpaAuditingConfig.class)
public class RepositoryTest {
}
