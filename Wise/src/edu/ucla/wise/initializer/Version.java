package edu.ucla.wise.initializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class Version {

    private Version() {
        // empty constructor
    }

    public static final String VERSION = "1.0.0";

    public static final String GIT_FILE_NAME = "WISE_git.txt";

    public static String getChangeLogText() {
        BufferedReader reader = null;
        try {
            StringBuilder builder = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(Version.class.getClassLoader().getResourceAsStream(
                    GIT_FILE_NAME)));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    return builder.toString();
                }
                builder.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Changelog is not available";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
    }

    public static void main(final String[] args) throws IOException {
        System.out.println("Version: " + VERSION);
        System.out.println();
        System.out.println("Press <Enter> for changelog");
        System.in.read();
        System.out.println(getChangeLogText());
    }
}
