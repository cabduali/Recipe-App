package com.RecipeBook.RecipeBookproject.Repository;

import com.RecipeBook.RecipeBookproject.Model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByNameContaining(String keyword);
}
