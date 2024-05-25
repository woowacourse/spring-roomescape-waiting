package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.request.AdminReservationRequest;
import roomescape.controller.request.ReservationRequest;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.*;
import static roomescape.model.Role.ADMIN;
import static roomescape.model.Role.MEMBER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/test_data.sql")
class ReservationServiceTest {

    @Autowired
    private final ThemeRepository themeRepository;
    @Autowired
    private final ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private final ReservationRepository reservationRepository;
    @Autowired
    private final MemberRepository memberRepository;
    @Autowired
    private final ReservationService reservationService;

    @Autowired
    public ReservationServiceTest(ThemeRepository themeRepository, ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository, MemberRepository memberRepository,
                                  ReservationService reservationService) {
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.reservationService = reservationService;
    }

    @DisplayName("모든 예약 시간을 반환한다")
    @Test
    void should_return_all_reservation_times() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        themeRepository.save(new Theme(2L, "name2", "description2", "thumbnail2"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, now(), reservationTime, theme1, member));
        reservationRepository.save(new Reservation(2L, now(), reservationTime, theme2, member));

        List<Reservation> reservations = reservationService.findAllReservations();

        assertThat(reservations).hasSize(2);
    }

    @DisplayName("검색 조건에 맞는 예약을 반환한다.")
    @Test
    void should_return_filtered_reservation() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        Theme theme = themeRepository.findById(1L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, now(), reservationTime, theme, member));
        reservationRepository.save(new Reservation(2L, now().plusDays(1), reservationTime, theme, member));

        List<Reservation> reservations = reservationService
                .filterReservation(1L, 1L, now().minusDays(1), now().plusDays(3));

        assertThat(reservations).hasSize(2);
    }

    @DisplayName("사용자가 예약 시간을 추가한다")
    @Test
    void should_add_reservation_times_when_give_member_request() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        ReservationRequest request = new ReservationRequest(now().plusDays(2), 1L, 1L);

        reservationService.addReservation(request, member);

        List<Reservation> allReservations = reservationRepository.findAll();
        assertThat(allReservations).hasSize(1);
    }

    @DisplayName("관리자가 예약 시간을 추가한다")
    @Test
    void should_add_reservation_times_when_give_admin_request() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        memberRepository.save(new Member(1L, "썬", ADMIN, "sun@email.com", "1111"));
        AdminReservationRequest request =
                new AdminReservationRequest(now().plusDays(2), 1L, 1L, 1L);
        reservationService.addReservation(request);

        List<Reservation> allReservations = reservationRepository.findAll();
        assertThat(allReservations).hasSize(1);
    }

    @DisplayName("관리자가 예약 시간을 추가할 때 사용자가 없으면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_member() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        AdminReservationRequest request =
                new AdminReservationRequest(now().plusDays(2), 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservation(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 아이디가 1인 사용자가 존재하지 않습니다.");
    }

    @DisplayName("예약 시간을 삭제한다")
    @Test
    void should_remove_reservation_times() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        themeRepository.save(new Theme(2L, "name2", "description2", "thumbnail2"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, now(), reservationTime, theme1, member));
        reservationRepository.save(new Reservation(2L, now(), reservationTime, theme2, member));

        reservationService.deleteReservation(1L);

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("존재하지 않는 예약을 삭제하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_reservation() {
        assertThatThrownBy(() -> reservationService.deleteReservation(1000000))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 해당 id:[1000000] 값으로 예약된 내역이 존재하지 않습니다.");
    }

    @DisplayName("존재하는 예약을 삭제하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_reservation() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        themeRepository.save(new Theme(2L, "name2", "description2", "thumbnail2"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, now(), reservationTime, theme1, member));
        reservationRepository.save(new Reservation(2L, now(), reservationTime, theme2, member));

        assertThatCode(() -> reservationService.deleteReservation(1))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재 이전으로 예약하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_previous_date() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        ReservationRequest request =
                new ReservationRequest(LocalDate.now().minusDays(1), 1L, 1L);
        Member member = new Member(1L, "썬", MEMBER, "sun@email.com", "1111");

        assertThatThrownBy(() -> reservationService.addReservation(request, member))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("[ERROR] 현재(", ") 이전 시간으로 예약할 수 없습니다.");
    }

    @DisplayName("현재 이후로 예약하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_later_date() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();
        reservationTimeRepository.save(new ReservationTime(LocalTime.now()));

        ReservationRequest request =
                new ReservationRequest(LocalDate.now().plusDays(2), 1L, 1L);

        assertThatCode(() -> reservationService.addReservation(request, member))
                .doesNotThrowAnyException();
    }

    @DisplayName("날짜, 시간, 테마가 일치하는 예약을 추가하려 할 때 예외가 발생한다.")
    @Test
    void should_throw_exception_when_add_exist_reservation() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        themeRepository.save(new Theme(2L, "name2", "description2", "thumbnail2"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        LocalDate date = now().plusDays(2);

        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, date, reservationTime, theme1, member));
        reservationRepository.save(new Reservation(2L, now(), reservationTime, theme2, member));

        ReservationRequest request = new ReservationRequest(date, 1L, 1L);
        assertThatThrownBy(() -> reservationService.addReservation(request, member))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 이미 해당 시간에 예약이 존재합니다.");
    }

    @DisplayName("사용자가 예약한 예약을 반환한다.")
    @Test
    void should_return_member_reservations() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        Theme theme = themeRepository.findById(1L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, now(), reservationTime, theme, member));
        reservationRepository.save(new Reservation(2L, now().plusDays(1), reservationTime, theme, member));

        List<Reservation> reservations = reservationService
                .findMemberReservations(member.getId());

        assertThat(reservations).hasSize(2);
    }

    @DisplayName("주어진 아이디에 맞는 예약을 반환한다.")
    @Test
    void should_return_reservation_when_given_id() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        Theme theme = themeRepository.findById(1L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, now(), reservationTime, theme, member));
        reservationRepository.save(new Reservation(2L, now().plusDays(1), reservationTime, theme, member));

        Reservation reservation = reservationService.findById(1L);
        assertThat(reservation.getId()).isEqualTo(1);
    }
}
