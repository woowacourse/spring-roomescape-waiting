package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.DomainFixtures.JUNK_USER;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.timeslot.TimeSlotRepository;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceTest {

    @Autowired
    private UserService service;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private TimeSlotRepository timeSlotRepository;
    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("사용자를 추가할 수 있다.")
    void saveUserUser() {
        // given
        var user = JUNK_USER;

        // when
        var created = service.saveUser(user.email(), user.password(), user.name());

        // then
        var users = service.findAllUsers();
        assertThat(users).contains(created);
    }
}
