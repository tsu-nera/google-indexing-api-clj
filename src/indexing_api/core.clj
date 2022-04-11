(ns indexing-api.core
  (:require
   [clojure.java.io :as io])
  (:import
   (com.google.auth.oauth2
    GoogleCredentials)))

(def scopes "https://www.googleapis.com/auth/indexing")

(def endpoint
  "https://indexing.googleapis.com/v3/urlNotifications:publish")

(def creds-path "credentials.json")

(def service-account
  (-> creds-path
      io/resource
      io/input-stream))
(def credentials (GoogleCredentials/fromStream service-account))

(defn ->content-updated [url]
  {:url  url
   :type "URL_UPDATED"})

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
