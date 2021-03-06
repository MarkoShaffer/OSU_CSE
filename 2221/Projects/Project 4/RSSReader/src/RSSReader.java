import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to convert an XML RSS (version 2.0) feed from a given URL into the
 * corresponding HTML output file.
 *
 * @author Ryan Shaffer
 *
 */
public final class RSSReader {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private RSSReader() {
    }

    /**
     * Outputs the "opening" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * <html> <head> <title>the channel tag title as the page title</title>
     * </head> <body>
     * <h1>the page title inside a link to the <channel> link</h1>
     * <p>
     * the channel description
     * </p>
     * <table border="1">
     * <tr>
     * <th>Date</th>
     * <th>Source</th>
     * <th>News</th>
     * </tr>
     *
     * @param channel
     *            the channel element XMLTree
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the root of channel is a <channel> tag] and out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    private static void outputHeader(XMLTree channel, SimpleWriter out) {
        assert channel != null : "Violation of: channel is not null";
        assert out != null : "Violation of: out is not null";
        assert channel.isTag() && channel.label().equals("channel") : ""
                + "Violation of: the label root of channel is a <channel> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        // Initialize title, description, and link from channel
        String title, description, link;
        int titleIndex = getChildElement(channel, "title");
        int descriptionIndex = getChildElement(channel, "description");
        int linkIndex = getChildElement(channel, "link");

        // Check title
        if (channel.child(titleIndex).numberOfChildren() == 0) {
            title = "Empty Title";
        }

        else {
            title = channel.child(titleIndex).child(0).label();
        }

        // Check description
        if (channel.child(descriptionIndex).numberOfChildren() == 0) {
            description = "No Description";
        }

        else {
            description = channel.child(descriptionIndex).child(0).label();
        }

        // Get link
        link = channel.child(linkIndex).child(0).label();

        // Printing header title, description,
        out.print("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\r\n"
                + "<head>\r\n"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\" />\r\n"
                + "<title>" + title + "</title>\n</head>");

        out.print("<body>\n<h1>\n<a href=\"" + link + "\">" + title
                + "\n</a>\n</h1>");
        out.print("<p>\n" + description + "\n<p>");

        out.print("<table border=\"1\">\r\n" + "<tr>\r\n" + "<th>Date</th>\r\n"
                + "<th>Source</th>\r\n" + "<th>News</th>\r\n" + "</tr>");
    }

    /**
     * Outputs the "closing" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * </table>
     * </body> </html>
     *
     * @param out
     *            the output stream
     * @updates out.contents
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    private static void outputFooter(SimpleWriter out) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";

        out.print("</table>\n</body>\n</html>");
    }

    /**
     * Finds the first occurrence of the given tag among the children of the
     * given {@code XMLTree} and return its index; returns -1 if not found.
     *
     * @param xml
     *            the {@code XMLTree} to search
     * @param tag
     *            the tag to look for
     * @return the index of the first child of type tag of the {@code XMLTree}
     *         or -1 if not found
     * @requires [the label of the root of xml is a tag]
     * @ensures <pre>
     * getChildElement =
     *  [the index of the first child of type tag of the {@code XMLTree} or
     *   -1 if not found]
     * </pre>
     */
    private static int getChildElement(XMLTree xml, String tag) {
        assert xml != null : "Violation of: xml is not null";
        assert tag != null : "Violation of: tag is not null";
        assert xml.isTag() : "Violation of: the label root of xml is a tag";

        for (int i = 0; i < xml.numberOfChildren(); i++) {
            if (xml.child(i).label().equalsIgnoreCase(tag)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Processes one news item and outputs one table row. The row contains three
     * elements: the publication date, the source, and the title (or
     * description) of the item.
     *
     * @param item
     *            the news item
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the label of the root of item is an <item> tag] and
     *           out.is_open
     * @ensures <pre>
     * out.content = #out.content *
     *   [an HTML table row with publication date, source, and title of news item]
     * </pre>
     */
    private static void processItem(XMLTree item, SimpleWriter out) {
        assert item != null : "Violation of: item is not null";
        assert out != null : "Violation of: out is not null";
        assert item.isTag() && item.label().equals("item") : ""
                + "Violation of: the label root of item is an <item> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        // Initialize variables
        String pubDate, source, url, title, link;
        int pubDateIndex, sourceIndex, titleIndex, linkIndex;

        // Publication date
        pubDateIndex = getChildElement(item, "pubDate");
        if (pubDateIndex != -1) {
            pubDate = item.child(pubDateIndex).child(pubDateIndex).child(0)
                    .label();
        } else {
            pubDate = "No date available";
        }

        // Source
        sourceIndex = getChildElement(item, "source");
        if (sourceIndex != -1) {
            source = item.child(sourceIndex).child(0).label();
            url = item.child(sourceIndex).attributeValue("url");
        } else {
            source = "No source available";
            url = "";
        }

        // Title or description
        titleIndex = getChildElement(item, "title");
        if (titleIndex != -1) {
            if (item.child(titleIndex).numberOfChildren() > 0
                    && item.child(titleIndex).child(0).label().length() > 0) {
                title = item.child(titleIndex).child(0).label();
            } else {
                title = "No title available";
            }
        } else {
            titleIndex = getChildElement(item, "description");
            if (item.child(titleIndex).numberOfChildren() > 0
                    && item.child(titleIndex).child(0).label().length() > 0) {
                title = item.child(titleIndex).child(0).label();
            } else {
                title = "No title available";
            }
        }

        // Link
        linkIndex = getChildElement(item, "link");
        if (linkIndex != -1) {
            link = item.child(linkIndex).child(0).label();
        } else {
            link = " ";
        }

        out.print("\n<tr>\n<th>" + pubDate + "</th>");
        out.print("\n<th><a href=\"" + url + "\">" + source + "</a></th>");
        out.print(
                "\n<th><a href=\"" + link + "\">" + title + "</a></th>\n</tr>");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        // Initialize
        SimpleWriter file;
        out.print("Enter link of RSS 2.0 feed: ");
        String rss = in.nextLine();
        XMLTree xml = new XMLTree1(rss);

        // Validity
        if (xml.label().equals("rss")
                && xml.attributeValue("version").equals("2.0")) {

            // Create file
            String filename = "";
            final String ext = ".html";
            while (filename.length() <= ext.length() || !filename
                    .substring(filename.length() - ext.length()).equals(ext)) {
                out.print("Enter name of output file. Include .html: ");
                filename = in.nextLine();
            }
            file = new SimpleWriter1L(filename);

            XMLTree channel = xml.child(0);
            outputHeader(channel, file);

            // Go through items/outputs
            for (int i = 0; i < channel.numberOfChildren(); i++) {
                if (channel.child(i).label().equalsIgnoreCase("item")) {
                    processItem(channel.child(i), file);
                }
            }

            outputFooter(file);

            file.close();
        } else {
            out.print("Invalid link. Quitting.");
        }

        /*
         * closes input and output streams
         */
        in.close();
        out.close();
    }

}