(ns klipse.lang.php
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [klipse.utils :refer [runonce]]
   [cljs.core.async :refer [<! timeout chan put!]]
   [klipse.common.registry :refer [codemirror-mode-src register-mode scripts-src]]
   [applied-science.js-interop :as j]))


(def load-php-engine
    (fn []
      (j/call js/uniter :createEngine "PHP")))

(defn execute [engine input]
  (j/call engine :execute input))

(defn str-eval-async [exp _]
  (let [c (chan)
        php-exp (str "<?php" exp) ]
    (go
      (let [php-engine  (load-php-engine)]
        (as-> (j/call php-engine :getStderr) $
            (j/call $ :on "data" #(put! c (str %))))
        (as-> (j/call php-engine :getStdout) $
            (j/call $ :on "data" #(put! c (str %))))
        (execute php-engine php-exp)))
    c))

(def opts {:editor-in-mode "text/x-php"
           :editor-out-mode "text/x-php"
           :eval-fn str-eval-async
           :external-scripts [(codemirror-mode-src "xml") (codemirror-mode-src "clike") (codemirror-mode-src "php") (codemirror-mode-src "javascript") (codemirror-mode-src "css") (scripts-src "uniter.js?r")]
           :comment-str "//"})

(register-mode "eval-php" "selector_eval_php" opts)
