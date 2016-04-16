(ns data.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-json.core :as json]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:import [java.nio.file FileSystems Files Paths]
           [java.net URLEncoder URLDecoder]))

(def log
  "log to standard out"
  (let [a (agent "")]
    (fn[& msg]
      (send a #(println "****** " (clojure.string/join "" %2)) msg)
      nil)))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/images" {{:strs [start end]} :query-params}
       (log "requesting <" start "> to <" end ">")
       (let [fs (FileSystems/getDefault)
             image-path (Files/newDirectoryStream (Paths/get "/" (into-array String ["home" "jmhirata" "Pictures"])) "*.{jpg,png}")]
         (let [results (map (comp (fn[i] {:path i}) #(URLEncoder/encode % "UTF-8")  str) image-path)
               s (when (seq start) (Integer/parseInt start))
               e (when (seq end) (Integer/parseInt end))]
         {:status 200
          :headers { "content-type" "application/json"}
          :body (json/generate-string (if (and s e)
                                        (take (- e s) (drop s results))
                                        results))})))
  (GET "/image/:id" [id]
       (let [i (URLDecoder/decode id)]
         (log "reading " i)
         (clojure.java.io/input-stream i)))
  (route/not-found "Not Found"))

(def app
  (wrap-cors
   (wrap-defaults app-routes site-defaults)
   :access-control-allow-origin [#"http://localhost:8000"]
   :access-control-allow-methods [:get]))
  
