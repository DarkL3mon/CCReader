# CCReader - Leitor de Cartões do Cidadão
>Neste pequeno aplicativo podemos encontrar um método de utilização da API do cartão do Cidadão da República Portuguesa.
>
>De modo a facilitar a sua utilização para projetos em java, foram criadas duas classes em java:
>### Mail.java
>>Demonstra como utilizar as classes do **javax**:
>>* javax.smartcardio.CardException - responsável por reportar os problemas com a deteção do sensor.
>>* javax.smartcardio.CardTerminal - responsável por detetar os leitores que estão ligados no computador.
>>* javax.smartcardio.TerminalFactory - responsável por detetar o cartão no leitor.

>### CartaoCidadao.java
>>Esta classe pode ser utilizada para conectar-se ao middleware desenvolvido pela República Portuguesa, desenvolvida de modo a facilitar a utilização do cartão do cidadão e recolha dos seus dados.

>### [Codacy Review](https://www.codacy.com/app/Rumos/CCReader/dashboard) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/d21ea4f111dd4086b2c354f3e7f0c696)](https://www.codacy.com/app/Rumos/CCReader?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=GoncaloGrupoRumos/CCReader&amp;utm_campaign=Badge_Grade)

## Requisitos
>* [Java Versão 8 ou Superior](https://www.java.com/en/download/) - necessário para que possa correr este aplicativo.
>* [Software do Cartão do Cidadão](https://www.cartaodecidadao.pt/index.php_option=com_content&task=view&id=102&Itemid=44&lang=pt.html) - obrigatória a presença deste programa para que esteja presente a api do cartão do cidadão português nas máquinas em que o CCReader seja necessário.

## Observações
>Também é explicado como se pode guardar uma imagem com o formato de **jpeg2000** convertendo-a para **jpg**, para tal foi utilizada a classe **com.idrsolutions.image.jpeg2000.Jpeg2000Decoder** da biblioteca **jdeli**.
```
Jpeg2000Decoder imageDecoder = new Jpeg2000Decoder();
BufferedImage bufferedPhoto = null;
try 
{
	bufferedPhoto = imageDecoder.read(picture);
} catch (Exception e) {
	e.printStackTrace();
}
```
