package roomescape.support;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 각 테스트 종료 후 {@link DatabaseCleaner} 로 모든 테이블을 정리한다.
 * {@link IntegrationTest} 가 이 확장을 등록하므로, 애노테이션만으로 정리 동작이 드러난다.
 */
public class DatabaseCleanupExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        SpringExtension.getApplicationContext(context)
                .getBean(DatabaseCleaner.class)
                .clear();
    }
}
