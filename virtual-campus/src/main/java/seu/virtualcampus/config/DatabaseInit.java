package seu.virtualcampus.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;


import javax.sql.DataSource;
import java.sql.Connection;


@Component
public class DatabaseInit implements CommandLineRunner {
    private final DataSource dataSource;
    public DatabaseInit(DataSource dataSource) { this.dataSource = dataSource; }


    @Override
    public void run(String... args) throws Exception {
        try (Connection c = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(c, new ClassPathResource("sqls/schema.sql"));
            ScriptUtils.executeSqlScript(c, new ClassPathResource("sqls/data.sql"));
        }
    }
}