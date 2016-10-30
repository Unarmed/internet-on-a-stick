package se.carlengstrom.internetonastick.http.payload;

import io.norberg.automatter.AutoMatter;

import java.util.List;

@AutoMatter
public interface GetJobsResponse {
    List<String> jobs();
}
