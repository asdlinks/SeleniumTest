package support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class CategoryProducts {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = CategoryProducts.class.getClassLoader()
                .getResourceAsStream("category-products.properties")) {
            if (in != null) {
                PROPERTIES.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load category-products.properties", e);
        }
    }

    private CategoryProducts() {
    }

    public static String path(String category) {
        return PROPERTIES.getProperty(category + ".path");
    }

    public static String heading(String category) {
        return PROPERTIES.getProperty(category + ".heading");
    }

    public static String product(String category) {
        return PROPERTIES.getProperty(category + ".product");
    }
}
