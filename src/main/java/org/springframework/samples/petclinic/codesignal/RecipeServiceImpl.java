package org.springframework.samples.petclinic.codesignal;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeServiceImpl implements RecipeService{
	private final RecipeRepository recipeRepository;

	public RecipeServiceImpl(RecipeRepository recipeRepository) {
		this.recipeRepository = recipeRepository;
	}

	@Override
	public List<Recipe> getRecipesByType(String type) {
		if(type == null || type.isEmpty()) {
			return recipeRepository.findAll();
		}
		return recipeRepository.findByTypeIgnoreCase(type);
	}

	@Override
	public List<Recipe> findByCategoryAndDietaryPreferenceIgnoreCase(String category, String preference) {
		// 1. Get all recipes in that category from the DB
		List<Recipe> categoryRecipes = recipeRepository.findByCategoryIgnoreCase(category);

		// 2. If a preference was provided, filter the list in Java
		if (preference != null && !preference.isEmpty()) {
			return categoryRecipes.stream()
				.filter(recipe -> recipe.getDietaryPreference().equalsIgnoreCase(preference))
				.collect(Collectors.toList());
		}

		// 3. Otherwise, return everything in that category
		return categoryRecipes;
	}
}
