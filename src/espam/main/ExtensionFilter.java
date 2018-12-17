package espam.main;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Class, filtering files in derectory by extension
 */
    public class ExtensionFilter implements FilenameFilter {

        private final String extension;

        public ExtensionFilter(String ext) {
            extension = ext;
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(extension);
        }

    }
