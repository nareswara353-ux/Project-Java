package com.example.enterprise.interfaces.rest;

import com.example.enterprise.application.port.ProductService;
import com.example.enterprise.domain.Product;
import com.example.enterprise.interfaces.rest.dto.ProductBulkRequest;
import com.example.enterprise.interfaces.rest.dto.ProductRequest;
import com.example.enterprise.interfaces.rest.dto.ProductResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = productService.createProduct(
                request.name(),
                request.price(),
                request.stock()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponse.fromDomain(product));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponse>> createProductsBulk(
            @Valid @RequestBody ProductBulkRequest request) {
        List<ProductService.CreateCommand> commands = request.items().stream()
                .map(r -> new ProductService.CreateCommand(r.name(), r.price(), r.stock()))
                .toList();
        List<Product> products = productService.createProductsBulk(commands);
        List<ProductResponse> responses = products.stream()
                .map(ProductResponse::fromDomain)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }


    @GetMapping(value = "/export", produces = "text/csv")
    public void exportProducts(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"products-" + Instant.now().getEpochSecond() + ".csv\"");

        List<Product> products = productService.getAllProducts();

        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withHeader("id", "name", "price", "stock"))) {
            for (Product p : products) {
                printer.printRecord(p.id(), p.name(), p.price(), p.stock());
            }
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> importProducts(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        List<ProductService.CreateCommand> commands = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        int rowNum = 2;

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withTrim()
                     .withIgnoreEmptyLines()
                     .parse(reader)) {

            if (!parser.getHeaderMap().containsKey("name")
                    || !parser.getHeaderMap().containsKey("price")
                    || !parser.getHeaderMap().containsKey("stock")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "CSV must contain headers: name, price, stock"
                ));
            }

            for (CSVRecord record : parser) {
                try {
                    String name = record.get("name");
                    double price = Double.parseDouble(record.get("price"));
                    int stock = Integer.parseInt(record.get("stock"));
                    if (name == null || name.isBlank()) {
                        throw new IllegalArgumentException("name cannot be blank");
                    }
                    if (price <= 0) {
                        throw new IllegalArgumentException("price must be > 0");
                    }
                    if (stock < 0) {
                        throw new IllegalArgumentException("stock must be >= 0");
                    }
                    commands.add(new ProductService.CreateCommand(name, price, stock));
                } catch (Exception e) {
                    errors.add(Map.of("row", rowNum, "error", e.getMessage()));
                }
                rowNum++;
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid CSV: " + e.getMessage()));
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "created", 0,
                    "errors", errors
            ));
        }

        if (commands.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No data rows found"));
        }

        try {
            List<Product> created = productService.createProductsBulk(commands);
            List<ProductResponse> responses = created.stream()
                    .map(ProductResponse::fromDomain)
                    .toList();
            return ResponseEntity.ok(Map.of(
                    "created", created.size(),
                    "products", responses
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {
        Product product = productService.updateProduct(
                id,
                request.name(),
                request.price(),
                request.stock()
        );
        return ResponseEntity.ok(ProductResponse.fromDomain(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ProductResponse.fromDomain(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> responses = productService.getAllProducts()
                .stream()
                .map(ProductResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String name) {
        List<ProductResponse> responses = productService.searchProducts(name)
                .stream()
                .map(ProductResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countProducts() {
        return ResponseEntity.ok(Map.of("count", productService.countProducts()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> restoreProduct(@PathVariable UUID id) {
        Product product = productService.restoreProduct(id);
        return ResponseEntity.ok(ProductResponse.fromDomain(product));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> adjustStock(
            @PathVariable UUID id,
            @RequestParam int delta) {
        Product product = productService.adjustStock(id, delta);
        return ResponseEntity.ok(ProductResponse.fromDomain(product));
    }
}
