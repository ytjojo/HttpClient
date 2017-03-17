package com.ytjojo.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * Utility to work with Java 5 generic type parameters.
 * Mainly for internal use within the framework.
 *
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.0.7
 */
public abstract class TypeUtils {

	/**
	 * Check if the right-hand side type may be assigned to the left-hand side
	 * type following the Java generics rules.
	 * @param lhsType the target type
	 * @param rhsType the value type that should be assigned to the target type
	 * @return true if rhs is assignable to lhs
	 */
	public static boolean isAssignable(Type lhsType, Type rhsType) {
		Assert.notNull(lhsType, "Left-hand side type must not be null");
		Assert.notNull(rhsType, "Right-hand side type must not be null");

		// all types are assignable to themselves and to class Object
		if (lhsType.equals(rhsType) || Object.class == lhsType) {
			return true;
		}

		if (lhsType instanceof Class<?>) {
			Class<?> lhsClass = (Class<?>) lhsType;

			// just comparing two classes
			if (rhsType instanceof Class<?>) {
				return ClassUtils.isAssignable(lhsClass, (Class<?>) rhsType);
			}

			if (rhsType instanceof ParameterizedType) {
				Type rhsRaw = ((ParameterizedType) rhsType).getRawType();

				// a parameterized type is always assignable to its raw class type
				if (rhsRaw instanceof Class<?>) {
					return ClassUtils.isAssignable(lhsClass, (Class<?>) rhsRaw);
				}
			}
			else if (lhsClass.isArray() && rhsType instanceof GenericArrayType) {
				Type rhsComponent = ((GenericArrayType) rhsType).getGenericComponentType();

				return isAssignable(lhsClass.getComponentType(), rhsComponent);
			}
		}

		// parameterized types are only assignable to other parameterized types and class types
		if (lhsType instanceof ParameterizedType) {
			if (rhsType instanceof Class<?>) {
				Type lhsRaw = ((ParameterizedType) lhsType).getRawType();

				if (lhsRaw instanceof Class<?>) {
					return ClassUtils.isAssignable((Class<?>) lhsRaw, (Class<?>) rhsType);
				}
			}
			else if (rhsType instanceof ParameterizedType) {
				return isAssignable((ParameterizedType) lhsType, (ParameterizedType) rhsType);
			}
		}

		if (lhsType instanceof GenericArrayType) {
			Type lhsComponent = ((GenericArrayType) lhsType).getGenericComponentType();

			if (rhsType instanceof Class<?>) {
				Class<?> rhsClass = (Class<?>) rhsType;

				if (rhsClass.isArray()) {
					return isAssignable(lhsComponent, rhsClass.getComponentType());
				}
			}
			else if (rhsType instanceof GenericArrayType) {
				Type rhsComponent = ((GenericArrayType) rhsType).getGenericComponentType();

				return isAssignable(lhsComponent, rhsComponent);
			}
		}

		if (lhsType instanceof WildcardType) {
			return isAssignable((WildcardType) lhsType, rhsType);
		}

		return false;
	}

	private static boolean isAssignable(ParameterizedType lhsType, ParameterizedType rhsType) {
		if (lhsType.equals(rhsType)) {
			return true;
		}

		Type[] lhsTypeArguments = lhsType.getActualTypeArguments();
		Type[] rhsTypeArguments = rhsType.getActualTypeArguments();

		if (lhsTypeArguments.length != rhsTypeArguments.length) {
			return false;
		}

		for (int size = lhsTypeArguments.length, i = 0; i < size; ++i) {
			Type lhsArg = lhsTypeArguments[i];
			Type rhsArg = rhsTypeArguments[i];

			if (!lhsArg.equals(rhsArg) &&
					!(lhsArg instanceof WildcardType && isAssignable((WildcardType) lhsArg, rhsArg))) {
				return false;
			}
		}

		return true;
	}

