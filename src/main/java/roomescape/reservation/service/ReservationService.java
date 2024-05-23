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
        ReservationTime reservationTime = reservationTimeRepository.getById(createReservationRequest.timeId());
        Theme theme = themeRepository.getById(createReservationRequest.themeId());
        Member member = memberRepository.getById(authInfo.getMemberId());

        checkAlreadyExistReservation(createReservationRequest, createReservationRequest.date(), theme.getName(),
                reservationTime.getStartAt());
        Reservation reservation = createReservationRequest.toReservation(member, reservationTime, theme);
        return CreateReservationResponse.from(reservationRepository.save(reservation));
    }

    private void checkAlreadyExistReservation(final CreateReservationRequest createReservationRequest,
                                              final LocalDate date, final String themeName, final LocalTime time) {
        if (reservationRepository.existsByDateAndReservationTimeIdAndThemeId(
                createReservationRequest.date(),
                createReservationRequest.timeId(),
                createReservationRequest.themeId())) {
            throw new IllegalArgumentException("이미 " + date + "의 " + themeName + " 테마에는 " + time
                    + " 시의 예약이 존재하여 예약을 생성할 수 없습니다.");
        }
    }

    public List<FindReservationResponse> getReservations() {
        return reservationRepository.findAll().stream()
                .map(FindReservationResponse::from)
                .toList();
    }

    public FindReservationResponse getReservation(final Long id) {
        Reservation reservation = reservationRepository.getById(id);
        return FindReservationResponse.from(reservation);
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
        return reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo).stream()
                .map(FindReservationResponse::from)
                .toList();
    }

    public void deleteReservation(final Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NoSuchElementException("식별자 " + id + "에 해당하는 예약이 존재하지 않습니다. 삭제가 불가능합니다.");
        }
        reservationRepository.deleteById(id);
    }

    public List<roomescape.member.dto.response.FindReservationResponse> getReservationsByMember(
            final AuthInfo authInfo) {
        return reservationRepository.findAllByMemberId(authInfo.getMemberId()).stream()
                .map(roomescape.member.dto.response.FindReservationResponse::from)
                .toList();
    }
}
