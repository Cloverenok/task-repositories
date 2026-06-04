package com.github.repositories;
import lombok.Getter;

@Getter
public class GitHubRepository {
    private String name;
    private boolean fork;
    private GitHubOwner owner;
}