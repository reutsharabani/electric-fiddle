(ns user
  (:require fiddle-manager))

(def x 1)
(fiddle-manager/write-loader-file "./src-dev/fiddles.cljc")
(require 'dev)

; Under :dev alias, automatically load 'dev so the REPL is ready to go with zero interaction
