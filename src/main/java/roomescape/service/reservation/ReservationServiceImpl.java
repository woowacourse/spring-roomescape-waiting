package roomescape.service.reservation;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.admin.AdminReservationRequest;
import roomescape.dto.reservation.MemberReservationResponse;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.search.SearchConditionsRequest;
import roomescape.exception.member.MemberNotFoundException;
import roomescape.exception.reservation.ReservationAlreadyExistsException;
import roomescape.exception.reservation.ReservationInPastException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.theme.ThemeNotFoundException;
import roomescape.repository.member.MemberRepository;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;
import roomescape.repository.waiting.WaitingRepository;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  ReservationTimeRepository timeRepository, ThemeRepository themeRepository,
                                  MemberRepository memberRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request, Member member) {
        ReservationTime reservationTime = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(request.timeId()));

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(request.themeId()));

        if (LocalDateTime.now().isAfter(LocalDateTime.of(request.date(), reservationTime.getStartAt()))) {
            throw new ReservationInPastException();
        }

        if (reservationRepository.existsByDateAndTimeId(request.date(), reservationTime.getId())) {
            throw new ReservationAlreadyExistsException();
        }

        Reservation newReservation = new Reservation(request.date(),
                reservationTime, theme, member);

        return ReservationResponse.from(reservationRepository.save(newReservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAll() {
        return ReservationResponse.from(reservationRepository.findAll());
    }

    @Transactional
    public void deleteById(Long id) {

        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> {
            throw new ReservationNotFoundException(id);
        });

        reservationRepository.deleteById(id);

        if (waitingRepository.existsByDateAndTimeIdAndThemeId(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId())) {
            promoteFirstWaitingToReservation(reservation);
        }
    }

    private void promoteFirstWaitingToReservation(Reservation reservation) {
        Waiting waiting = waitingRepository.findFirstWaitingByDateAndTimeIdAndThemeId(reservation.getDate(),
                reservation.getTime().getId(), reservation.getTheme().getId());

        Reservation newReservation = new Reservation(waiting.getDate(), waiting.getTime(), waiting.getTheme(),
                waiting.getMember());

        reservationRepository.save(newReservation);
        waitingRepository.deleteById(waiting.getId());

    }

    @Transactional
    @Override
    public ReservationResponse createByAdmin(AdminReservationRequest adminReservationRequest) {
        ReservationTime reservationTime = timeRepository.findById(adminReservationRequest.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(adminReservationRequest.timeId()));

        Theme theme = themeRepository.findById(adminReservationRequest.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(adminReservationRequest.themeId()));

        Member member = memberRepository.findById(adminReservationRequest.memberId())
                .orElseThrow(() -> new MemberNotFoundException(adminReservationRequest.memberId()));

        Reservation newReservation = new Reservation(adminReservationRequest.date(),
                reservationTime, theme, member);

        return ReservationResponse.from(reservationRepository.save(newReservation));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReservationResponse> getReservationsByConditions(@Valid SearchConditionsRequest searchConditionsRequest) {

        List<Reservation> reservations = reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(
                searchConditionsRequest.themeId(),
                searchConditionsRequest.memberId(),
                searchConditionsRequest.dateFrom(),
                searchConditionsRequest.dateTo()
        );
        return reservations.stream().
                map(reservation -> ReservationResponse.from(reservation))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<MemberReservationResponse> getReservationByMember(Member member) {
        return reservationRepository.findAllByMember(member)
                .stream()
                .map(MemberReservationResponse::from)
                .toList();
    }
}
