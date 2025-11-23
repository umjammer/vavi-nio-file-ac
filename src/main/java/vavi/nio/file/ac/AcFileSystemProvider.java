/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.ac;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import com.github.fge.filesystem.provider.FileSystemProviderBase;


/**
 * AcFileSystemProvider.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2025/11/16 umjammer initial version <br>
 */
public final class AcFileSystemProvider extends FileSystemProviderBase {

    private static final Logger logger = System.getLogger(AcFileSystemFactoryProvider.class.getName());

    public static final String PARAM_ALIAS = "alias";

    public AcFileSystemProvider() {
        super(new AcFileSystemRepository());
    }

    /**
     * utility
     * TODO consider more
     */
    public static URI createURI(String path) throws IOException {
        String url = URLEncoder.encode(Paths.get(path).toAbsolutePath().toString(), StandardCharsets.UTF_8);
        url = url.replace("%2F", "/");
        url = url.replace("+", "%20");
        URI uri = URI.create("ac:file:" + url);
logger.log(Level.DEBUG, "uri: " + uri);
        return uri;
    }
}
