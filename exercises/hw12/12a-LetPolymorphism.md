# Task 1: Let-polymorphism

```scala
val exTypeInferenceSTLC =
  Let("f", Fun("x", ???, "x"),
    Let("dummy", App("f", 1),
      App("f", Fun("y", NumType(), "y"))))
```

There is no replacement for `???` in the above expression that will make it typecheck in STLC. The parameter type of `f` must be declared in the `Fun` expression, but the two usages of `f` in the following program require two different parameter types. The first usage in the `Let` expression requires `f` to take arguments of type `NumType()` while the second usage in the `App` expression requires `f` to take arguments of type `FunType(NumType(),NumType())`. 

`f` cannot fulfill both requirements, therefore `???` cannot be replaced by a type that makes the expression typecheck.

---

When using Hindley-Milner type inference the parameter type declaration of `f` is no longer needed. In combination with _let-polymorphism_ this allows the following expression to typecheck.

```scala
val exTypeInferenceHM =
  Let("f", Fun("x", "x"),
    Let("dummy", App("f", 1),
      App("f", Fun("y", "y"))))
```

For bindings in `Let`-expressions the typecheck algorithm internally substitutes all occurences of the identifier with the bound expression, thereby making the type of the each occurence independent. This means that the type of `f` for the second usage can differ from that of the first usage. This allows `f` to have the type `FunType(NumType(),NumType())` for the first usage and `FunType( FunType(NumType(),NumType()), FunType(NumType(),NumType()) )` for the second usage.

Due to this so-called _let-polymorphism_, `Let` can be used for binding _polymorphic_ functions, such as an identity function for arbitrary types. By comparison, STLC would require a seperate identity function for every possible type.
