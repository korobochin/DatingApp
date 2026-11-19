package dating;

import dating.model.Gender;
import dating.model.Intent;
import dating.model.Profile;
import dating.sim.Network;

/** Small helpers for building test data. */
final class Fixtures {
    private Fixtures() {
    }

    /** Profile of gender OTHER that is open to everybody, so that orientation never gets in the way. */
    static Profile profile(int id, int age, String city, String... interests) {
        return Profile.builder().id(id).name("User" + id).age(age).city(city).gender(Gender.OTHER)
                .lookingFor(Gender.values()).intent(Intent.ANY).interests(interests).build();
    }

    /**
     * Users 1..4; photos 1 (owner 1), 2 and 3 (owner 2), 4 (owner 3); users 3 and 4 are friends.
     * User 1 likes "chess".
     */
    static Network engineNetwork() {
        return Network.builder()
                .addProfile(profile(1, 30, "Moscow", "chess"))
                .addProfile(profile(2, 31, "Moscow", "jazz"))
                .addProfile(profile(3, 32, "Kazan", "hiking"))
                .addProfile(profile(4, 33, "Kazan", "yoga"))
                .addPhoto(1, 1).addPhoto(2, 2).addPhoto(3, 2).addPhoto(4, 3)
                .addFriendship(3, 4)
                .build();
    }
}
