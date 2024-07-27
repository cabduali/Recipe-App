package com.RecipeBook.RecipeBookproject.Controller;

import com.RecipeBook.RecipeBookproject.Model.Category;
import com.RecipeBook.RecipeBookproject.Model.Recipe;
import com.RecipeBook.RecipeBookproject.Service.CategoryService;
import com.RecipeBook.RecipeBookproject.Service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final CategoryService categoryService;

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    public RecipeController(RecipeService recipeService, CategoryService categoryService) {
        this.recipeService = recipeService;
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public String listRecipes(Model model) {
        List<Recipe> recipes = recipeService.getAllRecipes();
        model.addAttribute("recipes", recipes);
        return "recipe-list";
    }

    @GetMapping
    public String showRecipes(Model model) {
        List<Recipe> recipes = recipeService.getAllRecipes();
        model.addAttribute("recipes", recipes);
        return "recipes";
    }

    @GetMapping("/new")
    public String showRecipeForm(Model model) {
        model.addAttribute("recipe", new Recipe());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "recipe-form";
    }

    @PostMapping
    public String saveRecipe(@ModelAttribute Recipe recipe, @RequestParam List<Long> categoryIds, @RequestParam("image") MultipartFile image) {
        Set<Category> categories = new HashSet<>();
        for (Long id : categoryIds) {
            categories.add(categoryService.getCategoryById(id));
        }
        recipe.setCategories(categories);

        // Handle image upload
        if (!image.isEmpty()) {
            try {
                String uploadDir = new File("src/main/resources/static/uploads").getAbsolutePath();
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                Files.createDirectories(filePath.getParent());
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                recipe.setImageUrl("/uploads/" + fileName); // Ensure this path is correct
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        recipeService.saveRecipe(recipe);
        return "redirect:/recipes";
    }

    @GetMapping("/{id}")
    public String getRecipeDetails(@PathVariable Long id, Model model) {
        Recipe recipe = recipeService.getRecipeById(id);
        model.addAttribute("recipe", recipe);
        return "recipe-detail";
    }

    @GetMapping("/edit/{id}")
    public String showEditRecipeForm(@PathVariable("id") Long id, Model model) {
        Recipe recipe = recipeService.getRecipeById(id);
        if (recipe == null) {
            return "redirect:/recipes/list";
        }
        model.addAttribute("recipe", recipe);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "recipe-form";
    }


    @PostMapping("/edit/{id}")
    public String updateRecipe(@PathVariable("id") Long id, @ModelAttribute Recipe updatedRecipe, @RequestParam List<Long> categoryIds, @RequestParam("image") MultipartFile image) {
        Recipe existingRecipe = recipeService.getRecipeById(id);
        if (existingRecipe != null) {
            existingRecipe.setName(updatedRecipe.getName());
            existingRecipe.setDescription(updatedRecipe.getDescription());
            existingRecipe.setCookingTime(updatedRecipe.getCookingTime());

            // Handle image upload
            if (!image.isEmpty()) {
                try {
                    String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                    String uploadDir = new File("src/main/resources/static/uploads").getAbsolutePath();
                    Path filePath = Paths.get(uploadDir, fileName);
                    Files.createDirectories(filePath.getParent());
                    Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    existingRecipe.setImageUrl("/uploads/" + fileName); // Save the filename in the database
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Update categories
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : categoryIds) {
                categories.add(categoryService.getCategoryById(categoryId));
            }
            existingRecipe.setCategories(categories);

            recipeService.saveRecipe(existingRecipe);
        }
        return "redirect:/recipes/list";
    }
    @PostMapping("/delete/{id}")
    public String deleteRecipe(@PathVariable("id") Long id) {
        recipeService.deleteRecipe(id);
        return "redirect:/recipes/list";
    }

    @GetMapping("/search")
    public String searchRecipes(@RequestParam("keyword") String keyword, Model model) {
        List<Recipe> recipes = recipeService.searchRecipes(keyword);
        model.addAttribute("recipes", recipes);
        return "recipe-list";
    }
}
