package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Response.CRUDProductRecommentResponse;
import com.hmdrinks.Response.CRUDProductResponse;
import com.hmdrinks.Response.ListRecommendResponse;
import com.hmdrinks.Response.ProductImageResponse;
import com.hmdrinks.Service.utils.Utils;
import com.hmdrinks.Service.utils.ValueComparator;
import jakarta.transaction.Transactional;
import org.apache.hadoop.shaded.com.nimbusds.jose.shaded.json.JSONArray;
import org.apache.hadoop.shaded.com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


import java.util.*;

@Component
public class Recommender {

    private static final int NUM_NEIGHBOURHOODS = 15;
    private static final int NUM_RECOMMENDATIONS = 30;
    private static final float MIN_VALUE_RECOMMENDATION = 0;

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    /**
     * Map with the user id as key and its ratings as value that is a map with book ASIN as key and its rating as value
     */
    private Map<Long, Map<Long, Integer>> ratings;

    /**
     * Average rating of each user where the key is the user id and the value its average rating
     */
    private Map<Long, Double> averageRating;

    /**
     * Constructor
     */
    public Recommender() {
        ratings = new HashMap<>();
        averageRating = new HashMap<>();
    }

    public Map<Long, Map<Long, Integer>> getRatings() {
        return ratings;
    }

    private void setRatings(Map<Long, Map<Long, Integer>> ratings) {
        this.ratings = ratings;
    }

    public Map<Long, Double> getAverageRating() {
        return averageRating;
    }

    private void setAverageRating(Map<Long, Double> averageRating) {
        this.averageRating = averageRating;
    }

    /**
     * Get the k-nearest neighbourhoods using Pearson:
     * sim(i,j) = numerator / (sqrt(userDenominator^2) * sqrt(otherUserDenominator^2))
     * numerator = sum((r(u,i) - r(u)) * (r(v,i) - r(v)))
     * userDenominator = sum(r(u,i) - r(i))
     * otherUserDenominator = sum(r(v,i) - r(v))
     * r(u,i): rating of the book i by the user u
     * r(u): average rating of the user u
     *
     * @param userRatings ratings of the user
     * @return nearest neighbourhoods
     */
    private Map<Long, Double> getNeighbourhoods(Map<Long, Integer> userRatings) {
        Map<Long, Double> neighbourhoods = new HashMap<>();
        ValueComparator valueComparator = new ValueComparator(neighbourhoods);
        Map<Long, Double> sortedNeighbourhoods = new TreeMap<>(valueComparator);

        double userAverage = getAverage(userRatings);

        for (long user : ratings.keySet()) {
            ArrayList<Long> matches = new ArrayList<>();
            for (long productId : userRatings.keySet()) {
                if (ratings.get(user).containsKey(productId)) {
                    matches.add(productId);
                }
            }
            double matchRate;
            if (matches.size() > 0) {
                double numerator = 0, userDenominator = 0, otherUserDenominator = 0;
                for (long bookASIN : matches) {
                    double u = userRatings.get(bookASIN) - userAverage;
                    double v = ratings.get(user).get(bookASIN) - averageRating.get(user);

                    numerator += u * v;
                    userDenominator += u * u;
                    otherUserDenominator += v * v;
                }
                if (userDenominator == 0 || otherUserDenominator == 0) {
                    matchRate = 0;
                } else {
                    matchRate = numerator / (Math.sqrt(userDenominator) * Math.sqrt(otherUserDenominator));
                }
            } else {
                matchRate = 0;
            }

            neighbourhoods.put(user, matchRate);
        }
        sortedNeighbourhoods.putAll(neighbourhoods);

        Map<Long, Double> output = new TreeMap<>();

        Iterator entries = sortedNeighbourhoods.entrySet().iterator();
        int i = 0;
        while (entries.hasNext() && i < NUM_NEIGHBOURHOODS) {
            Map.Entry entry = (Map.Entry) entries.next();
            if ((double) entry.getValue() > 0) {
                output.put((long) entry.getKey(), (double) entry.getValue());
                i++;
            }
        }
        return output;
    }

