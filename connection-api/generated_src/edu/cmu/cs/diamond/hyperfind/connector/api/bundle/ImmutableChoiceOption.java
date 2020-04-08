package edu.cmu.cs.diamond.hyperfind.connector.api.bundle;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
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
 * Immutable implementation of {@link ChoiceOption}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableChoiceOption.builder()}.
 * Use the static factory method to create immutable instances:
 * {@code ImmutableChoiceOption.of()}.
 */
@Generated(from = "ChoiceOption", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableChoiceOption
    extends ChoiceOption {
  private final String displayName;
  private final String name;
  private final ImmutableList<Choice> choices;
  private final String disabledValue;
  private final @Nullable Boolean initiallyEnabled;

  private ImmutableChoiceOption(
      String displayName,
      String name,
      Iterable<? extends Choice> choices,
      String disabledValue,
      Optional<Boolean> initiallyEnabled) {
    this.displayName = Objects.requireNonNull(displayName, "displayName");
    this.name = Objects.requireNonNull(name, "name");
    this.choices = ImmutableList.copyOf(choices);
    this.disabledValue = Objects.requireNonNull(disabledValue, "disabledValue");
    this.initiallyEnabled = initiallyEnabled.orElse(null);
  }

  private ImmutableChoiceOption(
      ImmutableChoiceOption original,
      String displayName,
      String name,
      ImmutableList<Choice> choices,
      String disabledValue,
      @Nullable Boolean initiallyEnabled) {
    this.displayName = displayName;
    this.name = name;
    this.choices = choices;
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
   * @return The value of the {@code choices} attribute
   */
  @Override
  public ImmutableList<Choice> choices() {
    return choices;
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
   * Copy the current immutable object by setting a value for the {@link ChoiceOption#displayName() displayName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for displayName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableChoiceOption withDisplayName(String value) {
    String newValue = Objects.requireNonNull(value, "displayName");
    if (this.displayName.equals(newValue)) return this;
    return new ImmutableChoiceOption(this, newValue, this.name, this.choices, this.disabledValue, this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ChoiceOption#name() name} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for name
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableChoiceOption withName(String value) {
    String newValue = Objects.requireNonNull(value, "name");
    if (this.name.equals(newValue)) return this;
    return new ImmutableChoiceOption(this, this.displayName, newValue, this.choices, this.disabledValue, this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link ChoiceOption#choices() choices}.
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableChoiceOption withChoices(Choice... elements) {
    ImmutableList<Choice> newValue = ImmutableList.copyOf(elements);
    return new ImmutableChoiceOption(this, this.displayName, this.name, newValue, this.disabledValue, this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link ChoiceOption#choices() choices}.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param elements An iterable of choices elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableChoiceOption withChoices(Iterable<? extends Choice> elements) {
    if (this.choices == elements) return this;
    ImmutableList<Choice> newValue = ImmutableList.copyOf(elements);
    return new ImmutableChoiceOption(this, this.displayName, this.name, newValue, this.disabledValue, this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ChoiceOption#disabledValue() disabledValue} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for disabledValue
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableChoiceOption withDisabledValue(String value) {
    String newValue = Objects.requireNonNull(value, "disabledValue");
    if (this.disabledValue.equals(newValue)) return this;
    return new ImmutableChoiceOption(this, this.displayName, this.name, this.choices, newValue, this.initiallyEnabled);
  }

  /**
   * Copy the current immutable object by setting a <i>present</i> value for the optional {@link ChoiceOption#initiallyEnabled() initiallyEnabled} attribute.
   * @param value The value for initiallyEnabled
   * @return A modified copy of {@code this} object
   */
  public final ImmutableChoiceOption withInitiallyEnabled(boolean value) {
    @Nullable Boolean newValue = value;
    if (Objects.equals(this.initiallyEnabled, newValue)) return this;
    return new ImmutableChoiceOption(this, this.displayName, this.name, this.choices, this.disabledValue, newValue);
  }

  /**
   * Copy the current immutable object by setting an optional value for the {@link ChoiceOption#initiallyEnabled() initiallyEnabled} attribute.
   * An equality check is used on inner nullable value to prevent copying of the same value by returning {@code this}.
   * @param optional A value for initiallyEnabled
   * @return A modified copy of {@code this} object
   */
  public final ImmutableChoiceOption withInitiallyEnabled(Optional<Boolean> optional) {
    @Nullable Boolean value = optional.orElse(null);
    if (Objects.equals(this.initiallyEnabled, value)) return this;
    return new ImmutableChoiceOption(this, this.displayName, this.name, this.choices, this.disabledValue, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableChoiceOption} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableChoiceOption
        && equalTo((ImmutableChoiceOption) another);
  }

  private boolean equalTo(ImmutableChoiceOption another) {
    return displayName.equals(another.displayName)
        && name.equals(another.name)
        && choices.equals(another.choices)
        && disabledValue.equals(another.disabledValue)
        && Objects.equals(initiallyEnabled, another.initiallyEnabled);
  }

  /**
   * Computes a hash code from attributes: {@code displayName}, {@code name}, {@code choices}, {@code disabledValue}, {@code initiallyEnabled}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + displayName.hashCode();
    h += (h << 5) + name.hashCode();
    h += (h << 5) + choices.hashCode();
    h += (h << 5) + disabledValue.hashCode();
    h += (h << 5) + Objects.hashCode(initiallyEnabled);
    return h;
  }

  /**
   * Prints the immutable value {@code ChoiceOption} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ChoiceOption")
        .omitNullValues()
        .add("displayName", displayName)
        .add("name", name)
        .add("choices", choices)
        .add("disabledValue", disabledValue)
        .add("initiallyEnabled", initiallyEnabled)
        .toString();
  }

  /**
   * Construct a new immutable {@code ChoiceOption} instance.
   * @param displayName The value for the {@code displayName} attribute
   * @param name The value for the {@code name} attribute
   * @param choices The value for the {@code choices} attribute
   * @param disabledValue The value for the {@code disabledValue} attribute
   * @param initiallyEnabled The value for the {@code initiallyEnabled} attribute
   * @return An immutable ChoiceOption instance
   */
  public static ImmutableChoiceOption of(String displayName, String name, List<Choice> choices, String disabledValue, Optional<Boolean> initiallyEnabled) {
    return of(displayName, name, (Iterable<? extends Choice>) choices, disabledValue, initiallyEnabled);
  }

  /**
   * Construct a new immutable {@code ChoiceOption} instance.
   * @param displayName The value for the {@code displayName} attribute
   * @param name The value for the {@code name} attribute
   * @param choices The value for the {@code choices} attribute
   * @param disabledValue The value for the {@code disabledValue} attribute
   * @param initiallyEnabled The value for the {@code initiallyEnabled} attribute
   * @return An immutable ChoiceOption instance
   */
  public static ImmutableChoiceOption of(String displayName, String name, Iterable<? extends Choice> choices, String disabledValue, Optional<Boolean> initiallyEnabled) {
    return new ImmutableChoiceOption(displayName, name, choices, disabledValue, initiallyEnabled);
  }

  /**
   * Creates an immutable copy of a {@link ChoiceOption} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ChoiceOption instance
   */
  public static ImmutableChoiceOption copyOf(ChoiceOption instance) {
    if (instance instanceof ImmutableChoiceOption) {
      return (ImmutableChoiceOption) instance;
    }
    return ImmutableChoiceOption.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableChoiceOption ImmutableChoiceOption}.
   * <pre>
   * ImmutableChoiceOption.builder()
   *    .displayName(String) // required {@link ChoiceOption#displayName() displayName}
   *    .name(String) // required {@link ChoiceOption#name() name}
   *    .addChoices|addAllChoices(edu.cmu.cs.diamond.hyperfind.connector.api.bundle.Choice) // {@link ChoiceOption#choices() choices} elements
   *    .disabledValue(String) // required {@link ChoiceOption#disabledValue() disabledValue}
   *    .initiallyEnabled(Boolean) // optional {@link ChoiceOption#initiallyEnabled() initiallyEnabled}
   *    .build();
   * </pre>
   * @return A new ImmutableChoiceOption builder
   */
  public static ImmutableChoiceOption.Builder builder() {
    return new ImmutableChoiceOption.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableChoiceOption ImmutableChoiceOption}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ChoiceOption", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DISPLAY_NAME = 0x1L;
    private static final long INIT_BIT_NAME = 0x2L;
    private static final long INIT_BIT_DISABLED_VALUE = 0x4L;
    private long initBits = 0x7L;

    private @Nullable String displayName;
    private @Nullable String name;
    private ImmutableList.Builder<Choice> choices = ImmutableList.builder();
    private @Nullable String disabledValue;
    private @Nullable Boolean initiallyEnabled;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code edu.cmu.cs.diamond.hyperfind.connector.api.bundle.ChoiceOption} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(ChoiceOption instance) {
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
      if (object instanceof ChoiceOption) {
        ChoiceOption instance = (ChoiceOption) object;
        if ((bits & 0x2L) == 0) {
          name(instance.name());
          bits |= 0x2L;
        }
        addAllChoices(instance.choices());
        disabledValue(instance.disabledValue());
        if ((bits & 0x1L) == 0) {
          displayName(instance.displayName());
          bits |= 0x1L;
        }
        Optional<Boolean> initiallyEnabledOptional = instance.initiallyEnabled();
        if (initiallyEnabledOptional.isPresent()) {
          initiallyEnabled(initiallyEnabledOptional);
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
     * Initializes the value for the {@link ChoiceOption#displayName() displayName} attribute.
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
     * Initializes the value for the {@link ChoiceOption#name() name} attribute.
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
     * Adds one element to {@link ChoiceOption#choices() choices} list.
     * @param element A choices element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addChoices(Choice element) {
      this.choices.add(element);
      return this;
    }

    /**
     * Adds elements to {@link ChoiceOption#choices() choices} list.
     * @param elements An array of choices elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addChoices(Choice... elements) {
      this.choices.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link ChoiceOption#choices() choices} list.
     * @param elements An iterable of choices elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder choices(Iterable<? extends Choice> elements) {
      this.choices = ImmutableList.builder();
      return addAllChoices(elements);
    }

    /**
     * Adds elements to {@link ChoiceOption#choices() choices} list.
     * @param elements An iterable of choices elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllChoices(Iterable<? extends Choice> elements) {
      this.choices.addAll(elements);
      return this;
    }

    /**
     * Initializes the value for the {@link ChoiceOption#disabledValue() disabledValue} attribute.
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
     * Initializes the optional value {@link ChoiceOption#initiallyEnabled() initiallyEnabled} to initiallyEnabled.
     * @param initiallyEnabled The value for initiallyEnabled
     * @return {@code this} builder for chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder initiallyEnabled(boolean initiallyEnabled) {
      this.initiallyEnabled = initiallyEnabled;
      return this;
    }

    /**
     * Initializes the optional value {@link ChoiceOption#initiallyEnabled() initiallyEnabled} to initiallyEnabled.
     * @param initiallyEnabled The value for initiallyEnabled
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder initiallyEnabled(Optional<Boolean> initiallyEnabled) {
      this.initiallyEnabled = initiallyEnabled.orElse(null);
      return this;
    }

    /**
     * Builds a new {@link ImmutableChoiceOption ImmutableChoiceOption}.
     * @return An immutable instance of ChoiceOption
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableChoiceOption build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableChoiceOption(null, displayName, name, choices.build(), disabledValue, initiallyEnabled);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DISPLAY_NAME) != 0) attributes.add("displayName");
      if ((initBits & INIT_BIT_NAME) != 0) attributes.add("name");
      if ((initBits & INIT_BIT_DISABLED_VALUE) != 0) attributes.add("disabledValue");
      return "Cannot build ChoiceOption, some of required attributes are not set " + attributes;
    }
  }
}
