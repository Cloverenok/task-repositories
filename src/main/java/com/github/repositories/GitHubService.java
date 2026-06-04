package com.github.repositories;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GitHubService {

    private final GitHubClient gitHubClient;

    public List<RepositoryDto> getUserRepositories(String username) {
        List<GitHubRepository> repositories = gitHubClient.getUserRepositories(username);
        return repositories.stream()
                .map(this::toDto)
                .toList();
    }

    private RepositoryDto toDto(GitHubRepository repository) {
        List<BranchDto> branches = gitHubClient.getRepositoryBranches(repository.getOwner().getLogin(),
                        repository.getName())
                .stream()
                .map(branch -> new BranchDto(branch.getName(), branch.getCommit().getSha()))
                .toList();

        return new RepositoryDto(
                repository.getName(),
                repository.getOwner().getLogin(),
                branches
        );
    }
}
