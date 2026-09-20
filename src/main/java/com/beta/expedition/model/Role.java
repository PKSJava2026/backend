package com.beta.expedition.model;

import com.beta.expedition.model.enums.RoleName;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Role {
    private Short id;
    private RoleName name;
}