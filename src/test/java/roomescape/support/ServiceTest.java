package roomescape.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(TestDataHelper.class)
@TestExecutionListeners(
        value = DatabaseCleanupListener.class,
        mergeMode = MergeMode.MERGE_WITH_DEFAULTS
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public @interface ServiceTest {
}
