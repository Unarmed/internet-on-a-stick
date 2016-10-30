package se.carlengstrom.internetonastick.http.payload;

import io.norberg.automatter.AutoMatter;

@AutoMatter
public interface SentenceResponse {
    String sentence();
}
