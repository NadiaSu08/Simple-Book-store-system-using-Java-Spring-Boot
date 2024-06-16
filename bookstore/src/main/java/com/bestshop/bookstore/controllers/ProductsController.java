package com.bestshop.bookstore.controllers;

import java.io.InputStream;

import java.nio.file.*;
import java.util.Date;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
//import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.bestshop.bookstore.models.Product;
import com.bestshop.bookstore.models.ProductDTO;
import com.bestshop.bookstore.services.ProductsRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductsController {
	
	@Autowired
	private ProductsRepository repo;
	
	@GetMapping({"","/"})
	public String showProductList(Model model) {
		List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("products", products);
		return "products/index";
	}
	@GetMapping({"/create"})
	public String showCreatePage(Model model) {
		ProductDTO ProductDTO = new ProductDTO();
		//List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("ProductDTO", ProductDTO);
		return "products/CreateProduct";
	}
	@PostMapping("/create")
	public String createProduct(@Valid @ModelAttribute("ProductDTO") ProductDTO ProductDTO, BindingResult result, Model model) {

	    if (result.hasErrors()) {
	        return "products/CreateProduct";
	    }

	    // Save image file
	    MultipartFile image = ProductDTO.getImageFile();
	    //String originalFileName = image.getOriginalFilename();
	    
	    // Generate a unique storage file name
	    Date createdAt = new Date();
	    String storageFileName = createdAt.getTime() + "-" + image.getOriginalFilename();

	    try {
	        String uploadDir = "public/Book images/";
	        Path uploadPath = Paths.get(uploadDir);
	        
	        if (!Files.exists(uploadPath)) {
	            Files.createDirectories(uploadPath);
	        }
	        
	        try (InputStream inputStream = image.getInputStream()) {
	            Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
	                    StandardCopyOption.REPLACE_EXISTING);
	        } catch (Exception ex) {
	            System.out.println("Exception: " + ex.getMessage());
	        }
	        
	        // Create a Product object and save it to the repository
	        Product Product = new Product();
	        Product.setName(ProductDTO.getName());
	        Product.setBrand(ProductDTO.getBrand());
	        Product.setCategory(ProductDTO.getCategory());
	        Product.setPrice(ProductDTO.getPrice());
	        Product.setDescription(ProductDTO.getDescription());
	        Product.setCreatedAt(createdAt);
	        Product.setImageFileName(storageFileName);
	        
	        repo.save(Product);

	    } catch (Exception e) {
	        e.printStackTrace();
	        // Handle exception appropriately, e.g., show error message to user
	        return "error-page"; // Replace with appropriate error handling
	    }

	    return "redirect:/products";
	}
	
	@GetMapping("/edit/{id}")
	public String showEditPage(@PathVariable int id, Model model) {

	    try {
	        Product Product = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
	        model.addAttribute("Product", Product);
	        
	        ProductDTO ProductDTO = new ProductDTO();
	        ProductDTO.setName(Product.getName());
	        ProductDTO.setBrand(Product.getBrand());
	        ProductDTO.setCategory(Product.getCategory());
	        ProductDTO.setPrice(Product.getPrice());
	        ProductDTO.setDescription(Product.getDescription());
	            
	        model.addAttribute("ProductDTO", ProductDTO);
	      
	    } catch (Exception e) {
	        e.printStackTrace();
	        // Log the exception for debugging purposes
	        // Handle exception appropriately, e.g., show error message to user
	        return "error-page"; // Replace with appropriate error handling
	    }
	    
	    return "products/EditProduct"; // for form submission
	}
	
	@PostMapping("/edit/{id}")
	public String UpdateProduct(Model model, @PathVariable int id, @Valid @ModelAttribute("ProductDTO") ProductDTO ProductDTO, BindingResult result ) {
	
		 try {
		        Product Product = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
		        model.addAttribute("Product", Product);
		        
		        if (result.hasErrors()) {
			        return "products/EditProduct";
			    }
		        
		        if(!ProductDTO.getImageFile().isEmpty()) {
		        	
		        	String uploadDir = "public/Book images/";
			        Path oldImagePath = Paths.get(uploadDir + Product.getImageFileName());
			        
			        try {
			        	Files.delete(oldImagePath);
			        }catch (Exception e) {
				        e.printStackTrace();
				        return "error-page";
		        	
		        }
			        
			        MultipartFile image = ProductDTO.getImageFile();
			        Date createdAt = new Date();
			        String storageFileName = createdAt.getTime() + "_" +image.getOriginalFilename();
			        
			        try (InputStream inputStream = image.getInputStream()) {
			            Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
			                    StandardCopyOption.REPLACE_EXISTING);
			        } 
			        
			        Product.setImageFileName(storageFileName);
			        }
		        Product.setName(ProductDTO.getName());
		        Product.setBrand(ProductDTO.getBrand());
		        Product.setCategory(ProductDTO.getCategory());
		        Product.setPrice(ProductDTO.getPrice());
		        Product.setDescription(ProductDTO.getDescription());
		        repo.save(Product);
		        }
			        catch (Exception ex) {
			            System.out.println("Exception: " + ex.getMessage());
			        }
		       
		return "redirect:/products";

	}	 
	
	@GetMapping("/delete/{id}")
	
	public String deleteProduct(@PathVariable int id) {
		
		
		try {
			Product Product = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
			Path imagePath = Paths.get("public/Book images/" + Product.getImageFileName());
			try {
				Files.delete(imagePath);
			}catch (Exception ex) {
	            System.out.println("Exception: " + ex.getMessage());
        }
			repo.delete(Product);}catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
		return "redirect:/products";
	}	
   
	}
