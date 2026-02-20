package com.upkeep.application.port.out.pkg;

import java.util.List;

public interface LockfileParser {

    List<String> parse(String content, String filename);

    boolean supports(String filename);
}

