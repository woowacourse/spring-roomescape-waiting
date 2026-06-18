package roomescape.domain.reservationdate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdate.dto.ReservationDateCreationRequest;
import roomescape.domain.reservationdate.dto.ReservationDateCreationResponse;
import roomescape.domain.reservationdate.dto.ReservationDateResponse;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.support.exception.RoomescapeException;

import java.time.LocalTime;

@SpringBootTest
@Sql("/truncate.sql")
class ReservationDateServiceTest {

    @Autowired
    private ReservationDateService reservationDateService;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("예약 날짜를 생성한다.")
    void createReservationDate() {
        ReservationDateCreationRequest request = new ReservationDateCreationRequest(LocalDate.now().plusDays(1));

        ReservationDateCreationResponse response = reservationDateService.createReservationDate(request);

        assertThat(response.playDay()).isEqualTo(LocalDate.now().plusDays(1));
    }

    @Test
    @DisplayName("중복된 날짜 생성 시 예외가 발생한다.")
    void createDuplicateDate() {
        LocalDate playDay = LocalDate.now().plusDays(1);
        reservationDateService.createReservationDate(new ReservationDateCreationRequest(playDay));

        assertThatThrownBy(() -> reservationDateService.createReservationDate(new ReservationDateCreationRequest(playDay)))
                .isInstanceOf(RoomescapeException.class);
    }

    @Test
    @DisplayName("오늘 이후의 날짜만 조회한다.")
    void getAllAvailableReservationDate() {
        reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now().minusDays(1)));
        reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));

        List<ReservationDateResponse> responses = reservationDateService.getAllAvailableReservationDate();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).playDay()).isEqualTo(LocalDate.now().plusDays(1));
    }

    @Test
    @DisplayName("사용 중인 날짜를 삭제하려 하면 예외가 발생한다.")
    void deleteInUseDate() {
        ReservationDate date = createDate(LocalDate.now().plusDays(1));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테스트테마", "설명", "url"));
        Member tester = memberRepository.save(Member.createWithoutId("테스터"));
        reservationRepository.save(Reservation.createWithoutId(tester, date, time, theme));

        assertThatThrownBy(() -> reservationDateService.deleteReservationDate(date.getId()))
                .isInstanceOf(RoomescapeException.class);
    }

    private ReservationDate createDate(LocalDate playDay) {
        return reservationDateRepository.save(ReservationDate.createWithoutId(playDay));
    }
}
