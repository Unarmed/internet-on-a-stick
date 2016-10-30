/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.carlengstrom.internetonastick.samples.irc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.norberg.automatter.jackson.AutoMatterModule;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import se.carlengstrom.internetonastick.http.payload.SentenceResponse;

import java.io.File;
import java.io.IOException;

public class IrcBot extends ListenerAdapter {

    private final IrcConfig config;
    private final ObjectMapper mapper;
    private final OkHttpClient client;

    public IrcBot(final IrcConfig config) {
        this.config = config;
        this.mapper = new ObjectMapper()
                .registerModule(new AutoMatterModule());
        this.client = new OkHttpClient();
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        final Request request = new Request.Builder()
          .url(config.internetOnAStickUrl())
          .get()
          .addHeader("cache-control", "no-cache")
          .build();

        final Response response = client.newCall(request).execute();
        final SentenceResponse reply = mapper.readValue(response.body().byteStream(), SentenceResponse.class);
        event.respondChannel(event.getUser().getNick() + ": " + reply.sentence());
    }

    public static void main(final String[] args) throws IOException, IrcException {
        if(args.length != 1) {
            System.out.println("Usage: java -jar internet-on-a-stick.jar <configfile>");
            System.exit(0);
        }

        final ObjectMapper mapper = new ObjectMapper()
                .registerModule(new AutoMatterModule());

        final IrcConfig config = mapper.readValue(new File(args[0]), IrcConfig.class);
        final Configuration configuration = new Configuration.Builder()
            .setName(config.name())
            .setRealName(config.realname())
            .addServer(config.server(), config.port())
            .setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates())
            .addAutoJoinChannels(config.autoJoinChannels())
            .addListener(new IrcBot(config))
            .buildConfiguration();

        //Create our bot with the configuration
        final PircBotX bot = new PircBotX(configuration);
        //Connect to the server
        bot.startBot();
    }
}
