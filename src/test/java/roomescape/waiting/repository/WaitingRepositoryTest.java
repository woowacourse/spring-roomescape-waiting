package roomescape.waiting.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.ReservationTimeTestDataConfig;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.fixture.ReservationTimeFixture;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.ThemeTestDataConfig;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.user.MemberTestDataConfig;
import roomescape.user.domain.Role;
import roomescape.user.domain.User;
import roomescape.user.fixture.UserFixture;
import roomescape.user.repository.UserRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;
import roomescape.waiting.fixture.WaitingFixture;
import roomescape.waiting.service.WaitingService;

@DataJpaTest
@Import({
        WaitingService.class,
        ReservationService.class,
        MemberTestDataConfig.class,
        ReservationTimeTestDataConfig.class,
        ThemeTestDataConfig.class
})
class WaitingRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private UserRepository userRepository;

    private ReservationTime savedReservationTime;
    private Theme savedTheme;
    private User savedUser;
    private LocalDate date = LocalDate.now().plusDays(1);

    @BeforeEach
    void beforeEach() {
        savedReservationTime = reservationTimeRepository.save(ReservationTimeFixture.create(LocalTime.of(14, 14)));
        savedTheme = themeRepository.save(new Theme("name1", "dd", "tt"));
        savedUser = userRepository.save(UserFixture.create(Role.ROLE_MEMBER, "n1", "e1", "p1"));
    }

    private Reservation createReservationDefault() {
        return ReservationFixture.createByBookedStatus(date, savedReservationTime, savedTheme, savedUser);
    }

    @Nested
    @DisplayName("예약 대기 목록과 순위 조회 기능")
    class findWaitingsWithRankByMemberId {

        @DisplayName("회원 ID로 예약 대기 목록과 순위를 조회한다")
        @Test
        void findWaitingsWithRankByMemberId_success_byMemberId() {
            // given
            Reservation reservation = createReservationDefault();
            Reservation savedReservation = reservationRepository.save(reservation);

            Waiting waiting1 = WaitingFixture.createByReservation(savedReservation);
            Waiting waiting2 = WaitingFixture.createByReservation(savedReservation);
            waitingRepository.save(waiting1);
            waitingRepository.save(waiting2);

            // when
            List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberId(savedUser.getId());

            // then
            org.junit.jupiter.api.Assertions.assertAll(
                    () -> Assertions.assertThat(waitings).hasSize(2),
                    () -> Assertions.assertThat(waitings.get(0).getRank()).isEqualTo(0L),
                    () -> Assertions.assertThat(waitings.get(1).getRank()).isEqualTo(1L),
                    () -> Assertions.assertThat(waitings.get(0).getWaiting().getMember().getId())
                            .isEqualTo(savedUser.getId())
            );
        }
    }
}
