(ns ^:figwheel-no-load clang.dev
  (:require
    [clang.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
