package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.CreateReservationRequest;
import roomescape.reservation.dto.response.CreateReservationResponse;
import roomescape.reservation.dto.response.FindAvailableTimesResponse;
import roomescape.reservation.dto.response.FindReservationResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.model.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository,
                              final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public CreateReservationResponse createReservation(final AuthInfo authInfo,
                                                       final CreateReservationRequest createReservationRequest) {
        ReservationTime reservationTime = findReservationTime(createReservationRequest.timeId());
        Theme theme = findTheme(createReservationRequest.themeId());
        Member member = findMember(authInfo.getMemberId());

        checkAlreadyExistReservation(createReservationRequest, createReservationRequest.date(), theme.getName(),
                reservationTime.getTime());
        Reservation reservation = createReservationRequest.toReservation(member, reservationTime, theme);
        return CreateReservationResponse.from(reservationRepository.save(reservation));
    }

    private void checkAlreadyExistReservation(final CreateReservationRequest createReservationRequest,
                                              final LocalDate date, final String themeName, final LocalTime time) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                createReservationRequest.date(),
                createReservationRequest.timeId(),
                createReservationRequest.themeId())) {
            throw new IllegalArgumentException("이미 " + date + "의 " + themeName + " 테마에는 " + time
                    + " 시의 예약이 존재하여 예약을 생성할 수 없습니다.");
        }
    }

    private ReservationTime findReservationTime(final Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 시간이 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    private Theme findTheme(final Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 테마가 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    private Member findMember(final Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 회원이 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    public List<FindReservationResponse> getReservations() {
        return mapToFindReservationResponse(reservationRepository.findAll());
    }

    public FindReservationResponse getReservation(final Long id) {
        Reservation reservation = findReservation(id);
        return FindReservationResponse.from(reservation);
    }

    private Reservation findReservation(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 예약이 존재하지 않아 예약을 조회할 수 없습니다."));
    }

    public List<FindAvailableTimesResponse> getAvailableTimes(final LocalDate date, final Long themeId) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<Reservation> reservations = reservationRepository.findAllByDateAndThemeId(date, themeId);
        return reservationTimes.stream()
                .map(reservationTime -> generateFindAvailableTimesResponse(reservations, reservationTime))
                .toList();
    }

    private static FindAvailableTimesResponse generateFindAvailableTimesResponse(final List<Reservation> reservations,
                                                                                 final ReservationTime reservationTime) {
        return FindAvailableTimesResponse.from(
                reservationTime,
                reservations.stream()
                        .anyMatch(reservation -> reservation.isSameTime(reservationTime)));
    }

    public List<FindReservationResponse> searchBy(final Long themeId, final Long memberId,
                                                  final LocalDate dateFrom, final LocalDate dateTo) {
        return mapToFindReservationResponse(reservationRepository.searchBy(themeId, memberId, dateFrom, dateTo));
    }

    private List<FindReservationResponse> mapToFindReservationResponse(final List<Reservation> reservations) {
        return reservations.stream()
                .map(FindReservationResponse::from)
                .toList();
    }

    public void deleteReservation(final Long id) {
        validateExistReservation(id);
        reservationRepository.deleteById(id);
    }

    private void validateExistReservation(final Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NoSuchElementException("식별자 " + id + "에 해당하는 예약이 존재하지 않습니다. 삭제가 불가능합니다.");
        }
    }
}
