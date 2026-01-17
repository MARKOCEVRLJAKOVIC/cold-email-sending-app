# Code Review - EmailOutreachApp Backend

## 📋 Pregled

Ovaj dokument sadrži detaljnu analizu koda Spring Boot aplikacije za email outreach. Review je organizovan po kategorijama sa konkretnim preporukama za poboljšanja.

---

## 1. 🏗️ ARHITEKTURA PROJEKTA

### 1.1 Struktura Foldera i Modula

**✅ Pozitivno:**
- Dobro organizovana paketna struktura po domenima (`email`, `auth`, `security`, `services`)
- Jasno razdvojeni slojevi (controllers, services, repositories, entities)
- Korišćenje base klasa (`BaseService`, `BaseController`) za smanjenje dupliranja

**⚠️ Problemi:**

1. **Prevelike odgovornosti u pojedinim modulima:**
   - `email` paket sadrži previše podpaketa (`connection`, `followup`, `reply`, `schedulers`, `send`, `spintax`)
   - Preporuka: Razmotriti podele na `email.core`, `email.sending`, `email.reply`, `email.scheduling`

2. **Nedosledna organizacija:**
   - `EmailReplyService` nije u `services` paketu već u `email.reply`
   - `ReplyResponseService` je takođe u `email.reply` umesto u `services`
   - Preporuka: Usaglasiti lokacije servisa - svi servisi u `services` paketu

3. **Nedostaje jasna granica između slojeva:**
   - `EmailReplyProcessor` direktno koristi `EmailMessageRepository` umesto da koristi servisni sloj
   - Preporuka: Uvesti jasne granice - repository se koristi samo u servisnom sloju

### 1.2 Skalabilnost i Održavanje

**Problemi:**

1. **Tight coupling:**
   ```java
   // EmailReplyProcessor.java - direktna zavisnost od repository
   private final EmailMessageRepository emailMessageRepository;
   ```
   Preporuka: Uvesti servisni sloj između procesora i repository-ja

2. **Nedostaje dependency injection pattern za factory klase:**
   - `GmailServiceFactory` - razmotriti korišćenje Spring Factory pattern-a

3. **Nedostaje interfejs za ključne servise:**
   - `EmailReplyService`, `EmailReplyProcessor` nemaju interfejse
   - Preporuka: Uvesti interfejse za lakše testiranje i zamenu implementacije

---

## 2. 💻 KVALITET KODA

### 2.1 Čitljivost i Konzistentnost

**✅ Pozitivno:**
- Konzistentno korišćenje Lombok anotacija
- Dobro imenovanje klasa i metoda
- Korišćenje MapStruct za mapiranje

**⚠️ Problemi:**

1. **Nedosledno formatiranje:**
   ```java
   // EmailReplyService.java linija 50
   EmailReply originalReply = replyRepository.findByIdAndUserId(replyId, user.getId()).orElseThrow();
   ```
   - Nedostaje specifična exception poruka
   - Preporuka: `orElseThrow(() -> new EmailReplyNotFoundException("Reply not found with id: " + replyId))`

2. **Nedostaju JavaDoc komentari:**
   - Većina javnih metoda nema dokumentaciju
   - Preporuka: Dodati JavaDoc za javne API-je

3. **Nedosledno korišćenje `var`:**
   - Ponekad se koristi `var`, ponekad eksplicitni tipovi
   - Preporuka: Usaglasiti stil - koristiti `var` za lokalne promenljive gde je tip očigledan

### 2.2 Code Smell-ovi

1. **God Object anti-pattern:**
   - `BaseService` pokušava da pokrije sve CRUD operacije, ali neki servisi imaju dodatne metode van ovog pattern-a
   - Preporuka: Razmotriti Composition over Inheritance

2. **Primitive Obsession:**
   ```java
   // EmailReplyService.java
   public EmailMessageDto respondToReply(Long replyId, EmailReplyResponseDto response)
   ```
   - `Long id` se koristi svuda umesto value object-a
   - Preporuka: Razmotriti korišćenje value object-a za ID-jeve (npr. `ReplyId`, `UserId`)

3. **Feature Envy:**
   ```java
   // EmailReplyProcessor.java
   String inReplyTo = GmailUtils.getHeader(msg, "In-Reply-To");
   ```
   - `EmailReplyProcessor` zna previše o Gmail strukturi
   - Preporuka: Ekstraktovati u `MessageHeaderExtractor` ili slično

