package org.jboss.pnc.bacon.auth.model;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.pnc.bacon.common.Constant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Slf4j
public class CacheFile {

    public static final String CACHE_FILE = Constant.CONFIG_FOLDER + "/" + "saved-user.json";

    private Map<String, Credential> cachedData;
    private static com.fasterxml.jackson.databind.ObjectMapper mapper;

    static {
        mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    public static void writeCredentialToCacheFile(String keycloakUrl, String realm, String username, Credential credential) {

        createConfigFolderIfAbsent();

        log.debug("Writing credential to cache file");

        try {
            Map<String, Credential> data = new HashMap<>();
            data.put(generateUsernameMd5(keycloakUrl, realm, username), credential);

            CacheFile cacheFile = new CacheFile();
            cacheFile.setCachedData(data);
            mapper.writeValue(new File(CACHE_FILE), cacheFile);
            setOwnerFilePermissions(CACHE_FILE);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public static Optional<Credential> getCredentialFromCacheFile(String keycloakUrl, String realm, String username) {

        String key = generateUsernameMd5(keycloakUrl, realm, username);

        if (!fileExists(CACHE_FILE)) {
            return Optional.empty();
        }

        try {
            CacheFile cacheFile = mapper.readValue(new File(CACHE_FILE), CacheFile.class);

            if (cacheFile.getCachedData() != null && cacheFile.getCachedData().containsKey(key)) {
                return Optional.of(cacheFile.getCachedData().get(key));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Optional.empty();
        }
    }

    private static void createConfigFolderIfAbsent() {
        if (!fileExists(Constant.CONFIG_FOLDER)) {
            log.debug("Creating config folder...");
            new File(Constant.CONFIG_FOLDER).mkdirs();
        }
    }

    private static void setOwnerFilePermissions(String path) {
        HashSet<PosixFilePermission> set = new HashSet<PosixFilePermission>();

        set.add(PosixFilePermission.OWNER_READ);
        set.add(PosixFilePermission.OWNER_WRITE);

        try {
            Files.setPosixFilePermissions(Paths.get(CACHE_FILE), set);
        } catch (IOException e) {
            log.error("Cache file doesn't exist!");
        }
    }

    private static String generateUsernameMd5(String keycloakUrl, String realm, String username) {
        return DigestUtils.md5Hex(keycloakUrl + ":" + realm + ":" + username);
    }

    private static boolean fileExists(String pathString) {

        Path path = Paths.get(pathString);
        return Files.exists(path);
    }

}
