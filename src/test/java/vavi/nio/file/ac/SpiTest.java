/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.ac;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * AcFileSystemProviderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-11-17 nsano initial version <br>
 */
@PropsEntity
class SpiTest {

    static boolean localPropertiesExists() {
        return java.nio.file.Files.exists(Paths.get("local.properties"));
    }

    @Property
    String dsk = "src/test/resources/prodos.dsk";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    @DisplayName("list")
    void test1() throws Exception {
Debug.print("dsk: " + dsk);
        URI subUri = Path.of(dsk).toUri();
Debug.print("subUri: " + subUri);
Debug.print("subUri.path: " + subUri.getPath());
        URI uri = URI.create("ac:" + subUri);
Debug.print("uri: " + uri);

        FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap());

        Path root = fs.getRootDirectories().iterator().next();
        Files.walk(root).forEach(p -> {try { System.err.printf("%-48s  %s%n", p, Files.getLastModifiedTime(p)); } catch (IOException ignore) {}});
        assertEquals(64, Files.walk(root).count());

        fs.close();
    }

    @Test
    @DisplayName("download")
    void test2() throws Exception {
Debug.print("disk: " + dsk);
        URI subUri = Path.of(dsk).toUri();
Debug.print("subUri: " + subUri);
Debug.print("subUri.path: " + subUri.getPath());
        URI uri = URI.create("ac:" + subUri);
Debug.print("uri: " + uri);

        FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap());

        Path root = fs.getRootDirectories().iterator().next();
        if (!Files.exists(Path.of("tmp"))) Files.createDirectory(Path.of("tmp"));
        Path out = Path.of("tmp", "test2.download");
        Files.copy(root.resolve("DATA/TEXT"), out, StandardCopyOption.REPLACE_EXISTING);
        assertEquals(289, Files.size(out));

        fs.close();
    }
}
