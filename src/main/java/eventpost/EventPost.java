package eventpost;

import io.micronaut.runtime.Micronaut;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import eventpost.persist.MessagePersistanceFactory;

/**
 * @author ericjbruno
 */
public class EventPost {

    public static String configFile = "./eventpost.properties";

    static Runtime rt = Runtime.getRuntime();
    static RuntimeMXBean rtBean = ManagementFactory.getRuntimeMXBean();
    static MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
    static OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();

    static NumberFormat nf = NumberFormat.getNumberInstance();
    static NumberFormat percentage = NumberFormat.getPercentInstance(Locale.US);
    static DecimalFormat df2 = new DecimalFormat("#.##");
    static DateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");

    static String serverURLName = "logs";
    static String pageName = "";
    static String jvmVersion = "";
    static String classpath = "";
    static String os = "";

    String webappDirLocation = "src/webapp/";
    //private Logger logger = LoggerFactory.getLogger(EventPost.class);

    /////////////////////////////////////////////////////////////////////
    public static void main(String[] args) throws Exception {
        //EventPost server = new EventPost();

        // Initialize the message store before starting, so a bad persistence
        // setting or unreachable database fails now, not on the first request
        MessagePersistanceFactory.getInstance();

        Micronaut.run(EventPost.class);
    }

    public EventPost() {
        jvmVersion = System.getProperty("java.version") + "-" + System.getProperty("java.vm.version");
        os = System.getProperty("os.name") + ", " + System.getProperty("os.arch") + ", " + System.getProperty("os.version");
        if (rtBean != null) {
            classpath = rtBean.getClassPath();
        }
    }

    public static void log(String s) {
        System.out.println("[EventPost] " + s);
    }

}
