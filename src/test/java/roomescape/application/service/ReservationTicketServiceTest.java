package roomescape.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.common.exception.DuplicatedException;
import roomescape.dto.LoginMember;
import roomescape.dto.request.ReservationTicketRegisterDto;
import roomescape.dto.response.MemberReservationResponseDto;
import roomescape.dto.response.ReservationTicketResponseDto;
import roomescape.infrastructure.db.MemberJpaRepository;
import roomescape.infrastructure.db.ThemeJpaRepository;
import roomescape.infrastructure.db.WaitingJpaRepository;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTicket;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;
import roomescape.model.Waiting;
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationTicketRepository;
import roomescape.persistence.repository.ReservationTimeRepository;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTicketServiceTest {

    @Autowired
    ReservationTicketService reservationTicketService;

    @Autowired
    ReservationTicketRepository reservationTicketRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Autowired
    WaitingJpaRepository waitingJpaRepository;

    private LoginMember loginMember;

    @Autowired
    private ThemeJpaRepository themeJpaRepository;

    @BeforeEach
    void setUp() {
        this.loginMember = new LoginMember(1L, "히로", "example@gmail.com", Role.ADMIN);
    }

    @DisplayName("예약을 정상적으로 저장한다.")
    @Test
    void test1() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ReservationTicketRegisterDto request = new ReservationTicketRegisterDto(
                tomorrow.toString(),
                1L,
                1L
        );

        // when
        ReservationTicketResponseDto response = this.reservationTicketService.saveReservation(request,
                this.loginMember);

        // then
        assertAll(
                () -> assertThat(response.member().name()).isEqualTo("히로"),
                () -> assertThat(response.date()).isEqualTo(tomorrow)
        );
    }

    @DisplayName("예약을 취소한다")
    @Test
    void test3() {
        // given
        ReservationTicketRegisterDto request = new ReservationTicketRegisterDto(
                LocalDate.now().plusDays(1).toString(), 1L, 1L);
        ReservationTicketResponseDto saved = this.reservationTicketService.saveReservation(request, this.loginMember);

        // when
        this.reservationTicketService.cancelReservation(saved.id());

        // then
        List<ReservationTicketResponseDto> reservations = this.reservationTicketService.getAllReservations();
        assertThat(reservations).isEmpty();
    }

    @DisplayName("이미 존재하는 예약 시간에 예약한다면 예외를 던진다")
    @Test
    void test4() {
        // given
        ReservationTicketRegisterDto request = new ReservationTicketRegisterDto(
                LocalDate.now().plusDays(1).toString(), 1L, 1L);
        this.reservationTicketService.saveReservation(request, this.loginMember);
        ReservationTicketRegisterDto savedRequest = new ReservationTicketRegisterDto(
                LocalDate.now().plusDays(1).toString(), 1L, 1L);

        // when && then
        assertThatThrownBy(
                () -> this.reservationTicketService.saveReservation(savedRequest, this.loginMember))
                .isInstanceOf(DuplicatedException.class);
    }

    @DisplayName("당일 예약을 한다면 예외를 던진다")
    @Test
    void test5() {
        // given
        ReservationTicketRegisterDto request = new ReservationTicketRegisterDto(
                LocalDate.now().toString(), 1L, 1L);
        // when && then
        assertThatThrownBy(
                () -> this.reservationTicketService.saveReservation(request, this.loginMember))
                .isInstanceOf(IllegalStateException.class);

    }

    @DisplayName("사용자가 예약한 예약 내역을 모두 가져온다")
    @Test
    void test6() {
        //given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 30));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("테마", "공포", "image");
        Theme savedTheme = themeJpaRepository.save(theme);

        Member member = new Member("도기", "email@gamil.com", "password", Role.ADMIN);
        Member savedMember = memberJpaRepository.save(member);

        ReservationTicket reservationTicket = new ReservationTicket(
                new Reservation(LocalDate.now().plusDays(1), savedReservationTime, savedTheme,
                        savedMember, LocalDate.now()));
        ReservationTicket savedReservationTicket = reservationTicketRepository.save(reservationTicket);

        LoginMember loginMember = new LoginMember(savedMember);

        //when
        List<MemberReservationResponseDto> response = reservationTicketService.getReservationsOfMember(loginMember);

        List<MemberReservationResponseDto> comparedResponse = List.of(
                new MemberReservationResponseDto(savedReservationTicket));

        //then
        assertAll(
                () -> assertThat(response).hasSize(1),
                () -> assertThat(response).isEqualTo(comparedResponse)
        );
    }

    @DisplayName("사용자의 예약 내역 삭제 시에 가장 높은 우선순위의 웨이팅을 예약으로 전환해 저장한다")
    @Test
    void test7() {
        //given
        String emailOfAdministrator = "email@gmail.com";
        Member administrator = memberJpaRepository.save(new Member("이름", emailOfAdministrator, "password", Role.ADMIN));
        Member user = memberJpaRepository.save(
                new Member("사용자", "user@gmail.com", "password", Role.USER));

        Theme theme = themeJpaRepository.save(new Theme("새로운 테마", "새로운 설명", "썸네일"));
        ReservationTime reservationTime = reservationTimeRepository.save(
                new ReservationTime(LocalTime.of(12, 30)));

        Waiting firstWaiting = waitingJpaRepository.save(new Waiting(
                LocalDateTime.now(),
                new Reservation(
                        LocalDate.now().plusDays(1),
                        reservationTime,
                        theme,
                        user,
                        LocalDate.now()
                )
        ));

        Waiting secondWaiting = waitingJpaRepository.save(new Waiting(
                LocalDateTime.now().plusHours(1),
                new Reservation(
                        LocalDate.now().plusDays(1),
                        reservationTime,
                        theme,
                        user,
                        LocalDate.now()
                )
        ));

        ReservationTicket reservationTicket = reservationTicketRepository.save(new ReservationTicket(new Reservation(
                LocalDate.now().plusDays(1),
                reservationTime,
                theme,
                user,
                LocalDate.now()
        )));

        // when
        reservationTicketService.cancelReservation(reservationTicket.getId());

        // then
        List<ReservationTicket> allReservationTickets = reservationTicketRepository.findAll();
        Optional<ReservationTicket> foundReservation = allReservationTickets.stream()
                .filter(reservation1 -> reservation1.getReservationTime().getId().equals(reservationTime.getId()))
                .filter(reservation1 -> reservation1.getTheme().getId().equals(theme.getId()))
                .filter(reservation1 -> reservation1.getMember().getId().equals(user.getId()))
                .findAny();

        assertAll(
                () -> assertThat(waitingJpaRepository.findAll()).doesNotContain(firstWaiting),
                () -> assertThat(allReservationTickets).doesNotContain(reservationTicket),
                () -> assertThat(foundReservation).isPresent()
        );
    }
}
