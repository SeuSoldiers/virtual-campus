package seu.virtualcampus.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 数据库初始化组件。
 * <p>
 * 此类在应用程序启动时运行，用于执行SQL脚本来初始化数据库。
 * 它实现了CommandLineRunner接口，确保在Spring Boot应用启动完成后执行run方法。
 */
@Component
public class DatabaseInit implements CommandLineRunner {
    private final DataSource dataSource;

    /**
     * DatabaseInit的构造函数。
     *
     * @param dataSource 数据源，通过依赖注入提供。
     */
    public DatabaseInit(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    /**
     * 在应用程序启动时执行数据库初始化脚本。
     * <p>
     * 此方法会依次执行schema.sql和data.sql两个脚本文件，
     * 分别用于创建数据库表结构和插入初始数据。
     *
     * @param args 应用程序启动时传入的命令行参数。
     * @throws Exception 如果在执行SQL脚本过程中发生错误。
     */
    @Override
    public void run(String... args) throws Exception {

        try (Connection c = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(c, new ClassPathResource("sqls/schema.sql"));
            ScriptUtils.executeSqlScript(c, new ClassPathResource("sqls/data.sql"));
        }
    }
}