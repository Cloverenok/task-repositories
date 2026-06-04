package com.github.repositories;

import lombok.Getter;

@Getter
public class GitHubBranch {
    private String name;
    private GitHubCommit commit;
}