

# payment-gateweay

## Avvio

Build dell'immagine Docker dell'applicazione (il demone Docker dev'essere già in esecuzione)

```bash
npm run java:docker
```

Aggiungere la seguente entry nell'host file di Linux/Windows:

```
127.0.0.1	kafka
```

Lancio della soluzione:

```
docker compose -f src/main/docker/app.yml up
```

Apertura del backoffice: http://localhost:8080 (username: `admin`, password: `admin`)

Apertura della UI per Kafka: http://localhost:18080/ (topic `payment-requests`)

Utilizzo della collection Postman (file `Payment Gateway.postman_collection.json` nella root del repository):

- autenticazione tramite JWT bearer token, ottenibile tramite l'operazione `authenticate`
- per invocare operazione API (`queuePaymentRequest` e `readPaymentRequest`), impostare il token ottenuto in precedenza

Specifica OpenAPI delle API REST del servizio: file `api.yml` nel percorso `payment-gateway-api/src/main/resources/swagger`.

## Appunti

### Architettura

Vedere diagramma `architecture.drawio` nella root del repository.

### Tecnologie

- Java &ge; 17
  
- Node.js &ge; 22 / NPM &ge; 10
- Maven
- PostgreSQL (out-of-the-box con JHipster)
- Docker
- Kafka
- Kafka UI
- JHipster
- Angular

### Spiegazioni e assuzioni

- Un singolo endpoint REST, ma più operazioni

  **Assunzione**: posso realizzare più operazioni sullo stesso endpoint, tramite diversi metodi HTTP
  N.B.: si sarebbe potuto realizzare anche con un'unica operazione idempotente, la sola per l'accodamento della richiesta di pagamento, facendo sì che questa restituisse errore in caso di pagamento già processato.

  > **What is an API endpoint?** (sottolineature mie)
  >
  > <u>An API endpoint is a URL</u> that acts as the point of contact between an API client and an API server. API clients send requests to API endpoints in order to access the API’s functionality and data.
  >
  > [...]
  >
  > <u>Requests to API endpoints must include a method, which indicates the operation to be performed</u>, as well as the necessary headers, parameters, authentication credentials, and body data.
  >
  > https://blog.postman.com/what-is-an-api-endpoint/

- L'applicazione è un "payment gateway" verso un upstream 3rd party service

  Penso che il termine "gateway" sia appropriato per definire l'applicazione da sviluppare, perché funge appunto da intermediario tra chi intende eseguire un pagamento e l'entità in grado di processarlo (i.e. la banca, o il BG). Questo non è in realtà importante, perché l'applicazione si comporterà semplicemente da client e non deve essere interessata a come l'applicazione terza svolgerà il proprio servizio, ma solo a cosa quest'ultima applicazione può svolgere e qual è il modo giusto per chiederglielo.

  > The payment gateway <u>bridges the gap between the customer, the business, and their respective financial institutions in one platform</u>
  >
  > https://stripe.com/au/resources/more/payment-gateways-101

​	**Assunzione**: l'applicazione gestisce implicitamente una sola *currency*, pertanto questa non viene specificata

- Operazione di pagamento idempotente:

  - idempotenza in HTTP:

    > An HTTP method is **idempotent** if <u>the intended effect on the server of making a single request is the same as the effect of making several identical requests</u>.
    >
    > [...]
    >
    > To be idempotent, <u>only the state of the server is considered</u>. <u>The response returned by each request may differ</u> [...].
    >
    > https://developer.mozilla.org/en-US/docs/Glossary/Idempotent

  - idempotenza nelle API REST

    > [...] idempotency relates to the concept that <u>making the same API request multiple times should yield the same result as making it just once</u>
    >
    > https://restfulapi.net/idempotent-rest-apis/

  - **idempotenza usata nell'ambito della sicurezza (o della robustezza)**

    - serve a rendere le API resistenti ai guasti (*fault-tolerant*)

      > [...] <u>Consumers can write the client code in such a way that there can be duplicate requests coming to the APIs</u>.
      >
      > These duplicate requests may be unintentional as well as intentional sometimes (e.g. due to timeout or network issues). We have to make our APIs ***fault-tolerant*** so that duplicate requests do not leave the system unstable.

    - serve a rendere il sistema più efficiente

      > Also, <u>idempotency is a key enabler for efficient caching and optimization strategies</u>. Caches (and CDNs) can store and serve the results of idempotent requests to reduce the load on servers and improve response times.

      ha quindi senso parlare anche di "efficienza" quando si fa riferimento al concetto di idempotenza;

      N.B.: anche se non è impossibile effettuare cache di risposte a richieste con metodo `POST` (utilizzando opportunamente gli header http `Cache-Control` e/o `Expires`), la specifica HTTP pare scoraggiare questo approccio

      > Responses to this method are not cacheable, unless the response includes appropriate Cache-Control or Expires header fields.
      >
      > http://www.faqs.org/rfcs/rfc2616.html

    - ATTENZIONE: idempotenza non significa sempre stessa risposta! La risposta può variare, l'effetto finale deve essere sempre lo stesso; ossia, richieste successive non debbono causare side-effects.

      **Assunzione**: data la non-idempotenza attribuita (dalla specifica, dalla filosofia REST e, probabilmente, da software intermediari) al metodo `POST` e alle implicazioni che tale aspetto ha sulla possibilità di effettuare cache delle risposte, ho deciso di non utilizzare il metodo HTTP `POST` per l'implementazione delle API, ma di utilizzare il metodo `PUT`. 

      > Generally – not necessarily – `POST` APIs are used to create a new resource on the server. So when we execute the same POST request N times, we will have N new resources on the server. So, **POST is not idempotent**.
      >
      > https://restfulapi.net/idempotent-rest-apis/

      N.B.: la proprietà di idempotenza di un'operazione API non è data dal metodo HTTP impiegato, ma dalla semantica implementata dall'operazione; questo significa che un'operazione API esposta tramite il metodo `POST` può essere implementata in modo da risultare idempotente e, paradossalmente, un'operazione API esposta tramite il metodo `GET` (considerato idempotente, ma anche *safe* (cioè che non dovrebbe alterare lo stato del server)) potrebbe erroneamente essere implementata in modo non idempotente.

      **Utilizzare il metodo `PUT`** in filosofia REST significa prevedere che la risorsa individuata dall'endpoint (dall'URL) venga completamente rimpiazzata dalla risorsa rappresentata nel body della richiesta HTTP, operazione questa idempotente; ciò non pone un problema di semantica, poiché è possibile considerare la risorsa da "rimpiazzare" come il pagamento individuato dal suo identificativo (dall'identificativo della transazione) già nella primissima richiesta (in modo un po' analogo a quello che si ottiene con una `UPSERT` SQL, che è generalmente considerata idempotente).

      N.B.: come è possibile verificare dalla specifica OpenAPI allegata, la risposta non ha body (`204`), ma è incluso l'header `Location` che il client può utilizzare per recuperare lo stato aggiornato della risorsa.

    **Assunzioni** sul BG: 

    - l'obiettivo è che il BG processi il pagamento una e una sola volta e che il PG filtri eventuali chiamate ripetute per lo stesso pagamento provenienti dal client

    - se il BG espone una funzione non idempotente per il pagamento, il che significa che chiamate ripetute all'operazione di pagamento fanno sì che vengano effettivamente eseguiti più pagamenti; questo potrebbe verificarsi se il PG dovesse rompersi subito dopo la ricezione della conferma di pagamento dal BG e prima di rendere persistente il cambio di stato localmente e di aver effettuato il *commit* di consumo del messaggio verso il broker, cosicché ad un riavvio del PG la stessa richiesta di pagamento verrebbe sottoposta nuovamente al BG; in questo contesto, si renderebbe necessaria una transazione distribuita che includa i due sistemi (es. tramite il protocollo *two-phase commit*) in modo da ottenere la semantica del "tutto o niente" (i.e. il pagamento sul BG non viene reso persistente se non si verifica il commit nel payment gateway), ma questo è deleterio dal punto di vista delle performance ed è un meccanismo piuttosto fragile; senza una transazione distribuita, l'unico modo per ripristinare la consistenza sarebbe tramite un intervento manuale sui sistemi a fronte di un'eventuale segnalazione da parte dell'utente;

    - una soluzione diversa, e che potrebbe rappresentare un compromesso accettabile, è far sì che il sistema raggiunga la consistenza nel tempo (i.e. *eventual consistency*); per fare ciò, è necessario che il BG esponga una funzione di pagamento idempotente (es. la prima chiamata crea il pagamento, le chiamate successive ritornano un errore); oppure, è necessario che il BG esponga un'ulteriore operazione per la sola verifica dell'avvenuto pagamento dato un identificativo esterno (es. il `transactionId`, oppure il `paymentId`, l'importante è che identifichi in modo univoco il pagamento e che sia sempre lo stesso); così facendo il payment gateway può ricostruire la situazione presente nel BG nell'ambito del riprocessamento ripetuto della richiesta di pagamento accodata nel broker.

- Semantica di consegna at-least-once da parte del produttore (il client):
  - significato della semantica
  
    **Assunzione**: il punto di vista per interpretare la scelta della semantica "at-least-once" è quella del client delle API REST (o Producer, se visto in ottica di produzione di messaggi verso una coda), quindi parliamo di <u>consegna</u> di messaggi; con questa semantica, un client è incaricato di inviare nuovamente un messaggio (in questo caso, una richiesta di pagamento) nel caso in cui la richiesta precedente non ha ricevuto risposta.
  
    **Attenzione: senza la proprietà di "idempotenza" associata a questa semantica, il comportamento atteso di at-least-once è quello che la richiesta può essere soddisfatta più volte (ossia, il pagamento avvenga più volte)!**
    Dato il problema alla mano, è fondamentale tenere conto di questo aspetto e prevedere l'idempotenza del metodo (come da requisito).
    
    N.B.: Nelle versioni recenti di Kafka è abilitata di default l'opzione di idempotenza per la consegna dei messaggi da parte dei produttore.
  
    > To achieve this, the broker assigns each producer an ID and deduplicates messages using a sequence number that is sent by the producer with every message.
  
    - L'applicazione riversa le richieste su Kafka
      - Sarebbe stato interessante associare l'identificativo del <u>primo</u> Producer all'identificativo della transazione di pagamento, così da riutilizzare lo stesso Producer in caso di tenativi multipli di pagamento per la stessa transazione (resume della sessione del Producer) e ottenere l'idempotenza della delivery fornita da Kafka. In realtà, ciò non è possibile perché un nuovo producer non può imporre un identificativo al broker, questo gli viene sempre attributo durante la fase iniziale (in altre parole, non è possibile ripristinare la sessione di un producer fallito e a ogni nuovo producer viene fornito identificativo) (vedi https://stackoverflow.com/questions/51739260/kafka-idempotent-producer)
    
    - L'applicazione legge e processa le richieste riversate su Kafka
  
      - Utilizzo di Spring Cloud Stream e Spring Cloud Function
  
      - Far sì che l'applicazione si comporti anche come Consumer Kafka e che si occupi di prelevare dalla coda una richiesta di pagamento e di inoltrarla al servizio di terze parti. <u>Il consumo del messaggio deve avvenire una e una sola volta (necessario commit del Consumer)</u>.
    
        - Una risposta da parte del servizio di terze parti alla richiesta di pagamento, sia essa di successo o di fallimento, fa sì che la richiesta di pagamento venga considerata come "processata" e che venga memorizzato lo stato ed effettuato il commit da parte del Consumer.
    
        - Il client dell'applicazione dovrà disporre di un'operazione per leggere lo stato del pagamento (metodo `GET` su stesso endpoint), tale stato verrà aggiornato dalla risposta del servizio di terze parti.
    
  - **Assunzione**: Probabilmente questa semantica è stata preferita alla semantica "exactly-once" (che, dal punto di vista puramente logico, sarebbe la più corretta delle semantiche per risolvere questo problema) per garantire minore latenza verso i client, dunque maggiore efficienza; probabilmente il numero di client è elevato ed è necessario gestire un carico di richieste considerevole.

#### Pattern utilizzati

- CQRS (command is PUT request, query is GET request)
  - Command: operazione di accodamento della richiesta di pagamento
  - Query: operazione di lettura dello stato attuale della richiesta di pagamento

- Avrei voluto utilizzare il "Money pattern" per rappresentare valori monetari
  - ho evitato comunque i float e i double, per i noti problemi nella gestione dei valori monetari

- Controller/Service/Repository - Clean Architecture

#### Sicurezza

- Avrei voluto impostare OAuth2 con Client Credentials flow, utilizzando un Identity Provider come Keycloak, per proteggere le API verso l'esterno
- In produzione, ovviamente, sarebbe fondamentale la protezione TLS (HTTPS)
- **Assunzione**: le API verrebbero usate da un'applicazione backend, che provvederà a implementare ulteriori meccanismi di sicurezza (e.g. CSRF)
- Oltre ai meccanismi di sicurezza sopra menzionati, sarebbe opportuno implementare la *non-repudiation* delle comunicazioni tra client e servizio, questo tramite meccanismi di firma digitale e certificati:
  - in modo che il servizio non possa modificare la cifra del pagamento dopo aver ricevuto la richiesta dal client
  - in modo che il client non possa dire in un secondo momento di aver specificato un pagamento diverso o di non averlo mai chiesto

#### Test

- In una soluzione di produzione è necessario scrivere degli integration test che dimostrino l'idempotenza e la semantica at-least once dell'operazione di pagamento

