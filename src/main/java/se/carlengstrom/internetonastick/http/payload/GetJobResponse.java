package se.carlengstrom.internetonastick.http.payload;

import io.norberg.automatter.AutoMatter;

@AutoMatter
public interface GetJobResponse {
    String jobName();
    String status();
    String stautsString();
    String sample();
    long durationMills();
}
