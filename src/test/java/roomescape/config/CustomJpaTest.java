package roomescape.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.fixture.db.MemberDbFixture;
import roomescape.fixture.db.ReservationDateTimeDbFixture;
import roomescape.fixture.db.ThemeDbFixture;
import roomescape.member.infrastructure.security.Sha256PasswordEncryptor;

@DataJpaTest
@Import({ReservationDateTimeDbFixture.class, MemberDbFixture.class, ThemeDbFixture.class, Sha256PasswordEncryptor.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomJpaTest {
}
