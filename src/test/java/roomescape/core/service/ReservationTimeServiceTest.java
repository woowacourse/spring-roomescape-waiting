package roomescape.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.core.domain.Member;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.Theme;
import roomescape.core.dto.reservationtime.ReservationTimeRequest;
import roomescape.core.dto.reservationtime.ReservationTimeResponse;
import roomescape.core.repository.MemberRepository;
import roomescape.core.repository.ReservationRepository;
import roomescape.core.repository.ReservationTimeRepository;
import roomescape.core.repository.ThemeRepository;
import roomescape.utils.DatabaseCleaner;
import roomescape.utils.TestFixture;

@ServiceTest
class ReservationTimeServiceTest {
    public static final String START_AT = "10:00";
    private static ReservationTimeRequest request;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestFixture testFixture;

    @BeforeEach
    void setUp() {
        databaseCleaner.executeTruncate();
        testFixture.initTestData();
        
        request = new ReservationTimeRequest(START_AT);
    }

    @Test
    @DisplayName("예약 시간을 생성한다.")
    void create() {
        final ReservationTimeResponse response = reservationTimeService.create(request);

        assertThat(response.getStartAt()).isEqualTo(START_AT);
    }

    @Test
    @DisplayName("중복된 예약 시간을 생성하려 하는 경우 예외가 발생한다.")
    void createWithInvalidStartAt() {
        final ReservationTimeRequest request = new ReservationTimeRequest(START_AT);

        reservationTimeService.create(request);

        assertThatThrownBy(() -> reservationTimeService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ReservationTimeService.DUPLICATED_TIME_EXCEPTION_MESSAGE);
    }

    @Test
    @DisplayName("예약 시간 목록을 조회한다.")
    void findAll() {
        assertThat(reservationTimeService.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("예약 가능한 시간 목록을 조회한다.")
    void findAllWithBookable() {
        final String date = TestFixture.getTomorrowDate();
        final long themeId = 1L;

        assertThat(reservationTimeService.findAllWithBookable(date, themeId)).hasSize(2);
    }

    @Test
    @DisplayName("예약 시간을 삭제한다.")
    void delete() {
        final ReservationTimeResponse response = reservationTimeService.create(request);

        reservationTimeService.delete(response.getId());

        assertThat(reservationTimeService.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("예약 시간 삭제 시, 예약 내역이 존재하면 예외가 발생한다.")
    void deleteWithReservation() {
        final Reservation reservation = new Reservation(getMember(), TestFixture.getTodayDate(),
                reservationTimeRepository.findById(1L).orElseThrow(), getTheme());
        reservationRepository.save(reservation);

        assertThatThrownBy(() -> reservationTimeService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ReservationTimeService.RESERVATION_DELETE_EXCEPTION_MESSAGE);
    }

    private Member getMember() {
        return memberRepository.findByEmail(TestFixture.getEmail());
    }

    private Theme getTheme() {
        return themeRepository.findById(1L).orElseThrow(IllegalArgumentException::new);
    }
}