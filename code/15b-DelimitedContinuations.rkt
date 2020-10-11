#lang racket

(require racket/control)

(* 2 (reset (+ 1 (shift k (k (k 5))))))
; 'shift' behaves the same as 'let/cc', but only binds the continuation
; until the closest call of 'reset'.
; 'reset' delimits the continuation.



