# Smartpush - Testando o envio de PUSH

Depois de configurar a SDK SMARTPUSH no seu projeto, instale o aplicativo em um device (real) e abra sua aplicação para que o registro seja realizado.

Você tem 2 maneiras de localizar um device na plataforma SMARTPUSH, pelo **ALIAS**, ou por uma **TAG**.

#### Localizando o device pelo ALIAS

O **ALIAS** é um código de 8 caracteres, gerado pela plataforma Smartpush após o registro do device, que permite localizar um device e interagir com ele.

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

Outra forma é utilizando um serviço da SDK que consulta os dados de registro do device.

> Caso esteja usando um módulo para integrar a biblioteca SMARTPUSH ao seu projeto em React Native, ou Flutter, pode ser necessário adicionar esse serviço ao seu módulo.

#### Exemplo de consulta ao ALIAS de um device no Android
```
    Smartpush.getUserInfo( this );
```
> Para maiores detalhes de uso verifique o projeto demo. 

#### Exemplo de consulta ao ALIAS de um device no iOS
```
    <under construction>
```
> Para maiores detalhes de uso verifique o projeto demo. 


#### Localizando um device por uma TAG

**TAG** é uma forma de adicionar informações personalizadas a um device. Você pode usar a TAG para associar informações perfil a um mou mais usuários, e depois usar os valores das TAGs para segmentar sua base de dispositivos e atingir um ou mais dispositivos que sejam similares.

Veja alguns exemplos de TAGs:

* ID (identificador único)
* Último acesso
* Gênero
* Time do coração
* Tipo de Noticias/Produtos de interesse

Entre outras tantas que você pode criar. A sua imaginação, e necessidade, são o limite. Então, antes de poder localizar um device por uma TAG, você precisa cria-la e associa-la ao device. 

Para testar o envio de push, sugerimos que você crie uma TAG, por exemplo "DEVICE_ID", no painel do [SMARTPUSH](https://admin.getmo.com.br/tags) e utilize na sua app, registrando uma informação que seja única para aquela instância da sua app, da seguinte forma:

#### Exemplo TAG Android
    Smartpush.setTag( this, "DEVICE_ID", "<INFO_IDENTIFICACAO_DO_DEVICE>" );

#### Exemplo TAG iOS
    [SmartpushSDK.sharedInstance setString:value forTag:key];

Depois disso fica simples. É só executar os passos a seguir:

1. Instale a aplicação em um device
2. Abra a aplicação para que ela se registre na plataforma SMARTPUSH
3. Talvez seja necessário fechar e abrir a app novamente para garantir que a TAG foi registrada

Pronto, agora que você já sabe como identificar um device é só enviar um push utilizando uma **TAG** ou **ALIAS**. Você pode enviar um push através do painel [SMARTPUSH](https://admin.getmo.com.br) ou utilizando nossa [API REST](https://github.com/Getmo-Inc/Docs/blob/master/REST-API.md). 

#### Enviando um push filtrando por um ALIAS
Exemplo de envio para um app iOS através da API REST.

```
curl --location --request POST 'https://api.getmo.com.br/push' \
--header 'Content-Type: application/json' \
--data-raw '{
    "when": "now",
    "devid": "[SEU_DEV_ID_AQUI]",
    "prod": "1",
    "notifications": [
        {
            "appid": "[SEU_APP_ID_IOS_AQUI]",
            "platform": "IOS",
            "params": {
                "aps": {
                    "alert": {
                        "title": "Go Getmo",
                        "body": "Notificações que engajam!"
                    }
                }
            }
        }
    ],
    "filter": {
        "type": "ALI",
        "alias": "[SEU_ALIAS_AQUI]"
    }
}'
```
> Não esqueça de substituir os valores onde tem [SUA/SEU_XXX] com a informação correspondente.
> 
> Você pode enviar para diversas plataformas (ANDROID, IOS, CHROME, SAFARI, FIREFOX e SMS) de uma única vez.

#### Enviando um push filtrando por uma TAG
Exemplo de envio para um app ANDROID.
```
curl --location --request POST 'https://api.getmo.com.br/push' \
--header 'Content-Type: application/json' \
--data-raw '{
    "when": "now",
    "devid": "[SEU_DEV_ID_AQUI]",
    "prod": "1",
    "notifications": [
        {
            "appid": "[SEU_APP_ID_ANDROID_AQUI]",
            "platform": "ANDROID",
            "params": {
                "title": "Go Getmo!",
                "detail": "Notificações que engajam 2!!",
                "link": "https://www.getmo.com.br",
                "provider": "smartpush",
                "type": "PUSH"
            }
        }
    ],
    "filter": {
        "type": "TAG",
		"rules": [
			["[SUA_TAG]", "=", "[VALOR_SUA_TAG]"]
		]
    }
}'
```

> Não esqueça de substituir os valores onde tem [SUA/SEU_XXX] com a informação correspondente.
> 
> Você pode enviar para diversas plataformas (ANDROID, IOS, CHROME, SAFARI, FIREFOX e SMS) de uma única vez.
