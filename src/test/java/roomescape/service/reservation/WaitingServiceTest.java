package roomescape.service.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.*;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.ReservationTimeRepository;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.ForbiddenException;
import roomescape.exception.InvalidReservationException;
import roomescape.service.reservation.dto.ReservationResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql("/truncate-with-time-and-theme.sql")
class WaitingServiceTest {
    @Autowired
    private WaitingService waitingService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationDetailRepository reservationDetailRepository;
    private ReservationDetail reservationDetail;
    private Theme theme;
    private Member member;
    private Member anotherMember;

    @BeforeEach
    void setUp() {
        ReservationDate reservationDate = ReservationDate.of(LocalDate.now().plusDays(1));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
        member = memberRepository.save(new Member("lini", "lini@email.com", "lini123", Role.GUEST));
        anotherMember = memberRepository.save(new Member("pedro", "pedro@email.com", "pedro123", Role.GUEST));
        reservationDetail = reservationDetailRepository.save(new ReservationDetail(new Schedule(reservationDate, reservationTime), theme));
    }

    @DisplayName("모든 예약 대기 내역을 조회한다.")
    @Test
    @Sql({"/truncate-with-time-and-theme.sql", "/insert-past-waiting.sql"})
    void findAllWaitings() {
        //when
        List<ReservationResponse> reservations = waitingService.findAll();

        //then
        assertThat(reservations).hasSize(1);
    }


    @DisplayName("사용자가 예약 대기를 삭제하려고 할 때 예약으로 바뀌었다면 예외가 발생한다.")
    @Test
    void cannotDeleteWaitingByIdIfReserved() {
        //given
        Reservation reservation = new Reservation(member, reservationDetail, ReservationStatus.RESERVED);
        Reservation target = reservationRepository.save(reservation);

        //when
        assertThatThrownBy(() -> waitingService.deleteWaitingById(target.getId(), member.getId()))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("예약은 삭제할 수 없습니다. 관리자에게 문의해주세요.");
    }

    @DisplayName("사용자가 본인 외 예약 대기를 삭제하려고 하면 예외가 발생한다.")
    @Test
    void cannotDeleteWaitingByIdIfNotOwner() {
        //given
        Reservation reservation = new Reservation(member, reservationDetail, ReservationStatus.WAITING);
        Reservation target = reservationRepository.save(reservation);

        //when
        assertThatThrownBy(() -> waitingService.deleteWaitingById(target.getId(), anotherMember.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("예약 대기를 삭제할 권한이 없습니다.");
    }
}
