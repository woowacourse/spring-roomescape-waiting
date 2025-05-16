package roomescape.service;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.business.ReservationWithBookStateDto;
import roomescape.exception.local.NotFoundUserException;
import roomescape.repository.ReservationRepository;
import roomescape.test.fixture.ReservationFixture;
import roomescape.test.fixture.ReservationTimeFixture;
import roomescape.test.fixture.ThemeFixture;
import roomescape.test.fixture.UserFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class UserServiceTest {

    @Autowired
    private UserService service;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User savedMember;
    private Theme savedTheme;
    private ReservationTime savedTime;

    @BeforeEach
    void setUp() {
        // Create and persist test data
        User member = UserFixture.create(Role.ROLE_MEMBER, "member_dummyName", "member_dummyEmail",
                "member_dummyPassword");
        Theme theme = ThemeFixture.create("dummyName", "dummyDescription", "dummyThumbnail");
        ReservationTime time = ReservationTimeFixture.create(LocalTime.of(2, 40));

        savedMember = entityManager.persist(member);
        savedTheme = entityManager.persist(theme);
        savedTime = entityManager.persist(time);

        entityManager.flush();
    }

    @Nested
    @DisplayName("유저에 예약 리스트를 조회하는 기능")
    class findAllReservationByMember {

        @DisplayName("유저의 예약 리스트가 여러개 존재할 때 예약 리스트를 모두 가져올 수 있다.")
        @Test
        void findAllReservationByMember_success_bySomeReservations() {
            // given
            List<Reservation> reservations = List.of(ReservationFixture.createByBookedStatus(
                            LocalDate.now().plusDays(1), savedTime, savedTheme, savedMember),
                    ReservationFixture.createByBookedStatus(
                            LocalDate.now().plusDays(2), savedTime, savedTheme, savedMember));
            List<Reservation> savedReservations = reservationRepository.saveAll(reservations);

            // when
            List<ReservationWithBookStateDto> allReservationByMember = service.findAllReservationByMember(savedMember);

            // then
            Assertions.assertThat(allReservationByMember).hasSize(2);
        }

        @DisplayName("유저의 예약 리스트가 빈 리스트여도 예외가 터지지 않는다.")
        @Test
        void findAllReservationByMember_success_byNonExistedReservation() {
            // given
            // when
            List<ReservationWithBookStateDto> allReservationByMember = service.findAllReservationByMember(savedMember);

            // then
            Assertions.assertThat(allReservationByMember).hasSize(0);
        }

        @DisplayName("유저를 찾지 못했을 때 예외가 발생한다. : NotFoundUserException")
        @Test
        void findAllReservationByMember_throwException_byInvalidMember() {
            // given
            User user = new User(Long.MAX_VALUE, Role.ROLE_MEMBER, "ustfarbmn1", "ustfarbme1", "ustfarbmp1");

            // when
            // then
            Assertions.assertThatThrownBy(
                    () -> service.findAllReservationByMember(user)
            ).isInstanceOf(NotFoundUserException.class);
        }
    }
}
