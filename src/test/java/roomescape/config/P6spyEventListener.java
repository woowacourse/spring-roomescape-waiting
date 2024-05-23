package roomescape.config;

import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.event.JdbcEventListener;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.Slf4JLogger;
import com.p6spy.engine.spy.appender.StdoutLogger;
import java.sql.SQLException;

public class P6spyEventListener extends JdbcEventListener {
    @Override
    public void onAfterGetConnection(ConnectionInformation connectionInformation, SQLException e) {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(P6spyFormatter.class.getName());
        P6SpyOptions.getActiveInstance().setAppender(Slf4JLogger.class.getName());
    }
}
