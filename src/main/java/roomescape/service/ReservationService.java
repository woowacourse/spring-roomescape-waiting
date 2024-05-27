package roomescape.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.ReservationWaitingRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.service.request.AdminSearchedReservationAppRequest;
import roomescape.service.request.ReservationAppRequest;
import roomescape.service.response.ReservationAppResponse;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository, MemberRepository memberRepository,
            ReservationWaitingRepository reservationWaitingRepository) {

        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    @Transactional
    public ReservationAppResponse save(ReservationAppRequest request) {
        Member member = findMember(request.memberId());
        ReservationDate date = new ReservationDate(request.date());
        ReservationTime time = findTime(request.timeId());
        Theme theme = findTheme(request.themeId());
        validateDuplication(date, request.timeId(), request.themeId());
        Reservation reservation = Reservation.create(member, date, time, theme);
        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationAppResponse.from(savedReservation);
    }

    private ReservationTime findTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchElementException("예약에 대한 예약시간이 존재하지 않습니다."));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchElementException("예약에 대한 테마가 존재하지 않습니다."));
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("예약에 대한 사용자가 존재하지 않습니다."));
    }

    private void validateDuplication(ReservationDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new IllegalStateException("이미 존재하는 예약 정보 입니다.");
        }
    }

    @Transactional
    public void delete(Long id) {
        Optional<ReservationWaiting> nextWaiting = reservationWaitingRepository.findFirstByReservationIdOrderByPriorityAsc(
                id);
        nextWaiting.ifPresentOrElse(reservationWaiting -> {
                    reservationWaiting.approveWaitingMember();
                    reservationWaitingRepository.delete(reservationWaiting);
                },
                () -> reservationRepository.deleteById(id)
        );
    }

    public List<ReservationAppResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationAppResponse::from)
                .toList();
    }

    public List<ReservationAppResponse> findAllSearched(AdminSearchedReservationAppRequest request) {
        List<Reservation> searchedReservations = reservationRepository.findAllByMemberIdAndThemeIdInPeriod(
                request.memberId(), request.themeId(), request.dateFrom(), request.dateTo());

        return searchedReservations.stream()
                .map(ReservationAppResponse::from)
                .toList();
    }

    public List<ReservationAppResponse> findByMemberId(Long id) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(id);

        return reservations.stream()
                .map(ReservationAppResponse::from)
                .toList();
    }
}
