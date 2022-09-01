
import me.asu.run.dao.FileIndexDao;

public class Test {


    public static void main(String[] args) {
        FileIndexDao dao = new FileIndexDao();
        int ext = dao.deleteByExt("jar");
        System.out.println("delete " + ext + " jar");
        ext = dao.deleteByExt("JAR");
        System.out.println("delete " + ext + " JAR");
        ext = dao.deleteByExt("go");
        System.out.println("delete " + ext + " go");
        ext = dao.deleteByExt("sh");
        System.out.println("delete " + ext + " sh");
        ext = dao.deleteByExt("py");
        System.out.println("delete " + ext + " py");
        ext = dao.deleteByExt("pl");
        System.out.println("delete " + ext + " pl");
        ext = dao.deleteByExt("rb");
        System.out.println("delete " + ext + " rb");

    }


}
