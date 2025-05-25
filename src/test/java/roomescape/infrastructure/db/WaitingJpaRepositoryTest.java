package roomescape.infrastructure.db;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.model.Member;
import roomescape.model.PendingReservation;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;
import roomescape.model.Waiting;

@DataJpaTest
class WaitingJpaRepositoryTest {

    @Autowired
    WaitingJpaRepository waitingJpaRepository;

    @Autowired
    ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Autowired
    ThemeJpaRepository themeJpaRepository;

    @Autowired
    MemberJpaRepository memberJpaRepository;


    @Test
    @DisplayName("예약 날짜 테마 시각에 따라 가장 이른 웨이팅을 찾는다")
    void test1() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 30));
        ReservationTime savedReservationTime = reservationTimeJpaRepository.save(reservationTime);

        Theme theme = new Theme("테마", "설명", "썸네일");
        Theme savedTheme = themeJpaRepository.save(theme);

        Theme anotherTheme = themeJpaRepository.save(new Theme("다른 테마", "설명", "썸네일"));

        Member member = new Member("도기", "email@gmail.com", "password", Role.ADMIN);
        Member savedMember = memberJpaRepository.save(member);

        LocalDate reservationDate = LocalDate.now().plusDays(1);
        Waiting targetWaiting = waitingJpaRepository.save(new Waiting(
                        LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 30)),
                        new PendingReservation(
                                reservationDate,
                                savedReservationTime,
                                savedTheme,
                                savedMember,
                                LocalDate.now()
                        )
                )
        );

        Waiting secondOrderWaiting = waitingJpaRepository.save(new Waiting(
                        LocalDateTime.of(LocalDate.now(), LocalTime.of(13, 30)),
                        new PendingReservation(
                                reservationDate,
                                savedReservationTime,
                                savedTheme,
                                savedMember,
                                LocalDate.now()
                        )
                )
        );

        Waiting differentThemeWaiting = waitingJpaRepository.save(new Waiting(
                        LocalDateTime.of(LocalDate.now(), LocalTime.of(13, 30)),
                        new PendingReservation(
                                reservationDate,
                                savedReservationTime,
                                anotherTheme,
                                savedMember,
                                LocalDate.now()
                        )
                )
        );

        // when
        Optional<Waiting> earliestWaiting = waitingJpaRepository.findEarliestWaitingBy(reservationDate,
                savedReservationTime, savedTheme);

        // then
        assertAll(
                () -> assertThat(earliestWaiting).isPresent(),
                () -> assertThat(earliestWaiting.get().getId()).isEqualTo(targetWaiting.getId())
        );
    }
}
