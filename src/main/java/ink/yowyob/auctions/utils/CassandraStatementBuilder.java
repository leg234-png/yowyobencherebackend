package ink.yowyob.auctions.utils;

import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.InsertInto;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import java.lang.reflect.Field;
import java.util.UUID;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;

public class CassandraStatementBuilder {

    public static <T> SimpleStatement buildInsertStatement(T entity, String tableName) {
        InsertInto insertInto = QueryBuilder.insertInto(tableName);

        InsertInto finalInsert = insertInto; // pour pouvoir muter via lambda
        RegularInsert step = null;

        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value != null) {
                    String columnName = getColumnName(field);
                    if (step == null) {
                        // Première valeur : démarre le Insert
                        step = finalInsert.value(columnName, literal(value));
                    } else {
                        // Ajoute les autres valeurs
                        step = step.value(columnName, literal(value));
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + field.getName(), e);
            }
        }

        if (step == null) {
            throw new IllegalStateException("No values provided for insert statement.");
        }

        return step.build();
    }


    private static String getColumnName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            return field.getAnnotation(Column.class).value();
        } else if (field.isAnnotationPresent(PrimaryKey.class)) {
            return field.getName();
        } else {
            // fallback : nom de champ si pas annoté
            return field.getName();
        }
    }
}

