import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Homepage expectations.
 *
 * The values come from shopbricks-data.json (the `homepage` block) so that
 * every expected value in the framework lives in one place.
 */
public final class HomePageData {

    private HomePageData() {
    }

    /** @return the raw `homepage` block */
    public static JsonNode homepage() {
        return DataProvider.homepage();
    }

    public static String shopByCategoryHeading() {
        return homepage().get("shopByCategoryHeading").asText();
    }

    public static String trustedBrandsHeading() {
        return homepage().get("trustedBrandsHeading").asText();
    }

    public static List<String> categories() {
        return textList(homepage().get("categories"));
    }

    public static List<String> sectionHeadings() {
        return textList(homepage().get("sectionHeadings"));
    }

    private static List<String> textList(JsonNode array) {
        List<String> values = new ArrayList<>();
        array.forEach(item -> values.add(item.asText()));
        return values;
    }
}
