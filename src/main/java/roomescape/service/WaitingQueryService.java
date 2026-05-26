package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Waiting;
import roomescape.repository.WaitingDao;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WaitingQueryService {

    private final WaitingDao waitingDao;
}
