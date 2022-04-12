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

(def file-path "sitemap.xml")
(def sitemap (sitemap/load-xml file-path))

(defn sitemap->urls [sitemap]
  (->> sitemap
       (sitemap/->contents)
       (->post-urls)
       (into [])))

(def urls (sitemap->urls sitemap))

(comment
  (def chunks (into [] (partition 50 urls)))

  (count chunks)
  (count (get chunks 0))

  (api/update-bulk! (get chunks 6))
  )

;; batch request は諦めた.
;; 1日のrequest制限が200らしいので複数日にわたって送信することにする.
;;
;; リクエスト制限でこうなる.
;; 429 Too Many Requests POST https://indexing.googleapis.com/v3/urlNotifications:publish { "error" : { "code" : 429, "message" : "Quota exceeded for quota metric 'Publish requests' and limit 'Publish requests per day' of service 'indexing.googleapis.com' for consumer 'project_number:885789757693'.", "errors" : [ { "message" : "Quota exceeded for quota metric 'Publish requests' and limit 'Publish requests per day' of service 'indexing.googleapis.com' for consumer 'project_number:885789757693'.", "domain" : "global", "reason" : "rateLimitExceeded" } ], "status" : "RESOURCE_EXHAUSTED", "details" : [ { "@type" : "type.googleapis.com/google.rpc.ErrorInfo", "reason" : "RATE_LIMIT_EXCEEDED", "domain" : "googleapis.com", "metadata" : { "quota_limit" : "DefaultPublishRequestsPerDayPerProject", "consumer" : "projects/885789757693", "quota_metric" : "indexing.googleapis.com/v3_publish_requests", "service" : "indexing.googleapis.com" } } ] } }

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  (def contents (sitemap/->contents sitemap))
  (count contents)

  (def posts (->posts contents))
  (count posts)

  (def post-urls (into [] (->post-urls contents)))
  (count post-urls)
  )

(comment
  (tag-url? "https://futurismo.biz/tags/twitter/")
  (category-url? "https://futurismo.biz/categories/tech/")
  )
