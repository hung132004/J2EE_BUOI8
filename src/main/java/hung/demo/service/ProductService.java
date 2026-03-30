package hung.demo.service;

import hung.demo.entity.Category;
import hung.demo.entity.Product;
import hung.demo.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo){
        this.repo = repo;
    }

    public List<Product> getAll(){
        return repo.findAll();
    }

    public Page<Product> searchProducts(String keyword, Integer categoryId, String sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<Product> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + keyword.trim().toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.join("category").get("id"), categoryId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repo.findAll(specification, pageable);
    }

    private Sort buildSort(String sort) {
        if ("priceAsc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.ASC, "price");
        }
        if ("priceDesc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "price");
        }
        return Sort.by(Sort.Direction.ASC, "id");
    }

    public Product save(Product p){
        return repo.save(p);
    }

    public void delete(Integer id){
        repo.deleteById(id);
    }

    public Product findById(Integer id){
        return repo.findById(id).orElse(null);
    }
}
