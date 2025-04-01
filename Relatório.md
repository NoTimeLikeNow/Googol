Francisco Tavares 2020211848 							
Brenda Medeiros   2019112462 

Relatório da Meta 1 do Projeto Googol de SD

1. Arquitetura de software

    Client.java
    A classe comunica com a Gateway através da invocação dos métodos searchWord e putNew da interface gate por RMI (Remote Method Invocation). Consegue ter acesso a três funcionalidades diferentes: pesquisar, indexar um novo URL à queue para ser processado e ainda procurar que websites referenciam um determinado URL. O output dos dois métodos está agrupado em grupos de 10 urls e podendo ser interrompido entre cada grupo.

    Downloader.java
    Esta classe tem como objetivo fazer o processamento dos URLs inseridos na queue. Partindo do próximo URL da queue presente no gateway, o downloader verifica a página a ele referente, procura outros URLs que lá estejam presentes e envia-os para a queue na Gateway, através do método pushNew. Para além disso, também procura um barrel disponível e pede a este que inicie o processo de multicast para a adição de todas as palavras encontradas como chaves de pesquisa.
    O downloader dispoem de uma lista de stopwords portuguesas e inglesas a ignorar adquirida dos seguintes trabalhos de pesquisa disponibilizados publicamente no github https://gist.github.com/sebleier/554280, https://gist.github.com/alopes/5358189

    Gate.java e Index.java
    Estas duas classes são interfaces implementadas, respetivamente, pela Gateway e pelo IndexStorageBarrel. Estas contêm métodos que podem ser chamados de forma remota noutras classes.

    Gateway.java
    A classe Gateway está encarregue de receber os pedidos do cliente e pedir informações aos barrels de forma a devolver resultados aos pedidos que recebeu. Para além disso, é na Gateway que existe a queue com os URLs e a lista de barrels disponíveis.

    IndexStorageBarrel.java
    A classe IndexStorageBarrel tem como principal função armazenar as informações. Nela podem ser encontradas as seguintes estruturas: 
    3 ConcurrentHashMaps que guardam a informação adquirida em cada website. IndexedItems cuja as chaves  são as palavras encontradas e o os seus valores são listas dos websites que contêm essas palavras, Found URls cujas as chaves são a lista de URLs encontrados (quer estejam indexados ou não) os seus valores são as páginas que referenciam esses URLs, pageInfo é a idenficação do título e da citação encontradas na página, as suas chaves são urls das suas páginas;
    1 uma lista de elementos únicos que guarda a lista de urls já indexados para evitar repetir processamento de páginas.

2. Replicação dos barrels (reliable multicast)
    O reliable multicast é conseguido através de um sistema semelhante ao das bases de dados mais comuns de confirmação dupla. 
    A função procura fazer ligação com todos os barrels disponíveis. Se obtivemos resposta de comunicação de todos os barrels isto é visto como um commit. Se qualquer ligação falhar a fase de commit. Nenhum barrel fará modificações aos seus dados. Se todos passarem no commit a ordem será dada para fazer alterações ao Indices. Se algum barrel falhar em devolver a resposta de que as alterações foram feitas, será enviado para todos a ordem de reverter as alterações. Método equivalente a um rollback.

3. Explicação dos métodos e componentes RMI
    Interface Gate:
        public String takeNext() throws RemoteException;
        Devolve o próximo o Url a ser indexado pelo downloader que invocar este método removendo o mesmo fila.

        public List<String> putNew(String url) throws java.rmi.RemoteException;
        Chamada pelo cliente, adiciona um novo URL à fila se ainda não foi indexado, para tal pede a um barrel disponivel para verificar se este index já foi indexado. Se já foi indexado retorna todos os websites que referenciam esse website.

        public List<String> searchWord(String word) throws java.rmi.RemoteException;
        Recebe uma ou mais palavras fornecidas pelo cliente e pede a um barrel os websites que contanham simultanemente essas palavras.
        Devolvendo se disponivel esses websites, o seu titulo e uma sitação do seu conteudo.

        public void addBarrel(String connetion, Boolean lock) throws java.rmi.RemoteException;
        Chamada pelos barrels ao serem iniciados esta função adiciona o novo barrel à lista de barrels disponivel e se pedido ira bloquear métodos sensiveis a serem executados enquanto os barrels existentes partilham os seus dados com o barrel recem chegado.

        public void removeBarrel(String connetion) throws java.rmi.RemoteException;
        Método chamado pelos barrels quando recebem singal de terminação do sistema operativo para que se removam da lista de barrels disponiveis.

        public Set<String> requestBarrelList() throws java.rmi.RemoteException;
        Método que retorna a lista de barrels disponiveis. Chamada pelos barrels quando procuram updates e pelos Downloaders para pedir uma execução de multicast.

        public void pushNew(Set<String> urls) throws java.rmi.RemoteException;
        Este método é chamado pelo downloaders para adicionar uma lista de urls descobertos à fila para serem indexados. O gateway irá pedir a um barrel que verifique se os links já foram indexados antes de os adiconar à fila.


    Interface Index:
        public void addToIndex(Set<String> words, String url, String title, String summary, Set<String> links) throws java.rmi.RemoteException;
        Indexa as palavras fornecidas, ao url de onde viream. O título e a citação (summary) são guardados e associados ao url. Os Urls encontrados são usados para identeficar que sites são referenciados pelo url.

        public void rollBack(Set<String> words, String url, String title, String summary, Set<String> links) throws java.rmi.RemoteException;
        Reverte as ações relevantes de addToIndex. Guarda algumas informações sem impacto para melhor o desempanho

        public void multicast(Set<String> words, String url, String title, String summary, Set<String> links, List<String> barrels) throws java.rmi.RemoteException;
        Estabelece ligação com todos os barrels. Se falhar em algum mudanças não executadas. Se conseguir dá-se ordem para executar addToIndex(words, url, title, summary, links) em todos os barrels. Se esta chamada remota falhar em qualquer barrel, é dada ordem de rollback(words, url, title, summary, links) em todos os barrels.

        public List<String> searchWord(String words) throws java.rmi.RemoteException;
        Devolve a lista da interseção dos resultados das palavras forecidas. Oredenado pela quantidade de referências de outros websites que esses Urls possuiem.
        
        public Set<String> requestReferences(String Url) throws java.rmi.RemoteException;
        Devolve a lista de Websites que referenciam o Url 

        public Boolean checkURL(String url) throws java.rmi.RemoteException;
        Verifica se determinado URL já foi indexado

        public ArrayList<Object> getIndexState() throws java.rmi.RemoteException;
        Devolve uma lista de todas as estruturas do barrel para um barrel novo se atualizar

4. Distribuição de tarefas pelos elementos do grupo

    O Francisco implementou as classes Gateway, Downloader e Index Storage Barrel e escreveu o readme de como usar o sistema.
    A Brenda implementou a classe Client e escreveu o relatório. 

5. Testes Realizados
    Comunicação entre dispositvos diferentes na mesma rede. Check
    Multiplos Barrels na mesma máquina e em máquinas diferentes. Check
    Multiplos downloaders na mesma máquina e em máquinas diferentes. Check
    Cliente testado em ambos os dispositivos. Check
    Barrel guarda o seu estado e recupera o seu estado com sucesso quando reiniciado.
    Multicast verificado. Ambos os barrels apresentam a mesma informação quando forçadamente falhados numa mudança não fazem as alterações pedidas. Check

    Barrel novo iniciado quando já existe um outro index aberto. Atualização de estados por via outro Barrel



