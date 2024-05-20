package roomescape;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class TestDataInitExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        TestDataInitializer testDataInitializer = SpringExtension.getApplicationContext(extensionContext)
                .getBean(TestDataInitializer.class);

        testDataInitializer.resetDatabase();
    }
}