    /**
     * Get predictions of each book by a user giving some ratings and its neighbourhood:
     * r(u,i) = r(u) + sum(sim(u,v) * (r(v,i) - r(v))) / sum(abs(sim(u,v)))
     * sim(u,v): similarity between u and v users
     * r(u,i): rating of the book i by the user u
     * r(u): average rating of the user u
     *
     * @param userRatings    ratings of the user
     * @param neighbourhoods nearest neighbourhoods
     * @param books          books in the database
     * @return predictions for each book
     */
    private Map<Long, Double> getRecommendations(Map<Long, Integer> userRatings,
                                                 Map<Long, Double> neighbourhoods, Map<Long, String> books) {

        Map<Long, Double> predictedRatings = new HashMap<>();

        // r(u)
        double userAverage = getAverage(userRatings);

        for (Long bookASIN : books.keySet()) {
            if (!userRatings.containsKey(bookASIN)) {

                // sum(sim(u,v) * (r(v,i) - r(v)))
                double numerator = 0;
                // sum(abs(sim(u,v)))
                double denominator = 0;

                for (Long neighbourhood : neighbourhoods.keySet()) {
                    if (ratings.get(neighbourhood).containsKey(bookASIN)) {
                        double matchRate = neighbourhoods.get(neighbourhood);
                        numerator +=
                                matchRate * (ratings.get(neighbourhood).get(bookASIN) - averageRating.get(neighbourhood));
                        denominator += Math.abs(matchRate);
                    }
                }

                double predictedRating = 0;
                if (denominator > 0) {
                    predictedRating = userAverage + numerator / denominator;
                    if (predictedRating > 5) {
                        predictedRating = 5;
                    }
                }
                predictedRatings.put(bookASIN, predictedRating);
            }
        }
        return predictedRatings;
    }

    /**
     * Get average of the ratings of a user
     *
     * @param userRatings ratings of a user
     * @return average or the ratings of a user
     */
    private double getAverage(Map<Long, Integer> userRatings) {
        double userAverage = 0;
        for (Map.Entry<Long, Integer> longIntegerEntry : userRatings.entrySet()) {
            userAverage += (int) ((Map.Entry) longIntegerEntry).getValue();
        }
        return userAverage / userRatings.size();

    }

    @Transactional
    public ResponseEntity<?> recommendedBooks(Long userId, UserRepository userRepository, ProductRepository productRepository) {

        Map<Long, Double> averageRating = new HashMap<>();
        Map<Long, Map<Long, Integer>> myRatesMap = new TreeMap<>();
        Map<Long, Map<Long, Integer>> userWithRatesMap = new TreeMap<>();

        User user = userRepository.findByUserIdAndIsDeletedFalse(Integer.valueOf(String.valueOf(userId)));
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user");
        }

        userRepository.findAll().forEach(userItem -> {
            Long userID = Long.parseLong(String.valueOf(userItem.getUserId()));
            Map<Long, Integer> userRatings = new HashMap<>();

            List<Review> reviews = reviewRepository.findByUser_UserId(userItem.getUserId());
            for (Review review : reviews) {
                userRatings.put(Long.parseLong(String.valueOf(review.getProduct().getProId())), review.getRatingStar());
            }

            if (userId.compareTo(userID) == 0) {
                myRatesMap.put(userID, userRatings);
            } else {
                userWithRatesMap.put(userID, userRatings);

                setRatings(userWithRatesMap);
                averageRating.put(userID, 0.0);

                for (Map.Entry<Long, Integer> longIntegerEntry : userRatings.entrySet()) {

                    if (ratings.containsKey(userID)) {
                        ratings.get(userID).put(longIntegerEntry.getKey(), longIntegerEntry.getValue());
                        averageRating.put(userID, averageRating.get(userID) + (double) longIntegerEntry.getValue());
                    } else {
                        Map<Long, Integer> bookRating = new HashMap<>();
                        bookRating.put(longIntegerEntry.getKey(), longIntegerEntry.getValue());
                        ratings.put(userID, bookRating);
                        averageRating.put(userID, (double) longIntegerEntry.getValue());
                    }
                }
            }
        });

        for (Map.Entry<Long, Double> longDoubleEntry : averageRating.entrySet()) {
            if (ratings.containsKey(longDoubleEntry.getKey())) {
                longDoubleEntry.setValue(longDoubleEntry.getValue() / (double) ratings.get(longDoubleEntry.getKey()).size());
            }
        }

        setAverageRating(averageRating);

