/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.ac;

import java.io.File;

import com.github.fge.filesystem.driver.ExtendedFileSystemDriverBase.ExtendedFileAttributesFactory;
import vavi.nio.file.ac.AcFileSystemDriver.AcEntry;


/**
 * AcFileAttributesFactory.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2025/11/16 umjammer initial version <br>
 */
public final class AcFileAttributesFactory extends ExtendedFileAttributesFactory {

    public AcFileAttributesFactory() {
        setMetadataClass(AcEntry.class);
        addImplementation("basic", AcBasicFileAttributesProvider.class);
    }
}
