package com.example.recommender;

import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity; // Changed from PearsonCorrelationSimilarity
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * A simple User-Based Recommendation System using Apache Mahout.
 * This class demonstrates how to load data, compute user similarity,
 * find nearest neighbors, and generate recommendations.
 */
public class RecommenderSystem {

    public static void main(String[] args) {
        try {
            // 1. Load the Data Model
            System.out.println("Attempting to load data.csv from resources...");
            URI fileUri = RecommenderSystem.class.getClassLoader().getResource("data.csv").toURI();
            File file = new File(fileUri);
            System.out.println("Attempting to load data from: " + file.getAbsolutePath());

            DataModel model = new FileDataModel(file);
            System.out.println("DataModel loaded successfully. Total users: " + model.getNumUsers() + ", Total items: " + model.getNumItems());

            // 2. Compute User Similarity
            // CHANGED: Using EuclideanDistanceSimilarity for potentially better results with sparse data
            System.out.println("Computing user similarity using Euclidean Distance Similarity...");
            UserSimilarity similarity = new EuclideanDistanceSimilarity(model);
            System.out.println("User similarity computed.");

            // 3. Define User Neighborhood
            // CHANGED: Use model.getNumUsers() to consider all users as potential neighbors
            // This ensures we're not missing neighbors due to a small 'N'
            System.out.println("Finding nearest " + model.getNumUsers() + " user neighbors (all users)...");
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(model.getNumUsers(), similarity, model);
            System.out.println("User neighborhood defined.");

            // 4. Build the Recommender
            System.out.println("Building the user-based recommender...");
            Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
            System.out.println("Recommender built.");

            // 5. Generate Recommendations for a specific user
            long userID = 3; // The user for whom we want recommendations
            int numRecommendations = 2; // The number of recommendations to generate

            System.out.println("\n--- Generating recommendations for user ID: " + userID + " ---");
            List<RecommendedItem> recommendations = recommender.recommend(userID, numRecommendations);

            // 6. Display Recommendations
            if (recommendations.isEmpty()) {
                System.out.println("No recommendations found for user " + userID + " at this time.");
                System.out.println("This could be due to:");
                System.out.println(" - The user having rated all available items.");
                System.out.println(" - No similar users (neighbors) found who have rated unrated items.");
                System.out.println(" - Insufficient data for similarity calculation (e.g., too few common ratings).");
                System.out.println(" - Predicted scores for unrated items are too low to be recommended.");
            } else {
                System.out.println("Top " + recommendations.size() + " recommendations for user " + userID + ":");
                for (RecommendedItem item : recommendations) {
                    System.out.println("  - Item ID: " + item.getItemID() + " | Predicted Score: " + String.format("%.2f", item.getValue()));
                }
            }

        } catch (Exception e) {
            System.err.println("An error occurred during recommendation generation:");
            e.printStackTrace();
        }
    }
}
