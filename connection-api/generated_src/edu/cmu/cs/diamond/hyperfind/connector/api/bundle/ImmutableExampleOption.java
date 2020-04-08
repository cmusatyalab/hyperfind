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
 * Immutable implementation of {@link ExampleOption}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableExampleOption.builder()}.
 * Use the static factory method to create immutable instances:
 * {@code ImmutableExampleOption.of()}.
 */
@Generated(from = "ExampleOption", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableExampleOption
    extends ExampleOption {
  private final String displayName;
  private final String name;

  private ImmutableExampleOption(String displayName, String name) {
    this.displayName = Objects.requireNonNull(displayName, "displayName");
    this.name = Objects.requireNonNull(name, "name");
  }

  private ImmutableExampleOption(ImmutableExampleOption original, String displayName, String name) {
    this.displayName = displayName;
    this.name = name;
  }

  /**
   * @return The value of the {@code displayName} attribute
   */
  @Override
  public String displayName() {
    return displayName;
  }

  /**
   * @return The value of the {@code name} attribute
   */
  @Override
  public String name() {
    return name;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ExampleOption#displayName() displayName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for displayName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableExampleOption withDisplayName(String value) {
    String newValue = Objects.requireNonNull(value, "displayName");
    if (this.displayName.equals(newValue)) return this;
    return new ImmutableExampleOption(this, newValue, this.name);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ExampleOption#name() name} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for name
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableExampleOption withName(String value) {
    String newValue = Objects.requireNonNull(value, "name");
    if (this.name.equals(newValue)) return this;
    return new ImmutableExampleOption(this, this.displayName, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableExampleOption} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableExampleOption
        && equalTo((ImmutableExampleOption) another);
  }

  private boolean equalTo(ImmutableExampleOption another) {
    return displayName.equals(another.displayName)
        && name.equals(another.name);
  }

  /**
   * Computes a hash code from attributes: {@code displayName}, {@code name}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + displayName.hashCode();
    h += (h << 5) + name.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code ExampleOption} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ExampleOption")
        .omitNullValues()
        .add("displayName", displayName)
        .add("name", name)
        .toString();
  }

  /**
   * Construct a new immutable {@code ExampleOption} instance.
   * @param displayName The value for the {@code displayName} attribute
   * @param name The value for the {@code name} attribute
   * @return An immutable ExampleOption instance
   */
  public static ImmutableExampleOption of(String displayName, String name) {
    return new ImmutableExampleOption(displayName, name);
  }

  /**
   * Creates an immutable copy of a {@link ExampleOption} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ExampleOption instance
   */
  public static ImmutableExampleOption copyOf(ExampleOption instance) {
    if (instance instanceof ImmutableExampleOption) {
      return (ImmutableExampleOption) instance;
    }
    return ImmutableExampleOption.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableExampleOption ImmutableExampleOption}.
   * <pre>
   * ImmutableExampleOption.builder()
   *    .displayName(String) // required {@link ExampleOption#displayName() displayName}
   *    .name(String) // required {@link ExampleOption#name() name}
   *    .build();
   * </pre>
   * @return A new ImmutableExampleOption builder
   */
  public static ImmutableExampleOption.Builder builder() {
    return new ImmutableExampleOption.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableExampleOption ImmutableExampleOption}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ExampleOption", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DISPLAY_NAME = 0x1L;
    private static final long INIT_BIT_NAME = 0x2L;
    private long initBits = 0x3L;

    private @Nullable String displayName;
    private @Nullable String name;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code edu.cmu.cs.diamond.hyperfind.connector.api.bundle.ExampleOption} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(ExampleOption instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    /**
     * Fill a builder with attribute values from the provided {@code edu.cmu.cs.diamond.hyperfind.connector.api.bundle.Option} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(Option instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    private void from(Object object) {
      @Var long bits = 0;
      if (object instanceof ExampleOption) {
        ExampleOption instance = (ExampleOption) object;
        if ((bits & 0x2L) == 0) {
          name(instance.name());
          bits |= 0x2L;
        }
        if ((bits & 0x1L) == 0) {
          displayName(instance.displayName());
          bits |= 0x1L;
        }
      }
      if (object instanceof Option) {
        Option instance = (Option) object;
        if ((bits & 0x2L) == 0) {
          name(instance.name());
          bits |= 0x2L;
        }
        if ((bits & 0x1L) == 0) {
          displayName(instance.displayName());
          bits |= 0x1L;
        }
      }
    }

    /**
     * Initializes the value for the {@link ExampleOption#displayName() displayName} attribute.
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
     * Initializes the value for the {@link ExampleOption#name() name} attribute.
     * @param name The value for name 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder name(String name) {
      this.name = Objects.requireNonNull(name, "name");
      initBits &= ~INIT_BIT_NAME;
      return this;
    }

    /**
     * Builds a new {@link ImmutableExampleOption ImmutableExampleOption}.
     * @return An immutable instance of ExampleOption
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableExampleOption build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableExampleOption(null, displayName, name);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DISPLAY_NAME) != 0) attributes.add("displayName");
      if ((initBits & INIT_BIT_NAME) != 0) attributes.add("name");
      return "Cannot build ExampleOption, some of required attributes are not set " + attributes;
    }
  }
}
