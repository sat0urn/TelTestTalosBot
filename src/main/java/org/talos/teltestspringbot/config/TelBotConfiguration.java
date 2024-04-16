package org.talos.teltestspringbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.TelegramOkHttpClientFactory;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class TelBotConfiguration {
    @Bean(value = "botToken")
    public String botToken() {
        return System.getenv("TG_TOKEN");
    }

    @Bean(value = "okClient")
    public OkHttpClient okClientHttp(
            @Value("${proxy.hostname}") String hostname,
            @Value("${proxy.port}") int port,
            @Value("${proxy.username}") String username,
            @Value("${proxy.password}") String password
    ) {
        return new TelegramOkHttpClientFactory.HttpProxyOkHttpClientCreator(
                () -> new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port)),
                () -> (route, response) -> {
                    String credential = Credentials.basic(username, password);
                    return response
                            .request()
                            .newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                }
        ).get();
    }

    @Bean(value = "telegramClient")
    public TelegramClient telegramClient(
            @Qualifier("okClient") OkHttpClient okClient,
            @Qualifier("botToken") String botToken
    ) {
        return new OkHttpTelegramClient(okClient, botToken);
    }

    @Bean(value = "telegramBotsApplication")
    public TelegramBotsLongPollingApplication telegramBotsApplication(
            @Qualifier("okClient") OkHttpClient okClient
    ) {
        return new TelegramBotsLongPollingApplication(ObjectMapper::new, () -> okClient);
    }
}
