package roomescape.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * DB 통합 테스트용 메타 애노테이션.
 *
 * <ul>
 *     <li>{@code webEnvironment = NONE} — 웹 계층이 필요 없으므로 실제 서버를 띄우지 않는다.</li>
 *     <li>{@link DatabaseCleaner} 를 빈으로 등록한다.</li>
 *     <li>{@link DatabaseCleanupExtension} 이 각 테스트 종료 후 테이블을 정리한다(정리 책임을 애노테이션에 명시).</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(DatabaseCleaner.class)
@ExtendWith(DatabaseCleanupExtension.class)
public @interface IntegrationTest {
}
