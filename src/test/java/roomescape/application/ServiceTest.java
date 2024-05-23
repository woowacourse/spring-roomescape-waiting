package roomescape.application;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.application.config.TestConfig;
import roomescape.support.DatabaseCleanerExtension;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
        classes = TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ExtendWith(DatabaseCleanerExtension.class)
public @interface ServiceTest {
}
