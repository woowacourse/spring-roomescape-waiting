package roomescape.common;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@Import(ClockConfig.class)
public class ServiceTestBase {

}