	private static boolean isAssignable(WildcardType lhsType, Type rhsType) {
		Type[] lUpperBounds = lhsType.getUpperBounds();

		// supply the implicit upper bound if none are specified
		if (lUpperBounds.length == 0) {
			lUpperBounds = new Type[] { Object.class };
		}

		Type[] lLowerBounds = lhsType.getLowerBounds();

		// supply the implicit lower bound if none are specified
		if (lLowerBounds.length == 0) {
			lLowerBounds = new Type[] { null };
		}

		if (rhsType instanceof WildcardType) {
			// both the upper and lower bounds of the right-hand side must be
			// completely enclosed in the upper and lower bounds of the left-
			// hand side.
			WildcardType rhsWcType = (WildcardType) rhsType;
			Type[] rUpperBounds = rhsWcType.getUpperBounds();

			if (rUpperBounds.length == 0) {
				rUpperBounds = new Type[] { Object.class };
			}

			Type[] rLowerBounds = rhsWcType.getLowerBounds();

			if (rLowerBounds.length == 0) {
				rLowerBounds = new Type[] { null };
			}

			for (Type lBound : lUpperBounds) {
				for (Type rBound : rUpperBounds) {
					if (!isAssignableBound(lBound, rBound)) {
						return false;
					}
				}

				for (Type rBound : rLowerBounds) {
					if (!isAssignableBound(lBound, rBound)) {
						return false;
					}
				}
			}

			for (Type lBound : lLowerBounds) {
				for (Type rBound : rUpperBounds) {
					if (!isAssignableBound(rBound, lBound)) {
						return false;
					}
				}

				for (Type rBound : rLowerBounds) {
					if (!isAssignableBound(rBound, lBound)) {
						return false;
					}
				}
			}
		}
		else {
			for (Type lBound : lUpperBounds) {
				if (!isAssignableBound(lBound, rhsType)) {
					return false;
				}
			}

			for (Type lBound : lLowerBounds) {
				if (!isAssignableBound(rhsType, lBound)) {
					return false;
				}
			}
		}

		return true;
	}

	public static boolean isAssignableBound(Type lhsType, Type rhsType) {
		if (rhsType == null) {
			return true;
		}

		if (lhsType == null) {
			return false;
		}
		return isAssignable(lhsType, rhsType);
	}


	/**
	 * retrofit utils
	 *
	 *
	 */
	static final Type[] EMPTY_TYPE_ARRAY = new Type[0];