4. **Long Parameter List:**
   ```java
   // ReplyResponseService.java (pretpostavka)
   createReplyMessage(originalReply, originalMessage, response, smtp, user)
   ```
   - Preporuka: Grupisati u DTO objekte

### 2.3 Dupliranje Logike

1. **Dupliranje user scope provere:**
   ```java
   // U svakom servisu
   var user = currentUserProvider.getCurrentUser();
   repository.findByIdAndUserId(id, user.getId())
   ```
   - Ovo je već rešeno u `BaseService`, ali neki servisi ne nasleđuju od njega
   - Preporuka: `EmailReplyService` bi trebalo da koristi `BaseService` pattern

2. **Dupliranje exception handling-a:**
   - Svuda se ponavlja `orElseThrow()` sa različitim exception-ima
   - Preporuka: Ekstraktovati u helper metode

3. **Dupliranje validacije:**
   - Validacija se ponavlja u više mesta
   - Preporuka: Koristiti `@Valid` anotacije i custom validatore

### 2.4 Neiskorišćene ili Prekomplikovane Delove

1. **Prekomplikovana generička hijerarhija:**
   ```java
   public abstract class BaseService<E, D, C, R extends UserScopedRepository<E> & JpaRepository<E, Long>, U>
   ```
   - 5 generičkih tipova čini kod teškim za čitanje
   - Preporuka: Razmotriti pojednostavljenje ili korišćenje composition pattern-a

2. **Nedostaje korišćenje Optional API-ja:**
   ```java
   // EmailReplyProcessor.java
   emailMessageRepository.findByMessageId(inReplyTo)
       .ifPresent(original -> replyPersistenceService.saveReply(original, msg, creds));
   ```
   - Ovo je dobro, ali može biti bolje sa `Optional.map()`

---

## 3. 🎯 BEST PRACTICES

### 3.1 Spring Framework Best Practices

**⚠️ Kritični Problemi:**

1. **Nedostaje `@Transactional` na servisnim metodama:**
   ```java
   // BaseService.java
   public D create(C request) {
       // Nema @Transactional
       repository.save(entity);
   }
   ```
   - Preporuka: Dodati `@Transactional` na sve metode koje menjaju podatke
   ```java
   @Transactional
   public D create(C request) { ... }
   
   @Transactional(readOnly = true)
   public List<D> getAll() { ... }
   ```

2. **Nedosledno korišćenje `@Transactional`:**
   - Neki servisi imaju `@Transactional`, neki nemaju
   - `EmailReplyService.deleteReply()` ima, ali `create()` nema

3. **EAGER fetching može uzrokovati N+1 problem:**
   ```java
   // EmailMessage.java
   @ManyToOne(fetch = FetchType.EAGER)
   private EmailTemplate emailTemplate;
   
   @ManyToOne(fetch = FetchType.EAGER)
   private Campaign campaign;
   ```
   - Preporuka: Koristiti LAZY fetching i `@EntityGraph` ili JOIN FETCH u query-jima gde je potrebno

4. **Nedostaje `@EnableTransactionManagement`:**
   - Proveriti da li je omogućeno u konfiguraciji

### 3.2 Dizajn Greške i Anti-patterni

1. **Anemic Domain Model:**
   ```java
   // EmailMessage.java
   @PrePersist
   @PreUpdate
   private void prePersistOrUpdate() {
       if (this.status == Status.SENT && this.sentAt == null) {
           this.sentAt = LocalDateTime.now(ZoneId.of("UTC"));
       }
   }
   ```
   - Logika je u entitetu, što je dobro, ali većina logike je u servisima
   - Preporuka: Premestiti više business logike u entitete

2. **Service Layer Anemia:**
   - Servisi su previše "thin" - samo delegiraju pozive
   - Preporuka: Dodati više business logike u servise

3. **Repository Exposure:**
   - `BaseService` izlaže `protected final R repository` - podklase mogu direktno pristupati
   - Preporuka: Sakriti repository iza metoda

### 3.3 Imenovanje, Tipizacija i Apstrakcija

