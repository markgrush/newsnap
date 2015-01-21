(defproject newsnap "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [liberator "0.12.2"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.3"]
                 [hiccup "1.0.5"]
                 [org.clojure/data.json "0.2.5"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler newsnap.core.handler/app}
  :uberjar-name "newsnap-standalone.jar"
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}
   :uberjar {:main newsnap.core.handler :aot :all}})
