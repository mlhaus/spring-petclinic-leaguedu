package org.springframework.samples.petclinic.codesignal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

	private final RecipeService recipeService;

	private final RecipeRepository recipeRepository;

	public RecipeController(RecipeService recipeService, RecipeRepository recipeRepository) {
		this.recipeRepository = recipeRepository;
		this.recipeService = recipeService;
	}

	@GetMapping
	public ResponseEntity<List<Recipe>> getAllRecipes(@RequestParam Optional<String> type) {
		try {
			List<Recipe> recipes;
			if (type.isPresent()) {
				recipes = recipeService.getRecipesByType(type.get());
			}
			else {
				recipes = recipeRepository.findAll();
			}
			return ResponseEntity.ok(recipes); // 200
		}
		catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
		}
	}

	@GetMapping("/category/{recipeCategory}")
	public ResponseEntity<List<Recipe>> getRecipesByCategoryAndDietaryPreference(
		@PathVariable String recipeCategory,
		@RequestParam Optional<String> dietaryPreference
	) {
		List<Recipe> recipes = recipeService.findByCategoryAndDietaryPreferenceIgnoreCase(
			recipeCategory, 
			dietaryPreference.orElse(null)
		);
		if(recipes.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(recipes);
	}

	@GetMapping("/{recipeId}")
	public ResponseEntity<Recipe> getRecipe(@PathVariable("recipeId") Long id) {
		return recipeRepository.findById(id)
			.map(recipe -> ResponseEntity.ok(recipe)) // 200
			.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build()); // 404
	}

	@PostMapping("/new")
	public ResponseEntity<Recipe> addRecipe(@RequestBody Recipe recipe) {
		if (recipe == null) {
			return ResponseEntity.badRequest().build(); // 400
		}
		Recipe savedRecipe = recipeRepository.save(recipe);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedRecipe);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Recipe> updateRecipe(
		@PathVariable Long id,
		@RequestBody Recipe updatedRecipe
	) {
		return recipeRepository.findById(id).map(existingRecipe -> {
			existingRecipe.setIngredients(updatedRecipe.getIngredients());
			existingRecipe.setInstructions(updatedRecipe.getInstructions());
			existingRecipe.setType(updatedRecipe.getType());
			existingRecipe.setCategory(updatedRecipe.getCategory());
			existingRecipe.setDietaryPreference(updatedRecipe.getDietaryPreference());
			existingRecipe.setInternalNotes(updatedRecipe.getInternalNotes());
			recipeRepository.save(existingRecipe);
			return ResponseEntity.ok(existingRecipe); // 200
		}).orElseGet(() -> ResponseEntity.notFound().build()); // 404
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
		if (!recipeRepository.existsById(id)) {
			return ResponseEntity.notFound().build(); // 404
		}
		recipeRepository.deleteById(id);
		return ResponseEntity.noContent().build(); // 204
	}
}
