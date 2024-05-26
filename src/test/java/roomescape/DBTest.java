package roomescape;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@DataJpaTest
public class DBTest {

    @Autowired
    protected ReservationRepository reservationRepository;

    @Autowired
    protected ReservationTimeRepository timeRepository;

    @Autowired
    protected ThemeRepository themeRepository;

    @Autowired
    protected MemberRepository memberRepository;
}
