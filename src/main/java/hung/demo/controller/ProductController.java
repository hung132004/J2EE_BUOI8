package hung.demo.controller;

import hung.demo.entity.Category;
import hung.demo.entity.Product;
import hung.demo.service.CartService;
import hung.demo.service.CategoryService;
import hung.demo.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
public class ProductController {

    private static final Path UPLOAD_DIR = Path.of("uploads");

    private final ProductService service;
    private final CategoryService categoryService;
    private final CartService cartService;

    public ProductController(ProductService service, CategoryService categoryService, CartService cartService){
        this.service = service;
        this.categoryService = categoryService;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public String home(){
        return "redirect:/products";
    }

    @GetMapping("/products")
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(required = false) Integer categoryId,
                       @RequestParam(defaultValue = "") String sort,
                       @RequestParam(defaultValue = "0") int page,
                       Model model,
                       HttpSession session){
        Page<Product> productPage = service.searchProducts(keyword, categoryId, sort, page, 5);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("hasNext", productPage.hasNext());
        model.addAttribute("hasPrevious", productPage.hasPrevious());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("cartItemCount", cartService.getTotalQuantity(session));
        return "product_list";
    }

    @GetMapping("/products/add")
    public String addForm(Model model){
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        return "product_add";
    }

    @PostMapping("/products/save")
    public String save(Product product,
                       @RequestParam(required = false) Integer categoryId,
                       @RequestParam("imageFile") MultipartFile imageFile){
        product.setCategory(resolveCategory(categoryId));
        product.setImage(storeImage(imageFile, null));
        service.save(product);
        return "redirect:/products";
    }

    @GetMapping("/products/delete/{id}")
    public String delete(@PathVariable Integer id){
        service.delete(id);
        return "redirect:/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model){
        Product product = service.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        return "product_edit";
    }

    @PostMapping("/products/update")
    public String update(Product product,
                         @RequestParam(required = false) Integer categoryId,
                         @RequestParam("imageFile") MultipartFile imageFile,
                         @RequestParam(required = false) String existingImage){
        product.setCategory(resolveCategory(categoryId));
        product.setImage(storeImage(imageFile, existingImage));
        service.save(product);
        return "redirect:/products";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Integer productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            @RequestParam(defaultValue = "") String keyword,
                            @RequestParam(required = false) Integer categoryId,
                            @RequestParam(defaultValue = "") String sort,
                            @RequestParam(defaultValue = "0") int page,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Product product = service.findById(productId);
        if (product != null) {
            cartService.addToCart(session, product, Math.max(quantity, 1));
            redirectAttributes.addFlashAttribute("message", "Da them san pham vao gio hang.");
        }

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("categoryId", categoryId);
        redirectAttributes.addAttribute("sort", sort);
        redirectAttributes.addAttribute("page", page);
        return "redirect:/products";
    }

    private Category resolveCategory(Integer categoryId) {
        return categoryService.findById(categoryId);
    }

    private String storeImage(MultipartFile imageFile, String fallbackImage) {
        if (imageFile == null || imageFile.isEmpty()) {
            return fallbackImage;
        }

        try {
            Files.createDirectories(UPLOAD_DIR);
            String originalFilename = imageFile.getOriginalFilename() == null ? "image" : Path.of(imageFile.getOriginalFilename()).getFileName().toString();
            String storedFilename = UUID.randomUUID() + "_" + originalFilename;
            Path target = UPLOAD_DIR.resolve(storedFilename);
            Files.copy(imageFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + storedFilename;
        } catch (IOException e) {
            throw new RuntimeException("Khong the luu file anh.", e);
        }
    }
}
