package com.upkeep.infrastructure.adapter.out.parser;

import com.upkeep.application.port.out.pkg.LockfileParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class LockfileParserAdapter implements LockfileParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    @Override
    public List<String> parse(String content, String filename) {
        if (filename.equals("package-lock.json")) {
            return parsePackageLockJson(content);
        } else if (filename.equals("yarn.lock")) {
            return parseYarnLock(content);
        }
        throw new IllegalArgumentException("Unsupported lockfile: " + filename);
    }

    @Override
    public boolean supports(String filename) {
        return "package-lock.json".equals(filename) || "yarn.lock".equals(filename);
    }

    private List<String> parsePackageLockJson(String content) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(content);
            List<String> packageNames = new ArrayList<>();

            // v2/v3 format: "packages" field with "node_modules/..." keys
            JsonNode packages = root.get("packages");
            if (packages != null && packages.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> fields = packages.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String key = entry.getKey();
                    if (key.isEmpty()) {
                        continue; // root package entry
                    }
                    String name = extractPackageNameFromNodeModulesPath(key);
                    if (name != null && !packageNames.contains(name)) {
                        packageNames.add(name);
                    }
                }
                return packageNames;
            }

            // v1 format: "dependencies" field
            JsonNode dependencies = root.get("dependencies");
            if (dependencies != null && dependencies.isObject()) {
                parseDependenciesV1(dependencies, packageNames);
            }

            return packageNames;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse package-lock.json: " + e.getMessage(), e);
        }
    }

    private void parseDependenciesV1(JsonNode node, List<String> names) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            if (!names.contains(entry.getKey())) {
                names.add(entry.getKey());
            }
            // Recurse into nested dependencies
            JsonNode nested = entry.getValue().get("dependencies");
            if (nested != null && nested.isObject()) {
                parseDependenciesV1(nested, names);
            }
        }
    }

    private String extractPackageNameFromNodeModulesPath(String path) {
        // "node_modules/@scope/name" -> "@scope/name"
        // "node_modules/name" -> "name"
        // "node_modules/a/node_modules/b" -> "b" (last segment)
        int lastNodeModules = path.lastIndexOf("node_modules/");
        if (lastNodeModules < 0) {
            return null;
        }
        return path.substring(lastNodeModules + "node_modules/".length());
    }

    private List<String> parseYarnLock(String content) {
        List<String> packageNames = new ArrayList<>();
        String[] lines = content.split("\n");

        for (String line : lines) {
            if (line.startsWith("#") || line.isBlank() || line.startsWith(" ") || line.startsWith("\t")) {
                continue;
            }

            // Yarn.lock top-level entries end with ":"
            // Formats: `lodash@^4.17.21:` or `"@types/node@^18.0.0":`
            String trimmed = line.trim();
            if (!trimmed.endsWith(":")) {
                continue;
            }
            trimmed = trimmed.substring(0, trimmed.length() - 1);

            // Remove surrounding quotes if present
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }

            // Extract package name: everything before the last `@` that separates name from version
            // For scoped: @types/node@^18.0.0 → name=@types/node
            // For regular: lodash@^4.17.21 → name=lodash
            String name = extractPackageNameFromYarnEntry(trimmed);
            if (name != null && !name.isEmpty() && !packageNames.contains(name)) {
                packageNames.add(name);
            }
        }

        return packageNames;
    }

    private String extractPackageNameFromYarnEntry(String entry) {
        // Scoped packages start with @, so the version separator is the SECOND @
        int atIndex;
        if (entry.startsWith("@")) {
            atIndex = entry.indexOf('@', 1);
        } else {
            atIndex = entry.indexOf('@');
        }
        if (atIndex <= 0) {
            return null;
        }
        return entry.substring(0, atIndex);
    }
}

