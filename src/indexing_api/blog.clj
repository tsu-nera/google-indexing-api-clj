(ns indexing-api.blog
  (:require
   [clojure.string :as string]
   [indexing-api.core :as api]
   [indexing-api.sitemap :as sitemap]))

(def base-url "https://futurismo.biz/")

(defn tag-url? [url]
  (string/includes? url "/tags/"))

(defn category-url? [url]
  (string/includes? url "/categories/"))

(defn tag? [content]
  (tag-url? (:url content)))

(defn category? [content]
  (category-url? (:url content)))

(defn other? [content]
  (let [url        (:url content)
        base       base-url
        posts      (str base "posts/")
        about      (str base "about/")
        archives   (str base "archives/")
        categories (str base "categories/")
        tags       (str base "tags/")
        search     (str base "search/")]
    (some #(= url %) [base
                      posts
                      about
                      archives
                      search
                      tags
                      categories])))

(defn post? [content]
  (and (not (tag? content))
       (not (category? content))
       (not (other? content))))

(defn ->posts
  [contents]
  (filter post? contents))

(defn ->post-urls
  [contents]
  (->> contents
       (filter post?)
       (map :url)))

;;;;;;;;;;;;;;;;;

(comment
  (tag-url? "https://futurismo.biz/tags/twitter/")
  (category-url? "https://futurismo.biz/categories/tech/")
  )

(comment
  (def file-path "sitemap.xml")
  (def sitemap (sitemap/load-xml file-path))

  (def contents (sitemap/->contents sitemap))
  (count contents)

  (def posts (->posts contents))
  (count posts)

  (def post-urls (into [] (->post-urls contents)))

  (count post-urls)
  (def chunks (into [] (partition 50 post-urls)))

  (count (get chunks 0))

  (api/update-bulk! (get chunks 2))
  )
