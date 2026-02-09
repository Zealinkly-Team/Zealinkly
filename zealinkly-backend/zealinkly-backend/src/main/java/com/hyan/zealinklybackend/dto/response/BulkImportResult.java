package com.hyan.zealinklybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportResult {
    private int successCount;
    private int failCount;
    @Builder.Default
    private List<String> errors = new ArrayList<>();
}
