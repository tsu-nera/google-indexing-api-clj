(ns indexing-api.sitemap
  (:require
   [clojure.java.io :as io]))

(defn load-xml [file-path]
  (-> file-path
      io/resource
      io/input-stream
      xml/parse))

(defn ->contents
  "Clojureのxml/parseで読み込んだsitemap.xmlを map & vectorに変換."
  [sitemap]
  (let [content (:content sitemap)]
    (map (fn [x]
           (let [content (:content x)
                 url     (first (:content (first content)))
                 lastmod (first (:content (second content)))]
             {:url     url
              :lastmod lastmod})) content)))

