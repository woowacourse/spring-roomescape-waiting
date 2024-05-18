package roomescape.support.extension;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class DatabaseClearExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        DatabaseCleaner databaseCleaner = getDataCleaner(context);
        databaseCleaner.clear();
    }

    private DatabaseCleaner getDataCleaner(ExtensionContext extensionContext) {
        return SpringExtension.getApplicationContext(extensionContext)
                .getBean(DatabaseCleaner.class);
    }
}
