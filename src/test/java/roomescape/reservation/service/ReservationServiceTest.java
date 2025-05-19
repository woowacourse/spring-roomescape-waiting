package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.common.CleanUp;
import roomescape.fixture.MemberDbFixture;
import roomescape.fixture.ReservationDateFixture;
import roomescape.fixture.ReservationDateTimeDbFixture;
import roomescape.fixture.ReservationTimeDbFixture;
import roomescape.fixture.ThemeDbFixture;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.global.exception.NoElementsException;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.theme.domain.Theme;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;
    @Autowired
    private ThemeDbFixture themeDbFixture;
    @Autowired
    private MemberDbFixture memberDbFixture;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationDateTimeDbFixture reservationDateTimeDbFixture;

    @Autowired
    private CleanUp cleanUp;

    @BeforeEach
    void setUp() {
        cleanUp.all();
    }

    @Test
    void 예약을_생성한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.열시();
        Theme theme = themeDbFixture.공포();
        Member reserver = memberDbFixture.유저1_생성();
        LocalDate date = ReservationDateFixture.예약날짜_내일.date();

        ReserveCommand command = new ReserveCommand(
                date,
                theme.getId(),
                reservationTime.getId(),
                reserver.getId()
        );

        // when
        ReservationResponse response = reservationService.reserve(command);

        assertThat(response.id()).isNotNull();
        assertThat(response.member().name()).isEqualTo(reserver.getName());
        assertThat(response.date()).isEqualTo(date);
        assertThat(response.time()).isEqualTo(ReservationTimeResponse.from(reservationTime));
    }

    @Test
    void 예약이_존재하면_예약을_생성할_수_없다() {
        Theme theme = themeDbFixture.공포();
        Member reserver = memberDbFixture.유저1_생성();
        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Reservation reservation = Reservation.reserve(
                reserver, reservationDateTime, theme
        );
        reservationRepository.save(reservation);

        ReserveCommand command = new ReserveCommand(
                reservation.getDate(),
                reservation.getTheme().getId(),
                reservation.getReservationTime().getId(),
                reservation.getReserver().getId()
        );

        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("이미 예약이 존재하는 시간입니다.");
    }

    @Test
    void 예약을_모두_조회한다() {
        Member reserver = memberDbFixture.유저1_생성();
        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Theme theme = themeDbFixture.공포();

        Reservation reservation = reservationRepository.save(Reservation.reserve(reserver, reservationDateTime, theme));

        List<ReservationResponse> responses = reservationService.getFilteredReservations(null, null, null, null);
        ReservationResponse response = responses.get(0);

        assertThat(response.id()).isNotNull();
        assertThat(response.member().name()).isEqualTo(reservation.getReserverName());
        assertThat(response.date()).isEqualTo(reservation.getDate());
        assertThat(response.time()).isEqualTo(ReservationTimeResponse.from(reservation.getReservationTime()));
    }

    @Test
    void 예약을_삭제한다() {
        Member reserver = memberDbFixture.유저1_생성();
        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Theme theme = themeDbFixture.공포();

        Reservation reservation = reservationRepository.save(Reservation.reserve(reserver, reservationDateTime, theme));

        reservationService.deleteById(reservation.getId());

        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @Test
    void 존재하지_않는_예약을_삭제할_수_없다() {
        assertThatThrownBy(() -> reservationService.deleteById(1L))
                .isInstanceOf(NoElementsException.class)
                .hasMessage("예약을 찾을 수 없습니다.");
    }

    @Test
    void 예약_목록을_필터링해서_조회한다() {
        final LocalDate today = LocalDate.now();
        final LocalDate tomorrow = today.plusDays(1);

        Theme theme = themeDbFixture.공포();
        Member member1 = memberDbFixture.유저1_생성();
        Member member2 = memberDbFixture.유저2_생성();
        ReservationDate reservationDate = ReservationDateFixture.예약날짜_내일;
        ReservationTime 열시 = reservationTimeDbFixture.열시();
        ReservationTime 열한시 = reservationTimeDbFixture.열한시();

        Reservation reservation1 = Reservation.reserve(
                member1, ReservationDateTime.create(reservationDate, 열시), theme
        );
        Reservation reservation2 = Reservation.reserve(
                member1, ReservationDateTime.create(reservationDate, 열한시), theme
        );
        Reservation reservation3 = Reservation.reserve(
                member2, ReservationDateTime.create(reservationDate, 열시), theme
        );
        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3));

        // when & then
        // 공포 필터링
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservationService.getFilteredReservations(
                    theme.getId(), null, null, null)
            ).hasSize(3);
            // 사용자1 필터링
            softly.assertThat(reservationService.getFilteredReservations(
                    null, member1.getId(), null, null)
            ).hasSize(2);
            // 오늘 필터링
            softly.assertThat(reservationService.getFilteredReservations
                    (null, null, today, today)
            ).isEmpty();
            // 공포 테마 & 내일 필터링
            softly.assertThat(reservationService.getFilteredReservations(
                    theme.getId(), null, tomorrow, tomorrow)
            ).hasSize(3);
            // 모든 필터 조합
            softly.assertThat(reservationService.getFilteredReservations(
                    theme.getId(), member2.getId(), tomorrow, tomorrow)
            ).hasSize(1);
            // 일치하는 결과가 없는 필터 조합
            softly.assertThat(reservationService.getFilteredReservations(
                    theme.getId(), member2.getId(), today, today)
            ).isEmpty();

            // 모든 결과 조회
            softly.assertThat(reservationService.getFilteredReservations(
                    null, null, null, null)
            ).hasSize(3);
        });
    }

    @Test
    void 내_예약_목록을_조회한다() {
        Member member1 = memberDbFixture.유저1_생성();
        Member member2 = memberDbFixture.유저2_생성();

        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Theme theme = themeDbFixture.공포();

        Reservation reservation1 = reservationRepository.save(Reservation.reserve(member1, reservationDateTime, theme));
        reservationRepository.save(Reservation.reserve(member2, reservationDateTime, theme));

        List<MyReservationResponse> myReservations = reservationService.getMyReservations(member1.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(myReservations).hasSize(1);
            softly.assertThat(myReservations.get(0).reservationId()).isEqualTo(reservation1.getId());
            softly.assertThat(myReservations.get(0).theme()).isEqualTo(reservation1.getTheme().getName());
            softly.assertThat(myReservations.get(0).date()).isEqualTo(reservation1.getDate());
            softly.assertThat(myReservations.get(0).time()).isEqualTo(reservation1.getStartAt());
            softly.assertThat(myReservations.get(0).status()).isEqualTo(reservation1.getStatus().getMessage());
        });
    }
}
