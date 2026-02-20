package com.upkeep.infrastructure.adapter.in.rest.pkg;

import java.util.List;

public record ImportListRequest(
        List<String> packageNames
) {
}

