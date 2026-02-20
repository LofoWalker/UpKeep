package com.upkeep.infrastructure.adapter.out.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LockfileParserAdapterTest {

    private LockfileParserAdapter parser;

    @BeforeEach
    void setUp() {
        parser = new LockfileParserAdapter();
    }

    @Test
    void shouldSupportPackageLockJson() {
        assertTrue(parser.supports("package-lock.json"));
    }

    @Test
    void shouldSupportYarnLock() {
        assertTrue(parser.supports("yarn.lock"));
    }

    @Test
    void shouldNotSupportUnknownFiles() {
        assertFalse(parser.supports("pom.xml"));
        assertFalse(parser.supports("go.sum"));
    }

    @Test
    void shouldParsePackageLockJsonV3() {
        String content = """
                {
                  "name": "my-project",
                  "lockfileVersion": 3,
                  "packages": {
                    "": {
                      "name": "my-project",
                      "dependencies": {
                        "lodash": "^4.17.21"
                      }
                    },
                    "node_modules/lodash": {
                      "version": "4.17.21"
                    },
                    "node_modules/@types/node": {
                      "version": "18.0.0"
                    },
                    "node_modules/express": {
                      "version": "4.18.2"
                    }
                  }
                }
                """;

        List<String> result = parser.parse(content, "package-lock.json");

        assertEquals(3, result.size());
        assertTrue(result.contains("lodash"));
        assertTrue(result.contains("@types/node"));
        assertTrue(result.contains("express"));
    }

    @Test
    void shouldParsePackageLockJsonV1() {
        String content = """
                {
                  "name": "my-project",
                  "lockfileVersion": 1,
                  "dependencies": {
                    "lodash": {
                      "version": "4.17.21"
                    },
                    "express": {
                      "version": "4.18.2",
                      "dependencies": {
                        "debug": {
                          "version": "2.6.9"
                        }
                      }
                    }
                  }
                }
                """;

        List<String> result = parser.parse(content, "package-lock.json");

        assertEquals(3, result.size());
        assertTrue(result.contains("lodash"));
        assertTrue(result.contains("express"));
        assertTrue(result.contains("debug"));
    }

    @Test
    void shouldParseYarnLock() {
        String content = """
                # yarn lockfile v1
                
                lodash@^4.17.21:
                  version "4.17.21"
                  resolved "https://registry.yarnpkg.com/lodash/-/lodash-4.17.21.tgz"
                
                "@types/node@^18.0.0":
                  version "18.0.0"
                  resolved "https://registry.yarnpkg.com/@types/node/-/node-18.0.0.tgz"
                
                express@^4.18.0:
                  version "4.18.2"
                  resolved "https://registry.yarnpkg.com/express/-/express-4.18.2.tgz"
                """;

        List<String> result = parser.parse(content, "yarn.lock");

        assertEquals(3, result.size());
        assertTrue(result.contains("lodash"));
        assertTrue(result.contains("@types/node"));
        assertTrue(result.contains("express"));
    }

    @Test
    void shouldThrowForInvalidJson() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("not valid json", "package-lock.json"));
    }

    @Test
    void shouldThrowForUnsupportedFile() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("{}", "unsupported.txt"));
    }
}

