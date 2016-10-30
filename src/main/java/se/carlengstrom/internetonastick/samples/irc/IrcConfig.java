package se.carlengstrom.internetonastick.samples.irc;

import io.norberg.automatter.AutoMatter;

import java.util.List;

@AutoMatter
public interface IrcConfig {
    String name();
    String realname();
    String server();
    int port();
    List<String> autoJoinChannels();

    String internetOnAStickUrl();
}
