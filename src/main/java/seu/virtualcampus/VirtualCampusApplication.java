package seu.virtualcampus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class VirtualCampusApplication {

	public static void main(String[] args) {
		// 设置日志编码为UTF-8，防止中文乱码
		LogManager logManager = LogManager.getLogManager();
		Logger rootLogger = logManager.getLogger("");
		for (Handler handler : rootLogger.getHandlers()) {
			if (handler instanceof ConsoleHandler) {
				try {
					handler.setEncoding(StandardCharsets.UTF_8.name());
					handler.setFormatter(new SimpleFormatter());
				} catch (Exception e) {
					System.err.println("设置日志编码失败: " + e.getMessage());
				}
			}
		}
		SpringApplication.run(VirtualCampusApplication.class, args);
	}

}