1. **Nedosledno imenovanje:**
   - `EmailReplyService` vs `ReplyResponseService` - konfuzno
   - Preporuka: Jasnije imenovanje (npr. `EmailReplyQueryService` i `EmailReplyCommandService`)

2. **Nedostaje tipizacija za error handling:**
   ```java
   // GlobalExceptionHandler.java
   @ExceptionHandler(Exception.class)
   public ResponseEntity<ErrorDto> handleGeneralException(Exception ex) {
       return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
   }
   ```
   - Gubi se originalna greška
   - Preporuka: Logovati exception i vratiti generičku poruku

3. **Nedostaje apstrakcija za email provajdere:**
   - Gmail logika je direktno u kodu
   - Preporuka: Uvesti `EmailProvider` interfejs sa `GmailProvider` implementacijom

---

## 4. ⚡ PERFORMANCE I SIGURNOST

### 4.1 Performance Problemi

1. **N+1 Query Problem:**
   ```java
   // EmailMessage.java - EAGER fetching
   @ManyToOne(fetch = FetchType.EAGER)
   private EmailTemplate emailTemplate;
   ```
   - Preporuka: Koristiti LAZY + `@EntityGraph` ili JOIN FETCH

2. **Nedostaje paginacija:**
   ```java
   // BaseService.java
   public List<D> getAll() {
       return toListDto(repository.findAllByUserId(user.getId()));
   }
   ```
   - Preporuka: Dodati paginaciju:
   ```java
   public Page<D> getAll(Pageable pageable) {
       var user = currentUserProvider.getCurrentUser();
       return repository.findAllByUserId(user.getId(), pageable)
           .map(this::toDto);
   }
   ```

3. **Nedostaju database indeksi:**
   - Proveriti da li postoje indeksi na `user_id`, `message_id`, itd.
   - Preporuka: Dodati indekse u migration fajlove

4. **Batch processing može biti optimizovan:**
   ```java
   // EmailReplyProcessor.java
   for (Message msg : messages) {
       // Svaki message je poseban DB poziv
   }
   ```
   - Preporuka: Batch insert/update gde je moguće

### 4.2 Sigurnosni Rizici

**🔴 KRITIČNI PROBLEMI:**

1. **AES Encryption sa ECB modom:**
   ```java
   // AesEncryptor.java
   Cipher cipher = Cipher.getInstance("AES");
   ```
   - ECB mod je nesiguran! Koristi se isti ključ za sve blokove
   - Preporuka: Koristiti AES/GCM/NoPadding:
   ```java
   Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
   ```

2. **Encryption key u konfiguraciji:**
   ```yaml
   # application.yaml
   token:
     encrypt: ${TOKEN_ENCRYPTION_KEY}
   ```
   - Ključ je u plain text-u u konfiguraciji
   - Preporuka: Koristiti secret management (AWS Secrets Manager, HashiCorp Vault, itd.)

3. **Nedostaje validacija inputa na nekim mestima:**
   ```java
   // EmailReplyController.java
   @GetMapping("/{id}")
   public ResponseEntity<EmailReplyDto> getEmailReply(@PathVariable Long id) {
       // Nema @Min, @Positive validacije
   }
   ```
   - Preporuka: Dodati `@Min(1)` ili `@Positive` na path variable

4. **SQL Injection potencijal:**
   - Proveriti da li se koriste prepared statements svuda (JPA bi trebalo da koristi)
   - Preporuka: Code review native query-ja

5. **Nedostaje rate limiting:**
   - API nema zaštitu od brute force napada
   - Preporuka: Dodati Spring Security rate limiting ili Redis-based rate limiting

6. **Sensitive data u logovima:**
   ```java
   // EmailStatusService.java
   log.error("Email failed for {}: {}", email.getRecipientEmail(), e.getMessage(), e);
   ```
   - Email adrese mogu biti sensitive
   - Preporuka: Maskirati ili hash-ovati u logovima

7. **Nedostaje HTTPS enforcement:**
   - `cookie.setSecure(true)` je dobro, ali proveriti da li aplikacija forsira HTTPS

8. **JWT Secret u konfiguraciji:**
   ```yaml
   jwt:
     secret: ${JWT_SECRET_KEY}
   ```
   - Preporuka: Koristiti strong secret (min 256 bita) i rotirati redovno

### 4.3 Error Handling

