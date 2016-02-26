(ns service-manager.ssh
  (:require [clj-ssh.ssh :as ssh]))

(defn bytes->string [b]
  (apply str (map char b)))

(defn generate-ssh-keypair []
  (let [a (ssh/ssh-agent {})
        keypair-bytes (ssh/generate-keypair a :rsa 2048 "")
        keypair (map bytes->string keypair-bytes)]
    (zipmap [:private_key :public_key] keypair)))
