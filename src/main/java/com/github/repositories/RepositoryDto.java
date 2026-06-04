package com.github.repositories;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RepositoryDto {

    private String repositoryName;
    private String ownerLogin;
    private List<BranchDto> branches;
}
