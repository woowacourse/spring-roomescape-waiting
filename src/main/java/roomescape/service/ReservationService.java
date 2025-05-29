package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.dto.reservation.MyReservationAndWaitingsResponse;
import roomescape.dto.reservation.ReservationCreateRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

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

    public ReservationResponse createReservation(ReservationCreateRequest request) {
        ReservationTime reservationTime = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 예약 시간을 찾을 수 없습니다. id : " + request.timeId()));

        validateDuplicate(request.date(), request.timeId(), request.themeId());
        Reservation.validateReservableTime(request.date(), reservationTime.getStartAt());

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 테마를 찾을 수 없습니다. id : " + request.themeId()));

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 유저를 찾을 수 없습니다. id : " + request.memberId()));

        Reservation requestReservation = Reservation.createWithoutId(member, request.date(), reservationTime, theme);
        Reservation newReservation = reservationRepository.save(requestReservation);

        return ReservationResponse.from(newReservation);
    }

    public void validateDuplicate(LocalDate date, Long timeId, Long themeId) {
        List<Reservation> reservations = reservationRepository.findReservationsByDateAndTimeIdAndThemeId(date, timeId,
                themeId);
        if (!reservations.isEmpty()) {
            throw new DuplicateContentException("[ERROR] 이미 예약이 존재합니다. 다른 예약 일정을 선택해주세요.");
        }
    }

    public List<ReservationResponse> findAllReservationResponses() {
        List<Reservation> allReservations = reservationRepository.findAll();

        return allReservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findReservationBetween(Long themeId, Long memberId, LocalDate from,
                                                            LocalDate to) {
        List<Reservation> reservationsByPeriodAndMemberAndTheme = reservationRepository.findReservationsByDateBetweenAndThemeIdAndMemberId(
                from, to, themeId, memberId);
        return reservationsByPeriodAndMemberAndTheme.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("[ERROR] 등록된 예약번호만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }
        reservationRepository.deleteById(id);
    }

    public List<MyReservationAndWaitingsResponse> findMyReservations(Long id) {
        List<Reservation> reservations = reservationRepository.findReservationsByMemberId(id);
        return reservations.stream().map(MyReservationAndWaitingsResponse::from).toList();
    }
}
