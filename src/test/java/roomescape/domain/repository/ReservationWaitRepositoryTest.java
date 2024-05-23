package roomescape.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWait;
import roomescape.domain.Theme;

@Transactional
@SpringBootTest
class ReservationWaitRepositoryTest {
    @Autowired
    private ReservationWaitRepository waitRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationTimeRepository timeRepository;
    private final Member dummyMember = new Member("aa", "aa@aa.aa", "aa");
    private final Theme dummyTheme = new Theme("n", "d", "t");
    private final ReservationTime dummyTime = new ReservationTime(LocalTime.of(1, 0));
    private final Reservation dummyReservation = new Reservation(LocalDate.of(2023, 12, 11), dummyTime, dummyTheme,
            dummyMember);

    @BeforeEach
    void setUp() {
        memberRepository.save(dummyMember);
        themeRepository.save(dummyTheme);
        timeRepository.save(dummyTime);
        reservationRepository.save(dummyReservation);
    }

    @Test
    @DisplayName("예약 대기 정보를 저장한다")
    void save_ShouldStoreReservationWaitInfo() {
        // given
        ReservationWait reservationWait = new ReservationWait(dummyMember, dummyReservation, 0,
                ReservationStatus.WAITING);

        // when
        waitRepository.save(reservationWait);

        // then
        Assertions.assertThat(waitRepository.findAll()).hasSize(1);
    }
}
