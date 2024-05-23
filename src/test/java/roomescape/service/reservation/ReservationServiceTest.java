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
import roomescape.exception.InvalidReservationException;
import roomescape.service.reservation.dto.AdminReservationRequest;
import roomescape.service.reservation.dto.ReservationFilterRequest;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql("/truncate-with-time-and-theme.sql")
class ReservationServiceTest {
    @Autowired
    private ReservationService reservationService;
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
    private Member admin;
    private Member member;
    private Member anotherMember;

    @BeforeEach
    void setUp() {
        ReservationDate reservationDate = ReservationDate.of(LocalDate.now().plusDays(1));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
        admin = memberRepository.save(new Member("admin", "admin@email.com", "admin123", Role.ADMIN));
        member = memberRepository.save(new Member("lini", "lini@email.com", "lini123", Role.GUEST));
        anotherMember = memberRepository.save(new Member("pedro", "pedro@email.com", "pedro123", Role.GUEST));
        reservationDetail = reservationDetailRepository.save(new ReservationDetail(new Schedule(reservationDate, reservationTime), theme));
    }

    @DisplayName("어드민이 새로운 예약을 저장한다.")
    @Test
    void createAdminReservation() {
        //given
        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(reservationDetail.getDate(), admin.getId(),
                reservationDetail.getReservationTime().getId(), theme.getId());

        //when
        ReservationResponse result = reservationService.createAdminReservation(adminReservationRequest);

        //then
        assertAll(
                () -> assertThat(result.id()).isNotZero(),
                () -> assertThat(result.time().id()).isEqualTo(reservationDetail.getReservationTime().getId()),
                () -> assertThat(result.theme().id()).isEqualTo(theme.getId()),
                () -> assertThat(result.status()).isEqualTo(ReservationStatus.RESERVED.getDescription())
        );
    }

    @DisplayName("사용자가 새로운 예약을 저장한다.")
    @Test
    void createMemberReservation() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(reservationDetail.getDate(),
                reservationDetail.getReservationTime().getId(), theme.getId());

        //when
        ReservationResponse result = reservationService.createMemberReservation(reservationRequest, member.getId());