1. **Gubitak stack trace-a:**
   ```java
   // GlobalExceptionHandler.java
   @ExceptionHandler(Exception.class)
   public ResponseEntity<ErrorDto> handleGeneralException(Exception ex) {
       return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
   }
   ```
   - Preporuka: Logovati exception pre vraćanja odgovora:
   ```java
   @ExceptionHandler(Exception.class)
   public ResponseEntity<ErrorDto> handleGeneralException(Exception ex) {
       log.error("Unexpected error occurred", ex);
       return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
   }
   ```

2. **Nedostaje error context:**
   - Error poruke ne sadrže dovoljno konteksta za debugging
   - Preporuka: Dodati correlation ID za praćenje grešaka

---

## 5. 🧪 TESTIRANJE I POUZDANOST

### 5.1 Testabilnost Koda

**✅ Pozitivno:**
- Korišćenje dependency injection
- Postoje neki testovi (17 test fajlova)

**⚠️ Problemi:**

1. **Tight coupling otežava testiranje:**
   - `EmailReplyProcessor` direktno koristi `EmailMessageRepository`
   - Preporuka: Uvesti interfejse i mock-ovati u testovima

2. **Nedostaju interfejsi:**
   - Većina servisa nema interfejse
   - Preporuka: Uvesti interfejse za glavne servise

3. **Static metode:**
   ```java
   // GmailUtils.java
   public static String getHeader(Message msg, String name)
   ```
   - Teško za mock-ovanje
   - Preporuka: Pretvoriti u instance metodu ili utility klasu sa dependency injection

4. **Nedostaje test coverage:**
   - Proveriti coverage - verovatno nedostaje pokrivenost za edge case-ove

### 5.2 Test Strategija

**Preporuke:**

1. **Unit testovi:**
   - Fokusirati se na business logiku u servisima
   - Mock-ovati sve zavisnosti
   - Testirati edge case-ove i error scenarije

2. **Integration testovi:**
   - Koristiti `@SpringBootTest` sa Testcontainers (već postoji dependency)
   - Testirati repository sloj sa realnom bazom
   - Testirati security konfiguraciju

3. **Prioriteti za testiranje:**
   - **Visok prioritet:**
     - `AuthService` - autentifikacija i autorizacija
     - `EmailReplyProcessor` - core business logika
     - `EmailSendService` - kritična funkcionalnost
   - **Srednji prioritet:**
     - `CampaignService` - business logika
     - `SmtpService` - credential management
   - **Nizak prioritet:**
     - Utility klase
     - Mapper klase

### 5.3 Pouzdanost

1. **Nedostaje retry mehanizam:**
   - Email slanje može da padne
   - Preporuka: Koristiti `@Retryable` (već postoji spring-retry dependency)

2. **Nedostaje circuit breaker:**
   - Za eksterne servise (Gmail API)
   - Preporuka: Razmotriti Resilience4j

3. **Nedostaje monitoring i alerting:**
   - Preporuka: Dodati Micrometer metrics i health checks

---

## 6. 📝 KONKRETNE PREPORUKE

### 6.1 Hitne Ispravke (High Priority)

1. **Ispraviti AES encryption:**
   ```java
   // AesEncryptor.java
   // PROMENITI:
   Cipher cipher = Cipher.getInstance("AES");
   
   // U:
   Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
   // + dodati IV (Initialization Vector)
   ```

2. **Dodati @Transactional na BaseService:**
   ```java
   @Transactional
   public D create(C request) { ... }
   
   @Transactional(readOnly = true)
   public List<D> getAll() { ... }
   ```

3. **Promeniti EAGER u LAZY:**
   ```java
   // EmailMessage.java
   @ManyToOne(fetch = FetchType.LAZY)
   private EmailTemplate emailTemplate;
   ```

4. **Dodati validaciju na path variables:**
   ```java
   @GetMapping("/{id}")
   public ResponseEntity<EmailReplyDto> getEmailReply(
       @PathVariable @Min(1) Long id) {
   ```

5. **Dodati logging u exception handler:**
   ```java
   @ExceptionHandler(Exception.class)
   public ResponseEntity<ErrorDto> handleGeneralException(Exception ex) {
       log.error("Unexpected error", ex);
       return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
   }
   ```

### 6.2 Srednji Prioritet

