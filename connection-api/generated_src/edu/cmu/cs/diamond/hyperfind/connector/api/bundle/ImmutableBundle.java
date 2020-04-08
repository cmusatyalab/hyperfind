package edu.cmu.cs.diamond.hyperfind.connector.api.bundle;

import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link Bundle}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableBundle.builder()}.
 * Use the static factory method to create immutable instances:
 * {@code ImmutableBundle.of()}.
 */
@Generated(from = "Bundle", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableBundle implements Bundle {
  private final String displayName;
  private final BundleType type;

  private ImmutableBundle(String displayName, BundleType type) {
    this.displayName = Objects.requireNonNull(displayName, "displayName");
    this.type = Objects.requireNonNull(type, "type");
  }

  private ImmutableBundle(
      ImmutableBundle original,
      String displayName,
      BundleType type) {
    this.displayName = displayName;
    this.type = type;
  }

  /**
   * @return The value of the {@code displayName} attribute
   */
  @Override
  public String displayName() {
    return displayName;
  }

  /**
   * @return The value of the {@code type} attribute
   */
  @Override
  public BundleType type() {
    return type;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Bundle#displayName() displayName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for displayName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBundle withDisplayName(String value) {
    String newValue = Objects.requireNonNull(value, "displayName");
    if (this.displayName.equals(newValue)) return this;
    return new ImmutableBundle(this, newValue, this.type);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Bundle#type() type} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for type
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBundle withType(BundleType value) {
    if (this.type == value) return this;
    BundleType newValue = Objects.requireNonNull(value, "type");
    if (this.type.equals(newValue)) return this;
    return new ImmutableBundle(this, this.displayName, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableBundle} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableBundle
        && equalTo((ImmutableBundle) another);
  }

  private boolean equalTo(ImmutableBundle another) {
    return displayName.equals(another.displayName)
        && type.equals(another.type);
  }

  /**
   * Computes a hash code from attributes: {@code displayName}, {@code type}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + displayName.hashCode();
    h += (h << 5) + type.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code Bundle} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Bundle")
        .omitNullValues()
        .add("displayName", displayName)
        .add("type", type)
        .toString();
  }

  /**
   * Construct a new immutable {@code Bundle} instance.
   * @param displayName The value for the {@code displayName} attribute
   * @param type The value for the {@code type} attribute
   * @return An immutable Bundle instance
   */
  public static ImmutableBundle of(String displayName, BundleType type) {
    return new ImmutableBundle(displayName, type);
  }

  /**
   * Creates an immutable copy of a {@link Bundle} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable Bundle instance
   */
  public static ImmutableBundle copyOf(Bundle instance) {
    if (instance instanceof ImmutableBundle) {
      return (ImmutableBundle) instance;
    }
    return ImmutableBundle.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableBundle ImmutableBundle}.
   * <pre>
   * ImmutableBundle.builder()
   *    .displayName(String) // required {@link Bundle#displayName() displayName}
   *    .type(edu.cmu.cs.diamond.hyperfind.connector.api.bundle.BundleType) // required {@link Bundle#type() type}
   *    .build();
   * </pre>
   * @return A new ImmutableBundle builder
   */
  public static ImmutableBundle.Builder builder() {
    return new ImmutableBundle.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableBundle ImmutableBundle}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "Bundle", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DISPLAY_NAME = 0x1L;
    private static final long INIT_BIT_TYPE = 0x2L;
    private long initBits = 0x3L;

    private @Nullable String displayName;
    private @Nullable BundleType type;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code Bundle} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(Bundle instance) {
      Objects.requireNonNull(instance, "instance");
      displayName(instance.displayName());
      type(instance.type());
      return this;
    }

    /**
     * Initializes the value for the {@link Bundle#displayName() displayName} attribute.
     * @param displayName The value for displayName 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder displayName(String displayName) {
      this.displayName = Objects.requireNonNull(displayName, "displayName");
      initBits &= ~INIT_BIT_DISPLAY_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link Bundle#type() type} attribute.
     * @param type The value for type 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder type(BundleType type) {
      this.type = Objects.requireNonNull(type, "type");
      initBits &= ~INIT_BIT_TYPE;
      return this;
    }

    /**
     * Builds a new {@link ImmutableBundle ImmutableBundle}.
     * @return An immutable instance of Bundle
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableBundle build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableBundle(null, displayName, type);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DISPLAY_NAME) != 0) attributes.add("displayName");
      if ((initBits & INIT_BIT_TYPE) != 0) attributes.add("type");
      return "Cannot build Bundle, some of required attributes are not set " + attributes;
    }
  }
}
