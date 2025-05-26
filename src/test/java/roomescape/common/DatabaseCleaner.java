package roomescape.common;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseCleaner {

    private static final String FOREIGN_KEY_SET_ON = "SET REFERENTIAL_INTEGRITY TRUE";
    private static final String FOREIGN_KEY_SET_OFF = "SET REFERENTIAL_INTEGRITY FALSE";
    private static final String TRUNCATE_TABLE_QUERY = "TRUNCATE TABLE %s RESTART IDENTITY";

    @PersistenceContext
    private EntityManager entityManager;
    private List<String> tableNames;

    @PostConstruct
    void setTableNames() {
        tableNames = entityManager.getMetamodel().getEntities().stream()
            .filter(entityType -> entityType.getJavaType().getAnnotation(Entity.class) != null)
            .map(entityType -> CaseFormat.convertCamelToSnake(entityType.getName()).toLowerCase())
            .filter(tableName -> !(tableName.equals("member") || tableName.equals("admin")))
            .toList();
    }

    @Transactional
    void clean() {
        entityManager.createNativeQuery(FOREIGN_KEY_SET_OFF).executeUpdate();
        for (String tableName : tableNames) {
            entityManager.createNativeQuery(String.format(TRUNCATE_TABLE_QUERY, tableName)).executeUpdate();
        }
        entityManager.createNativeQuery(FOREIGN_KEY_SET_ON).executeUpdate();

        entityManager.flush();
        entityManager.clear();
    }
}
