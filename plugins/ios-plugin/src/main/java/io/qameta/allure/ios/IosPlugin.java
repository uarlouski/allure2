package io.qameta.allure.ios;

import io.qameta.allure.Reader;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xmlwise.Plist;
import xmlwise.XmlParseException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.newDirectoryStream;

/**
 * @author charlie (Dmitry Baev).
 */
public class IosPlugin implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(IosPlugin.class);

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final JacksonContext jacksonContext = configuration.requireContext(JacksonContext.class);

        final List<Path> files = listResults(directory);
        for (Path file : files) {
            try {
                final Object loaded = Plist.loadObject(file.toFile());
                jacksonContext.getValue().writeValue(System.out, loaded);
            } catch (XmlParseException | IOException e) {
                LOGGER.error("Could not parse file {}: {}", file, e);
            }
        }
    }

    private static List<Path> listResults(final Path directory) {
        List<Path> result = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return result;
        }

        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, "*.plist")) {
            for (Path path : directoryStream) {
                if (!Files.isDirectory(path)) {
                    result.add(path);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not read data from {}: {}", directory, e);
        }
        return result;
    }
}
