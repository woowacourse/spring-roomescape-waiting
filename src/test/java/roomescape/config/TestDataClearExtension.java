package roomescape.config;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class TestDataClearExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        TestDataCleaner dataCleaner = getDataCleaner(context);
        dataCleaner.clear();
    }

    private TestDataCleaner getDataCleaner(ExtensionContext extensionContext) {
        return SpringExtension.getApplicationContext(extensionContext)
                .getBean(TestDataCleaner.class);
    }
}