1. **Refaktorisati EmailReplyProcessor:**
   ```java
   // Umesto direktnog pristupa repository-ju
   private final EmailMessageService emailMessageService;
   
   public void processReplies(List<Message> messages, SmtpCredentials creds) {
       messages.stream()
           .map(msg -> GmailUtils.getHeader(msg, "In-Reply-To"))
           .filter(Objects::nonNull)
           .forEach(inReplyTo -> 
               emailMessageService.findByMessageId(inReplyTo)
                   .ifPresent(original -> 
                       replyPersistenceService.saveReply(original, msg, creds)
                   )
           );
   }
   ```

2. **Uvesti paginaciju:**
   ```java
   // BaseService.java
   public Page<D> getAll(Pageable pageable) {
       var user = currentUserProvider.getCurrentUser();
       return repository.findAllByUserId(user.getId(), pageable)
           .map(this::toDto);
   }
   ```

3. **Dodati interfejse za servise:**
   ```java
   public interface EmailReplyService {
       List<EmailReplyDto> getAllRepliesFromUser();
       EmailReplyDto getEmailReply(Long id);
       // ...
   }
   ```

4. **Ekstraktovati GmailUtils u service:**
   ```java
   @Service
   public class MessageHeaderExtractor {
       public Optional<String> extractHeader(Message msg, String headerName) {
           // ...
       }
   }
   ```

### 6.3 Dugoročne Preporuke

1. **Razmotriti CQRS pattern:**
   - Odvojiti read i write operacije
   - Korisno za skaliranje

2. **Uvesti Event-Driven Architecture:**
   - Za email sending events
   - Koristiti Spring Events ili message queue

3. **Dodati API versioning:**
   - Za buduće promene API-ja

4. **Uvesti OpenAPI/Swagger dokumentaciju:**
   - Dodati `springdoc-openapi` dependency

5. **Razmotriti multi-tenancy:**
   - Ako planiraš da podržiš više organizacija

---

## 7. 📊 REZIME

### Ukupna Ocena: **7/10**

**Jake Strane:**
- ✅ Dobra struktura projekta
- ✅ Korišćenje modernih tehnologija (Spring Boot 3, Java 24)
- ✅ Postoje testovi
- ✅ Korišćenje best practices (Lombok, MapStruct)

**Glavni Problemi:**
- 🔴 Sigurnosni rizici (AES ECB, secrets u config)
- ⚠️ Performance problemi (EAGER fetching, nedostaje paginacija)
- ⚠️ Nedostaje @Transactional na kritičnim mestima
- ⚠️ Neki code smell-ovi (tight coupling, god object)

**Prioriteti:**
1. **Hitno:** Ispraviti sigurnosne probleme
2. **Visok:** Dodati @Transactional, promeniti EAGER u LAZY
3. **Srednji:** Refaktorisati za bolju testabilnost
4. **Nizak:** Dugoročne arhitektonske promene

---

## 8. 🔧 PRIMERI REFACTORINGA

### Primer 1: EmailReplyProcessor Refactoring

**Pre:**
```java
@Service
public class EmailReplyProcessor {
    private final EmailMessageRepository emailMessageRepository;
    private final ReplyPersistenceService replyPersistenceService;

    public void processReplies(List<Message> messages, SmtpCredentials creds) {
        for (Message msg : messages) {
            String inReplyTo = GmailUtils.getHeader(msg, "In-Reply-To");
            if (inReplyTo == null) continue;
            
            emailMessageRepository.findByMessageId(inReplyTo)
                .ifPresent(original -> replyPersistenceService.saveReply(original, msg, creds));
        }
    }
}
```

