package roomescape.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Import(DataInitializer.class)
public abstract class IntegrationTestSupport {

    @Autowired
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer.clear();
    }
}
