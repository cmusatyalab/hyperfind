package edu.cmu.cs.diamond.hyperfind.connector.api.bundle;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.Booleans;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link StringOption}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableStringOption.builder()}.
 * Use the static factory method to create immutable instances:
 * {@code ImmutableStringOption.of()}.
 */
@Generated(from = "StringOption", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableStringOption
    extends StringOption {
  private final String displayName;
  private final String name;
  private final String defaultValue;
  private final int height;
  private final int width;
  private final boolean multiLine;
  private final String disabledValue;
  private final @Nullable Boolean initiallyEnabled;

  private ImmutableStringOption(
      String displayName,
      String name,
      String defaultValue,
      int height,
      int width,
      boolean multiLine,
      String disabledValue,
      Optional<Boolean> initiallyEnabled) {
    this.displayName = Objects.requireNonNull(displayName, "displayName");
    this.name = Objects.requireNonNull(name, "name");
    this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
    this.height = height;
    this.width = width;
    this.multiLine = multiLine;
    this.disabledValue = Objects.requireNonNull(disabledValue, "disabledValue");
    this.initiallyEnabled = initiallyEnabled.orElse(null);
  }

  private ImmutableStringOption(
      ImmutableStringOption original,
      String displayName,
      String name,
      String defaultValue,
      int height,
      int width,
      boolean multiLine,
      String disabledValue,
      @Nullable Boolean initiallyEnabled) {
    this.displayName = displayName;
    this.name = name;
    this.defaultValue = defaultValue;
    this.height = height;
    this.width = width;
    this.multiLine = multiLine;
    this.disabledValue = disabledValue;
    this.initiallyEnabled = initiallyEnabled;
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
   * @return The value of the {@code defaultValue} attribute
   */
  @Override
  public String defaultValue() {
    return defaultValue;
  }

  /**
   * @return The value of the {@code height} attribute
   */
  @Override
  public int height() {
    return height;
  }

  /**
   * @return The value of the {@code width} attribute
   */
  @Override
  public int width() {
    return width;
  }

  /**
   * @return The value of the {@code multiLine} attribute
   */
  @Override
  public boolean multiLine() {
    return multiLine;
  }

  /**
   * @return The value of the {@code disabledValue} attribute
   */
  @Override
  public String disabledValue() {
    return disabledValue;
  }

  /**
   * @return The value of the {@code initiallyEnabled} attribute
   */
  @Override
  public Optional<Boolean> initiallyEnabled() {
    return Optional.ofNullable(initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link StringOption#displayName() displayName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for displayName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableStringOption withDisplayName(String value) {
    String newValue = Objects.requireNonNull(value, "displayName");
    if (this.displayName.equals(newValue)) return this;
    return new ImmutableStringOption(
        this,
        newValue,
        this.name,
        this.defaultValue,
        this.height,
        this.width,
        this.multiLine,
        this.disabledValue,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link StringOption#name() name} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for name
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableStringOption withName(String value) {
    String newValue = Objects.requireNonNull(value, "name");
    if (this.name.equals(newValue)) return this;
    return new ImmutableStringOption(
        this,
        this.displayName,
        newValue,
        this.defaultValue,
        this.height,
        this.width,
        this.multiLine,
        this.disabledValue,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link StringOption#defaultValue() defaultValue} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for defaultValue
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableStringOption withDefaultValue(String value) {
    String newValue = Objects.requireNonNull(value, "defaultValue");
    if (this.defaultValue.equals(newValue)) return this;
    return new ImmutableStringOption(
        this,
        this.displayName,
        this.name,
        newValue,
        this.height,
        this.width,
        this.multiLine,
        this.disabledValue,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link StringOption#height() height} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for height
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableStringOption withHeight(int value) {
    if (this.height == value) return this;
    return new ImmutableStringOption(
        this,
        this.displayName,
        this.name,
        this.defaultValue,
        value,
        this.width,
        this.multiLine,
        this.disabledValue,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link StringOption#width() width} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for width
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableStringOption withWidth(int value) {
    if (this.width == value) return this;
    return new ImmutableStringOption(
        this,
        this.displayName,
        this.name,
        this.defaultValue,
        this.height,
        value,
        this.multiLine,
        this.disabledValue,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link StringOption#multiLine() multiLine} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for multiLine
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableStringOption withMultiLine(boolean value) {
    if (this.multiLine == value) return this;
    return new ImmutableStringOption(
        this,
        this.displayName,
        this.name,
        this.defaultValue,
        this.height,
        this.width,
        value,
        this.disabledValue,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link StringOption#disabledValue() disabledValue} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for disabledValue
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableStringOption withDisabledValue(String value) {
    String newValue = Objects.requireNonNull(value, "disabledValue");
    if (this.disabledValue.equals(newValue)) return this;
    return new ImmutableStringOption(
        this,
        this.displayName,
        this.name,
        this.defaultValue,
        this.height,
        this.width,
        this.multiLine,
        newValue,
        this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a <i>present</i> value for the optional {@link StringOption#initiallyEnabled() initiallyEnabled} attribute.
   * @param value The value for initiallyEnabled
   * @return A modified copy of {@code this} object
   */
  public final ImmutableStringOption withInitiallyEnabled(boolean value) {
    @Nullable Boolean newValue = value;
    if (Objects.equals(this.initiallyEnabled, newValue)) return this;
    return new ImmutableStringOption(
        this,
        this.displayName,
        this.name,
        this.defaultValue,
        this.height,
        this.width,
        this.multiLine,
        this.disabledValue,
        newValue);
  }

  /**
   * Copy the current immutable object by setting an optional value for the {@link StringOption#initiallyEnabled() initiallyEnabled} attribute.
   * An equality check is used on inner nullable value to prevent copying of the same value by returning {@code this}.
   * @param optional A value for initiallyEnabled
   * @return A modified copy of {@code this} object
   */
  public final ImmutableStringOption withInitiallyEnabled(Optional<Boolean> optional) {
    @Nullable Boolean value = optional.orElse(null);
    if (Objects.equals(this.initiallyEnabled, value)) return this;
    return new ImmutableStringOption(
        this,
        this.displayName,
        this.name,
        this.defaultValue,
        this.height,
        this.width,
        this.multiLine,
        this.disabledValue,
        value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableStringOption} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableStringOption
        && equalTo((ImmutableStringOption) another);
  }

  private boolean equalTo(ImmutableStringOption another) {
    return displayName.equals(another.displayName)
        && name.equals(another.name)
        && defaultValue.equals(another.defaultValue)
        && height == another.height
        && width == another.width
        && multiLine == another.multiLine
        && disabledValue.equals(another.disabledValue)
        && Objects.equals(initiallyEnabled, another.initiallyEnabled);
  }

  /**
   * Computes a hash code from attributes: {@code displayName}, {@code name}, {@code defaultValue}, {@code height}, {@code width}, {@code multiLine}, {@code disabledValue}, {@code initiallyEnabled}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + displayName.hashCode();
    h += (h << 5) + name.hashCode();
    h += (h << 5) + defaultValue.hashCode();
    h += (h << 5) + height;
    h += (h << 5) + width;
    h += (h << 5) + Booleans.hashCode(multiLine);
    h += (h << 5) + disabledValue.hashCode();
    h += (h << 5) + Objects.hashCode(initiallyEnabled);
    return h;
  }

  /**
   * Prints the immutable value {@code StringOption} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("StringOption")
        .omitNullValues()
        .add("displayName", displayName)
        .add("name", name)
        .add("defaultValue", defaultValue)
        .add("height", height)
        .add("width", width)
        .add("multiLine", multiLine)
        .add("disabledValue", disabledValue)
        .add("initiallyEnabled", initiallyEnabled)
        .toString();
  }

  /**
   * Construct a new immutable {@code StringOption} instance.
   * @param displayName The value for the {@code displayName} attribute
   * @param name The value for the {@code name} attribute
   * @param defaultValue The value for the {@code defaultValue} attribute
   * @param height The value for the {@code height} attribute
   * @param width The value for the {@code width} attribute
   * @param multiLine The value for the {@code multiLine} attribute
   * @param disabledValue The value for the {@code disabledValue} attribute
   * @param initiallyEnabled The value for the {@code initiallyEnabled} attribute
   * @return An immutable StringOption instance
   */
  public static ImmutableStringOption of(String displayName, String name, String defaultValue, int height, int width, boolean multiLine, String disabledValue, Optional<Boolean> initiallyEnabled) {
    return new ImmutableStringOption(displayName, name, defaultValue, height, width, multiLine, disabledValue, initiallyEnabled);
  }

  /**
   * Creates an immutable copy of a {@link StringOption} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable StringOption instance
   */
  public static ImmutableStringOption copyOf(StringOption instance) {
    if (instance instanceof ImmutableStringOption) {
      return (ImmutableStringOption) instance;
    }
    return ImmutableStringOption.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableStringOption ImmutableStringOption}.
   * <pre>
   * ImmutableStringOption.builder()
   *    .displayName(String) // required {@link StringOption#displayName() displayName}
   *    .name(String) // required {@link StringOption#name() name}
   *    .defaultValue(String) // required {@link StringOption#defaultValue() defaultValue}
   *    .height(int) // required {@link StringOption#height() height}
   *    .width(int) // required {@link StringOption#width() width}
   *    .multiLine(boolean) // required {@link StringOption#multiLine() multiLine}
   *    .disabledValue(String) // required {@link StringOption#disabledValue() disabledValue}
   *    .initiallyEnabled(Boolean) // optional {@link StringOption#initiallyEnabled() initiallyEnabled}
   *    .build();
   * </pre>
   * @return A new ImmutableStringOption builder
   */
  public static ImmutableStringOption.Builder builder() {
    return new ImmutableStringOption.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableStringOption ImmutableStringOption}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "StringOption", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DISPLAY_NAME = 0x1L;
    private static final long INIT_BIT_NAME = 0x2L;
    private static final long INIT_BIT_DEFAULT_VALUE = 0x4L;
    private static final long INIT_BIT_HEIGHT = 0x8L;
    private static final long INIT_BIT_WIDTH = 0x10L;
    private static final long INIT_BIT_MULTI_LINE = 0x20L;
    private static final long INIT_BIT_DISABLED_VALUE = 0x40L;
    private long initBits = 0x7fL;

    private @Nullable String displayName;
    private @Nullable String name;
    private @Nullable String defaultValue;
    private int height;
    private int width;
    private boolean multiLine;
    private @Nullable String disabledValue;
    private @Nullable Boolean initiallyEnabled;

    private Builder() {
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

    /**
     * Fill a builder with attribute values from the provided {@code edu.cmu.cs.diamond.hyperfind.connector.api.bundle.StringOption} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(StringOption instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    private void from(Object object) {
      @Var long bits = 0;
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
      if (object instanceof StringOption) {
        StringOption instance = (StringOption) object;
        disabledValue(instance.disabledValue());
        if ((bits & 0x1L) == 0) {
          displayName(instance.displayName());
          bits |= 0x1L;
        }
        defaultValue(instance.defaultValue());
        multiLine(instance.multiLine());
        Optional<Boolean> initiallyEnabledOptional = instance.initiallyEnabled();
        if (initiallyEnabledOptional.isPresent()) {
          initiallyEnabled(initiallyEnabledOptional);
        }
        if ((bits & 0x2L) == 0) {
          name(instance.name());
          bits |= 0x2L;
        }
        width(instance.width());
        height(instance.height());
      }
    }

    /**
     * Initializes the value for the {@link StringOption#displayName() displayName} attribute.
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
     * Initializes the value for the {@link StringOption#name() name} attribute.
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
     * Initializes the value for the {@link StringOption#defaultValue() defaultValue} attribute.
     * @param defaultValue The value for defaultValue 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder defaultValue(String defaultValue) {
      this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
      initBits &= ~INIT_BIT_DEFAULT_VALUE;
      return this;
    }

    /**
     * Initializes the value for the {@link StringOption#height() height} attribute.
     * @param height The value for height 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder height(int height) {
      this.height = height;
      initBits &= ~INIT_BIT_HEIGHT;
      return this;
    }

    /**
     * Initializes the value for the {@link StringOption#width() width} attribute.
     * @param width The value for width 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder width(int width) {
      this.width = width;
      initBits &= ~INIT_BIT_WIDTH;
      return this;
    }

    /**
     * Initializes the value for the {@link StringOption#multiLine() multiLine} attribute.
     * @param multiLine The value for multiLine 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder multiLine(boolean multiLine) {
      this.multiLine = multiLine;
      initBits &= ~INIT_BIT_MULTI_LINE;
      return this;
    }

    /**
     * Initializes the value for the {@link StringOption#disabledValue() disabledValue} attribute.
     * @param disabledValue The value for disabledValue 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder disabledValue(String disabledValue) {
      this.disabledValue = Objects.requireNonNull(disabledValue, "disabledValue");
      initBits &= ~INIT_BIT_DISABLED_VALUE;
      return this;
    }

    /**
     * Initializes the optional value {@link StringOption#initiallyEnabled() initiallyEnabled} to initiallyEnabled.
     * @param initiallyEnabled The value for initiallyEnabled
     * @return {@code this} builder for chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder initiallyEnabled(boolean initiallyEnabled) {
      this.initiallyEnabled = initiallyEnabled;
      return this;
    }

    /**
     * Initializes the optional value {@link StringOption#initiallyEnabled() initiallyEnabled} to initiallyEnabled.
     * @param initiallyEnabled The value for initiallyEnabled
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder initiallyEnabled(Optional<Boolean> initiallyEnabled) {
      this.initiallyEnabled = initiallyEnabled.orElse(null);
      return this;
    }

    /**
     * Builds a new {@link ImmutableStringOption ImmutableStringOption}.
     * @return An immutable instance of StringOption
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableStringOption build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableStringOption(
          null,
          displayName,
          name,
          defaultValue,
          height,
          width,
          multiLine,
          disabledValue,
          initiallyEnabled);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DISPLAY_NAME) != 0) attributes.add("displayName");
      if ((initBits & INIT_BIT_NAME) != 0) attributes.add("name");
      if ((initBits & INIT_BIT_DEFAULT_VALUE) != 0) attributes.add("defaultValue");
      if ((initBits & INIT_BIT_HEIGHT) != 0) attributes.add("height");
      if ((initBits & INIT_BIT_WIDTH) != 0) attributes.add("width");
      if ((initBits & INIT_BIT_MULTI_LINE) != 0) attributes.add("multiLine");
      if ((initBits & INIT_BIT_DISABLED_VALUE) != 0) attributes.add("disabledValue");
      return "Cannot build StringOption, some of required attributes are not set " + attributes;
    }
  }
}
