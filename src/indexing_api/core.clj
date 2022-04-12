(ns indexing-api.core
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as io])
  (:import
   (com.google.api.client.googleapis.auth.oauth2
    GoogleCredential)
   (com.google.api.client.http
    ByteArrayContent
    GenericUrl)
   (com.google.api.client.http.javanet
    NetHttpTransport)))

(def scopes "https://www.googleapis.com/auth/indexing")
(def creds-path "credentials.json")

(def endpoint
  "https://indexing.googleapis.com/v3/urlNotifications:publish")

(def credentials
  (delay (let [service-account (-> creds-path
                                   io/resource
                                   io/input-stream)]
           (.createScoped
            (GoogleCredential/fromStream service-account) [scopes]))))

(def type-updated "URL_UPDATED")
(def type-deleted "URL_DELETED")

(defn make-content [url type]
  (let [content {:url  url
                 :type type}]
    (json/generate-string content)))

(defn- post [content]
  (let [http-transport  (NetHttpTransport.)
        request-factory (.createRequestFactory http-transport)
        generic-url     (GenericUrl. endpoint)
        params          (ByteArrayContent/fromString
                         "application/json" content)
        request         (.buildPostRequest request-factory
                                           generic-url params)]
    (do
      (.initialize @credentials request)
      (.execute request))))

(defn update! [url]
  (let [content (make-content url type-updated)
        message (str "update index: " url)]
    (println message)
    (post content)))

(defn update-bulk! [urls]
  (doseq [url urls]
    (do (update! url))))

(defn delete! [url]
  (let [content (make-content url type-deleted)]
    (post content)))

(defn make-metadata-endpoint [url]
  (str
   "https://indexing.googleapis.com/v3/urlNotifications/metadata?url="
   url))

(defn get-status [url]
  (let [endpoint        (make-metadata-endpoint url)
        http-transport  (NetHttpTransport.)
        request-factory (.createRequestFactory http-transport)
        generic-url     (GenericUrl. endpoint)
        request         (.buildGetRequest request-factory generic-url)]
    (do
      (.initialize @credentials request)
      (-> (.execute request)
          (.parseAsString)
          (json/parse-string)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  ;;;
  (def sample-url "https://futurismo.biz/archives/3464/")

  (def resp (update! sample-url))
  (def statsu (.getStatusCode resp))

  (def resp (get-status sample-url))
  ;;;
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; https://developers.google.com/search/apis/indexing-api/v3/prereqs?hl=ja

;; String scopes = "https://www.googleapis.com/auth/indexing";
;; String endPoint = "https://indexing.googleapis.com/v3/urlNotifications:publish";

;; JsonFactory jsonFactory = new JacksonFactory();

;; // service_account_file.json is the private key that you created for your service account.
;; InputStream in = IOUtils.toInputStream("service_account_file.json");

;; GoogleCredential credentials =
;; GoogleCredential.fromStream(in, this.httpTransport, jsonFactory).createScoped(Collections.singleton(scopes));

;; GenericUrl genericUrl = new GenericUrl(endPoint);
;; HttpRequestFactory requestFactory = this.httpTransport.createRequestFactory();

;; // Define content here. The structure of the content is described in the next step.
;; String content = "{"
;; + "\"url\": \"http://example.com/jobs/42\","
;; + "\"type\": \"URL_UPDATED\","
;; + "}";

;; HttpRequest request =
;; requestFactory.buildPostRequest(genericUrl, ByteArrayContent.fromString("application/json", content));

;; credentials.initialize(request);
;; HttpResponse response = request.execute();
;; int statusCode = response.getStatusCode();