        Map<Long, String> products = new HashMap<>();

        productRepository.findAll().forEach(book -> products.put(Long.parseLong(String.valueOf(book.getProId())), book.getProName()));
        Map<Long, Double> neighbourhoods = getNeighbourhoods(myRatesMap.get(userId));
        Map<Long, Double> recommendations = getRecommendations(myRatesMap.get(userId), neighbourhoods, products);

        ValueComparator valueComparator = new ValueComparator(recommendations);
        Map<Long, Double> sortedRecommendations = new TreeMap<>(valueComparator);
        sortedRecommendations.putAll(recommendations);

        Iterator<Map.Entry<Long, Double>> sortedREntries = sortedRecommendations.entrySet().iterator();
        List<CRUDProductRecommentResponse> crudProductResponses = new ArrayList<>();
        int i = 0;
        int total = 0;
        while (sortedREntries.hasNext() && i < NUM_RECOMMENDATIONS) {
            Map.Entry<Long, Double> entry = sortedREntries.next();
            if (entry.getValue() >= MIN_VALUE_RECOMMENDATION) {
                Product product = productRepository.findByProId(Integer.parseInt(entry.getKey().toString()));
                if (product != null) {
                    List<ProductImageResponse> productImageResponses = new ArrayList<>();
                    String currentProImg = product.getListProImg();
                    if (currentProImg != null && !currentProImg.trim().isEmpty()) {
                        String[] imageEntries1 = currentProImg.split(", ");
                        for (String imageEntry : imageEntries1) {
                            String[] parts = imageEntry.split(": ");
                            int stt = Integer.parseInt(parts[0]);
                            String url = parts[1];
                            productImageResponses.add(new ProductImageResponse(stt, url));
                        }
                    }
                    crudProductResponses.add(new CRUDProductRecommentResponse(
                            product.getProId(),
                            Utils.round(entry.getValue(), 1),
                            product.getCategory().getCateId(),
                            product.getProName(),
                            productImageResponses,
                            product.getDescription(),
                            product.getIsDeleted(),
                            product.getDateDeleted(),
                            product.getDateCreated(),
                            product.getDateUpdated()
                    ));
                    total++;
                }
                i++;
            }
        }

        if (crudProductResponses.isEmpty()) {
            Set<Long> processedCategoryIds = new HashSet<>();
            List<Orders> orders = orderRepository.findAllByUserUserId(Math.toIntExact(userId));
            for (Orders order : orders) {
                OrderItem orderItem = order.getOrderItem();
                System.out.println(orderItem.getOrderItemId());
                Cart cart = orderItem.getCart();
                List<CartItem> cartItem = cartItemRepository.findByCart_CartId(cart.getCartId());
                for (CartItem cartItem1 : cartItem) {
                    ProductVariants productVariants = cartItem1.getProductVariants();
                    Product product = productRepository.findByProId(productVariants.getProduct().getProId());
                    Category category = product.getCategory();
                    List<Product> productList = productRepository.findByCategory_CateId(category.getCateId());

                    if (!processedCategoryIds.contains(Long.valueOf(category.getCateId()))) {
                        for (Product product1 : productList) {
                            List<ProductImageResponse> productImageResponses = new ArrayList<>();
                            String currentProImg = product.getListProImg();
                            if (currentProImg != null && !currentProImg.trim().isEmpty()) {
                                String[] imageEntries1 = currentProImg.split(", ");
                                for (String imageEntry : imageEntries1) {
                                    String[] parts = imageEntry.split(": ");
                                    int stt = Integer.parseInt(parts[0]);
                                    String url = parts[1];
                                    productImageResponses.add(new ProductImageResponse(stt, url));
                                }
                            }
                            crudProductResponses.add(new CRUDProductRecommentResponse(
                                    product.getProId(),
                                    0,
                                    product1.getCategory().getCateId(),
                                    product1.getProName(),
                                    productImageResponses,
                                    product1.getDescription(),
                                    product1.getIsDeleted(),
                                    product1.getDateDeleted(),
                                    product1.getDateCreated(),
                                    product1.getDateUpdated()));
                            total++;
                        }
                        processedCategoryIds.add((long) category.getCateId());
                    }
                }
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(new ListRecommendResponse(Math.toIntExact(userId), total, crudProductResponses));

    }
}
