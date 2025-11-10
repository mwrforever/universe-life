// 测试代码：验证Spring Boot是否自动配置AuthorizationServerSettings

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

@SpringBootApplication
public class TestAutoConfig {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TestAutoConfig.class, args);

        try {
            // 尝试获取AuthorizationServerSettings bean
            AuthorizationServerSettings settings = context.getBean(AuthorizationServerSettings.class);
            System.out.println("找到自动配置的AuthorizationServerSettings: " + settings.getIssuer());
        } catch (Exception e) {
            System.out.println("未找到自动配置的AuthorizationServerSettings: " + e.getMessage());
            System.out.println("这证实了AuthorizationServerSettings需要手动配置");
        }

        context.close();
    }
}