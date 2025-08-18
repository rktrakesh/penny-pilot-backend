package com.pennypilot.dto.request;

import com.pennypilot.model.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor@Builder
public class CategoryRequest {

    private Long id;
    private String name;
    private String type;
    private String iconUrl;

}
