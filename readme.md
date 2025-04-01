Como usar:
Para instalar os componentes deste sistema distribuído: É necessário ter instalada/descarregada a biblioteca jsoup, neste caso já a disponibilizamos na pasta target/lib.
Faça a compilação dos ficheiros para o seu sistema operativo, executando o ficheiro build apropriado ao seu sistema (.cmd (windows/CRLF encoding), .sh Unix encoding) num terminal aberto na pasta onde se encontram. Pode ainda compilar manualmente todos os ficheiros na pasta src/main/java/search, não se esqueça de incluir a biblioteca jsoup na compilação (Ex.: javac -d ../../../target/ -cp ../../../target/lib/jsoup-1.18.3.jar search/*.java)


Para executar são providenciados os ficheiros run-<parte do sistema>. Deverá abrir aqueles que usar e alterar os ips pelos ips das máquinas usadas e dos ports conforme necessidade. Siga a estrutura dos exemplos abaixo, começando por iniciar a Gateway.


Qualquer outra parte do sistema distribuído pode ser iniciado pelo ficheiro de execução apropriado em qualquer ordem, tendo só que ter em atenção as portas e os ips que devem ser mudados para  referirem as máquinas certas:


run-gateway
cd target/
java -cp "./lib/jsoup-1.18.3.jar:." search.Gateway <ip da máquina que executa este programa (host do gateway)> <porta usado pelo servidor Gateway>
cd ..


run-Client
cd target/
java -cp "./lib/jsoup-1.18.3.jar:." search.Client <ip da máquina do Gateway> <porta do gateway>
cd ..


run-downloader
cd target/
java -cp "./lib/jsoup-1.18.3.jar:." search.Downloader  <ip da máquina do Gateway> <porta do gateway>
cd ..


run-barrel
cd target
java -cp "./lib/jsoup-1.18.3.jar;." search.IndexStorageBarrel <ip da máquina que executa este programa (host deste index barrel)> <porta do servidor do barrel> <ip da máquina do gateway> <porta do gateway>
cd ..




Estes ficheiros devem ser executados num terminal diferente para cada parte do sistema, na pasta onde eles se encontram. Pode ainda executar diretamente na consola usando os comandos acima referidos, apropriado-os ao seu sistema operativo caso necessário, diretamente na consola


Uma vez executado o client Client. Links fornecidos seram automaticamente reconhecidos, adicionados para indexar se não foram ainda ou será devolvido em grupos de 10 os websites que referenciam esse link. Use a letra "b" para interromper o output.

Pode ainda colocar conjuntos de palavras para pesquisar websites relevantes.