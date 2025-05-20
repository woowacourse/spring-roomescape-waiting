package roomescape.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.ActiveProfiles;
import roomescape.support.RepositoryTestSupport.TestConfig;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public abstract class RepositoryTestSupport {

    @Autowired
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer.clear();
    }

    @TestConfiguration
    @ComponentScan(
            basePackages = "roomescape",
            includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class)
    )
    static class TestConfig {
        @Bean
        public DataInitializer dataInitializer() {
            return new DataInitializer();
        }
    }
}
