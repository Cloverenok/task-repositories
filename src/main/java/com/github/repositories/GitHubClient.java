package com.github.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class GitHubClient {

    private final RestClient restClient;

    public GitHubClient(@Value("${github.api.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public List<GitHubRepository> getUserRepositories(String username) {
        List<GitHubRepository> allRepos = restClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new UserNotFoundException("User not found: " + username);
                })
                .body(new ParameterizedTypeReference<List<GitHubRepository>>() {});
        return allRepos.stream()
                .filter(repo -> !repo.isFork())
                .toList();
    }

    public List<GitHubBranch> getRepositoryBranches(String owner, String repo) {
        return restClient.get()
                .uri("/repos/{owner}/{repo}/branches", owner, repo)
                .retrieve()
                .body(new ParameterizedTypeReference<List<GitHubBranch>>() {});
    }
}