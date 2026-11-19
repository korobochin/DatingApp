package dating.filter;

import dating.model.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A reusable search specification: age range, city and required interests.
 * It is itself a {@link Predicate}, so it composes with other filters.
 * Built with {@link #builder()}.
 */
public final class SearchQuery implements Predicate<Profile> {
    private final Predicate<Profile> combined;
    private final String description;

    private SearchQuery(Predicate<Profile> combined, String description) {
        this.combined = combined;
        this.description = description;
    }

    @Override
    public boolean test(Profile profile) {
        return combined.test(profile);
    }

    @Override
    public String toString() {
        return description;
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link SearchQuery}. */
    public static final class Builder {
        private Integer minAge;
        private Integer maxAge;
        private String city;
        private final List<String> interests = new ArrayList<>();

        private Builder() {
        }

        /**
         * @param minAge minimal age, inclusive
         * @return this builder
         */
        public Builder minAge(int minAge) {
            this.minAge = minAge;
            return this;
        }

        /**
         * @param maxAge maximal age, inclusive
         * @return this builder
         */
        public Builder maxAge(int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        /**
         * @param city required city
         * @return this builder
         */
        public Builder city(String city) {
            this.city = city;
            return this;
        }

        /**
         * @param interest an interest that the profile must have
         * @return this builder
         */
        public Builder interest(String interest) {
            this.interests.add(interest);
            return this;
        }

        /**
         * @return the query
         * @throws IllegalArgumentException if minAge is greater than maxAge
         */
        public SearchQuery build() {
            if (minAge != null && maxAge != null && minAge > maxAge) {
                throw new IllegalArgumentException("minAge > maxAge");
            }
            Predicate<Profile> result = p -> true;
            if (minAge != null || maxAge != null) {
                result = result.and(ProfileFilters.ageBetween(
                        minAge == null ? 0 : minAge, maxAge == null ? Integer.MAX_VALUE : maxAge));
            }
            if (city != null) {
                result = result.and(ProfileFilters.inCity(city));
            }
            for (String interest : interests) {
                result = result.and(ProfileFilters.hasInterest(interest));
            }
            String text = "age[" + minAge + ".." + maxAge + "] city=" + city + " interests=" + interests;
            return new SearchQuery(result, text);
        }
    }
}
