/*
 * Copyright (c) ${year} by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.ac;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.webcodepro.applecommander.storage.DiskFactory;
import com.webcodepro.applecommander.storage.Disks;
import com.webcodepro.applecommander.storage.os.dos33.DosFormatDisk;
import org.applecommander.source.Source;
import org.applecommander.source.Sources;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 ${date} nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class TestCase {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property
    String dsk = "src/test/resources/test.dsk";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    void test1() throws Exception {
        Source source = Sources.create(dsk).orElseThrow();
        DiskFactory.Context context = Disks.inspect(source);
Debug.println("disks: " + context.disks.size());
        var disk = context.disks.get(0);
Debug.println(disk.getClass() + ", " + disk.getFormat());
disk.getDiskInformation().stream().map(o -> o.getLabel() + ": " + o.getValue()).forEach(System.out::println);
        if (disk instanceof DosFormatDisk dosDisk) {
Debug.println(disk.getDiskName());
            dosDisk.getFiles().forEach(entry -> System.out.printf("%-30s%s %8d %s %b%n", entry.getFilename(), entry.isDirectory() ? "/" : " ", entry.getSize(), entry.getFiletype(), entry.isDeleted()));
        }
    }
}
