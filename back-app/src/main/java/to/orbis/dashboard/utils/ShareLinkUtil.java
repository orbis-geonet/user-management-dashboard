package to.orbis.dashboard.utils;

public class ShareLinkUtil {

    public static String generateShareLink(String tmp, String name, Long count) {
        var shareLinkGroupName = count == 0 ?
                name.replace(" ", "").toLowerCase() :
                name.replace(" ", "").toLowerCase() + "-" + count;
        return tmp + shareLinkGroupName;
    }
}