	public static Class<?> getRawType(Type type) {
		if (type == null) throw new NullPointerException("type == null");

		if (type instanceof Class<?>) {
			// Type is a normal class.
			return (Class<?>) type;
		}
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;

			// I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
			// suspects some pathological case related to nested classes exists.
			Type rawType = parameterizedType.getRawType();
			if (!(rawType instanceof Class)) throw new IllegalArgumentException();
			return (Class<?>) rawType;
		}
		if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			return Array.newInstance(getRawType(componentType), 0).getClass();
		}
		if (type instanceof TypeVariable) {
			// We could use the variable's bounds, but that won't work if there are multiple. Having a raw
			// type that's more general than necessary is okay.
			return Object.class;
		}
		if (type instanceof WildcardType) {
			return getRawType(((WildcardType) type).getUpperBounds()[0]);
		}

		throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
				+ "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
	}

	/** Returns true if {@code a} and {@code b} are equal. */
	static boolean equals(Type a, Type b) {
		if (a == b) {
			return true; // Also handles (a == null && b == null).

		} else if (a instanceof Class) {
			return a.equals(b); // Class already specifies equals().

		} else if (a instanceof ParameterizedType) {
			if (!(b instanceof ParameterizedType)) return false;
			ParameterizedType pa = (ParameterizedType) a;
			ParameterizedType pb = (ParameterizedType) b;
			return equal(pa.getOwnerType(), pb.getOwnerType())
					&& pa.getRawType().equals(pb.getRawType())
					&& Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());

		} else if (a instanceof GenericArrayType) {
			if (!(b instanceof GenericArrayType)) return false;
			GenericArrayType ga = (GenericArrayType) a;
			GenericArrayType gb = (GenericArrayType) b;
			return equals(ga.getGenericComponentType(), gb.getGenericComponentType());

		} else if (a instanceof WildcardType) {
			if (!(b instanceof WildcardType)) return false;
			WildcardType wa = (WildcardType) a;
			WildcardType wb = (WildcardType) b;
			return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
					&& Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

		} else if (a instanceof TypeVariable) {
			if (!(b instanceof TypeVariable)) return false;
			TypeVariable<?> va = (TypeVariable<?>) a;
			TypeVariable<?> vb = (TypeVariable<?>) b;
			return va.getGenericDeclaration() == vb.getGenericDeclaration()
					&& va.getName().equals(vb.getName());

		} else {
			return false; // This isn't a type we support!
		}
	}

	/**
	 * Returns the generic supertype for {@code supertype}. For example, given a class {@code
	 * IntegerSet}, the result for when supertype is {@code Set.class} is {@code Set<Integer>} and the
	 * result when the supertype is {@code Collection.class} is {@code Collection<Integer>}.
	 */
	static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
		if (toResolve == rawType) return context;

		// We skip searching through interfaces if unknown is an interface.
		if (toResolve.isInterface()) {
			Class<?>[] interfaces = rawType.getInterfaces();
			for (int i = 0, length = interfaces.length; i < length; i++) {
				if (interfaces[i] == toResolve) {
					return rawType.getGenericInterfaces()[i];
				} else if (toResolve.isAssignableFrom(interfaces[i])) {
					return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
				}
			}
		}

		// Check our supertypes.
		if (!rawType.isInterface()) {
			while (rawType != Object.class) {
				Class<?> rawSupertype = rawType.getSuperclass();
				if (rawSupertype == toResolve) {
					return rawType.getGenericSuperclass();
				} else if (toResolve.isAssignableFrom(rawSupertype)) {
					return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
				}
				rawType = rawSupertype;
			}
		}

		// We can't resolve this further.
		return toResolve;
	}
	/**
	 * Returns the component type of this array type.
	 * @throws ClassCastException if this type is not an array.
	 */
	public static Type getArrayComponentType(Type array) {
		return array instanceof GenericArrayType
				? ((GenericArrayType) array).getGenericComponentType()
				: ((Class<?>) array).getComponentType();
	}

	/**
	 * Returns the element type of this collection type.
	 * @throws IllegalArgumentException if this type is not a collection.
	 */
	public static Type getCollectionElementType(Type context, Class<?> contextRawType) {
		Type collectionType = getSupertype(context, contextRawType, Collection.class);

		if (collectionType instanceof WildcardType) {
			collectionType = ((WildcardType)collectionType).getUpperBounds()[0];
		}
		if (collectionType instanceof ParameterizedType) {
			return ((ParameterizedType) collectionType).getActualTypeArguments()[0];
		}
		return Object.class;
	}

	/**
	 * Returns a two element array containing this map's key and value types in
	 * positions 0 and 1 respectively.
	 */
	public static Type[] getMapKeyAndValueTypes(Type context, Class<?> contextRawType) {
    /*
     * Work around a problem with the declaration of java.util.Properties. That
     * class should extend Hashtable<String, String>, but it's declared to
     * extend Hashtable<Object, Object>.
     */
		if (context == Properties.class) {
			return new Type[] { String.class, String.class }; // TODO: test subclasses of Properties!
		}

		Type mapType = getSupertype(context, contextRawType, Map.class);
		// TODO: strip wildcards?
		if (mapType instanceof ParameterizedType) {
			ParameterizedType mapParameterizedType = (ParameterizedType) mapType;
			return mapParameterizedType.getActualTypeArguments();
		}
		return new Type[] { Object.class, Object.class };
	}

	private static int indexOf(Object[] array, Object toFind) {
		for (int i = 0; i < array.length; i++) {
			if (toFind.equals(array[i])) return i;
		}
		throw new NoSuchElementException();
	}

	private static boolean equal(Object a, Object b) {
		return a == b || (a != null && a.equals(b));
	}

	static int hashCodeOrZero(Object o) {
		return o != null ? o.hashCode() : 0;
	}

	static String typeToString(Type type) {
		return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
	}

	/**
	 * Returns the generic form of {@code supertype}. For example, if this is {@code
	 * ArrayList<String>}, this returns {@code Iterable<String>} given the input {@code
	 * Iterable.class}.
	 *
	 * @param supertype a superclass of, or interface implemented by, this.
	 */
	static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
		if (!supertype.isAssignableFrom(contextRawType)) throw new IllegalArgumentException();
		return resolve(context, contextRawType,
				getGenericSupertype(context, contextRawType, supertype));
	}

	static Type resolve(Type context, Class<?> contextRawType, Type toResolve) {
		// This implementation is made a little more complicated in an attempt to avoid object-creation.
		while (true) {
			if (toResolve instanceof TypeVariable) {
				TypeVariable<?> typeVariable = (TypeVariable<?>) toResolve;
				toResolve = resolveTypeVariable(context, contextRawType, typeVariable);
				if (toResolve == typeVariable) {
					return toResolve;
				}

			} else if (toResolve instanceof Class && ((Class<?>) toResolve).isArray()) {
				Class<?> original = (Class<?>) toResolve;
				Type componentType = original.getComponentType();
				Type newComponentType = resolve(context, contextRawType, componentType);
				return componentType == newComponentType ? original : new GenericArrayTypeImpl(
						newComponentType);

			} else if (toResolve instanceof GenericArrayType) {
				GenericArrayType original = (GenericArrayType) toResolve;
				Type componentType = original.getGenericComponentType();
				Type newComponentType = resolve(context, contextRawType, componentType);
				return componentType == newComponentType ? original : new GenericArrayTypeImpl(
						newComponentType);

			} else if (toResolve instanceof ParameterizedType) {
				ParameterizedType original = (ParameterizedType) toResolve;
				Type ownerType = original.getOwnerType();
				Type newOwnerType = resolve(context, contextRawType, ownerType);
				boolean changed = newOwnerType != ownerType;

				Type[] args = original.getActualTypeArguments();
				for (int t = 0, length = args.length; t < length; t++) {
					Type resolvedTypeArgument = resolve(context, contextRawType, args[t]);
					if (resolvedTypeArgument != args[t]) {
						if (!changed) {
							args = args.clone();
							changed = true;
						}
						args[t] = resolvedTypeArgument;
					}
				}

				return changed
						? new ParameterizedTypeImpl(newOwnerType, original.getRawType(), args)
						: original;

			} else if (toResolve instanceof WildcardType) {
				WildcardType original = (WildcardType) toResolve;
				Type[] originalLowerBound = original.getLowerBounds();
				Type[] originalUpperBound = original.getUpperBounds();

				if (originalLowerBound.length == 1) {
					Type lowerBound = resolve(context, contextRawType, originalLowerBound[0]);
					if (lowerBound != originalLowerBound[0]) {
						return new WildcardTypeImpl(new Type[] { Object.class }, new Type[] { lowerBound });
					}
				} else if (originalUpperBound.length == 1) {
					Type upperBound = resolve(context, contextRawType, originalUpperBound[0]);
					if (upperBound != originalUpperBound[0]) {
						return new WildcardTypeImpl(new Type[] { upperBound }, EMPTY_TYPE_ARRAY);
					}
				}
				return original;

			} else {
				return toResolve;
			}
		}
	}

	private static Type resolveTypeVariable(
			Type context, Class<?> contextRawType, TypeVariable<?> unknown) {
		Class<?> declaredByRaw = declaringClassOf(unknown);

		// We can't reduce this further.
		if (declaredByRaw == null) return unknown;

		Type declaredBy = getGenericSupertype(context, contextRawType, declaredByRaw);
		if (declaredBy instanceof ParameterizedType) {
			int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
			return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
		}

		return unknown;
	}

	/**
	 * Returns the declaring class of {@code typeVariable}, or {@code null} if it was not declared by
	 * a class.
	 */
	private static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
		GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
		return genericDeclaration instanceof Class ? (Class<?>) genericDeclaration : null;
	}

	static void checkNotPrimitive(Type type) {
		if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()) {
			throw new IllegalArgumentException();
		}
	}

	static <T> T checkNotNull(T object, String message) {
		if (object == null) {
			throw new NullPointerException(message);
		}
		return object;
	}

	/** Returns true if {@code annotations} contains an instance of {@code cls}. */
	static boolean isAnnotationPresent(Annotation[] annotations,
									   Class<? extends Annotation> cls) {
		for (Annotation annotation : annotations) {
			if (cls.isInstance(annotation)) {
				return true;
			}
		}
		return false;
	}


	static Type getParameterUpperBound(int index, ParameterizedType type) {
		Type[] types = type.getActualTypeArguments();
		if (index < 0 || index >= types.length) {
			throw new IllegalArgumentException(
					"Index " + index + " not in range [0," + types.length + ") for " + type);
		}
		Type paramType = types[index];
		if (paramType instanceof WildcardType) {
			return ((WildcardType) paramType).getUpperBounds()[0];
		}
		return paramType;
	}

	static boolean hasUnresolvableType(Type type) {
		if (type instanceof Class<?>) {
			return false;
		}
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
				if (hasUnresolvableType(typeArgument)) {
					return true;
				}
			}
			return false;
		}
		if (type instanceof GenericArrayType) {
			return hasUnresolvableType(((GenericArrayType) type).getGenericComponentType());
		}
		if (type instanceof TypeVariable) {
			return true;
		}
		if (type instanceof WildcardType) {
			return true;
		}
		String className = type == null ? "null" : type.getClass().getName();
		throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
				+ "GenericArrayType, but <" + type + "> is of type " + className);
	}

	static Type getCallResponseType(Type returnType) {
		if (!(returnType instanceof ParameterizedType)) {
			throw new IllegalArgumentException(
					"Call return type must be parameterized as Call<Foo> or Call<? extends Foo>");
		}
		return getParameterUpperBound(0, (ParameterizedType) returnType);
	}

	private static final class ParameterizedTypeImpl implements ParameterizedType {
		private final Type ownerType;
		private final Type rawType;
		private final Type[] typeArguments;

		public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
			// Require an owner type if the raw type needs it.
			if (rawType instanceof Class<?>
					&& (ownerType == null) != (((Class<?>) rawType).getEnclosingClass() == null)) {
				throw new IllegalArgumentException();
			}

			this.ownerType = ownerType;
			this.rawType = rawType;
			this.typeArguments = typeArguments.clone();

			for (Type typeArgument : this.typeArguments) {
				if (typeArgument == null) throw new NullPointerException();
				checkNotPrimitive(typeArgument);
			}
		}

		@Override public Type[] getActualTypeArguments() {
			return typeArguments.clone();
		}

		@Override public Type getRawType() {
			return rawType;
		}

		@Override public Type getOwnerType() {
			return ownerType;
		}

		@Override public boolean equals(Object other) {
			return other instanceof ParameterizedType && TypeUtils.equals(this, (ParameterizedType) other);
		}

		@Override public int hashCode() {
			return Arrays.hashCode(typeArguments) ^ rawType.hashCode() ^ hashCodeOrZero(ownerType);
		}

		@Override public String toString() {
			StringBuilder result = new StringBuilder(30 * (typeArguments.length + 1));
			result.append(typeToString(rawType));
			if (typeArguments.length == 0) return result.toString();
			result.append("<").append(typeToString(typeArguments[0]));
			for (int i = 1; i < typeArguments.length; i++) {
				result.append(", ").append(typeToString(typeArguments[i]));
			}
			return result.append(">").toString();
		}
	}

	private static final class GenericArrayTypeImpl implements GenericArrayType {
		private final Type componentType;

		public GenericArrayTypeImpl(Type componentType) {
			this.componentType = componentType;
		}

		@Override public Type getGenericComponentType() {
			return componentType;
		}

		@Override public boolean equals(Object o) {
			return o instanceof GenericArrayType
					&& TypeUtils.equals(this, (GenericArrayType) o);
		}

		@Override public int hashCode() {
			return componentType.hashCode();
		}

		@Override public String toString() {
			return typeToString(componentType) + "[]";
		}
	}

	/**
	 * The WildcardType interface supports multiple upper bounds and multiple
	 * lower bounds. We only support what the Java 6 language needs - at most one
	 * bound. If a lower bound is set, the upper bound must be Object.class.
	 */
	private static final class WildcardTypeImpl implements WildcardType {
		private final Type upperBound;
		private final Type lowerBound;

		public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
			if (lowerBounds.length > 1) throw new IllegalArgumentException();
			if (upperBounds.length != 1) throw new IllegalArgumentException();

			if (lowerBounds.length == 1) {
				if (lowerBounds[0] == null) throw new NullPointerException();
				checkNotPrimitive(lowerBounds[0]);
				if (upperBounds[0] != Object.class) throw new IllegalArgumentException();
				this.lowerBound = lowerBounds[0];
				this.upperBound = Object.class;
			} else {
				if (upperBounds[0] == null) throw new NullPointerException();
				checkNotPrimitive(upperBounds[0]);
				this.lowerBound = null;
				this.upperBound = upperBounds[0];
			}
		}

		@Override public Type[] getUpperBounds() {
			return new Type[] { upperBound };
		}

		@Override public Type[] getLowerBounds() {
			return lowerBound != null ? new Type[] { lowerBound } : EMPTY_TYPE_ARRAY;
		}

		@Override public boolean equals(Object other) {
			return other instanceof WildcardType && TypeUtils.equals(this, (WildcardType) other);
		}

		@Override public int hashCode() {
			// This equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds()).
			return (lowerBound != null ? 31 + lowerBound.hashCode() : 1) ^ (31 + upperBound.hashCode());
		}

		@Override public String toString() {
			if (lowerBound != null) return "? super " + typeToString(lowerBound);
			if (upperBound == Object.class) return "?";
			return "? extends " + typeToString(upperBound);
		}
	}
	/**
	 * Returns a type that is functionally equal but not necessarily equal
	 * according to {@link Object#equals(Object) Object.equals()}. The returned
	 * type is {@link java.io.Serializable}.
	 */
	public static Type canonicalize(Type type) {
		if (type instanceof Class) {
			Class<?> c = (Class<?>) type;
			return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;

		} else if (type instanceof ParameterizedType) {
			ParameterizedType p = (ParameterizedType) type;
			return new ParameterizedTypeImpl(p.getOwnerType(),
					p.getRawType(), p.getActualTypeArguments());

		} else if (type instanceof GenericArrayType) {
			GenericArrayType g = (GenericArrayType) type;
			return new GenericArrayTypeImpl(g.getGenericComponentType());

		} else if (type instanceof WildcardType) {
			WildcardType w = (WildcardType) type;
			return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());

		} else {
			// type is either serializable as-is or unsupported
			return type;
		}
	}

	/**
	 * Private helper function that performs some assignability checks for
	 * the provided GenericArrayType.
	 */
	private static boolean isAssignableFrom(Type from, GenericArrayType to) {
		Type toGenericComponentType = to.getGenericComponentType();
		if (toGenericComponentType instanceof ParameterizedType) {
			Type t = from;
			if (from instanceof GenericArrayType) {
				t = ((GenericArrayType) from).getGenericComponentType();
			} else if (from instanceof Class<?>) {
				Class<?> classType = (Class<?>) from;
				while (classType.isArray()) {
					classType = classType.getComponentType();
				}
				t = classType;
			}
			return isAssignableFrom(t, (ParameterizedType) toGenericComponentType,
					new HashMap<String, Type>());
		}
		// No generic defined on "to"; therefore, return true and let other
		// checks determine assignability
		return true;
	}

	/**
	 * Private recursive helper function to actually do the type-safe checking
	 * of assignability.
	 */
	private static boolean isAssignableFrom(Type from, ParameterizedType to,
											Map<String, Type> typeVarMap) {

		if (from == null) {
			return false;
		}

		if (to.equals(from)) {
			return true;
		}

		// First figure out the class and any type information.
		Class<?> clazz =TypeUtils.getRawType(from);
		ParameterizedType ptype = null;
		if (from instanceof ParameterizedType) {
			ptype = (ParameterizedType) from;
		}

		// Load up parameterized variable info if it was parameterized.
		if (ptype != null) {
			Type[] tArgs = ptype.getActualTypeArguments();
			TypeVariable<?>[] tParams = clazz.getTypeParameters();
			for (int i = 0; i < tArgs.length; i++) {
				Type arg = tArgs[i];
				TypeVariable<?> var = tParams[i];
				while (arg instanceof TypeVariable<?>) {
					TypeVariable<?> v = (TypeVariable<?>) arg;
					arg = typeVarMap.get(v.getName());
				}
				typeVarMap.put(var.getName(), arg);
			}

			// check if they are equivalent under our current mapping.
			if (typeEquals(ptype, to, typeVarMap)) {
				return true;
			}
		}

		for (Type itype : clazz.getGenericInterfaces()) {
			if (isAssignableFrom(itype, to, new HashMap<String, Type>(typeVarMap))) {
				return true;
			}
		}

		// Interfaces didn't work, try the superclass.
		Type sType = clazz.getGenericSuperclass();
		return isAssignableFrom(sType, to, new HashMap<String, Type>(typeVarMap));
	}

	/**
	 * Checks if two parameterized types are exactly equal, under the variable
	 * replacement described in the typeVarMap.
	 */
	private static boolean typeEquals(ParameterizedType from,
									  ParameterizedType to, Map<String, Type> typeVarMap) {
		if (from.getRawType().equals(to.getRawType())) {
			Type[] fromArgs = from.getActualTypeArguments();
			Type[] toArgs = to.getActualTypeArguments();
			for (int i = 0; i < fromArgs.length; i++) {
				if (!matches(fromArgs[i], toArgs[i], typeVarMap)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	/**
	 * Checks if two types are the same or are equivalent under a variable mapping
	 * given in the type map that was provided.
	 */
	private static boolean matches(Type from, Type to, Map<String, Type> typeMap) {
		return to.equals(from)
				|| (from instanceof TypeVariable
				&& to.equals(typeMap.get(((TypeVariable<?>) from).getName())));

	}
}