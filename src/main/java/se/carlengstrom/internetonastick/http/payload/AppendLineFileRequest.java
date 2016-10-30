package se.carlengstrom.internetonastick.http.payload;

import io.norberg.automatter.AutoMatter;

@AutoMatter
public interface AppendLineFileRequest {
    String data(); //base64 encoded data
}
