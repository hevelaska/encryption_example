# Cryptographie

(Note: Si vous êtes sur windows; il faudra passer par un émulateur de terminal linux type GitBash, et précéder toutes
les commandes de `winpty`).

## Encryption Asymétrique

### 1. Générer une clé privée

On peut générer une clé privée de 2048 bits avec la ligne de commande et le logiciel "openssl":

```bash
openssl genrsa -des3 -out privateKey.pem 2048
```

Cette commande vous demande un mot de passe, et génère un fichier `privateKey.pem` ayant un contenu du genre:

```
-----BEGIN RSA PRIVATE KEY-----
Proc-Type: 4,ENCRYPTED
DEK-Info: DES-EDE3-CBC,097D2FDA2670B0C8

12345789abcdef...<reste de la clé>

-----END RSA PRIVATE KEY-----
```

### 2. Générer la clé publique

On peut générer une clé publique à partir d'une clé privée et de son mot de passe, avec la ligne de commande et le
logiciel "openssl":

```bash
openssl rsa -in privateKey.pem -outform DER -pubout -out publicKey.der
```

Après avoir entré le mot de passe de la clé privée, un fichier `publicKey.pem` est généré avec un contenu du genre:

```
-----BEGIN PUBLIC KEY-----

MIIBIjANB...<reste de la clé>

-----END PUBLIC KEY-----

```

### 3. Convertir la clé privée en PKCS pour Java

Les programmes JAVA ne peuvent pas gérer le format de clés créé par openssl, il faut donc les transformer en clés PKCS:

```bash
openssl pkcs8 -topk8 -inform PEM -outform DER -in privateKey.pem -out privateKey.der -nocrypt
```

Un fichier.der est généré, contenant les informations utilisables par Java (ainsi que part la keychain windows).

### 4. Lire les clés dans un programme JAVA

Selon l'endroit où les fichiers sont stockés, vous obtiendrez une référence vers ceux-ci d'une manière propre.

Dans le cas de notre exemple, les clés sont stockées dans le dossier "resources", et on peut donc les obtenir via un
simple `getResourceAsStream`

Une fois la clé lue sous forme de tableau de bytes, il faut créer la "KeySpec" correspondant aux spécificités de votre
clé (privée ou publique).

La clé publique est encodée au format X509 au point 2. La clé privée est donc encodée au format PKCS8 au point 3.

Ensuite, en faisant appel à java.security.KeyFactory, on peut récupérer la clé.

Exemple pour la clé privée:

```java
PKCS8EncodedKeySpec spec=new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf=KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
```

### 5. Encrypter avec la clé publique en JAVA

Une fois que nous avons une référence à une clé publique (Voir pt 4), nous pouvons encrypter des données avec celle-ci.
La clé publique ne peut **QUE** encrypter les données, une fois celles-ci encryptées, elles ne pourront être lues que
par le détenteur de la clé privée.

Par facilité de lecture, le résultat est encodé en Base64, mais ça n'est pas forcément nécessaire.

```java
public String encrypt(PublicKey publicKey,String data)throws NoSuchPaddingException,NoSuchAlgorithmException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException{
        Cipher cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        var encodedBytes=cipher.doFinal(data.getBytes());
        return new String(Base64.getEncoder().encode(encodedBytes));
        }
```

Le résultat donne un contenu illisible:

```
Avant encryption: My password is password
Après encryption: fGsPbxTMMe7HxosvNZYlrsPAFi5oxEx27MvyVc1+iB2B7NMkDs70ChaDk/3H9245e4wW4ygGQ03udqF8ylzekjX9IdnlLhYw3qd/SZHQa01rsLZ93ph+kwKVPmSm3KjIZ8NAHncLfFXNoSqsRH+G/uN7BZ9JUeKj5mE+EX24KB8jft1i6XZNsH5fcVa86C5HkQMsFqhRqvh47U0faro3LbB0Na9ER/WvaKz8nEU7Q7I8lBFFCfKNlYgP22iYSPxnXCBLdF9rSaYIfKtzBvGEt+cRTNRvHjHiQ8R3oxw0haQbvXfqkoW3aoUsCM8YE0J3ctVaGX3ExBp/nbBt5EToXQ==
```

### 6. Décrypter avec la clé privée en JAVA

Comme en point 5 nous avons encodé le résultat en Base64, nous allons commencer par décoder la base64. Ensuite, nous
pouvons décrypter les données grâce à la clé privée.

```java
public String decrypt(PrivateKey privateKey,String encodedBase64Data)throws NoSuchPaddingException,NoSuchAlgorithmException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException{
        byte[]encryptedData=Base64.getDecoder().decode(encodedBase64Data);
        Cipher cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE,privateKey);
        var decodedBytes=cipher.doFinal(encryptedData);
        return new String(decodedBytes);
        }
```

Le résultat est exactement ce qui avait été encrypté:

```
Après décryption: My password is password
```
