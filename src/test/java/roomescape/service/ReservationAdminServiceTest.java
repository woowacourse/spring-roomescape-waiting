package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.common.exception.NotFoundException;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationAdminRegisterDto;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest
class ReservationAdminServiceTest {

    @Autowired
    private ReservationAdminService reservationAdminService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAllInBatch();
        reservationTimeRepository.deleteAllInBatch();
        themeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("관리자는 user의 예약을 할 수 있다.")
    @Test
    void saveReservation() {
        //given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(21, 0));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("test", "test", "test");
        Theme savedTheme = themeRepository.save(theme);

        Member member = new Member("도기", "test@test.com", "1234", Role.USER);
        Member savedMember = memberRepository.save(member);

        ReservationAdminRegisterDto reservationAdminRegisterDto = new ReservationAdminRegisterDto(
                LocalDate.now().plusDays(1),
                savedTheme.getId(),
                savedReservationTime.getId(),
                savedMember.getId()
        );

        //when
        reservationAdminService.saveReservation(reservationAdminRegisterDto);

        //then
        List<Reservation> actual = reservationRepository.findAll();
        assertThat(actual).hasSize(1);
    }

    @DisplayName("예약을 생성할 때 요청한 테마가 존재하지 않으면 예외가 발생한다.")
    @Test
    void saveReservationNonTheme() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(21, 0));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("test", "test", "test");
        Theme savedTheme = themeRepository.save(theme);

        Member member = new Member("도기", "test@test.com", "1234", Role.USER);
        Member savedMember = memberRepository.save(member);

        ReservationAdminRegisterDto reservationAdminRegisterDto = new ReservationAdminRegisterDto(
                LocalDate.now().plusDays(1),
                99L,
                savedReservationTime.getId(),
                savedMember.getId()
        );

        //when //then
        assertThatThrownBy(() -> reservationAdminService.saveReservation(reservationAdminRegisterDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("해당 id 를 가진 테마는 존재하지 않습니다.");
    }

    @DisplayName("예약을 생성할 때 요청한 시간이 존재하지 않으면 예외가 발생한다.")
    @Test
    void saveReservationNoneTime() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(21, 0));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("test", "test", "test");
        Theme savedTheme = themeRepository.save(theme);

        Member member = new Member("도기", "test@test.com", "1234", Role.USER);
        Member savedMember = memberRepository.save(member);

        ReservationAdminRegisterDto reservationAdminRegisterDto = new ReservationAdminRegisterDto(
                LocalDate.now().plusDays(1),
                savedTheme.getId(),
                99L,
                savedMember.getId()
        );

        //when //then
        assertThatThrownBy(() -> reservationAdminService.saveReservation(reservationAdminRegisterDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("해당 id 를 가진 예약 시각은 존재하지 않습니다.");
    }

    @DisplayName("예약을 생성할 때 요청한 회원이 존재하지 않으면 예외가 발생한다.")
    @Test
    void saveReservationNoneMember() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(21, 0));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("test", "test", "test");
        Theme savedTheme = themeRepository.save(theme);

        Member member = new Member("도기", "test@test.com", "1234", Role.USER);
        Member savedMember = memberRepository.save(member);

        ReservationAdminRegisterDto reservationAdminRegisterDto = new ReservationAdminRegisterDto(
                LocalDate.now().plusDays(1),
                savedTheme.getId(),
                savedReservationTime.getId(),
                99L
        );

        //when //then
        assertThatThrownBy(() -> reservationAdminService.saveReservation(reservationAdminRegisterDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("해당 id 를 가진 회원은 존재하지 않습니다.");
    }

}
