import java.util.LinkedHashMap;
import java.util.Map;

/** Каталог товаров. Гарантирует уникальность id товара. */
public class ProductCatalog {

    private final Map<String, Product> products = new LinkedHashMap<>();

    public void add(Product product) {
        if (products.containsKey(product.getId())) {
            throw new IllegalArgumentException("товар с id " + product.getId() + " уже есть в каталоге");
        }
        products.put(product.getId(), product);
    }

    public Product get(String id) {
        Product product = products.get(id);
        if (product == null) {
            throw new IllegalArgumentException("товар " + id + " не найден в каталоге");
        }
        return product;
    }

    public int size() {
        return products.size();
    }
}
