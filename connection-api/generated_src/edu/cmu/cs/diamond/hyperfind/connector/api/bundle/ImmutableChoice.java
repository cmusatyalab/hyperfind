package edu.cmu.cs.diamond.hyperfind.connector.api.bundle;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.Booleans;
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
 * Immutable implementation of {@link Choice}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableChoice.builder()}.
 * Use the static factory method to create immutable instances:
 * {@code ImmutableChoice.of()}.
 */
@Generated(from = "Choice", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableChoice implements Choice {
  private final String displayName;
  private final String value;
  private final boolean isDefault;

  private ImmutableChoice(String displayName, String value, boolean isDefault) {
    this.displayName = Objects.requireNonNull(displayName, "displayName");
    this.value = Objects.requireNonNull(value, "value");
    this.isDefault = isDefault;
  }

  private ImmutableChoice(ImmutableChoice original, String displayName, String value, boolean isDefault) {
    this.displayName = displayName;
    this.value = value;
    this.isDefault = isDefault;
  }

  /**
   * @return The value of the {@code displayName} attribute
   */
  @Override
  public String displayName() {
    return displayName;
  }

  /**
   * @return The value of the {@code value} attribute
   */
  @Override
  public String value() {
    return value;
  }

  /**
   * @return The value of the {@code isDefault} attribute
   */
  @Override
  public boolean isDefault() {
    return isDefault;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Choice#displayName() displayName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for displayName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableChoice withDisplayName(String value) {
    String newValue = Objects.requireNonNull(value, "displayName");
    if (this.displayName.equals(newValue)) return this;
    return new ImmutableChoice(this, newValue, this.value, this.isDefault);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Choice#value() value} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for value
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableChoice withValue(String value) {
    String newValue = Objects.requireNonNull(value, "value");
    if (this.value.equals(newValue)) return this;
    return new ImmutableChoice(this, this.displayName, newValue, this.isDefault);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Choice#isDefault() isDefault} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for isDefault
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableChoice withIsDefault(boolean value) {
    if (this.isDefault == value) return this;
    return new ImmutableChoice(this, this.displayName, this.value, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableChoice} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableChoice
        && equalTo((ImmutableChoice) another);
  }

  private boolean equalTo(ImmutableChoice another) {
    return displayName.equals(another.displayName)
        && value.equals(another.value)
        && isDefault == another.isDefault;
  }

  /**
   * Computes a hash code from attributes: {@code displayName}, {@code value}, {@code isDefault}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + displayName.hashCode();
    h += (h << 5) + value.hashCode();
    h += (h << 5) + Booleans.hashCode(isDefault);
    return h;
  }

  /**
   * Prints the immutable value {@code Choice} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Choice")
        .omitNullValues()
        .add("displayName", displayName)
        .add("value", value)
        .add("isDefault", isDefault)
        .toString();
  }

  /**
   * Construct a new immutable {@code Choice} instance.
   * @param displayName The value for the {@code displayName} attribute
   * @param value The value for the {@code value} attribute
   * @param isDefault The value for the {@code isDefault} attribute
   * @return An immutable Choice instance
   */
  public static ImmutableChoice of(String displayName, String value, boolean isDefault) {
    return new ImmutableChoice(displayName, value, isDefault);
  }

  /**
   * Creates an immutable copy of a {@link Choice} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable Choice instance
   */
  public static ImmutableChoice copyOf(Choice instance) {
    if (instance instanceof ImmutableChoice) {
      return (ImmutableChoice) instance;
    }
    return ImmutableChoice.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableChoice ImmutableChoice}.
   * <pre>
   * ImmutableChoice.builder()
   *    .displayName(String) // required {@link Choice#displayName() displayName}
   *    .value(String) // required {@link Choice#value() value}
   *    .isDefault(boolean) // required {@link Choice#isDefault() isDefault}
   *    .build();
   * </pre>
   * @return A new ImmutableChoice builder
   */
  public static ImmutableChoice.Builder builder() {
    return new ImmutableChoice.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableChoice ImmutableChoice}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "Choice", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DISPLAY_NAME = 0x1L;
    private static final long INIT_BIT_VALUE = 0x2L;
    private static final long INIT_BIT_IS_DEFAULT = 0x4L;
    private long initBits = 0x7L;

    private @Nullable String displayName;
    private @Nullable String value;
    private boolean isDefault;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code Choice} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(Choice instance) {
      Objects.requireNonNull(instance, "instance");
      displayName(instance.displayName());
      value(instance.value());
      isDefault(instance.isDefault());
      return this;
    }

    /**
     * Initializes the value for the {@link Choice#displayName() displayName} attribute.
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
     * Initializes the value for the {@link Choice#value() value} attribute.
     * @param value The value for value 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder value(String value) {
      this.value = Objects.requireNonNull(value, "value");
      initBits &= ~INIT_BIT_VALUE;
      return this;
    }

    /**
     * Initializes the value for the {@link Choice#isDefault() isDefault} attribute.
     * @param isDefault The value for isDefault 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder isDefault(boolean isDefault) {
      this.isDefault = isDefault;
      initBits &= ~INIT_BIT_IS_DEFAULT;
      return this;
    }

    /**
     * Builds a new {@link ImmutableChoice ImmutableChoice}.
     * @return An immutable instance of Choice
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableChoice build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableChoice(null, displayName, value, isDefault);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DISPLAY_NAME) != 0) attributes.add("displayName");
      if ((initBits & INIT_BIT_VALUE) != 0) attributes.add("value");
      if ((initBits & INIT_BIT_IS_DEFAULT) != 0) attributes.add("isDefault");
      return "Cannot build Choice, some of required attributes are not set " + attributes;
    }
  }
}
