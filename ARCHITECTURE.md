# Digital Signature Architecture

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     MAIL SYSTEM WITH DIGITAL SIGNATURES          │
└─────────────────────────────────────────────────────────────────┘

                          SENDER SIDE
┌────────────────────────────────────┐
│      Compose Mail                  │
│  From: alice@localhost             │
│  To: bob@localhost                 │
│  Subject: Hello                    │
│  Body: This is a message           │
└────────────┬─────────────────────────┘
             │
             ▼
┌────────────────────────────────────┐
│   MailSMTPClient.send()            │
│   (Enhanced with signing)          │
└────────────┬─────────────────────────┘
             │
             ├─ Retrieve alice's private key
             │  from UserKeyStore
             │
             ├─ Sign content: 
             │  SHA256(FROM + TO + SUBJECT + BODY)
             │
             ├─ Get alice's public key
             │
             └─ Embed in mail:
                 SIGNATURE: <base64>
                 PUBKEY: <base64>
             │
             ▼
┌────────────────────────────────────┐
│   SMTP Protocol                    │
│   Send to localhost:2500           │
└────────────┬─────────────────────────┘
             │
             ▼
┌────────────────────────────────────┐
│   SmtpHandler (Server)             │
│   Receive & Store                  │
└────────────┬─────────────────────────┘
             │
             ▼
┌────────────────────────────────────┐
│   File Storage                     │
│   data/mails/bob@localhost/        │
│   mail-00001.eml (with signature)  │
└────────────────────────────────────┘


                          RECIPIENT SIDE
┌────────────────────────────────────┐
│   POP3 Client                      │
│   RETR 1                           │
└────────────┬─────────────────────────┘
             │
             ▼
┌────────────────────────────────────┐
│   Pop3Handler.run()                │
│   (Enhanced with verification)     │
└────────────┬─────────────────────────┘
             │
             ├─ Extract signature
             │
             ├─ Extract public key
             │
             ├─ Reconstruct content:
             │  FROM + TO + SUBJECT + BODY
             │
             ├─ Verify signature:
             │  Using sender's public key
             │
             └─ Add header:
                 X-Signature-Status: VALID
                 (or INVALID if tampering detected)
             │
             ▼
┌────────────────────────────────────┐
│   Return to POP3 Client            │
│   With verification status         │
└────────────┬─────────────────────────┘
             │
             ▼
┌────────────────────────────────────┐
│   Display Mail                     │
│   Shows: Signature: VALID          │
│   From: alice@localhost (verified) │
└────────────────────────────────────┘
```

---

## Component Interaction Diagram

```
┌──────────────────────────────────────────────────────────┐
│                 CLIENT APPLICATION                        │
└────────────────────┬─────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
   ┌────────┐  ┌──────────┐  ┌───────────┐
   │  Mail  │  │ SMTP     │  │ POP3      │
   │  Model │  │ Client   │  │ Client    │
   └────────┘  └──────────┘  └───────────┘
        │            │            │
        │            ├─────┬──────┘
        │            │     │
        ▼            ▼     ▼
   ┌───────────────────────────────────────┐
   │      SECURITY LAYER                   │
   │  ┌─────────────────────────────────┐  │
   │  │  DigitalSignature               │  │
   │  │  - generateKeyPair()            │  │
   │  │  - signMail()                   │  │
   │  │  - verifyMailSignature()        │  │
   │  │  - export/importKey()           │  │
   │  └─────────────────────────────────┘  │
   │  ┌─────────────────────────────────┐  │
   │  │  UserKeyStore                   │  │
   │  │  - getOrGenerateKeyPair()       │  │
   │  │  - getPrivateKey()              │  │
   │  │  - getPublicKey()               │  │
   │  │  - persist keys to disk         │  │
   │  └─────────────────────────────────┘  │
   └────────────────┬──────────────────────┘
                    │
                    ▼
   ┌───────────────────────────────────────┐
   │      FILE SYSTEM                      │
   │  ┌─────────────────────────────────┐  │
   │  │  data/keys/                     │  │
   │  │  - user1@localhost.pub          │  │
   │  │  - user1@localhost.key          │  │
   │  └─────────────────────────────────┘  │
   │  ┌─────────────────────────────────┐  │
   │  │  data/mails/                    │  │
   │  │  - user@localhost/mail-*.eml    │  │
   │  │  (with SIGNATURE and PUBKEY)    │  │
   │  └─────────────────────────────────┘  │
   └───────────────────────────────────────┘

        ↕              ↕              ↕
   ┌────────┐    ┌──────────┐   ┌───────────┐
   │  SMTP  │    │ NETWORK  │   │   POP3    │
   │ Server │────┤          ├───│  Server   │
   └────────┘    └──────────┘   └───────────┘
