package software.amazonaws.example.product.product.dao;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazonaws.example.product.product.entity.Product;
import software.amazonaws.example.product.product.entity.Products;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class DynamoProductDao implements ProductDao {
  private static final Logger logger = LoggerFactory.getLogger(DynamoProductDao.class);

  private String PRODUCT_TABLE_NAME = System.getenv("PRODUCT_TABLE_NAME");

  private final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
    .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
    .httpClient(UrlConnectionHttpClient.builder().build())
    .overrideConfiguration(ClientOverrideConfiguration.builder()
      .addExecutionInterceptor(new TracingInterceptor())
      .build())
    .build();

  @Override
  public Optional<Product> getProduct(String id) {
    GetItemResponse getItemResponse = dynamoDbClient.getItem(GetItemRequest.builder()
      .key(Map.of("PK", AttributeValue.builder().s(id).build()))
      .tableName(PRODUCT_TABLE_NAME)
      .build());

    if (getItemResponse.hasItem()) {
      return Optional.of(ProductMapper.productFromDynamoDB(getItemResponse.item()));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void putProduct(Product product) {
    dynamoDbClient.putItem(PutItemRequest.builder()
      .tableName(PRODUCT_TABLE_NAME)
      .item(ProductMapper.productToDynamoDb(product))
      .build());
  }

  @Override
  public void deleteProduct(String id) {
    dynamoDbClient.deleteItem(DeleteItemRequest.builder()
      .tableName(PRODUCT_TABLE_NAME)
      .key(Map.of("PK", AttributeValue.builder().s(id).build()))
      .build());
  }

  @Override
  public Products getAllProduct() {
    ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder()
      .tableName(PRODUCT_TABLE_NAME)
      .build());

    logger.info("Scan returned: {} item(s)", scanResponse.count());

    List<Product> productList = new ArrayList<>();

    for (Map<String, AttributeValue> item : scanResponse.items()) {
      productList.add(ProductMapper.productFromDynamoDB(item));
    }

    return new Products(productList);
  }
}