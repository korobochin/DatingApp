package dating.sim;

import dating.model.Profile;

import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Reports over the state of the platform, built with Stream API collectors. */
public final class Statistics {
    private Statistics() {
    }

    /**
     * @param n state
     * @return number of profiles per city, cities sorted alphabetically
     */
    public static Map<String, Long> profilesByCity(Network n) {
        return n.profiles().stream()
                .collect(Collectors.groupingBy(Profile::city, TreeMap::new, Collectors.counting()));
    }

    /**
     * @param n state
     * @return average age per city, cities sorted alphabetically
     */
    public static Map<String, Double> averageAgeByCity(Network n) {
        return n.profiles().stream()
                .collect(Collectors.groupingBy(Profile::city, TreeMap::new, Collectors.averagingInt(Profile::age)));
    }

    /**
     * @param n state
     * @return count, min, max, average and sum of ages
     */
    public static IntSummaryStatistics ageSummary(Network n) {
        return n.profiles().stream().collect(Collectors.summarizingInt(Profile::age));
    }

    /**
     * @param n         state
     * @param threshold age border
     * @return {@code true} to the number of profiles younger than the border, {@code false} to the others
     */
    public static Map<Boolean, Long> youngerThan(Network n, int threshold) {
        return n.profiles().stream()
                .collect(Collectors.partitioningBy(p -> p.age() < threshold, Collectors.counting()));
    }

    /**
     * @param n state
     * @return interests with the number of people who have them, most popular first, ties alphabetical
     */
    public static List<Map.Entry<String, Long>> interestPopularity(Network n) {
        return n.profiles().stream()
                .flatMap(p -> p.interests().stream())
                .collect(Collectors.groupingBy(i -> i, TreeMap::new, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .toList();
    }

    /**
     * @param n state
     * @return friend count to number of users with this friend count, ascending
     */
    public static Map<Integer, Long> friendCountDistribution(Network n) {
        return n.profiles().stream()
                .collect(Collectors.groupingBy(p -> n.friends().degree(p.id()), TreeMap::new, Collectors.counting()));
    }

    /**
     * @param n     state
     * @param limit maximal number of ids
     * @return ids of the profiles with the most likes received, ties by id
     */
    public static List<Integer> mostLikedProfiles(Network n, int limit) {
        Comparator<Profile> order = Comparator.comparingInt((Profile p) -> n.likesReceived(p.id()))
                .reversed().thenComparingInt(Profile::id);
        return n.profiles().stream().sorted(order).limit(limit).map(Profile::id).toList();
    }

    /**
     * @param n state
     * @return total number of likes given on the platform
     */
    public static long totalLikes(Network n) {
        return n.profiles().stream().mapToLong(p -> n.likesReceived(p.id())).sum();
    }
}
