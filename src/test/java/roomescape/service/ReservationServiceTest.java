package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.common.exception.DuplicatedException;
import roomescape.domain.LoginMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;
import roomescape.dto.request.ReservationRegisterDto;
import roomescape.dto.response.MemberReservationResponseDto;
import roomescape.dto.response.ReservationResponseDto;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@Slf4j
@SpringBootTest
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    WaitingRepository waitingRepository;

    @Autowired
    ThemeRepository themeRepository;

    @BeforeEach
    void tear() {
        reservationRepository.deleteAllInBatch();
        waitingRepository.deleteAllInBatch();
        reservationTimeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        themeRepository.deleteAllInBatch();
    }

    @DisplayName("예약을 정상적으로 저장한다.")
    @Test
    void test1() {
        // given

        Theme theme = new Theme("test", "test", "test");
        Theme savedTheme = themeRepository.save(theme);

        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(23, 0)));
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ReservationRegisterDto request = new ReservationRegisterDto(
                tomorrow.toString(),
                savedTime.getId(),
                savedTheme.getId()
        );

        LoginMember loginMember = getLoginMember();

        // when
        ReservationResponseDto response = reservationService.saveReservation(request, loginMember);

        // then
        assertAll(
                () -> assertThat(response.member().name()).isEqualTo("도기"),
                () -> assertThat(response.date()).isEqualTo(tomorrow)
        );
    }

    @DisplayName("예약을 취소한다")
    @Test
    void test3() {
        // given
        Reservation savedReservation = createSaveReservation();

        // when
        reservationService.cancelReservation(savedReservation.getId());

        // then
        List<ReservationResponseDto> reservations = reservationService.getAllReservations();
        assertThat(reservations).isEmpty();
    }

    @DisplayName("이미 존재하는 예약 시간에 예약한다면 예외를 던진다")
    @Test
    void test4() {
        // given
        Reservation savedReservation = createSaveReservation();
        LoginMember loginMember = getLoginMember();

        ReservationRegisterDto savedRequest = new ReservationRegisterDto(
                savedReservation.getDate().toString(),
                savedReservation.getReservationTime().getId(),
                savedReservation.getTheme().getId()
        );

        // when && then
        assertThatThrownBy(() -> reservationService.saveReservation(savedRequest, loginMember))
                .isInstanceOf(DuplicatedException.class);
    }

    @DisplayName("당일 예약을 한다면 예외를 던진다")
    @Test
    void test5() {
        // given
        Reservation savedReservation = createSaveReservation();

        ReservationRegisterDto request = new ReservationRegisterDto(
                LocalDate.now().toString(),
                savedReservation.getReservationTime().getId(),
                savedReservation.getTheme().getId()
        );

        LoginMember loginMember = getLoginMember();

        // when && then
        assertThatThrownBy(
                () -> reservationService.saveReservation(request, loginMember))
                .isInstanceOf(IllegalStateException.class);
    }

    @DisplayName("사용자가 예약한 예약과 신청한 대기 내역을 모두 가져온다. 대기 내역은 순위와 함께 반환한다.")
    @Test
    void test6() {
        //given
        Reservation savedReservation = createSaveReservation();
        LoginMember loginMember = LoginMember.from(savedReservation.getMember());

        Waiting savedWaiting = createSaveWaiting(savedReservation);
        WaitingWithRank waitingWithRank = new WaitingWithRank(savedWaiting, 1L);

        //when
        List<MemberReservationResponseDto> response = reservationService.getReservationsOfMember(loginMember);

        /**/
        MemberReservationResponseDto reservationResponse = MemberReservationResponseDto.from(savedReservation);
        MemberReservationResponseDto waitingRanksResponse = MemberReservationResponseDto.from(waitingWithRank);

        //then
        assertThat(response).hasSize(2);
        assertThat(response).contains(reservationResponse, waitingRanksResponse);
    }

    private Waiting createSaveWaiting(final Reservation savedReservation) {
        Waiting waiting = Waiting.of(
                savedReservation.getDate(),
                savedReservation.getTheme(),
                savedReservation.getReservationTime(),
                savedReservation.getMember(),
                LocalDateTime.of(2023, 1, 1, 1, 1, 1)
        );

        return waitingRepository.save(waiting);
    }

    private Reservation createSaveReservation() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 30));
        reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("테마", "공포", "image");
        themeRepository.save(theme);

        Member member = new Member("도기", "email@gamil.com", "password", Role.ADMIN);
        memberRepository.save(member);

        Reservation reservation = new Reservation(
                LocalDate.now().plusDays(1),
                reservationTime,
                theme,
                member,
                LocalDate.now()
        );

        return reservationRepository.save(reservation);
    }

    private LoginMember getLoginMember() {
        Member member = new Member("도기", "test", "test", Role.ADMIN);
        memberRepository.save(member);
        return LoginMember.from(member);
    }


}
