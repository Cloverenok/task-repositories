package com.github.repositories;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BranchDto {

    private String name;
    private String lastCommitSha;
}
