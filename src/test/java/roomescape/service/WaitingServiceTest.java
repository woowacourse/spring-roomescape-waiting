package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.NotFoundException;
import roomescape.dto.LoginMember;
import roomescape.dto.request.WaitingRequest;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;
import roomescape.model.Waiting;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@SpringBootTest
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        waitingRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        reservationTimeRepository.deleteAllInBatch();
        themeRepository.deleteAllInBatch();
    }

    @DisplayName("예약 대기를 저장할 수 있다.")
    @Test
    void register() {
        //given
        Reservation reservation = createReservation();

        WaitingRequest request = new WaitingRequest(
                reservation.getDate(),
                reservation.getTheme().getId(),
                reservation.getReservationTime().getId()
        );

        LoginMember loginMember = new LoginMember(reservation.getMember());

        //when
        Long actual = waitingService.register(request, loginMember);

        //then
        assertThat(actual).isNotNull();
    }

    @DisplayName("같은 사용자에 대해서 중복된 예약 대기인 경우 예외가 발생한다.")
    @Test
    void duplicate() {
        //given
        Reservation reservation = createReservation();

        LoginMember loginMember = new LoginMember(reservation.getMember());

        WaitingRequest request = new WaitingRequest(
                reservation.getDate(),
                reservation.getTheme().getId(),
                reservation.getReservationTime().getId()
        );

        Waiting waiting = Waiting.of(
                reservation.getDate(),
                reservation.getTheme(),
                reservation.getReservationTime(),
                reservation.getMember()
        );

        waitingRepository.save(waiting);

        //when //then
        assertThatThrownBy(() -> waitingService.register(request, loginMember))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("이미 대기 중인 예약입니다.");
    }

    @DisplayName("다른 사용자의 경우 동일한 예약에 대해서 대기가 가능하다")
    @Test
    void otherMemberRegister() {
        //given
        Reservation reservation = createReservation();

        WaitingRequest request = new WaitingRequest(
                reservation.getDate(),
                reservation.getTheme().getId(),
                reservation.getReservationTime().getId()
        );

        Waiting waiting = Waiting.of(
                reservation.getDate(),
                reservation.getTheme(),
                reservation.getReservationTime(),
                reservation.getMember()
        );
        waitingRepository.save(waiting);

        Member savedOtherMember = memberRepository.save(
                new Member("test2", "test2", "test2", Role.USER));

        LoginMember loginMember = new LoginMember(savedOtherMember);

        //when
        Long actual = waitingService.register(request, loginMember);

        //then
        assertThat(actual).isNotNull();
    }

    @DisplayName("대기 신청을 취소할 수 있다.")
    @Test
    void cancel() {
        //given
        Reservation reservation = createReservation();

        WaitingRequest request = new WaitingRequest(
                reservation.getDate(),
                reservation.getTheme().getId(),
                reservation.getReservationTime().getId()
        );

        LoginMember loginMember = new LoginMember(reservation.getMember());

        Long savedId = waitingService.register(request, loginMember);

        //when
        waitingService.cancel(savedId);

        //then
        List<Waiting> actual = waitingRepository.findAll();
        assertThat(actual).hasSize(0);
    }

    @DisplayName("취소하려는 대기가 존재하지 않는 경우 예외가 발생한다.")
    @Test
    void nonCancel() {
        //given
        Long savedId = 1L;

        //when //then
        assertThatThrownBy(() -> waitingService.cancel(savedId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("대기 신청이 존재하지 않습니다.");
    }

    private Reservation createReservation() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        Member member = new Member("test", "test", "test", Role.ADMIN);
        memberRepository.save(member);

        LocalDate today = LocalDate.of(2025, 1, 1);

        LocalDate reservationDate = LocalDate.now().plusDays(1);

        Reservation reservation = new Reservation(
                reservationDate,
                reservationTime,
                theme,
                member,
                today
        );
        return reservationRepository.save(reservation);
    }

}
