package software.amazonaws.example.product.product.dao;

import software.amazonaws.example.product.product.entity.Product;
import software.amazonaws.example.product.product.entity.Products;

import java.util.Optional;

public interface ProductDao {

  Optional<Product> getProduct(String id);

  void putProduct(Product product);

  void deleteProduct(String id);

  Products getAllProduct();
}
