## Subtask 1

### 1.
The equivalent of `(+ 1 (let/cc k (k 3)))` in our language is:
```scala
Add(1, LetCC("k", App("k", 3)))
```

After CPS transformation (excluding `LetCC`), this becomes:
```scala
Fun("k0", App([LetCC("k", App("k",3))], Fun("a", App("k0", Add(1,"a")))))
```

We finish by replacing `LetCC` with a function:
```scala
Fun("k0", App(Fun("k", App("k",3)), Fun("a", App("k0", Add(1,"a")))))
```

Without the outer continuation:
```scala
App(Fun("k", App("k",3)), Fun("a", Add(1,"a")))
```

### 2.
The equivalent of `(+ 1 (let/cc k (k 3)))` in our language is:
```scala
Add(LetCC("k", App("k", 3)), 2)
```

CPS-transformed (excluding `LetCC`):
```scala
Fun("k0", App([LetCC("k", App("k",3))], Fun("a", App("k0", Add("a",2)))))
```

Replace `LetCC` with `Fun`:
```scala
Fun("k0", App(Fun("k", App("k",3)), Fun("a", App("k0", Add("a",2)))))
```

Without the outer continuation:
```scala
App(Fun("k", App("k",3)), Fun("a", Add("a",2)))
```

### 3.
The equivalent of 
```scheme
(+ 3 (let/cc k
        (let ([f (lambda (x) (+ 1 x))])
			(k (+ (f 2) (f 5))))))
```
in our language is:
```scala
Add(3, LetCC("k",
  wth("f", Fun("x", Add(1,"x")),
    App("k", Add(App("f",2), App("f",5))))))
```

CPS-transformed (excluding `LetCC`):
```scala
Fun("k0", App([LetCC("k",
  wth("f", Fun("x", Add(1,"x")),
    App("k", Add(App("f",2), App("f",5)))))],
	  Fun("a", App("k0", Add(3, "a")))))
```

Replace `LetCC` with `Fun`:
```scala
Fun("k0", App(Fun("k",
  wth("f", Fun("x", Add(1,"x")),
    App("k", Add(App("f",2), App("f",5))))),
	  Fun("a", App("k0", Add(3,"a")))))
```

Without the outer continuation:
```scala
App(Fun("k",
  wth("f", Fun("x", Add(1,"x")),
    App("k", Add(App("f",2), App("f",5))))),
	  Fun("a", Add(3,"a")))
```


## Subtask 2

The first example `(+ 1 (let/cc k 3))` differs from the previous examples by only binding the continuation and never calling it. The seconds example `(+ 1 (let/cc k (+ (k 3) 2)))` differs due to the `+`-call surrounding `(k 3)` which is ignored since the undelimited continuation `k` does not return (hence the result is `4`).

The result of transforming the first example using the steps from [Subtask 1](#subtask-1) is
```scala
Add(1, LetCC("k", 3))
// ~~>
App(Fun("k", 3), Fun("a", Add(1,"a")))
```

The transformed expression evaluates to `3`, the `+`-Operation with `1` is not performed. The result is therefore not the same as that of the original expression (`4`). In the second example, the expression surrounding the continuation call needs to be removed, which is not taken into consideration when transforming like in [Subtask 1](#subtask-1).

`LetCC` is not syntactic sugar, rather it increases the expressive capability of our language (e.g. when "jumping" without returning and thereby ignoring the context of the continuation call).












