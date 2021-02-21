# Smartpush Messaging Client

O [SMARTPUSH](http://admin.getmo.com.br) é a plataforma de multicanal de mensagens da [GETMO](https://www.getmo.com.br). 

O Smartpush Messaging Client é uma biblioteca android desenvolvida pela GETMO que administra o processo de registro dos dispositivos android permitindo o envio de mensagens push, além outras funcionalidades como novos modelos de notificações (banner, gif, carrossel, video, entre outras), mensagens geolocalizadas, construção de perfil (tags), agendamentos, e muitas outras.  

---
## <a name="starting"></a>Adicionando o SMARTPUSH a sua app 

A biblioteca android do **SMARTPUSH** é responsável por integrar sua aplicação aos serviços do **SMARTPUSH** para a gestão do cadastro de dispositivos, tags, geofences, processamento e monitoramento de mensagens push.   


### Configurando as dependências do FCM e do SMARTPUSH
1. Adicione o [Firebase](https://firebase.google.com/docs/android/setup?authuser=0) ao seu projeto. O Firebase é o responsável pela entrega do push nos dispositivos.

2. Adicione o repositório do [jitpack.io](https://jitpack.io) ao seu arquivo build.gradle a nível de projeto, dentro de allprojects repositories. É através dele que você tem acesso a versão mais atual do **Smartpush Messaging Client**.
```
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
2. Adicione as dependências abaixo ao arquivo build.gradle a nível de módulo
```
dependencies {
    // Import the BoM for the Firebase platform
    implementation platform('com.google.firebase:firebase-bom:26.3.0')

    // Declare the dependencies for the FCM and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation 'com.google.firebase:firebase-messaging'
    implementation 'com.google.firebase:firebase-analytics'
    
    // Import Smartpush library
    implementation 'org.bitbucket.getmo:android-smartpush-lib:9.2.0'
 }
```

3. Sincronize o projeto apertando o botão "Sync Now" no Android Studio para baixar as dependências necessárias ao seu projeto.

Feito isso, o próximo passo é configurar sua app para usar a biblioteca e permitir o cadastramento dos dispositivos, das tags, geofences e o processamento das mensagens push.


### Editando o arquivo de Manifesto

Adicione os seguintes itens ao manifesto do app:

* Dentro da tag ```<manifest>``` adicione as permissões abaixo.

```xml
    <uses-permission
        android:name="android.permission.ACCESS_NETWORK_STATE"/>

```

* Dentro da tag ```<application>``` adicione as tags de metadados a seguir;

```xml
    <meta-data
        android:name="br.com.smartpush.APPID"
        android:value="[SEU_APP_ID]" />

    <meta-data
        android:name="br.com.smartpush.APIKEY"
        android:value="[SUA_API_KEY]"/>

    <meta-data
        android:name="br.com.smartpush.default_notification_small_icon"
        android:resource="@drawable/[NOTIFICATION_SMALL_ICON]" />

    <meta-data
        android:name="br.com.smartpush.default_notification_big_icon"
        android:resource="@drawable/[NOTIFICATION_BIG_ICON]" />
        
    <meta-data
        android:name="br.com.smartpush.default_notification_color"
        android:resource="@color/[SUA_COR]" />
```

> Você deve substituir [SEU_APP_ID] e [SUA_API_KEY] pelos códigos obtidos no painel de controle do [SMARTPUSH](https://admin.getmo.com.br). Em caso de dúvida sobre como obter esses códigos consulte [aqui]().
>
> Você deve substituir [NOTIFICATION_SMALL_ICON], [NOTIFICATION_BIG_ICON] e [SUA_COR] pelos recursos correspondentes na sua aplicação. Estas propriedades definem os icones pequeno e grande, e também a cor, que devem ser utilizados na notificação.

* Ainda na tag ```<application>``` configure o Service **SmartpushService** conforme a seguir. Este serviço é responsável por processar as notificações customizadas (Carrossel, banner, video, etc) e reportar os eventos (IMPRESSAO, CLICK, PREVIEW, REDIRECT, etc).

```xml
    <service
        android:name="br.com.smartpush.SmartpushService"
        android:exported="true"/>
```

* Ainda na tag ```<application>``` configure a Activity **SmartpushActivity** conforme a seguir. Esta activity é responsável por carregar o Player de Video embedado que pode ser executado a partir de um push.

```xml
    <activity
        android:name="br.com.smartpush.SmartpushActivity"
        android:hardwareAccelerated="true"
        android:launchMode="singleTask"
        android:taskAffinity=""
        android:excludeFromRecents="true" >
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.DEFAULT" />
        </intent-filter>
    </activity>
```

### Criando o serviço para tratar do push e criar notificações

Vamos criar um serviço simples para tratar do push e criar uma notificação. Para isso crie um serviço que extenda a classe **SmartpushMessagingListenerService**. Veja o exemplo a seguir:

```java
public class MySmartpushListenerService extends SmartpushMessagingListenerService {

    @Override
    protected void handleMessage( RemoteMessage data ) {
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         *
         * If you intent to use our notifications leave this method empty.
         */
                                                        
    }

}
```
> O nome da classe pode ser alterada de acordo com sua própria politica de nomeação. O importante é que ela estenda a classe **SmartpushListenerService**.

Não esqueça de configurar o serviço no arquivo de manifesto para tratar a criação das notificações, ou outro comportamento desejado/esperado, a partir da chegada de um push.


```xml
    <service
        android:name=".MySmartpushListenerService"
        android:exported="true">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
    </service>
```

### Registrando o dispositivo para receber push

Na Activity principal da sua aplicação adicione no método _onCreate_ uma chamada ao serviço de registro da plataforma Smartpush para ativar a chegada de push e a criação de notificações.

Veja um exemplo:

```java
@Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );

        // Register at Smartpush!
        Smartpush.subscribe( this );
        
        // do something else...
    }
```
> A chamada de registro é não-bloqueante, ou seja ela é executada numa linha de execução paralela. 

Pronto, com isso terminamos a configuração básica para ativar o push. 

Agora, compile seu projeto, instale em um dispositivo, abra a aplicação para que o registro seja efetivado. Vamos dar uma olhada no LOGCAT para ver o que aconteceu.

```
04-08 21:40:56.961 13209-13209/? D/LOG: checkSmartpush() : begin - Configurations tests : br.com.mycompany.MyApp
04-08 21:40:56.971 13209-13209/? D/LOG: checkSmartpush() : Metadata, pass!
04-08 21:40:56.971 13209-13209/? D/LOG: checkSmartpush() : Activity, pass!
04-08 21:40:56.971 13209-13209/? D/LOG: checkSmartpush() : end - Configurations tests : br.com.mycompany.MyApp
04-08 21:40:57.242 13209-14152/? D/LOG: GCM Registration Token: dXbwAUTsFfQ:APA91bH0oEItl6TeUowjFE_vTxWEYwbwdbokn-sA0BGgWAvcgfNoF9mS-sgMPJbIGX9y7ktVeX2mTOq15tc13KTvygY_xIVstxlJdplJZ5_VIKxIUephAua79YhssZ0AF86T4YBTDIgk
04-08 21:40:57.262 13209-14152/? D/LOG: url : https://api.getmo.com.br/device
04-08 21:40:57.262 13209-14152/? D/LOG: method: POST
04-08 21:40:57.262 13209-14152/? D/LOG: params : device=SM-J320M&platformId=ANDROID&devid=CN6Z8Eka3FSQ9IG&uuid=702E265C2321AC0E&appid=AK0Z1AzB1tr31TT&manufacturer=SAMSUNG&regid=dXbwAUTsFfQ%3AAPA91bH0oEItl6TeUowjFE_vTxWEYwbwdbokn-sA0BGgWAvcgfNoF9mS-sgMPJbIGX9y7ktVeX2mTOq15tc13KTvygY_xIVstxlJdplJZ5_VIKxIUephAua79YhssZ0AF86T4YBTDIgk&framework=5.1.1
04-08 21:40:59.314 13209-14152/? D/LOG: rsp : {"status":true,"message":"Success","alias":"E1E18049","hwid":"702E265C2321AC0E"}
04-08 21:40:59.314 13209-14152/? D/LOG: {
                                            "status": true,
                                            "message": "Success",
                                            "alias": "E1E18049",
                                            "hwid": "702E265C2321AC0E"
                                        }
```

A informação importante aqui é o "**alias**" ele é um identificador gerado pela plataforma Smartpush que permite localizar o seu dispositivo na base de dispositivos. 

Copie o valor do código "**alias**" e teste o envio de push. Para saber como enviar um push a partir do painel do Smartpush acesse esse [link]().

### Executando operações logo após o registro do dispositivo

Em alguns casos você pode querer acionar outros métodos da SDK **Smartpush Messaging Client** logo após, e somente após, a realização do subscribe, para isso defina um listener (Broadcastreceiver) para a action **Smartpush.ACTION_REGISTRATION_RESULT**. Veja um exemplo:

```java
    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager
                .getInstance( this )
                .registerReceiver( mRegistrationBroadcastReceiver,
                        new IntentFilter(
                                Smartpush.ACTION_REGISTRATION_RESULT ) );
        // do something else...
    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager
                .getInstance( this )
                .unregisterReceiver( mRegistrationBroadcastReceiver );
        // do something else...
    }

    private BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent data ) {
            if ( data.getAction().equals( Smartpush.ACTION_REGISTRATION_RESULT) ) {
                SmartpushDeviceInfo device =
                        data.getParcelableExtra( Smartpush.EXTRA_DEVICE_INFO );

                boolean registered = ( device != null && !Strings.isEmptyOrWhitespace( device.alias ) );

                // set your user id TAG here!
                if ( registered ) {
                    Smartpush.getUserInfo( MainActivity.this );
                    Smartpush.setTag(MainActivity.this, "SMARTPUSH_ID", device.alias );
                }
            }
        }
    };

```

> No exemplo aproveitamos o final do registro para gravar uma **TAG** chamada **SMARTPUSH_ID**. Você pode criar seus próprios identificadores, veremos isso quando falarmos em **TAGs**. 

Para explorar outros recursos da plataforma Smartpush como a criação de **TAGs**, **GEOFENCE**, entre outros acesse os projetos de exemplo disponiveis no [Github](https://github.com/Getmo-Inc/android-smartpush-samples).

Bom era isso! Esperamos que o tutorial seja útil e se tiver qualquer dúvida ou dica envie um email a nossa equipe **developer@getmo.com.br**, teremos o maior prazer em te auxiliar.
