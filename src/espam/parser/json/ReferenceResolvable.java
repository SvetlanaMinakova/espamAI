package espam.parser.json;

/**
 * Resolves references after the JSON deserialization
 * should be implemented for every JSON serializable class,
 * contains transient references
 */
public interface ReferenceResolvable {
    /**
     * Resolves references after the JSON deserialization
     * should be implemented for all transient references
     */
    void resolveReferences();
}
