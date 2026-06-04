package roomescape.support;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleaner {

    @Autowired
    private DataSource dataSource;

    private List<String> tableNames;

    // schema.sql 초기화가 끝난 ApplicationReadyEvent 시점에 사용자 테이블 이름을 한 번만 추출해 캐싱한다
    @EventListener(ApplicationReadyEvent.class)
    public void cacheTableNames() {
        try (Connection connection = dataSource.getConnection()) {
            tableNames = extractTableNames(connection);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void clear() {
        try (Connection connection = dataSource.getConnection()) {
            cleanUpDatabase(connection, tableNames);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    // 모든 사용자 테이블 이름 추출
    private List<String> extractTableNames(Connection conn) throws SQLException {
        List<String> extractedTableNames = new ArrayList<>();

        try (ResultSet tables = conn.getMetaData()
                .getTables(conn.getCatalog(), "PUBLIC", "%", new String[]{"BASE TABLE"})
        ) {
            while (tables.next()) {
                extractedTableNames.add(tables.getString("TABLE_NAME"));
            }

            return extractedTableNames;
        }
    }

    // 모든 테이블 데이터 제거 및 ID 초기화
    private void cleanUpDatabase(Connection conn, List<String> tableNames) throws SQLException {
        try (Statement statement = conn.createStatement()) {

            // 데이터 무결성 설정 OFF
            statement.executeUpdate("SET REFERENTIAL_INTEGRITY FALSE");

            for (String tableName : tableNames) {
                // 데이터 제거 + AUTO_INCREMENT ID를 1부터 재시작
                statement.executeUpdate("TRUNCATE TABLE " + tableName + " RESTART IDENTITY");
            }

            // 데이터 무결성 설정 ON
            statement.executeUpdate("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }
}