        //then
        assertAll(
                () -> assertThat(result.id()).isNotZero(),
                () -> assertThat(result.time().id()).isEqualTo(reservationDetail.getReservationTime().getId()),
                () -> assertThat(result.theme().id()).isEqualTo(theme.getId()),
                () -> assertThat(result.status()).isEqualTo(ReservationStatus.RESERVED.getDescription())
        );
    }

    @DisplayName("사용자가 새로운 예약 대기를 저장한다.")
    @Test
    void createMemberWaiting() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(reservationDetail.getDate(),
                reservationDetail.getReservationTime().getId(), theme.getId());
        reservationService.createMemberReservation(reservationRequest, member.getId());

        ReservationRequest anotherReservationRequest = new ReservationRequest(reservationDetail.getDate(),
                reservationDetail.getReservationTime().getId(), theme.getId());

        //when
        ReservationResponse result = reservationService.createMemberReservation(anotherReservationRequest, anotherMember.getId());

        //then
        assertAll(
                () -> assertThat(result.id()).isNotZero(),
                () -> assertThat(result.time().id()).isEqualTo(reservationDetail.getReservationTime().getId()),
                () -> assertThat(result.theme().id()).isEqualTo(theme.getId()),
                () -> assertThat(result.status()).isEqualTo(ReservationStatus.WAITING.getDescription())
        );
    }

    @DisplayName("사용자가 이미 예약인 상태에서 예약 요청을 한다면 예외가 발생한다.")
    @Test
    void cannotCreateByExistingMemberReservation() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(reservationDetail.getDate(),
                reservationDetail.getReservationTime().getId(), theme.getId());
        reservationService.createMemberReservation(reservationRequest, member.getId());

        //when & then
        assertThatThrownBy(() -> reservationService.createMemberReservation(reservationRequest, member.getId()))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("이미 예약(대기) 상태입니다.");
    }

    @DisplayName("사용자가 이미 예약 대기인 상태에서 예약 요청을 한다면 예외가 발생한다.")
    @Test
    void cannotCreateByExistingMemberWaiting() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(reservationDetail.getDate(),
                reservationDetail.getReservationTime().getId(), theme.getId());
        reservationService.createMemberReservation(reservationRequest, member.getId());
        ReservationRequest anotherReservationRequest = new ReservationRequest(reservationDetail.getDate(),
                reservationDetail.getReservationTime().getId(), theme.getId());
        reservationService.createMemberReservation(anotherReservationRequest, anotherMember.getId());

        //when & then
        assertThatThrownBy(() -> reservationService.createMemberReservation(reservationRequest, anotherMember.getId()))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("이미 예약(대기) 상태입니다.");
    }

    @DisplayName("모든 예약 내역을 조회한다.")
    @Test
    @Sql({"/truncate-with-time-and-theme.sql", "/insert-past-reservation.sql"})
    void findAllReservations() {
        //when
        List<ReservationResponse> reservations = reservationService.findAll();

        //then
        assertThat(reservations).hasSize(3);
    }

    @DisplayName("사용자 조건으로 예약 내역을 조회한다.")
    @Test
    void findByMember() {
        //given
        Reservation reservation = new Reservation(member, reservationDetail, ReservationStatus.RESERVED);
        reservationRepository.save(reservation);
        ReservationFilterRequest reservationFilterRequest = new ReservationFilterRequest(member.getId(), null, null,
                null);

        //when
        List<ReservationResponse> reservations = reservationService.findByCondition(reservationFilterRequest);

        //then
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("사용자와 테마 조건으로 예약 내역을 조회한다.")
    @Test
    void findByMemberAndTheme() {
        //given
        Reservation reservation = new Reservation(member, reservationDetail, ReservationStatus.RESERVED);
        reservationRepository.save(reservation);
        long notMemberThemeId = theme.getId() + 1;
        ReservationFilterRequest reservationFilterRequest = new ReservationFilterRequest(member.getId(),
                notMemberThemeId, null, null);

        //when
        List<ReservationResponse> reservations = reservationService.findByCondition(reservationFilterRequest);

        //then
        assertThat(reservations).isEmpty();
    }

    @DisplayName("관리자가 id로 예약을 삭제한다.")
    @Test
    void deleteReservationById() {
        //given
        Reservation reservation = new Reservation(admin, reservationDetail, ReservationStatus.RESERVED);
        Reservation target = reservationRepository.save(reservation);

        //when
        reservationService.deleteById(target.getId());

        //then
        assertThat(reservationService.findAll()).isEmpty();
    }

    @DisplayName("관리자가 예약을 삭제하고, 예약 대기가 있다면 가장 우선순위가 높은 예약 대기를 예약으로 전환한다.")
    @Test
    void deleteThenUpdateReservation() {
        //given
        Reservation reservation = new Reservation(admin, reservationDetail, ReservationStatus.RESERVED);
        Reservation reservation2 = new Reservation(member, reservationDetail, ReservationStatus.WAITING);
        Reservation reservation3 = new Reservation(anotherMember, reservationDetail, ReservationStatus.WAITING);
        reservationRepository.save(reservation);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);

        //when
        reservationService.deleteById(reservation.getId());

        //then
        List<ReservationWithRank> reservations = reservationRepository.findWithRankingByMemberId(member.getId());
        assertThat(reservations.get(0).getReservation().isReserved()).isTrue();
    }

    @DisplayName("관리자가 과거 예약을 삭제하려고 하면 예외가 발생한다.")
    @Test
    @Sql({"/truncate.sql", "/insert-past-reservation.sql"})
    void cannotDeleteReservationByIdIfPast() {
        //given
        long id = 1;

        //when & then
        assertThatThrownBy(() -> reservationService.deleteById(id))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("이미 지난 예약은 삭제할 수 없습니다.");
    }

    @DisplayName("존재하지 않는 시간으로 예약을 추가하면 예외를 발생시킨다.")
    @Test
    void cannotCreateByUnknownTime() {
        //given
        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(reservationDetail.getDate(), member.getId(), 0L,
                theme.getId());

        //when & then
        assertThatThrownBy(() -> reservationService.createAdminReservation(adminReservationRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("더이상 존재하지 않는 시간입니다.");
    }

    @DisplayName("존재하지 않는 테마로 예약을 추가하면 예외를 발생시킨다.")
    @Test
    void cannotCreateByUnknownTheme() {
        //given
        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(reservationDetail.getDate(), member.getId(),
                reservationDetail.getReservationTime().getId(), 0L);

        //when & then
        assertThatThrownBy(() -> reservationService.createAdminReservation(adminReservationRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("더이상 존재하지 않는 테마입니다.");
    }
}
