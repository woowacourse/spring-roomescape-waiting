package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.Fixture.MEMBER_1;
import static roomescape.fixture.Fixture.MEMBER_2;
import static roomescape.fixture.Fixture.RESERVATION_TIME_1;
import static roomescape.fixture.Fixture.THEME_1;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.request.ReservationRequest;
import roomescape.application.dto.response.MemberResponse;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.application.dto.response.ReservationTimeResponse;
import roomescape.application.dto.response.ThemeResponse;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRepository;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.ReservationTimeRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;
import roomescape.exception.BadRequestException;

class ReservationServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    private Member member1;
    private Member member2;
    private ReservationTime time1;
    private Theme theme;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(MEMBER_1);
        member2 = memberRepository.save(MEMBER_2);
        theme = themeRepository.save(THEME_1);
        time1 = reservationTimeRepository.save(RESERVATION_TIME_1);
    }

    @Nested
    @DisplayName("예약을 추가하는 경우 ")
    class AddReservation {

        @Test
        @DisplayName("성공한다.")
        void success() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            ReservationRequest request = new ReservationRequest(
                    date,
                    time1.getId(),
                    theme.getId(),
                    member1.getId()
            );
            ReservationResponse response = reservationService.addReservation(request);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.date()).isEqualTo(date);
                softly.assertThat(response.member()).isEqualTo(MemberResponse.from(member1));
                softly.assertThat(response.time()).isEqualTo(ReservationTimeResponse.from(time1));
                softly.assertThat(response.theme()).isEqualTo(ThemeResponse.from(theme));
            });
        }

        @Test
        @DisplayName("이미 해당 날짜/시간의 테마에 예약이 존재하면 예외를 발생시킨다.")
        void failWhenReservationExists() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            ReservationDetail detail = new ReservationDetail(date, time1, theme);
            reservationRepository.save(new Reservation(detail, member2));

            ReservationRequest request = new ReservationRequest(
                    date,
                    time1.getId(),
                    theme.getId(),
                    member1.getId()
            );

            assertThatThrownBy(() -> reservationService.addReservation(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("이미 예약이 존재합니다.");
        }

        @Test
        @DisplayName("이미 해당 날짜/시간의 테마에 예약 대기가 존재하면 예외를 발생시킨다.")
        void failWhenWaitingExists() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            ReservationDetail detail = new ReservationDetail(date, time1, theme);
            waitingRepository.save(new Waiting(detail, member2));

            ReservationRequest request = new ReservationRequest(
                    date,
                    time1.getId(),
                    theme.getId(),
                    member1.getId()
            );

            assertThatThrownBy(() -> reservationService.addReservation(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("예약 대기가 존재하여 예약을 할 수 없습니다.");
        }
    }

    @Test
    @DisplayName("조건에 맞는 예약들을 조회한다.")
    void getReservationsByConditions() {
        LocalDate date = LocalDate.of(2024, 4, 9);
        ReservationDetail detail1 = new ReservationDetail(date, time1, theme);
        reservationRepository.save(new Reservation(detail1, member1));

        List<ReservationResponse> responses = reservationService.getReservationsByConditions(
                member1.getId(),
                theme.getId(),
                date,
                date
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(1);
            softly.assertThat(responses.get(0).date()).isEqualTo(date);
            softly.assertThat(responses.get(0).member()).isEqualTo(MemberResponse.from(member1));
            softly.assertThat(responses.get(0).time()).isEqualTo(ReservationTimeResponse.from(time1));
            softly.assertThat(responses.get(0).theme()).isEqualTo(ThemeResponse.from(theme));
        });
    }

    @Nested
    @DisplayName("예약을 삭제하는 경우")
    class DeleteReservation {

        @Test
        @DisplayName("성공한다.")
        void success() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            ReservationDetail detail = new ReservationDetail(date, time1, theme);
            Reservation reservation = reservationRepository.save(new Reservation(detail, member1));

            reservationService.deleteReservationById(reservation.getId());

            assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
        }

        @Test
        @DisplayName("해당 id의 예약이 존재하지 않으면 예외를 발생시킨다.")
        void failWhenReservationNotExists() {
            assertThatThrownBy(() -> reservationService.deleteReservationById(-1L))
                    .isInstanceOf(DomainNotFoundException.class)
                    .hasMessage(String.format("해당 id의 예약이 존재하지 않습니다. (id: %d)", -1L));
        }
    }
}
