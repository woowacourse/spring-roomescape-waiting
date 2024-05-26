package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.MEMBER_ARU;
import static roomescape.fixture.MemberFixture.MEMBER_PK;
import static roomescape.fixture.ThemeFixture.TEST_THEME;
import static roomescape.fixture.TimeFixture.TWELVE_PM;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.application.ServiceTest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.application.reservation.dto.response.ReservationStatusResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.fixture.ReservationFixture;

@ServiceTest
@Import(ReservationFixture.class)
class ReservationLookupServiceTest {

    @Autowired
    private ReservationLookupService reservationLookupService;

    @Autowired
    private ReservationFixture reservationFixture;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("모든 예약을 조회한다.")
    void shouldReturnReservationResponsesWhenReservationsExist() {
        reservationFixture.saveReservation();
        List<ReservationResponse> reservationResponses = reservationLookupService.findAllBookedReservations();
        assertThat(reservationResponses).hasSize(1);
    }

    @Test
    @DisplayName("한 사람의 예약 및 대기 정보를 조회한다.")
    void lookupReservationStatus() {
        Member aru = memberRepository.save(MEMBER_ARU.create());
        Member pk = memberRepository.save(MEMBER_PK.create());
        ReservationTime time = reservationTimeRepository.save(TWELVE_PM.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        LocalDate date = LocalDate.of(2024, 5, 21);
        LocalDateTime createdAt = LocalDateTime.of(1999, 1, 1, 12, 0);

        reservationRepository.save(
                new Reservation(aru, theme, date, time, createdAt, BookStatus.BOOKED)
        );
        reservationRepository.save(
                new Reservation(pk, theme, date, time, createdAt.plusMinutes(1), BookStatus.WAITING)
        );
        reservationRepository.save(
                new Reservation(pk, theme, date.plusDays(1), time, createdAt.plusDays(1), BookStatus.BOOKED)
        );

        List<ReservationStatusResponse> responses =
                reservationLookupService.getReservationStatusesByMemberId(pk.getId());

        assertThat(responses)
                .extracting(ReservationStatusResponse::waitingCount)
                .containsExactly(1L, 0L);
    }
}
