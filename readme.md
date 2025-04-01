Como executar numa situação normal:
Para instalar os components deste sistema distribuido: É necessário ter instalda a biblioteca jsoup, neste caso já a disponibilizamos na pasta target/lib.
E fazer a compilação dos ficheiros para o seu sitema operativo, executando o ficheiro build apropriado ao seu sistema (.cmd (windows)) num terminal aberto na pasta onde se encontram. Pode ainda compilar manualmente todos os ficheiros na pasta src/main/java/search, não se esqueça de incluir a biblioteca jsoup na compilação (Ex.: javac -d ../../../target/ -cp ../../../target/lib/jsoup-1.18.3.jar search/*.java)

Qualquer parte do sistema distribuido pode ser iniciado pelo ficheiro de execução apropriado em qualquer ordem, tendo só que ter em atenção as portas e os ips que devem ser mudados para as para referirem as maquinas certas ou utilizar uma porta desocupada:

run-gateway
cd target/
java -cp "./lib/jsoup-1.18.3.jar:." search.Gateway <porta usado pelo servidor Gateway>
cd ..

run-Client
cd target/
java -cp "./lib/jsoup-1.18.3.jar:." search.Client <ip do Gateway> <porta do gateway>
cd ..

run-downloader
cd target/
java -cp "./lib/jsoup-1.18.3.jar:." search.Downloader  <ip do Gateway> <porta do gateway>
cd ..

run-barrel
cd target
java -cp "./lib/jsoup-1.18.3.jar;." search.IndexStorageBarrel <porta do servidor do barrel> <ip do gateway> <porta do gateway>
cd ..


Este ficheiro deve ser executado num terminal na pasta onde ele se encontra. Pode ainda executar diretamente na consola usando so comandos acima referidos, apropriado-os ao seu sistema operativo caso necessário, diretamente na consola.