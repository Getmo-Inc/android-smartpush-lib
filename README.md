# android-smartpush-lib

A [SMARTPUSH](http://admin.getmo.com.br) é a plataforma de mensagens (push, webpush, sms, email, redes sociais, e chatbots) da [GETMO](http://novo.getmo.com.br). 

#### Requisitos e Dependências

- Android minSdkVersion **11**
- Google Play Services **11.6.0**

### Configurando a biblioteca SMARTPUSH

A biblioteca android do **SMARTPUSH** é responsável por integrar as aplicações mobile ao backend do **SMARTPUSH** para a gestão do cadastro de dispositivos, tags, geofences, processamento e monitoramento de mensagens push. 

Para adicionar suporte a push em sua aplicação android siga as instruções a seguir.

1. Acesse o [GITHUB](https://github.com/Getmo-Inc/android-smartpush-lib) da GETMO e clone o projeto ou faça o download do arquivo **gcm-smartpush-lib-release.aar**.

2. Abra o seu projeto de aplicação no _Android Studio_ e selecione adicionar um novo _módulo_.

![Step1](images/add_new_module.png)

Selecione a opção "Import JAR/AAR Package" conforme a figura abaixo. 

![Step2](images/add_new_module_2.png)

Na próxima tela, clique no botão para navegar pelo seu filesystem ...

![Step3](images/add_new_module_3.png)

... então navegue até o local onde você baixou a biblioteca, selecione o arquivo .aar, pressione OK e então FINISH. 

![Step4](images/add_new_module_4.png)

Agora no "_Project explorer_" selecione sua app, clique com o botão direito do mouse, e
selecione a ação "_open module settings_". Conforme a figura a seguir:

![Step5](images/add_new_module_5.png)

Agora, vamos adicionar uma "_Module Dependency_" ao seu projeto. Clique no + dentro da aba "_Dependencies_", então na janela que irá abrir selecione o module :gcm-smartpush-lib-release
e pressione ok. 

![Step5](images/add_new_module_6.png)

Feito isso, o próximo passo é configurar sua app para usar a biblioteca e permitir o cadastramento dos dispositivos, das tags, geofences e o processamento das
mensagens push.


Para isso vamos começar pelo arquivo de manifesto da sua aplicação. 
