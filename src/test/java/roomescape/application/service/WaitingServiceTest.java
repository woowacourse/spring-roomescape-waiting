package roomescape.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.dto.LoginMember;
import roomescape.dto.request.WaitingRegisterDto;
import roomescape.dto.response.MemberWaitingResponseDto;
import roomescape.infrastructure.db.MemberJpaRepository;
import roomescape.infrastructure.db.ReservationTicketJpaRepository;
import roomescape.infrastructure.db.ReservationTimeJpaRepository;
import roomescape.infrastructure.db.ThemeJpaRepository;
import roomescape.infrastructure.db.WaitingJpaRepository;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTicket;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;
import roomescape.model.Waiting;

@SpringBootTest
public class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private WaitingJpaRepository waitingJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private ThemeJpaRepository themeJpaRepository;

    @Autowired
    private ReservationTimeJpaRepository reservationTimeJpaRepository;
    @Autowired
    private ReservationTicketJpaRepository reservationTicketJpaRepository;

    @BeforeEach
    void cleanDatabase() {
        reservationTicketJpaRepository.deleteAll();
        waitingJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
        themeJpaRepository.deleteAll();
        reservationTimeJpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("Waiting 을 등록할 때")
    class Test1 {
        @Test
        @DisplayName("정상적으로 등록된다")
        void test1() {
            // given
            Member user = memberJpaRepository.save(
                    new Member("사용자", "user@gmail.com", "password", Role.USER));
            LoginMember loginMember = new LoginMember(user);

            Theme theme = themeJpaRepository.save(new Theme("새로운 테마", "새로운 설명", "썸네일"));
            ReservationTime reservationTime = reservationTimeJpaRepository.save(
                    new ReservationTime(LocalTime.of(12, 30)));

            LocalDate date = LocalDate.now().plusDays(1);

            ReservationTicket reservationTicket = reservationTicketJpaRepository.save(
                    new ReservationTicket(new Reservation(
                            date,
                            reservationTime,
                            theme,
                            user,
                            LocalDate.now()
                    )));

            WaitingRegisterDto waitingRegisterDto = new WaitingRegisterDto(
                    theme.getId(),
                    reservationTime.getId(),
                    date
            );

            // when
            waitingService.registerWaiting(loginMember, waitingRegisterDto);

            // then
            assertAll(
                    () -> assertThat(waitingJpaRepository.findAll()).hasSize(1),
                    () -> assertThat(
                            waitingJpaRepository.findAll()
                                    .getFirst()
                                    .getReservation()
                                    .getMember()
                                    .getId()
                    ).isEqualTo(user.getId()),
                    () -> assertThat(
                            waitingJpaRepository.findAll()
                                    .getFirst()
                                    .getReservation()
                                    .getDate()
                    ).isEqualTo(date),
                    () -> assertThat(
                            waitingJpaRepository.findAll()
                                    .getFirst()
                                    .getReservation()
                                    .getTheme()
                                    .getId()
                    ).isEqualTo(theme.getId())
            );
        }

        @Test
        @DisplayName("현재보다 이전인 경우 예외를 던진다")
        void test2() {
            // given
            Member member = memberJpaRepository.save(new Member("이름", "email@gmail.com", "password", Role.ADMIN));
            LoginMember loginMember = new LoginMember(member);

            ReservationTime reservationTime = reservationTimeJpaRepository.save(
                    new ReservationTime(LocalTime.of(12, 30)));
            Theme theme = themeJpaRepository.save(new Theme("새로운 테마", "새로운 설명", "썸네일"));

            LocalDate date = LocalDate.now().minusDays(1);

            ReservationTicket reservationTicket = reservationTicketJpaRepository.save(
                    new ReservationTicket(new Reservation(
                            date,
                            reservationTime,
                            theme,
                            member,
                            LocalDate.now().minusDays(2)
                    )));

            WaitingRegisterDto waitingRegisterDto = new WaitingRegisterDto(
                    theme.getId(),
                    reservationTime.getId(),
                    date
            );

            // when
            assertThatThrownBy(() -> waitingService.registerWaiting(loginMember, waitingRegisterDto))
                    .isInstanceOf(IllegalStateException.class);
        }

    }

    @Nested
    @DisplayName("Waiting 을 삭제할 때")
    class Test2 {

        @Test
        @DisplayName("존재하지 않는 id 로 요청하는 경우 예외를 던진다")
        void test1() {
            // given
            Member member = memberJpaRepository.save(new Member("이름", "email@gmail.com", "password", Role.ADMIN));
            LoginMember loginMember = new LoginMember(member);

            // when
            assertThatThrownBy(() -> waitingService.deleteWaiting(loginMember, 999L))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("권한이 없는 사용자가 요청하면 예외를 던진다")
        void test2() {
            // given
            Member owner = memberJpaRepository.save(new Member("이름", "email@gmail.com", "password", Role.ADMIN));
            Member invalidOwner = memberJpaRepository.save(
                    new Member("다른 이름", "example@gmail.com", "password", Role.ADMIN));
            LoginMember loginMember = new LoginMember(invalidOwner);

            Theme theme = themeJpaRepository.save(new Theme("새로운 테마", "새로운 설명", "썸네일"));
            ReservationTime reservationTime = reservationTimeJpaRepository.save(
                    new ReservationTime(LocalTime.of(12, 30)));

            Waiting waiting = waitingJpaRepository.save(new Waiting(
                    LocalDateTime.now(),
                    new Reservation(
                            LocalDate.now().plusDays(1),
                            reservationTime,
                            theme,
                            owner,
                            LocalDate.now()
                    )
            ));

            // when
            assertThatThrownBy(() -> waitingService.deleteWaiting(loginMember, waiting.getId()))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("정상적으로 삭제 된다")
        void test3() {
            // given
            Member owner = memberJpaRepository.save(new Member("이름", "email@gmail.com", "password", Role.ADMIN));
            LoginMember loginMember = new LoginMember(owner);

            Theme theme = themeJpaRepository.save(new Theme("새로운 테마", "새로운 설명", "썸네일"));
            ReservationTime reservationTime = reservationTimeJpaRepository.save(
                    new ReservationTime(LocalTime.of(12, 30)));

            Waiting waiting = waitingJpaRepository.save(new Waiting(
                    LocalDateTime.now(),
                    new Reservation(
                            LocalDate.now().plusDays(1),
                            reservationTime,
                            theme,
                            owner,
                            LocalDate.now()
                    )
            ));

            // when
            waitingService.deleteWaiting(loginMember, waiting.getId());

            // then
            assertThat(waitingJpaRepository.findAll()).isEmpty();
        }

    }

    @Nested
    @DisplayName("나의 웨이팅 목록을 가져올 때")
    class Test3 {

        @Test
        @DisplayName("사용자에 대한 것만 가져온다")
        void test1() {
            // given
            Member owner = memberJpaRepository.save(new Member("이름", "email@gmail.com", "password", Role.ADMIN));
            Member anotherOwner = memberJpaRepository.save(
                    new Member("주인아님", "anotherOwner@gmail.com", "password", Role.ADMIN));
            LoginMember loginMember = new LoginMember(owner);

            Theme theme = themeJpaRepository.save(new Theme("새로운 테마", "새로운 설명", "썸네일"));
            ReservationTime reservationTime = reservationTimeJpaRepository.save(
                    new ReservationTime(LocalTime.of(12, 30)));
            ReservationTime anotherReservationTime = reservationTimeJpaRepository.save(
                    new ReservationTime(LocalTime.of(12, 40)));

            Waiting waiting = waitingJpaRepository.save(new Waiting(
                    LocalDateTime.now(),
                    new Reservation(
                            LocalDate.now().plusDays(1),
                            reservationTime,
                            theme,
                            owner,
                            LocalDate.now()
                    )
            ));

            Waiting anotherWaiting = waitingJpaRepository.save(new Waiting(
                    LocalDateTime.now(),
                    new Reservation(
                            LocalDate.now().plusDays(1),
                            anotherReservationTime,
                            theme,
                            anotherOwner,
                            LocalDate.now()
                    )
            ));

            // when
            List<MemberWaitingResponseDto> myWaitings = waitingService.getMyWaitings(loginMember);

            // then
            List<Long> waitingIds = myWaitings.stream()
                    .map(MemberWaitingResponseDto::id)
                    .toList();

            assertThat(waitingIds).doesNotContain(anotherWaiting.getId());
        }


        @Test
        @DisplayName("알맞은 순서를 반환한다")
        void test2() {
            // given
            Member owner = memberJpaRepository.save(new Member("이름", "email@gmail.com", "password", Role.ADMIN));
            Member anotherOwner = memberJpaRepository.save(
                    new Member("주인아님", "anotherOwner@gmail.com", "password", Role.ADMIN));
            LoginMember loginMember = new LoginMember(owner);

            Theme theme = themeJpaRepository.save(new Theme("새로운 테마", "새로운 설명", "썸네일"));
            ReservationTime reservationTime = reservationTimeJpaRepository.save(
                    new ReservationTime(LocalTime.of(12, 30)));

            Waiting firstWaiting = waitingJpaRepository.save(new Waiting(
                    LocalDateTime.now(),
                    new Reservation(
                            LocalDate.now().plusDays(1),
                            reservationTime,
                            theme,
                            anotherOwner,
                            LocalDate.now()
                    )
            ));

            Waiting secondWaiting = waitingJpaRepository.save(new Waiting(
                    LocalDateTime.now(),
                    new Reservation(
                            LocalDate.now().plusDays(1),
                            reservationTime,
                            theme,
                            owner,
                            LocalDate.now()
                    )
            ));

            // when
            List<MemberWaitingResponseDto> myWaitings = waitingService.getMyWaitings(loginMember);

            // then
            assertThat(myWaitings.getFirst().order()).isEqualTo(2);
        }

    }
}
