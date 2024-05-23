package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.util.Fixture.HORROR_DESCRIPTION;
import static roomescape.util.Fixture.HORROR_THEME_NAME;
import static roomescape.util.Fixture.HOUR_10;
import static roomescape.util.Fixture.HOUR_11;
import static roomescape.util.Fixture.KAKI_EMAIL;
import static roomescape.util.Fixture.KAKI_NAME;
import static roomescape.util.Fixture.KAKI_PASSWORD;
import static roomescape.util.Fixture.THUMBNAIL;
import static roomescape.util.Fixture.TODAY;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.config.DatabaseCleaner;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Description;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;
import roomescape.reservation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationTimeServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
    }

    @DisplayName("예약 시간 아이디로 조회 시 존재하지 않는 아이디면 예외가 발생한다.")
    @Test
    void findByIdExceptionTest() {
        assertThatThrownBy(() -> reservationTimeService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 가능한 시간을 조회한다.")
    @Test
    void findAvailableTimesTest() {
        Theme theme = themeRepository.save(
                new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        ReservationTime hour10 = reservationTimeRepository.save(new ReservationTime(HOUR_10));
        ReservationTime hour11 = reservationTimeRepository.save(new ReservationTime(HOUR_11));

        Member member = memberRepository.save(Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        Reservation reservation = reservationRepository.save(new Reservation(member, TODAY, theme, hour10, ReservationStatus.SUCCESS));

        List<AvailableReservationTimeResponse> availableTimes = reservationTimeService.findAvailableTimes(
                reservation.getDate(),
                theme.getId()
        );

        assertThat(availableTimes).containsExactly(
                AvailableReservationTimeResponse.toResponse(hour10, true),
                AvailableReservationTimeResponse.toResponse(hour11, false)
        );
    }

    @DisplayName("이미 해당 시간으로 예약 되있을 경우 삭제 시 예외가 발생한다.")
    @Test
    void deleteExceptionTest() {
        Theme theme = themeRepository.save(
                new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        ReservationTime hour10 = reservationTimeRepository.save(new ReservationTime(HOUR_10));

        Member member = memberRepository.save(Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        reservationRepository.save(new Reservation(member, TODAY, theme, hour10, ReservationStatus.SUCCESS));

        assertThatThrownBy(() -> reservationTimeService.delete(hour10.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
