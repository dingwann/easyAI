package dingwan.easy.ai.core.config;

import dingwan.easy.ai.core.chat.ChatClient;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@AutoConfiguration
@EnableConfigurationProperties(EasyAiProperties.class)
public class CoreAutoConfiguration {

    @Autowired EasyAiProperties easyAiProperties;

    @Bean
    @ConditionalOnMissingBean
    public OkHttpClient easyOkHttpClient() {
        return new OkHttpClient.Builder()
                // 超时配置
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                // 连接池（复用连接，提升性能）
                .connectionPool(new ConnectionPool(
                        10,        // 最大空闲连接数
                        5,         // 保活时间
                        TimeUnit.MINUTES
                ))
                // 日志拦截器（调试用）
                .addInterceptor(chain -> chain.proceed(
                        chain.request().newBuilder()
                                // .addHeader("Authorization", "Bearer YOUR_API_KEY")
                                .addHeader("Content-Type", "application/json")
                                .build()
                ))
                .build();
    }

    @Bean
    public ChatClient chatClient() {
        return new ChatClient(easyOkHttpClient(), easyAiProperties);
    }

}

