package me.asu.run;

public abstract class OsUtils {

    public enum OSType {
        OS_TYPE_LINUX, OS_TYPE_WIN, OS_TYPE_SOLARIS, OS_TYPE_MAC, OS_TYPE_FREEBSD, OS_TYPE_OTHER
    }

    public static final OSType  osType  = getOSType();

    public static final boolean WINDOWS = (osType == OSType.OS_TYPE_WIN);
    // OSType detection
    public static final boolean SOLARIS = (osType == OSType.OS_TYPE_SOLARIS);
    public static final boolean MAC     = (osType == OSType.OS_TYPE_MAC);
    public static final boolean FREEBSD = (osType == OSType.OS_TYPE_FREEBSD);
    // Helper static vars for each platform
    public static final boolean LINUX   = (osType == OSType.OS_TYPE_LINUX);
    public static final boolean OTHER   = (osType == OSType.OS_TYPE_OTHER);

    private static OSType getOSType() {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            return OSType.OS_TYPE_WIN;
        } else if (osName.contains("SunOS") || osName.contains("Solaris")) {
            return OSType.OS_TYPE_SOLARIS;
        } else if (osName.contains("Mac")) {
            return OSType.OS_TYPE_MAC;
        } else if (osName.contains("FreeBSD")) {
            return OSType.OS_TYPE_FREEBSD;
        } else if (osName.startsWith("Linux")) {
            return OSType.OS_TYPE_LINUX;
        } else {
            // Some other form of Unix
            return OSType.OS_TYPE_OTHER;
        }
    }

}
