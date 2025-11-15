/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.ac;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;

import com.github.fge.filesystem.driver.ExtendedFileSystemDriver;
import com.github.fge.filesystem.provider.FileSystemFactoryProvider;

import static vavi.nio.file.Util.isAppleDouble;
import static vavi.nio.file.Util.toFilenameString;


/**
 * AcFileSystemDriver.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2025/11/16 umjammer initial version <br>
 */
@ParametersAreNonnullByDefault
public final class AcFileSystemDriver extends ExtendedFileSystemDriver<File> {

    /**
     * @param env { "baseUrl": "smb://10.3.1.1/Temporary Share/", "ignoreAppleDouble": boolean }
     */
    public AcFileSystemDriver(FileStore fileStore,
                              FileSystemFactoryProvider provider,
                              Map<String, ?> env) throws IOException {

        super(fileStore, provider);

        setEnv(env);
    }

    @Override
    protected String getFilenameString(File entry) {
        return entry.getName();
    }

    @Override
    protected boolean isFolder(File entry) throws IOException {
        return entry.isDirectory();
    }

    @Override
    protected boolean exists(File entry) throws IOException {
        return entry.exists();
    }

    // VFS might have cache?
    @Override
    protected File getEntry(Path path) throws IOException {
        return getEntry(path, true);
    }

    /**
     * @param check check existence of the path
     */
    private File getEntry(Path path, boolean check) throws IOException {
//logger.log(Level.TRACE, "path: " + path);
        if (ignoreAppleDouble && path.getFileName() != null && isAppleDouble(path)) {
            throw new NoSuchFileException("ignore apple double file: " + path);
        }

        File entry = path.toFile();
//logger.log(Level.TRACE, "entry: " + entry + ", " + entry.exists());
        if (check) {
            if (entry.exists()) {
                return entry;
            } else {
                throw new NoSuchFileException(path.toString());
            }
        } else {
            return entry;
        }
    }

    @Override
    protected InputStream downloadEntry(File entry, Path path, Set<? extends OpenOption> options) throws IOException {
        return Files.newInputStream(entry.toPath());
    }

    @Override
    protected OutputStream uploadEntry(File parentEntry, Path path, Set<? extends OpenOption> options) throws IOException {
        File targetEntry = getEntry(path, false);
        Files.createFile(targetEntry.toPath());
        return Files.newOutputStream(targetEntry.toPath());
    }

    /** */
    protected List<File> getDirectoryEntries(File dirEntry, Path dir) throws IOException {
//System.err.println("path: " + dir);
//Arrays.stream(dirEntry.getChildren()).forEach(System.err::println);
        return Arrays.stream(dirEntry.listFiles()).collect(Collectors.toList());
    }

    @Override
    protected File createDirectoryEntry(File parentEntry, Path dir) throws IOException {
        File dirEntry = getEntry(dir, false);
        Files.createDirectory(dirEntry.toPath());
        return dirEntry;
    }

    @Override
    protected boolean hasChildren(File dirEntry, Path dir) throws IOException {
        return dirEntry.list().length > 0;
    }

    @Override
    protected void removeEntry(File entry, Path path) throws IOException {
        if (!entry.delete()) {
            throw new IOException("delete failed: " + path);
        }
    }

    @Override
    protected File copyEntry(File sourceEntry, File targetParentEntry, Path source, Path target, Set<CopyOption> options) throws IOException {
        File targetEntry = getEntry(target, false);
        Files.copy(sourceEntry.toPath(), targetEntry.toPath());
        return targetEntry;
    }

    @Override
    protected File moveEntry(File sourceEntry, File targetParentEntry, Path source, Path target, boolean targetIsParent) throws IOException {
        File targetEntry = getEntry(targetIsParent ? target.resolve(toFilenameString(source)) : target, false);
        Files.move(sourceEntry.toPath(), targetEntry.toPath());
        return targetEntry;
    }

    @Override
    protected File moveFolderEntry(File sourceEntry, File targetParentEntry, Path source, Path target, boolean targetIsParent) throws IOException {
        return moveEntry(sourceEntry, targetParentEntry, source, target, targetIsParent);
    }

    @Override
    protected File renameEntry(File sourceEntry, File targetParentEntry, Path source, Path target) throws IOException {
        return moveEntry(sourceEntry, targetParentEntry, source, target, false);
    }
}
