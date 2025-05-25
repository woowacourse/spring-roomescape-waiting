package roomescape.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.dto.response.WaitingAdminResponseDto;
import roomescape.infrastructure.db.MemberJpaRepository;
import roomescape.infrastructure.db.ReservationTimeJpaRepository;
import roomescape.infrastructure.db.ThemeJpaRepository;
import roomescape.infrastructure.db.WaitingJpaRepository;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;
import roomescape.model.Waiting;

@SpringBootTest
class WaitingAdminServiceTest {

    @Autowired
    private WaitingAdminService waitingAdminService;

    @Autowired
    private WaitingJpaRepository waitingJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private ThemeJpaRepository themeJpaRepository;

    @Autowired
    private ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Test
    @DisplayName("대기를 등록한 사용자와 관계 없이 모든 웨이팅 목록을 반환한다")
    void test1() {
        String emailOfAdministrator = "email@gmail.com";
        Member firstOwner = memberJpaRepository.save(new Member("이름", emailOfAdministrator, "password", Role.ADMIN));
        Member secondOwner = memberJpaRepository.save(
                new Member("주인아님", "secondOwner@gmail.com", "password", Role.USER));

        Theme theme = themeJpaRepository.save(new Theme("새로운 테마", "새로운 설명", "썸네일"));
        ReservationTime reservationTime = reservationTimeJpaRepository.save(
                new ReservationTime(LocalTime.of(12, 30)));

        Waiting firstWaiting = waitingJpaRepository.save(new Waiting(
                LocalDateTime.now(),
                new Reservation(
                        LocalDate.now().plusDays(1),
                        reservationTime,
                        theme,
                        firstOwner,
                        LocalDate.now()
                )
        ));

        Waiting secondWaiting = waitingJpaRepository.save(new Waiting(
                LocalDateTime.now(),
                new Reservation(
                        LocalDate.now().plusDays(1),
                        reservationTime,
                        theme,
                        secondOwner,
                        LocalDate.now()
                )
        ));

        // when
        List<WaitingAdminResponseDto> actual = waitingAdminService.getAllWaitings();

        // then
        List<Long> ids = actual.stream()
                .map(WaitingAdminResponseDto::id)
                .toList();

        assertAll(
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(ids).contains(firstWaiting.getId(), secondWaiting.getId())
        );
    }

    @Test
    @DisplayName("대기를 거절할 수 있다")
    void test2() {
        String emailOfAdministrator = "email@gmail.com";
        Member administrator = memberJpaRepository.save(new Member("이름", emailOfAdministrator, "password", Role.ADMIN));
        Member user = memberJpaRepository.save(
                new Member("사용자", "user@gmail.com", "password", Role.USER));

        Theme theme = themeJpaRepository.save(new Theme("새로운 테마", "새로운 설명", "썸네일"));
        ReservationTime reservationTime = reservationTimeJpaRepository.save(
                new ReservationTime(LocalTime.of(12, 30)));

        Waiting waiting = waitingJpaRepository.save(new Waiting(
                LocalDateTime.now(),
                new Reservation(
                        LocalDate.now().plusDays(1),
                        reservationTime,
                        theme,
                        user,
                        LocalDate.now()
                )
        ));

        // when
        waitingAdminService.rejectWaiting(waiting.getId());

        // then
        List<Waiting> waitings = waitingJpaRepository.findAll();

        assertThat(waitings).doesNotContain(waiting);
    }
}
