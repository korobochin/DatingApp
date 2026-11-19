package dating.gen;

import dating.model.Gender;
import dating.model.Intent;
import dating.model.Profile;
import dating.random.RandomSource;
import dating.sim.Network;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Factory of random but reproducible networks: the same seed always gives the same network. */
public final class ProfileGenerator {
    private static final List<String> NAMES = List.of("Alex", "Maria", "Ivan", "Olga", "Dmitry", "Anna",
            "Sergey", "Elena", "Pavel", "Nina", "Egor", "Vera", "Timur", "Lena", "Oleg", "Sasha", "Kate",
            "Boris", "Dasha", "Max");
    private static final List<Gender> GENDER_POOL = List.of(Gender.MALE, Gender.MALE, Gender.MALE, Gender.MALE,
            Gender.FEMALE, Gender.FEMALE, Gender.FEMALE, Gender.FEMALE, Gender.OTHER, Gender.OTHER);
    private static final List<String> CITIES = List.of("Moscow", "Kazan", "Perm", "Samara");
    /** Interest pool used for random profiles. */
    public static final List<String> INTERESTS = List.of("chess", "hiking", "cinema", "jazz", "cooking",
            "cycling", "photography", "reading", "yoga", "gaming", "travel", "painting");

    private ProfileGenerator() {
    }

    /**
     * Generates a network with ids {@code 1..users}, one or two photos per user and a few friendships.
     *
     * @param users  number of profiles, at least 3
     * @param random the only source of randomness
     * @return the initial state
     */
    public static Network generate(int users, RandomSource random) {
        if (users < 3) {
            throw new IllegalArgumentException("At least 3 users are required");
        }
        Network.Builder builder = Network.builder();
        int photoId = 1;
        for (int id = 1; id <= users; id++) {
            Gender gender = random.pick(GENDER_POOL);
            Set<String> interests = new TreeSet<>();
            int wanted = random.nextInt(3, 5);
            while (interests.size() < wanted) {
                interests.add(random.pick(INTERESTS));
            }
            builder.addProfile(Profile.builder()
                    .id(id)
                    .name(random.pick(NAMES))
                    .age(random.nextInt(18, 45))
                    .city(random.pick(CITIES))
                    .gender(gender)
                    .lookingFor(lookingFor(gender, random))
                    .intent(random.pick(List.of(Intent.values())))
                    .interests(interests.toArray(String[]::new))
                    .build());
            int photos = random.nextInt(1, 2);
            for (int i = 0; i < photos; i++) {
                builder.addPhoto(photoId++, id);
            }
        }
        for (int id = 1; id <= users; id++) {
            for (int i = 0; i < 2; i++) {
                int other = random.nextInt(1, users);
                if (other != id) {
                    builder.addFriendship(id, other);
                }
            }
        }
        return builder.build();
    }

    private static Gender[] lookingFor(Gender gender, RandomSource random) {
        if (gender == Gender.OTHER || !random.chance(0.85)) {
            return Gender.values();
        }
        return new Gender[]{gender == Gender.MALE ? Gender.FEMALE : Gender.MALE};
    }
}