**Posle:**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailReplyProcessor {
    private final EmailMessageService emailMessageService;
    private final ReplyPersistenceService replyPersistenceService;
    private final MessageHeaderExtractor headerExtractor;

    @Transactional
    public void processReplies(List<Message> messages, SmtpCredentials creds) {
        messages.stream()
            .map(msg -> extractReplyInfo(msg))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(replyInfo -> processReply(replyInfo, creds));
    }

    private Optional<ReplyInfo> extractReplyInfo(Message msg) {
        return headerExtractor.extractHeader(msg, "In-Reply-To")
            .map(inReplyTo -> new ReplyInfo(msg, inReplyTo));
    }

    private void processReply(ReplyInfo replyInfo, SmtpCredentials creds) {
        emailMessageService.findByMessageId(replyInfo.inReplyTo())
            .ifPresentOrElse(
                original -> {
                    replyPersistenceService.saveReply(original, replyInfo.message(), creds);
                    log.debug("Processed reply for message: {}", original.getId());
                },
                () -> log.warn("Original message not found for In-Reply-To: {}", replyInfo.inReplyTo())
            );
    }

    private record ReplyInfo(Message message, String inReplyTo) {}
}
```

### Primer 2: BaseService sa @Transactional

**Pre:**
```java
public abstract class BaseService<...> {
    public D create(C request) {
        var user = currentUserProvider.getCurrentUser();
        var entity = toEntity(request);
        setUser(entity, user);
        repository.save(entity);
        return toDto(entity);
    }
}
```

**Posle:**
```java
public abstract class BaseService<...> {
    @Transactional
    public D create(C request) {
        var user = currentUserProvider.getCurrentUser();
        var entity = toEntity(request);
        setUser(entity, user);
        var saved = repository.save(entity);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<D> getAll() {
        var user = currentUserProvider.getCurrentUser();
        return toListDto(repository.findAllByUserId(user.getId()));
    }

    @Transactional(readOnly = true)
    public D getById(Long id) {
        var user = currentUserProvider.getCurrentUser();
        var entity = repository.findByIdAndUserId(id, user.getId())
            .orElseThrow(notFound);
        return toDto(entity);
    }

    @Transactional
    public D update(Long id, U request) {
        var user = currentUserProvider.getCurrentUser();
        var entity = repository.findByIdAndUserId(id, user.getId())
            .orElseThrow(notFound);
        updateEntity(entity, request);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        var user = currentUserProvider.getCurrentUser();
        if (!repository.existsByIdAndUserId(id, user.getId())) {
            throw notFound.get();
        }
        repository.deleteById(id);
    }
}
```

### Primer 3: AesEncryptor sa GCM modom

**Pre:**
```java
@Component
public class AesEncryptor implements Encryptor {
    @Value("${spring.token.encrypt}")
    private String secretKey;

    @Override
    public String encrypt(String value) {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        String encrypted = Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes()));
        return PREFIX + encrypted;
    }
}
```

**Posle:**
```java
@Component
@Slf4j
public class AesEncryptor implements Encryptor {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final String PREFIX = "ENC::";

    @Value("${spring.token.encrypt}")
    private String secretKey;

    @Override
    public String encrypt(String value) {
        try {
            SecretKeySpec keySpec = getKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
            
            byte[] encrypted = cipher.doFinal(value.getBytes());
            
            // Combine IV + encrypted data
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            
            return PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Encryption error", e);
            throw new EncryptionException("Failed to encrypt value", e);
        }
    }

    @Override
    public String decrypt(String value) {
        if (!isEncrypted(value)) {
            throw new IllegalArgumentException("Value is not encrypted");
        }
        try {
            String withoutPrefix = value.substring(PREFIX.length());
            byte[] combined = Base64.getDecoder().decode(withoutPrefix);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            
            SecretKeySpec keySpec = getKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
            
            return new String(cipher.doFinal(encrypted));
        } catch (Exception e) {
            log.error("Decryption error", e);
            throw new EncryptionException("Failed to decrypt value", e);
        }
    }
}
```

---

## 9. 📚 DODATNE NAPOMENE

### 9.1 Dependencies

- **MapStruct verzije:** Koristiš 1.6.2 za mapstruct i 1.6.1 za processor - usaglasiti verzije
- **JWT verzije:** Različite verzije jjwt biblioteka (0.12.6 i 0.12.5) - usaglasiti
- **Java 24:** Veoma nova verzija - proveriti kompatibilnost sa svim dependency-jima

### 9.2 Konfiguracija

- **application.yaml:** Dobro strukturisan, ali nedostaju neki property-ji (npr. async config)
- **Test config:** Dobro odvojen u `application-test.yaml`

### 9.3 Database Migrations

- Flyway je dobro konfigurisan
- Proveriti da li svi indeksi postoje u migration fajlovima

---

**Kraj Review-a**

Za dodatna pitanja ili pojašnjenja, slobodno pitaj!
