package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.CreateReservationRequest;
import roomescape.reservation.dto.response.CreateReservationResponse;
import roomescape.reservation.dto.response.FindAvailableTimesResponse;
import roomescape.reservation.dto.response.FindReservationResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Slot;
import roomescape.reservation.model.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository, WaitingRepository waitingRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    private static FindAvailableTimesResponse generateFindAvailableTimesResponse(List<Reservation> reservations,
                                                                                 ReservationTime reservationTime) {
        return FindAvailableTimesResponse.from(
                reservationTime,
                reservations.stream()
                        .anyMatch(reservation -> reservation.hasSameTime(reservationTime)));
    }

    public CreateReservationResponse createReservation(AuthInfo authInfo,
                                                       CreateReservationRequest createReservationRequest) {
        Member member = getMember(authInfo.getMemberId());
        ReservationTime reservationTime = getReservationTime(createReservationRequest.timeId());
        Theme theme = getTheme(createReservationRequest.themeId());
        Slot slot = new Slot(createReservationRequest.date(), reservationTime, theme);
        Reservation reservation = Reservation.create(member, slot);

        checkAlreadyExistReservation(slot);
        checkWaitingExists(slot);

        return CreateReservationResponse.from(reservationRepository.save(reservation));
    }

    private void checkAlreadyExistReservation(Slot slot) {
        if (reservationRepository.existsBySlot(slot)) {
            throw new IllegalArgumentException("이미 예약이 존재하여 예약을 생성할 수 없습니다.");
        }
    }

    private void checkWaitingExists(Slot slot) {
        if (waitingRepository.existsBySlot(slot)) {
            throw new IllegalArgumentException("대기자가 있어 예약을 생성할 수 없습니다.");
        }
    }

    private ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 시간이 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    private Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 테마가 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 회원이 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<FindReservationResponse> getReservations() {
        return mapToFindReservationResponse(reservationRepository.findAll());
    }

    @Transactional(readOnly = true)
    public FindReservationResponse getOneReservation(Long id) {
        return FindReservationResponse.from(getReservation(id));
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 예약이 존재하지 않아 예약을 조회할 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<FindAvailableTimesResponse> getAvailableTimes(LocalDate date, Long themeId) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<Reservation> reservations = reservationRepository.findAllBySlot_DateAndSlot_ThemeId(date, themeId);
        return reservationTimes.stream()
                .map(reservationTime -> generateFindAvailableTimesResponse(reservations, reservationTime))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FindReservationResponse> searchBy(Long themeId, Long memberId,
                                                  LocalDate dateFrom, LocalDate dateTo) {
        return mapToFindReservationResponse(
                reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo));
    }

    private List<FindReservationResponse> mapToFindReservationResponse(List<Reservation> reservations) {
        return reservations.stream()
                .map(FindReservationResponse::from)
                .toList();
    }

    public void deleteReservation(Long id) {
        validateExistReservation(id);
        reservationRepository.deleteById(id);
    }

    private void validateExistReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NoSuchElementException("식별자 " + id + "에 해당하는 예약이 존재하지 않습니다. 삭제가 불가능합니다.");
        }
    }
}