```

---

## Cryptographic Process Flow

### SENDING (Signing)

```
Mail Object
    │
    ├─ FROM: "alice@localhost"
    ├─ TO: "bob@localhost"
    ├─ SUBJECT: "Hello"
    └─ BODY: "This is a message"
    │
    ▼
Concatenate Content
    │
    └─ mailContent = "alice@localhoust" + "bob@localhost" + 
                      "Hello" + "This is a message"
    │
    ▼
Retrieve Private Key
    │
    ├─ Check UserKeyStore cache
    ├─ If not in cache:
    │   └─ Load from data/keys/alice@localhost.key
    │   └─ Cache in memory
    └─ Return PrivateKey object
    │
    ▼
Sign Content
    │
    ├─ Algorithm: SHA-256withRSA
    ├─ Input: mailContent string
    ├─ Signature = RSA_Sign(SHA256(mailContent), privateKey)
    └─ Output: byte array
    │
    ▼
Encode Signature
    │
    ├─ Encode: Base64(byte array)
    └─ Result: String (Base64 format)
    │
    ▼
Get Public Key
    │
    ├─ Extract from PrivateKey's pair
    ├─ Encode: Base64(publicKey.getEncoded())
    └─ Result: String (Base64 format)
    │
    ▼
Embed in Mail
    │
    ├─ mail.setSignature(signatureString)
    ├─ mail.setSenderPublicKey(publicKeyString)
    └─ Mail now contains SIGNATURE: and PUBKEY: fields
    │
    ▼
Send to Server
    │
    └─ SMTP transmits signed mail
```

### RECEIVING (Verification)

```
POP3 RETR Command
    │
    ▼
Retrieve Mail from Storage
    │
    ├─ Load from data/mails/bob@localhost/mail-*.eml
    └─ Deserialize Mail object
    │
    ▼
Extract Components
    │
    ├─ FROM: "alice@localhost"
    ├─ TO: "bob@localhost"
    ├─ SUBJECT: "Hello"
    ├─ BODY: "This is a message"
    ├─ SIGNATURE: <Base64 string>
    └─ PUBKEY: <Base64 string>
    │
    ▼
Reconstruct Content
    │
    └─ mailContent = FROM + TO + SUBJECT + BODY
    │
    ▼
Decode Public Key
    │
    ├─ Decode: Base64.decode(publicKeyString)
    ├─ Import: X509EncodedKeySpec(bytes)
    └─ Result: PublicKey object
    │
    ▼
Decode Signature
    │
    ├─ Decode: Base64.decode(signatureString)
    └─ Result: byte array
    │
    ▼
Verify Signature
    │
    ├─ Algorithm: SHA-256withRSA
    ├─ Input: mailContent, signature bytes, publicKey
    ├─ Verify = RSA_Verify(SHA256(mailContent), 
    │                       signature, publicKey)
    └─ Result: boolean (true/false)
    │
    ▼
Add Status Header
    │
    ├─ If verified: Add "X-Signature-Status: VALID"
    └─ If not verified: Add "X-Signature-Status: INVALID"
    │
    ▼
