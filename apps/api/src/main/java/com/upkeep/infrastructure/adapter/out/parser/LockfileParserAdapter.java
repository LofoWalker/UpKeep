package com.upkeep.infrastructure.adapter.out.parser;

import com.upkeep.application.port.out.pkg.LockfileParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class LockfileParserAdapter implements LockfileParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // yarn.lock: matches lines like "lodash@^4.17.21:"  or  "@types/node@^18.0.0:"
    private static final Pattern YARN_PACKAGE_PATTERN = Pattern.compile(
            "^\"?(@?[^@\"]+)@[^:]+\":?\\s*$"
    );

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
            if (line.startsWith("#") || line.isBlank()) {
                continue;
            }
            Matcher matcher = YARN_PACKAGE_PATTERN.matcher(line);
            if (matcher.matches()) {
                String name = matcher.group(1).trim();
                if (!name.isEmpty() && !packageNames.contains(name)) {
                    packageNames.add(name);
                }
            }
        }

        return packageNames;
    }
}

