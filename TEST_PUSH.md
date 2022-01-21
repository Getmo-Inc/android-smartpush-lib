# Smartpush - Testando o envio de PUSH

Depois de configurar a SDK SMARTPUSH no seu projeto, instale o aplicativo em um device (real) e abra sua aplicação para que o registro seja realizado.

Você tem 2 maneiras de localizar um device na plataforma SMARTPUSH, pelo **ALIAS**, ou por uma **TAG**.

#### Localizando o ALIAS de um device

O **ALIAS** é um código de 8 caracteres gerado pela plataforma Smartpush após o registro do device e que permite localizar um device e interagir com ele.

Uma forma de ter acesso ao código ALIAS é conectar o device ao seu computador e procurar no log.

```
04-08 21:40:56.961 13209-13209/? D/LOG: checkSmartpush() : begin - Configurations tests : br.com.mycompany.MyApp
04-08 21:40:56.971 13209-13209/? D/LOG: checkSmartpush() : Metadata, pass!
04-08 21:40:56.971 13209-13209/? D/LOG: checkSmartpush() : Activity, pass!
04-08 21:40:56.971 13209-13209/? D/LOG: checkSmartpush() : end - Configurations tests : br.com.mycompany.MyApp
...
04-08 21:40:59.314 13209-14152/? D/LOG: rsp : {"status":true,"message":"Success","alias":"E1E18049","hwid":"702E265C2321AC0E"}
```
Após a execução da tentativa de registro do device em nossa plataforma o codigo ALIAS é apresentado no log. 

> O código gerado para o device do exemplo acima foi "alias":"E1E18049".

#### Localizando um device por uma TAG