Return to Client
    │
    └─ POP3 response includes mail + verification status
```

---

## Key Generation Process

```
User First Connect (no keys exist)
    │
    ▼
UserKeyStore.getOrGenerateKeyPair("user@localhost")
    │
    ├─ Check cache (empty)
    │
    ├─ Check disk:
    │   ├─ data/keys/user@localhost.pub
    │   └─ data/keys/user@localhost.key
    │   (files don't exist)
    │
    ▼
Generate New KeyPair
    │
    ├─ Algorithm: RSA
    ├─ Key Size: 2048 bits
    └─ Result: (PublicKey, PrivateKey)
    │
    ▼
Export Keys to Base64
    │
    ├─ Export Public: Base64.encode(publicKey.getEncoded())
    ├─ Export Private: Base64.encode(privateKey.getEncoded())
    └─ Result: String representations
    │
    ▼
Write to Disk
    │
    ├─ data/keys/user@localhost.pub ← Public key file
    └─ data/keys/user@localhost.key ← Private key file
    │
    ▼
Cache in Memory
    │
    └─ Store in UserKeyStore.KEY_CACHE map
    │
    ▼
Ready to Use
    │
    └─ Keys available for signing/verification
```

---

## Security Guarantees

```
┌─────────────────────────────────────────────────────────────┐
│            DIGITAL SIGNATURE GUARANTEES                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  AUTHENTICATION                                             │
│  ✓ Only sender's private key can create signature           │
│  ✓ Public key proves authenticity                           │
│  ✓ Recipient knows mail came from claimed sender           │
│                                                              │
│  INTEGRITY                                                  │
│  ✓ Any modification to mail breaks signature               │
│  ✓ Receiver can detect tampering                           │
│  ✓ Even 1-bit change causes verification to fail           │
│                                                              │
│  NON-REPUDIATION                                            │
│  ✓ Sender cannot deny creating signature                    │
│  ✓ Only sender has private key                             │
│  ✓ Signature is proof of sender's action                   │
│                                                              │
│  STRENGTH                                                   │
│  ✓ RSA 2048-bit: ~110 bits of symmetric equivalent         │
│  ✓ SHA-256: Collision resistance                           │
│  ✓ Combined: 256-bit effective security                    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Data Format: Before and After

### Before Implementation
```
Simple mail format:
FROM:alice@localhost
TO:bob@localhost
SUBJECT:Hello
TIME:2025-10-23 14:30:00
BODY:
Message content
.
```

### After Implementation
```
Enhanced mail format with digital signature:
FROM:alice@localhost
TO:bob@localhost
SUBJECT:Hello
TIME:2025-10-23 14:30:00
BODY:
Message content
SIGNATURE:MIIDXQIBAAKCAQEAu0X3KZy+tC5Qz2Wz...
PUBKEY:MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCg...
.
```

---

## Integration Timeline

```
1. User Composes Mail
       │
       ▼
2. SMTP Client Receives
       │
       ├─ Get sender's private key ← UserKeyStore
       ├─ Sign mail ← DigitalSignature
       ├─ Get sender's public key ← UserKeyStore
       └─ Embed signature in mail
       │
       ▼
3. Send to Server
       │
       ▼
4. SMTP Handler Receives
       │
       └─ Store in database (with signature)
       │
       ▼
5. Time Passes
       │
       ▼
6. User Requests Mail via POP3
       │
       ▼
7. POP3 Handler Processes
       │
       ├─ Retrieve mail ← MailStorage
       ├─ Extract signature and pubkey
       ├─ Verify signature ← DigitalSignature
       └─ Add verification status header
       │
       ▼
8. Return to Client
       │
       └─ Client sees: "X-Signature-Status: VALID"
```

This architecture ensures that:
- ✅ All emails are automatically signed
- ✅ All signatures are automatically verified
- ✅ Users don't need to do anything special
- ✅ Tampering is automatically detected
- ✅ Keys are automatically managed
