package dating.model;

/**
 * A photo of a profile. Likes are not stored here: they are part of the network state.
 *
 * @param id      unique photo id
 * @param ownerId id of the profile that owns the photo
 */
public record Photo(int id, int ownerId) {
}
